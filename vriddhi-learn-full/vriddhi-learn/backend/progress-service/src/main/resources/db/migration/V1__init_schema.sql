-- progress-service: what each learner practised, how they are doing per topic, and when each question
-- is next due for revision (Leitner boxes). question_id / topic_id are plain UUIDs pointing at
-- content-service rows (separate database, so no foreign keys).
CREATE TABLE practice_results (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL,
    question_id   UUID         NOT NULL,
    topic_id      UUID         NOT NULL,
    topic_name    VARCHAR(150) NOT NULL,
    subject_name  VARCHAR(150) NOT NULL,
    correct       BOOLEAN      NOT NULL,
    answered_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_practice_results_user_time ON practice_results (user_id, answered_at DESC);

CREATE TABLE topic_stats (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID         NOT NULL,
    topic_id           UUID         NOT NULL,
    topic_name         VARCHAR(150) NOT NULL,
    subject_name       VARCHAR(150) NOT NULL,
    attempts           INT          NOT NULL DEFAULT 0,
    correct_count      INT          NOT NULL DEFAULT 0,
    last_practiced_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_topic_stats UNIQUE (user_id, topic_id),
    CONSTRAINT chk_topic_stats_counts CHECK (attempts >= 0 AND correct_count >= 0 AND correct_count <= attempts)
);

CREATE TABLE review_states (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    question_id     UUID        NOT NULL,
    topic_id        UUID        NOT NULL,
    box             INT         NOT NULL DEFAULT 1,
    attempts        INT         NOT NULL DEFAULT 0,
    last_correct    BOOLEAN     NOT NULL,
    next_review_at  TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_review_state UNIQUE (user_id, question_id),
    CONSTRAINT chk_review_box CHECK (box BETWEEN 1 AND 5)
);
CREATE INDEX idx_review_states_due ON review_states (user_id, next_review_at);
