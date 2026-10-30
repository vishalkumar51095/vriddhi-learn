-- Vriddhi Learn — Exam Service schema
-- Exams are defined against topic pools; when an attempt starts, questions
-- are snapshotted (text, options, correct answers) into attempt_questions so
-- a later edit in Content Service can never change an in-progress or
-- completed attempt's scoring (doc sections 5, 10, 15).

CREATE TABLE exams (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title             VARCHAR(255) NOT NULL,
    category_code     VARCHAR(50) NOT NULL,   -- matches Content Service's categories.code
    duration_minutes  INT NOT NULL,
    negative_marking  NUMERIC(4,2) NOT NULL DEFAULT 0.25,
    navigation_mode   VARCHAR(20) NOT NULL DEFAULT 'FREE'
                          CHECK (navigation_mode IN ('FREE','FORWARD_ONLY','SECTION_LOCKED')),
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    created_by        UUID NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE exam_sections (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id        UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    name           VARCHAR(150) NOT NULL,
    sort_order     INT NOT NULL DEFAULT 0,
    question_count INT NOT NULL
);

-- Which Content Service topics a section draws its question pool from.
CREATE TABLE exam_section_topics (
    section_id UUID NOT NULL REFERENCES exam_sections(id) ON DELETE CASCADE,
    topic_id   UUID NOT NULL,  -- Content Service topic id, no local FK (different service/db)
    PRIMARY KEY (section_id, topic_id)
);

CREATE TABLE exam_attempts (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id      UUID NOT NULL REFERENCES exams(id),
    user_id      UUID NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'
                     CHECK (status IN ('IN_PROGRESS','SUBMITTED','AUTO_SUBMITTED')),
    started_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at   TIMESTAMPTZ NOT NULL,       -- server-enforced deadline (doc: "timer is server-backed")
    submitted_at TIMESTAMPTZ,
    score        NUMERIC(6,2),
    correct_count   INT,
    wrong_count     INT,
    skipped_count   INT,
    manual_pending_count INT NOT NULL DEFAULT 0
);

CREATE TABLE attempt_questions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id          UUID NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE,
    section_id          UUID NOT NULL REFERENCES exam_sections(id),
    section_name        VARCHAR(150) NOT NULL,
    sort_order          INT NOT NULL,
    question_id         UUID NOT NULL,          -- Content Service question id (audit trail)
    question_version_id UUID NOT NULL,          -- exact version snapshotted
    question_type       VARCHAR(30) NOT NULL,
    question_text       TEXT NOT NULL,
    code_snippet        TEXT,
    explanation         TEXT,
    fill_blank_answer   VARCHAR(500)            -- reference answer, manually graded
);

CREATE TABLE attempt_question_options (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_question_id  UUID NOT NULL REFERENCES attempt_questions(id) ON DELETE CASCADE,
    option_text          VARCHAR(1000) NOT NULL,
    is_correct           BOOLEAN NOT NULL,
    sort_order           INT NOT NULL
);

CREATE TABLE attempt_answers (
    attempt_question_id UUID PRIMARY KEY REFERENCES attempt_questions(id) ON DELETE CASCADE,
    status               VARCHAR(20) NOT NULL DEFAULT 'NOT_VISITED'
                              CHECK (status IN ('NOT_VISITED','NOT_ANSWERED','ANSWERED','MARKED','ANSWERED_MARKED')),
    selected_option_ids  UUID[],          -- single or multi-correct selections
    free_text_answer     TEXT,            -- fill-blank / coding submissions
    answered_at          TIMESTAMPTZ
);

CREATE INDEX idx_attempts_user ON exam_attempts(user_id);
CREATE INDEX idx_attempts_exam ON exam_attempts(exam_id);
CREATE INDEX idx_attempt_questions_attempt ON attempt_questions(attempt_id);
CREATE INDEX idx_exam_sections_exam ON exam_sections(exam_id);
