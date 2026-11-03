-- Exam attempts reported by exam-service are applied at most once: the attempt id is the idempotency key.
CREATE TABLE processed_attempts (
    attempt_id    UUID PRIMARY KEY,
    processed_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
