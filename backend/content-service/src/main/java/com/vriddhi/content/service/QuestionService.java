package com.vriddhi.content.service;

import com.vriddhi.content.dto.*;
import com.vriddhi.content.entity.*;
import com.vriddhi.content.exception.BadRequestException;
import com.vriddhi.content.exception.NotFoundException;
import com.vriddhi.content.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// Read methods traverse LAZY relations (topic -> subject, tags, currentVersion). With
// spring.jpa.open-in-view=false there is no session outside a transaction, so without
// this every read would throw LazyInitializationException. Write methods below override it.
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionVersionRepository versionRepository;
    private final QuestionOptionRepository optionRepository;
    private final TopicRepository topicRepository;
    private final TagRepository tagRepository;

    @Transactional
    public QuestionResponse create(QuestionCreateRequest request, UUID authorId) {
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new NotFoundException("Topic not found"));

        QuestionType type = parseType(request.type());
        Difficulty difficulty = parseDifficulty(request.difficulty());
        validateOptions(type, request);

        Question question = new Question();
        question.setTopic(topic);
        question.setType(type);
        question.setDifficulty(difficulty);
        question.setStatus(QuestionStatus.DRAFT);
        question.setCreatedBy(authorId);
        question.setTags(resolveTags(request.tags()));
        question = questionRepository.save(question);

        QuestionVersion version = createVersion(question.getId(), 1, request, authorId);
        question.setCurrentVersion(version);
        question = questionRepository.save(question);

        return toResponse(question, version);
    }

    /** Publishing edits creates a brand new version; past attempts keep referencing the old one. */
    @Transactional
    public QuestionResponse createNewVersion(UUID questionId, QuestionCreateRequest request, UUID authorId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        QuestionType type = parseType(request.type());
        validateOptions(type, request);

        int nextVersion = versionRepository.findByQuestionIdOrderByVersionNumberDesc(questionId)
                .stream().findFirst().map(v -> v.getVersionNumber() + 1).orElse(1);

        QuestionVersion version = createVersion(questionId, nextVersion, request, authorId);
        question.setCurrentVersion(version);
        question.setDifficulty(parseDifficulty(request.difficulty()));
        question.setStatus(QuestionStatus.DRAFT); // edits go back through review
        question = questionRepository.save(question);

        return toResponse(question, version);
    }

    @Transactional
    public QuestionResponse submitForReview(UUID questionId) {
        Question question = get(questionId);
        question.setStatus(QuestionStatus.PENDING_REVIEW);
        return toResponse(questionRepository.save(question), question.getCurrentVersion());
    }

    @Transactional
    public QuestionResponse review(UUID questionId, ReviewDecisionRequest request) {
        Question question = get(questionId);
        if ("APPROVE".equalsIgnoreCase(request.decision())) {
            question.setStatus(QuestionStatus.PUBLISHED);
        } else if ("REJECT".equalsIgnoreCase(request.decision())) {
            question.setStatus(QuestionStatus.REJECTED);
        } else {
            throw new BadRequestException("Decision must be APPROVE or REJECT");
        }
        return toResponse(questionRepository.save(question), question.getCurrentVersion());
    }

    /** Authoring/review view — includes which options are correct. */
    public QuestionResponse getById(UUID id) {
        Question question = get(id);
        return toResponse(question, question.getCurrentVersion(), true);
    }

    /** Learner-facing practice view — correct answers are stripped out. */
    public QuestionResponse getForLearner(UUID id) {
        Question question = get(id);
        if (question.getStatus() != QuestionStatus.PUBLISHED) {
            throw new NotFoundException("Question not found");
        }
        return toResponse(question, question.getCurrentVersion(), false);
    }

    public Page<QuestionResponse> list(UUID topicId, QuestionStatus status, Pageable pageable) {
        Page<Question> page = topicId != null
                ? questionRepository.findByTopic_IdAndStatus(topicId, status, pageable)
                : questionRepository.findByStatus(status, pageable);
        return page.map(q -> toResponse(q, q.getCurrentVersion(), true));
    }

    public Page<QuestionResponse> listMine(UUID authorId, Pageable pageable) {
        return questionRepository.findByCreatedBy(authorId, pageable).map(q -> toResponse(q, q.getCurrentVersion(), true));
    }

    /** Practice mode: a random published question from the given topics, answers stripped. */
    public List<QuestionResponse> randomForLearner(List<UUID> topicIds, int count) {
        List<Question> pool = questionRepository.findRandomPublishedByTopics(topicIds, org.springframework.data.domain.PageRequest.of(0, count));
        return pool.stream().map(q -> toResponse(q, q.getCurrentVersion(), false)).collect(Collectors.toList());
    }

    /**
     * Same random-by-topic pool as practice mode, but with correct answers
     * included — this is what exam-service's attempt-start snapshotting
     * calls, and it must know which options are correct to snapshot them.
     */
    public List<QuestionResponse> randomForInternal(List<UUID> topicIds, int count) {
        List<Question> pool = questionRepository.findRandomPublishedByTopics(topicIds, org.springframework.data.domain.PageRequest.of(0, count));
        return pool.stream().map(q -> toResponse(q, q.getCurrentVersion(), true)).collect(Collectors.toList());
    }

    /**
     * Grades one practice attempt on the spot, without creating any exam
     * attempt or persisting state — practice mode is deliberately stateless.
     * FILL_BLANK/CODING/SUBJECTIVE come back flagged for manual review, same
     * as they would inside a real exam.
     */
    public PracticeCheckResponse checkAnswer(UUID questionId, PracticeCheckRequest request) {
        Question question = get(questionId);
        if (question.getStatus() != QuestionStatus.PUBLISHED) {
            throw new NotFoundException("Question not found");
        }
        QuestionVersion version = question.getCurrentVersion();

        boolean isManual = question.getType() == QuestionType.FILL_BLANK
                || question.getType() == QuestionType.CODING
                || question.getType() == QuestionType.SUBJECTIVE;
        if (isManual) {
            return new PracticeCheckResponse(true, null, List.of(), version.getExplanation());
        }

        List<QuestionOption> options = optionRepository.findByQuestionVersionIdOrderBySortOrder(version.getId());
        Set<UUID> correctIds = options.stream().filter(QuestionOption::isCorrect).map(QuestionOption::getId).collect(Collectors.toSet());
        Set<UUID> selectedIds = request.selectedOptionIds() == null ? Set.of() : Set.copyOf(request.selectedOptionIds());

        boolean correct = !selectedIds.isEmpty() && correctIds.equals(selectedIds);
        return new PracticeCheckResponse(false, correct, List.copyOf(correctIds), version.getExplanation());
    }

    private Question get(UUID id) {
        return questionRepository.findById(id).orElseThrow(() -> new NotFoundException("Question not found"));
    }

    private QuestionVersion createVersion(UUID questionId, int versionNumber, QuestionCreateRequest request, UUID authorId) {
        QuestionVersion version = new QuestionVersion();
        version.setQuestionId(questionId);
        version.setVersionNumber(versionNumber);
        version.setQuestionText(request.questionText());
        version.setCodeSnippet(request.codeSnippet());
        version.setExplanation(request.explanation());
        version.setFillBlankAnswer(request.fillBlankAnswer());
        version.setCreatedBy(authorId);
        version = versionRepository.save(version);

        if (request.options() != null) {
            int order = 0;
            for (OptionRequest opt : request.options()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionVersionId(version.getId());
                option.setOptionText(opt.text());
                option.setCorrect(opt.correct());
                option.setSortOrder(order++);
                optionRepository.save(option);
            }
        }
        return version;
    }

    private QuestionResponse toResponse(Question question, QuestionVersion version) {
        return toResponse(question, version, true);
    }

    private void validateOptions(QuestionType type, QuestionCreateRequest request) {
        boolean needsOptions = type == QuestionType.SINGLE_CORRECT || type == QuestionType.MULTIPLE_CORRECT || type == QuestionType.TRUE_FALSE;
        if (!needsOptions) return;
        if (request.options() == null || request.options().size() < 2) {
            throw new BadRequestException("At least 2 options are required for this question type");
        }
        long correctCount = request.options().stream().filter(OptionRequest::correct).count();
        if (correctCount == 0) {
            throw new BadRequestException("At least one option must be marked correct");
        }
        if (type == QuestionType.SINGLE_CORRECT && correctCount != 1) {
            throw new BadRequestException("Single-correct questions must have exactly one correct option");
        }
    }

    private Set<Tag> resolveTags(List<String> names) {
        if (names == null) return Set.of();
        return names.stream()
                .map(name -> tagRepository.findByNameIgnoreCase(name).orElseGet(() -> {
                    Tag t = new Tag();
                    t.setName(name);
                    return tagRepository.save(t);
                }))
                .collect(Collectors.toSet());
    }

    private QuestionType parseType(String value) {
        try {
            return QuestionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown question type: " + value);
        }
    }

    private Difficulty parseDifficulty(String value) {
        try {
            return Difficulty.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown difficulty: " + value);
        }
    }

    private QuestionResponse toResponse(Question question, QuestionVersion version, boolean includeAnswers) {
        List<QuestionOptionResponse> options = version == null ? List.of() :
                optionRepository.findByQuestionVersionIdOrderBySortOrder(version.getId()).stream()
                        .map(o -> new QuestionOptionResponse(o.getId(), o.getOptionText(), includeAnswers ? o.isCorrect() : null))
                        .collect(Collectors.toList());

        return new QuestionResponse(
                question.getId(),
                question.getTopic().getId(),
                question.getTopic().getName(),
                question.getTopic().getSubject().getName(),
                question.getType().name(),
                question.getDifficulty().name(),
                question.getStatus().name(),
                version == null ? 0 : version.getVersionNumber(),
                version == null ? null : version.getQuestionText(),
                version == null ? null : version.getCodeSnippet(),
                version == null ? null : version.getExplanation(),
                options,
                question.getTags().stream().map(Tag::getName).collect(Collectors.toSet()),
                question.getUpdatedAt()
        );
    }
}
