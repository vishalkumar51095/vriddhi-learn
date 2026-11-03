-- learning-service: study notes (lessons) and which lessons each learner has finished.
-- topic_id points at a content-service topic. It is a plain UUID, not a foreign key, because the two
-- services have separate databases (the same rule exam-service follows for its topic references).
CREATE TABLE lessons (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug           VARCHAR(120) NOT NULL UNIQUE,
    title          VARCHAR(200) NOT NULL,
    category_code  VARCHAR(60)  NOT NULL,
    subject_name   VARCHAR(150) NOT NULL,
    topic_id       UUID,
    topic_name     VARCHAR(150),
    sort_order     INT          NOT NULL DEFAULT 0,
    body_markdown  TEXT         NOT NULL,
    content_hash   VARCHAR(64)  NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_lessons_status CHECK (status IN ('DRAFT', 'PUBLISHED'))
);
CREATE INDEX idx_lessons_topic ON lessons (topic_id);
CREATE INDEX idx_lessons_category_order ON lessons (category_code, sort_order);

CREATE TABLE lesson_completions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL,
    lesson_id     UUID        NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    completed_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_lesson_completion UNIQUE (user_id, lesson_id)
);
CREATE INDEX idx_lesson_completions_user ON lesson_completions (user_id);
