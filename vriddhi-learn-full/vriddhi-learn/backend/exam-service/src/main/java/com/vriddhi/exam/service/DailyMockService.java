package com.vriddhi.exam.service;

import com.vriddhi.exam.client.ContentServiceClient;
import com.vriddhi.exam.client.ProgressClient;
import com.vriddhi.exam.dto.ContentTopicDto;
import com.vriddhi.exam.dto.DailyMockStatus;
import com.vriddhi.exam.dto.ProgressDailyPlanDto;
import com.vriddhi.exam.dto.StartAttemptResponse;
import com.vriddhi.exam.entity.AttemptStatus;
import com.vriddhi.exam.entity.Exam;
import com.vriddhi.exam.entity.ExamAttempt;
import com.vriddhi.exam.entity.ExamKind;
import com.vriddhi.exam.entity.ExamSection;
import com.vriddhi.exam.entity.ExamStatus;
import com.vriddhi.exam.entity.NavigationMode;
import com.vriddhi.exam.exception.BadRequestException;
import com.vriddhi.exam.exception.NotFoundException;
import com.vriddhi.exam.exception.UpstreamUnavailableException;
import com.vriddhi.exam.repository.AttemptQuestionRepository;
import com.vriddhi.exam.repository.ExamAttemptRepository;
import com.vriddhi.exam.repository.ExamRepository;
import com.vriddhi.exam.repository.ExamSectionRepository;
import com.vriddhi.exam.repository.SectionTopicDao;
import com.vriddhi.exam.service.DailyExamBlueprint.SectionSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The timed daily mock. It does not have its own timer or scoring: it creates a private DAILY exam for the
 * learner for today (sections come from progress-service's daily plan) and then starts it through the normal
 * AttemptService.start(), so the server-owned deadline, auto-submit, palette, scoring and result page all apply.
 *
 * One mock per learner, category and day: calling start again resumes the attempt or returns its status.
 * Remote calls (progress-service, content-service) are made outside any database transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyMockService {

    private final ExamRepository examRepository;
    private final ExamSectionRepository sectionRepository;
    private final SectionTopicDao sectionTopicDao;
    private final ExamAttemptRepository attemptRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final AttemptService attemptService;
    private final ProgressClient progressClient;
    private final ContentServiceClient contentServiceClient;
    private final PlatformTransactionManager transactionManager;

    /** Starts today's mock, or returns the status of the one that already exists. */
    public DailyMockStatus startOrResume(UUID userId, String category) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Exam exam = findOrCreateExam(userId, category, today);
        return new TransactionTemplate(transactionManager).execute(tx -> startOnce(exam, userId));
    }

    /** Read-only status for the UI (dashboard card and the Daily page). */
    public DailyMockStatus today(UUID userId, String category) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Optional<Exam> exam = findExam(userId, category, today);
        if (exam.isEmpty()) {
            return DailyMockStatus.notStarted();
        }
        return attemptRepository.findFirstByExamIdAndUserIdOrderByStartedAtDesc(exam.get().getId(), userId)
                .map(attempt -> statusOf(exam.get(), attempt))
                .orElse(DailyMockStatus.notStarted());
    }

    private DailyMockStatus startOnce(Exam exam, UUID userId) {
        // Row lock: two quick clicks cannot both create an attempt for the same exam.
        examRepository.findByIdForUpdate(exam.getId()).orElseThrow(() -> new NotFoundException("Exam not found"));
        Optional<ExamAttempt> existing = attemptRepository.findFirstByExamIdAndUserIdOrderByStartedAtDesc(exam.getId(), userId);
        if (existing.isPresent()) {
            return statusOf(exam, existing.get());
        }
        StartAttemptResponse started = attemptService.start(exam.getId(), userId);
        ExamAttempt attempt = attemptRepository.findById(started.attemptId())
                .orElseThrow(() -> new NotFoundException("Attempt not found"));
        return statusOf(exam, attempt);
    }

    private Exam findOrCreateExam(UUID userId, String category, LocalDate today) {
        Optional<Exam> existing = findExam(userId, category, today);
        if (existing.isPresent()) {
            return existing.get();
        }
        List<SectionSpec> specs = buildSpecs(userId, category, today);
        try {
            return new TransactionTemplate(transactionManager).execute(tx -> createExam(userId, category, today, specs));
        } catch (DataIntegrityViolationException race) {
            // Another request created today's exam between our lookup and our insert: use theirs.
            return findExam(userId, category, today).orElseThrow(() -> race);
        }
    }

    private Optional<Exam> findExam(UUID userId, String category, LocalDate today) {
        return examRepository.findByOwnerUserIdAndCategoryCodeAndPlanDateAndKind(userId, category, today, ExamKind.DAILY);
    }

    private List<SectionSpec> buildSpecs(UUID userId, String category, LocalDate today) {
        try {
            ProgressDailyPlanDto plan = progressClient.dailyPlan(userId, category);
            List<SectionSpec> specs = plan == null ? List.of() : DailyExamBlueprint.sectionsFromPlan(plan.items());
            if (!specs.isEmpty()) {
                return specs;
            }
        } catch (RuntimeException ex) {
            log.warn("progress-service daily plan unavailable, using a random mix instead: {}", ex.getMessage());
        }
        List<ContentTopicDto> catalog;
        try {
            catalog = contentServiceClient.publishedTopics();
        } catch (RuntimeException ex) {
            throw new UpstreamUnavailableException("The question catalog is temporarily unavailable. Please try again.", ex);
        }
        List<SectionSpec> fallback = DailyExamBlueprint.fallbackSections(catalog, category, Objects.hash(userId, today.toEpochDay()));
        if (fallback.isEmpty()) {
            throw new BadRequestException("There are no published questions in this category yet");
        }
        return fallback;
    }

    private Exam createExam(UUID userId, String category, LocalDate today, List<SectionSpec> specs) {
        Exam exam = new Exam();
        exam.setTitle("Daily Mock " + today);
        exam.setCategoryCode(category);
        exam.setDurationMinutes(DailyExamBlueprint.durationMinutes(DailyExamBlueprint.totalQuestions(specs)));
        exam.setNegativeMarking(BigDecimal.ZERO);
        exam.setNavigationMode(NavigationMode.FREE);
        exam.setStatus(ExamStatus.PUBLISHED);
        exam.setKind(ExamKind.DAILY);
        exam.setOwnerUserId(userId);
        exam.setPlanDate(today);
        exam.setCreatedBy(userId);
        exam = examRepository.saveAndFlush(exam);

        int order = 0;
        for (SectionSpec spec : specs) {
            ExamSection section = new ExamSection();
            section.setExamId(exam.getId());
            section.setName(spec.name());
            section.setSortOrder(order++);
            section.setQuestionCount(spec.questionCount());
            section = sectionRepository.save(section);
            for (UUID topicId : spec.topicIds()) {
                sectionTopicDao.link(section.getId(), topicId);
            }
        }
        return exam;
    }

    private DailyMockStatus statusOf(Exam exam, ExamAttempt attempt) {
        boolean running = attempt.getStatus() == AttemptStatus.IN_PROGRESS && !attempt.isExpired();
        int questionCount = attemptQuestionRepository.findByAttemptIdOrderBySortOrder(attempt.getId()).size();
        return new DailyMockStatus(
                running ? "IN_PROGRESS" : "COMPLETED",
                exam.getId(),
                attempt.getId(),
                attempt.getExpiresAt(),
                exam.getDurationMinutes(),
                questionCount,
                attempt.getScore(),
                attempt.getCorrectCount());
    }
}
