package com.vriddhi.exam.service;

import com.vriddhi.exam.dto.ExamCreateRequest;
import com.vriddhi.exam.dto.ExamDetailResponse;
import com.vriddhi.exam.dto.ExamSectionRequest;
import com.vriddhi.exam.dto.ExamSummaryResponse;
import com.vriddhi.exam.entity.Exam;
import com.vriddhi.exam.entity.ExamSection;
import com.vriddhi.exam.entity.ExamStatus;
import com.vriddhi.exam.entity.NavigationMode;
import com.vriddhi.exam.exception.BadRequestException;
import com.vriddhi.exam.exception.NotFoundException;
import com.vriddhi.exam.repository.ExamRepository;
import com.vriddhi.exam.repository.ExamSectionRepository;
import com.vriddhi.exam.repository.SectionTopicDao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamSectionRepository sectionRepository;
    private final SectionTopicDao sectionTopicDao;

    @Transactional
    public ExamDetailResponse create(ExamCreateRequest request, UUID createdBy) {
        NavigationMode navMode;
        try {
            navMode = NavigationMode.valueOf(request.navigationMode());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown navigation mode: " + request.navigationMode());
        }

        Exam exam = new Exam();
        exam.setTitle(request.title());
        exam.setCategoryCode(request.categoryCode());
        exam.setDurationMinutes(request.durationMinutes());
        exam.setNegativeMarking(request.negativeMarking());
        exam.setNavigationMode(navMode);
        exam.setStatus(ExamStatus.DRAFT);
        exam.setCreatedBy(createdBy);
        exam = examRepository.save(exam);

        int order = 0;
        for (ExamSectionRequest sectionReq : request.sections()) {
            ExamSection section = new ExamSection();
            section.setExamId(exam.getId());
            section.setName(sectionReq.name());
            section.setSortOrder(order++);
            section.setQuestionCount(sectionReq.questionCount());
            section = sectionRepository.save(section);
            for (UUID topicId : sectionReq.topicIds()) {
                sectionTopicDao.link(section.getId(), topicId);
            }
        }

        return getDetail(exam.getId());
    }

    @Transactional
    public ExamDetailResponse publish(UUID examId) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new NotFoundException("Exam not found"));
        exam.setStatus(ExamStatus.PUBLISHED);
        examRepository.save(exam);
        return getDetail(examId);
    }

    public ExamDetailResponse getDetail(UUID examId) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new NotFoundException("Exam not found"));
        List<ExamSection> sections = sectionRepository.findByExamIdOrderBySortOrder(examId);
        return new ExamDetailResponse(
                exam.getId(), exam.getTitle(), exam.getCategoryCode(), exam.getDurationMinutes(),
                exam.getNegativeMarking(), exam.getNavigationMode().name(), exam.getStatus().name(),
                sections.stream().map(s -> new ExamDetailResponse.SectionSummary(s.getId(), s.getName(), s.getQuestionCount()))
                        .collect(Collectors.toList())
        );
    }

    public Page<ExamSummaryResponse> listPublished(String categoryCode, Pageable pageable) {
        Page<Exam> page = categoryCode != null
                ? examRepository.findByStatusAndCategoryCode(ExamStatus.PUBLISHED, categoryCode, pageable)
                : examRepository.findByStatus(ExamStatus.PUBLISHED, pageable);
        return page.map(this::toSummary);
    }

    private ExamSummaryResponse toSummary(Exam exam) {
        List<ExamSection> sections = sectionRepository.findByExamIdOrderBySortOrder(exam.getId());
        int totalQuestions = sections.stream().mapToInt(ExamSection::getQuestionCount).sum();
        return new ExamSummaryResponse(exam.getId(), exam.getTitle(), exam.getCategoryCode(),
                exam.getDurationMinutes(), sections.size(), totalQuestions, exam.getStatus().name());
    }
}
