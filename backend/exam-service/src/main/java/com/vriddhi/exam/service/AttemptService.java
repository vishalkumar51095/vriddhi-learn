package com.vriddhi.exam.service;

import com.vriddhi.exam.client.ContentServiceClient;
import com.vriddhi.exam.dto.*;
import com.vriddhi.exam.entity.*;
import com.vriddhi.exam.exception.AttemptExpiredException;
import com.vriddhi.exam.exception.BadRequestException;
import com.vriddhi.exam.exception.NotFoundException;
import com.vriddhi.exam.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The exam engine: starting an attempt snapshots questions so later content
 * edits can't retroactively change scoring; every read/write against an
 * in-progress attempt re-checks the server-side deadline (expires_at) first,
 * since the client's countdown is a display only, never the source of truth.
 */
@Service
@RequiredArgsConstructor
public class AttemptService {

    private final ExamRepository examRepository;
    private final ExamSectionRepository sectionRepository;
    private final SectionTopicDao sectionTopicDao;
    private final ExamAttemptRepository attemptRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final AttemptQuestionOptionRepository attemptOptionRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final ContentServiceClient contentServiceClient;

    @Transactional
    public StartAttemptResponse start(UUID examId, UUID userId) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new NotFoundException("Exam not found"));
        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            throw new BadRequestException("This exam is not currently available");
        }

        List<ExamSection> sections = sectionRepository.findByExamIdOrderBySortOrder(examId);
        if (sections.isEmpty()) {
            throw new BadRequestException("This exam has no sections configured");
        }

        ExamAttempt attempt = new ExamAttempt();
        attempt.setExamId(examId);
        attempt.setUserId(userId);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(Instant.now());
        attempt.setExpiresAt(Instant.now().plus(exam.getDurationMinutes(), ChronoUnit.MINUTES));
        attempt = attemptRepository.save(attempt);

        List<StartAttemptResponse.AttemptQuestionSummary> summaries = new ArrayList<>();
        int globalOrder = 0;

        for (ExamSection section : sections) {
            List<UUID> topicIds = sectionTopicDao.findTopicIdsBySection(section.getId());
            List<ContentQuestionDto> pool = contentServiceClient.fetchRandomQuestionsForTopics(topicIds, section.getQuestionCount());

            for (ContentQuestionDto q : pool) {
                AttemptQuestion aq = new AttemptQuestion();
                aq.setAttemptId(attempt.getId());
                aq.setSectionId(section.getId());
                aq.setSectionName(section.getName());
                aq.setSortOrder(globalOrder++);
                aq.setQuestionId(q.id());
                aq.setQuestionVersionId(q.id()); // content-service response is already the current version
                aq.setQuestionType(q.type());
                aq.setQuestionText(q.questionText());
                aq.setCodeSnippet(q.codeSnippet());
                aq.setExplanation(q.explanation());
                aq = attemptQuestionRepository.save(aq);

                if (q.options() != null) {
                    int optOrder = 0;
                    for (ContentQuestionDto.ContentOptionDto opt : q.options()) {
                        AttemptQuestionOption option = new AttemptQuestionOption();
                        option.setAttemptQuestionId(aq.getId());
                        option.setOptionText(opt.text());
                        option.setCorrect(Boolean.TRUE.equals(opt.correct()));
                        option.setSortOrder(optOrder++);
                        attemptOptionRepository.save(option);
                    }
                }

                AttemptAnswer answer = new AttemptAnswer();
                answer.setAttemptQuestionId(aq.getId());
                answer.setStatus(AnswerStatus.NOT_VISITED);
                attemptAnswerRepository.save(answer);

                summaries.add(new StartAttemptResponse.AttemptQuestionSummary(
                        aq.getId(), section.getId(), section.getName(), aq.getSortOrder(), AnswerStatus.NOT_VISITED.name()));
            }
        }

        return new StartAttemptResponse(attempt.getId(), attempt.getExpiresAt(), exam.getNavigationMode().name(), summaries);
    }

    @Transactional
    public AttemptQuestionDetailResponse getQuestion(UUID attemptId, UUID userId, UUID attemptQuestionId) {
        ExamAttempt attempt = requireActiveAttempt(attemptId, userId);
        AttemptQuestion aq = attemptQuestionRepository.findById(attemptQuestionId)
                .filter(q -> q.getAttemptId().equals(attemptId))
                .orElseThrow(() -> new NotFoundException("Question not found in this attempt"));

        Exam exam = examRepository.findById(attempt.getExamId()).orElseThrow(() -> new NotFoundException("Exam not found"));
        enforceNavigation(exam.getNavigationMode(), attempt, aq);

        if (aq.getSortOrder() > attempt.getMaxSortOrderReached()) {
            attempt.setMaxSortOrderReached(aq.getSortOrder());
        }

        AttemptAnswer answer = attemptAnswerRepository.findById(attemptQuestionId)
                .orElseThrow(() -> new NotFoundException("Answer record missing"));
        if (answer.getStatus() == AnswerStatus.NOT_VISITED) {
            answer.setStatus(AnswerStatus.NOT_ANSWERED);
            attemptAnswerRepository.save(answer);
        }
        attemptRepository.save(attempt);

        List<AttemptQuestionDetailResponse.OptionView> options = attemptOptionRepository
                .findByAttemptQuestionIdOrderBySortOrder(aq.getId()).stream()
                .map(o -> new AttemptQuestionDetailResponse.OptionView(o.getId(), o.getOptionText()))
                .collect(Collectors.toList());

        return new AttemptQuestionDetailResponse(
                aq.getId(), aq.getSectionName(), aq.getSortOrder(), aq.getQuestionType(), aq.getQuestionText(),
                aq.getCodeSnippet(), options, answer.getStatus().name(),
                answer.getSelectedOptionIds() == null ? List.of() : Arrays.asList(answer.getSelectedOptionIds()),
                answer.getFreeTextAnswer(),
                Math.max(0, Instant.now().until(attempt.getExpiresAt(), ChronoUnit.SECONDS))
        );
    }

    @Transactional
    public void submitAnswer(UUID attemptId, UUID userId, UUID attemptQuestionId, SubmitAnswerRequest request) {
        ExamAttempt attempt = requireActiveAttempt(attemptId, userId);
        AttemptAnswer answer = attemptAnswerRepository.findById(attemptQuestionId)
                .filter(a -> belongsToAttempt(a, attemptId))
                .orElseThrow(() -> new NotFoundException("Question not found in this attempt"));

        boolean hasSelection = request.selectedOptionIds() != null && !request.selectedOptionIds().isEmpty();
        boolean hasText = request.freeTextAnswer() != null && !request.freeTextAnswer().isBlank();

        answer.setSelectedOptionIds(hasSelection ? request.selectedOptionIds().toArray(new UUID[0]) : null);
        answer.setFreeTextAnswer(hasText ? request.freeTextAnswer() : null);
        answer.setAnsweredAt(Instant.now());

        boolean wasMarked = answer.getStatus() == AnswerStatus.MARKED || answer.getStatus() == AnswerStatus.ANSWERED_MARKED;
        boolean hasAnswer = hasSelection || hasText;
        answer.setStatus(hasAnswer
                ? (wasMarked ? AnswerStatus.ANSWERED_MARKED : AnswerStatus.ANSWERED)
                : (wasMarked ? AnswerStatus.MARKED : AnswerStatus.NOT_ANSWERED));

        attemptAnswerRepository.save(answer);
    }

    @Transactional
    public void clearAnswer(UUID attemptId, UUID userId, UUID attemptQuestionId) {
        submitAnswer(attemptId, userId, attemptQuestionId, new SubmitAnswerRequest(List.of(), null));
    }

    @Transactional
    public void markForReview(UUID attemptId, UUID userId, UUID attemptQuestionId, MarkForReviewRequest request) {
        requireActiveAttempt(attemptId, userId);
        AttemptAnswer answer = attemptAnswerRepository.findById(attemptQuestionId)
                .filter(a -> belongsToAttempt(a, attemptId))
                .orElseThrow(() -> new NotFoundException("Question not found in this attempt"));

        boolean hasAnswer = answer.getStatus() == AnswerStatus.ANSWERED || answer.getStatus() == AnswerStatus.ANSWERED_MARKED;
        if (request.marked()) {
            answer.setStatus(hasAnswer ? AnswerStatus.ANSWERED_MARKED : AnswerStatus.MARKED);
        } else {
            answer.setStatus(hasAnswer ? AnswerStatus.ANSWERED : AnswerStatus.NOT_ANSWERED);
        }
        attemptAnswerRepository.save(answer);
    }

    @Transactional
    public PaletteResponse getPalette(UUID attemptId, UUID userId) {
        ExamAttempt attempt = requireOwnedAttempt(attemptId, userId);
        autoSubmitIfExpired(attempt);

        List<AttemptQuestion> questions = attemptQuestionRepository.findByAttemptIdOrderBySortOrder(attemptId);
        List<PaletteResponse.PaletteItem> items = questions.stream().map(q -> {
            AttemptAnswer answer = attemptAnswerRepository.findById(q.getId()).orElseThrow();
            return new PaletteResponse.PaletteItem(q.getId(), q.getSectionName(), q.getSortOrder(), answer.getStatus().name());
        }).collect(Collectors.toList());

        long secondsRemaining = attempt.getStatus() == AttemptStatus.IN_PROGRESS
                ? Math.max(0, Instant.now().until(attempt.getExpiresAt(), ChronoUnit.SECONDS)) : 0;

        return new PaletteResponse(secondsRemaining, items);
    }

    @Transactional
    public AttemptResultResponse submit(UUID attemptId, UUID userId) {
        ExamAttempt attempt = requireOwnedAttempt(attemptId, userId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return buildResult(attempt);
        }
        return finalizeAttempt(attempt, AttemptStatus.SUBMITTED);
    }

    @Transactional
    public AttemptResultResponse getResult(UUID attemptId, UUID userId) {
        ExamAttempt attempt = requireOwnedAttempt(attemptId, userId);
        autoSubmitIfExpired(attempt);
        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This attempt has not been submitted yet");
        }
        return buildResult(attempt);
    }

    // ---- internal helpers ----

    private ExamAttempt requireOwnedAttempt(UUID attemptId, UUID userId) {
        return attemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new NotFoundException("Attempt not found"));
    }

    private ExamAttempt requireActiveAttempt(UUID attemptId, UUID userId) {
        ExamAttempt attempt = requireOwnedAttempt(attemptId, userId);
        autoSubmitIfExpired(attempt);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new AttemptExpiredException("This attempt has already been submitted");
        }
        return attempt;
    }

    /** The server-side deadline check — called before every read/write against an attempt. */
    private void autoSubmitIfExpired(ExamAttempt attempt) {
        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS && attempt.isExpired()) {
            finalizeAttempt(attempt, AttemptStatus.AUTO_SUBMITTED);
        }
    }

    private boolean belongsToAttempt(AttemptAnswer answer, UUID attemptId) {
        return attemptQuestionRepository.findById(answer.getAttemptQuestionId())
                .map(q -> q.getAttemptId().equals(attemptId))
                .orElse(false);
    }

    private void enforceNavigation(NavigationMode mode, ExamAttempt attempt, AttemptQuestion target) {
        if (mode == NavigationMode.FORWARD_ONLY && target.getSortOrder() < attempt.getMaxSortOrderReached()) {
            throw new BadRequestException("This exam does not allow returning to earlier questions");
        }
        if (mode == NavigationMode.SECTION_LOCKED) {
            List<AttemptQuestion> earlierSectionQuestions = attemptQuestionRepository.findByAttemptIdOrderBySortOrder(attempt.getId())
                    .stream()
                    .filter(q -> !q.getSectionId().equals(target.getSectionId()) && q.getSortOrder() < target.getSortOrder())
                    .collect(Collectors.toList());
            boolean priorSectionIncomplete = earlierSectionQuestions.stream().anyMatch(q -> {
                AttemptAnswer a = attemptAnswerRepository.findById(q.getId()).orElseThrow();
                return a.getStatus() == AnswerStatus.NOT_VISITED;
            });
            if (priorSectionIncomplete) {
                throw new BadRequestException("Complete the current section before moving to the next one");
            }
        }
    }

    private AttemptResultResponse finalizeAttempt(ExamAttempt attempt, AttemptStatus finalStatus) {
        Exam exam = examRepository.findById(attempt.getExamId()).orElseThrow(() -> new NotFoundException("Exam not found"));
        List<AttemptQuestion> questions = attemptQuestionRepository.findByAttemptIdOrderBySortOrder(attempt.getId());

        int correct = 0, wrong = 0, skipped = 0, manualPending = 0;
        Map<String, int[]> sectionTally = new LinkedHashMap<>(); // name -> [correct, gradable]

        for (AttemptQuestion q : questions) {
            AttemptAnswer answer = attemptAnswerRepository.findById(q.getId()).orElseThrow();
            boolean isManual = "FILL_BLANK".equals(q.getQuestionType()) || "CODING".equals(q.getQuestionType()) || "SUBJECTIVE".equals(q.getQuestionType());
            boolean hasAnswer = answer.getStatus() == AnswerStatus.ANSWERED || answer.getStatus() == AnswerStatus.ANSWERED_MARKED;

            if (isManual) {
                if (hasAnswer) manualPending++; else skipped++;
                continue;
            }

            int[] tally = sectionTally.computeIfAbsent(q.getSectionName(), k -> new int[2]);
            tally[1]++; // gradable

            if (!hasAnswer) { skipped++; continue; }

            List<AttemptQuestionOption> options = attemptOptionRepository.findByAttemptQuestionIdOrderBySortOrder(q.getId());
            Set<UUID> correctIds = options.stream().filter(AttemptQuestionOption::isCorrect).map(AttemptQuestionOption::getId).collect(Collectors.toSet());
            Set<UUID> selectedIds = answer.getSelectedOptionIds() == null ? Set.of() : new HashSet<>(Arrays.asList(answer.getSelectedOptionIds()));

            if (correctIds.equals(selectedIds)) {
                correct++;
                tally[0]++;
            } else {
                wrong++;
            }
        }

        BigDecimal score = BigDecimal.valueOf(correct)
                .subtract(exam.getNegativeMarking().multiply(BigDecimal.valueOf(wrong)))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        attempt.setStatus(finalStatus);
        attempt.setSubmittedAt(Instant.now());
        attempt.setScore(score);
        attempt.setCorrectCount(correct);
        attempt.setWrongCount(wrong);
        attempt.setSkippedCount(skipped);
        attempt.setManualPendingCount(manualPending);
        attemptRepository.save(attempt);

        List<AttemptResultResponse.SectionResult> sectionResults = sectionTally.entrySet().stream()
                .map(e -> new AttemptResultResponse.SectionResult(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .collect(Collectors.toList());

        return new AttemptResultResponse(
                attempt.getId(), attempt.getStatus().name(), score, questions.size(),
                correct, wrong, skipped, manualPending,
                attempt.getStartedAt(), attempt.getSubmittedAt(), sectionResults
        );
    }

    private AttemptResultResponse buildResult(ExamAttempt attempt) {
        List<AttemptQuestion> questions = attemptQuestionRepository.findByAttemptIdOrderBySortOrder(attempt.getId());
        // Recompute the section breakdown for display without re-scoring (score is already persisted).
        Map<String, int[]> sectionTally = new LinkedHashMap<>();
        for (AttemptQuestion q : questions) {
            boolean isManual = "FILL_BLANK".equals(q.getQuestionType()) || "CODING".equals(q.getQuestionType()) || "SUBJECTIVE".equals(q.getQuestionType());
            if (isManual) continue;
            int[] tally = sectionTally.computeIfAbsent(q.getSectionName(), k -> new int[2]);
            tally[1]++;
            List<AttemptQuestionOption> options = attemptOptionRepository.findByAttemptQuestionIdOrderBySortOrder(q.getId());
            Set<UUID> correctIds = options.stream().filter(AttemptQuestionOption::isCorrect).map(AttemptQuestionOption::getId).collect(Collectors.toSet());
            AttemptAnswer answer = attemptAnswerRepository.findById(q.getId()).orElseThrow();
            Set<UUID> selectedIds = answer.getSelectedOptionIds() == null ? Set.of() : new HashSet<>(Arrays.asList(answer.getSelectedOptionIds()));
            if (!selectedIds.isEmpty() && correctIds.equals(selectedIds)) tally[0]++;
        }
        List<AttemptResultResponse.SectionResult> sectionResults = sectionTally.entrySet().stream()
                .map(e -> new AttemptResultResponse.SectionResult(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .collect(Collectors.toList());

        return new AttemptResultResponse(
                attempt.getId(), attempt.getStatus().name(), attempt.getScore(), questions.size(),
                attempt.getCorrectCount() == null ? 0 : attempt.getCorrectCount(),
                attempt.getWrongCount() == null ? 0 : attempt.getWrongCount(),
                attempt.getSkippedCount() == null ? 0 : attempt.getSkippedCount(),
                attempt.getManualPendingCount(),
                attempt.getStartedAt(), attempt.getSubmittedAt(), sectionResults
        );
    }
}
