-- Teacher applications: self-service sign-up creates a normal LEARNER account plus a PENDING application.
-- The TEACHER role is only granted when an administrator approves it (never at registration time).
CREATE TABLE teacher_applications (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subjects         JSONB NOT NULL,                 -- ["Java", "Spring Boot", ...]
    experience_years INT NOT NULL CHECK (experience_years BETWEEN 0 AND 60),
    qualification    VARCHAR(200) NOT NULL,
    organization     VARCHAR(200),
    bio              TEXT NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    review_note      VARCHAR(500),
    reviewed_by      UUID REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One open application per user; rejected/approved ones are kept as history.
CREATE UNIQUE INDEX uq_teacher_app_pending ON teacher_applications (user_id) WHERE status = 'PENDING';
CREATE INDEX idx_teacher_app_status ON teacher_applications (status, created_at DESC);
