-- Phase 2: a private, timed "daily mock" exam per learner per day, and syncing finished attempts to progress-service.

-- A DAILY exam belongs to one learner and one calendar day. STANDARD exams are unchanged (owner/date stay NULL).
ALTER TABLE exams
    ADD COLUMN kind          VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    ADD COLUMN owner_user_id UUID,
    ADD COLUMN plan_date     DATE,
    ADD CONSTRAINT chk_exams_kind CHECK (kind IN ('STANDARD', 'DAILY')),
    ADD CONSTRAINT chk_exams_daily_owner CHECK (kind = 'STANDARD' OR (owner_user_id IS NOT NULL AND plan_date IS NOT NULL));

-- At most one daily mock per learner, category and day (makes "start" idempotent and race-safe).
CREATE UNIQUE INDEX uq_daily_exam_per_user_day ON exams (owner_user_id, category_code, plan_date) WHERE kind = 'DAILY';
CREATE INDEX idx_exams_kind_status ON exams (kind, status);

-- Remember each question's topic so a finished attempt can be reported to progress-service.
-- NULL for attempts that were started before this migration (they are simply not synced).
ALTER TABLE attempt_questions
    ADD COLUMN topic_id     UUID,
    ADD COLUMN topic_name   VARCHAR(150),
    ADD COLUMN subject_name VARCHAR(150);

-- NOT_APPLICABLE: nothing to report. PENDING: finished, results not yet delivered. DONE: delivered.
ALTER TABLE exam_attempts
    ADD COLUMN progress_sync_status VARCHAR(20) NOT NULL DEFAULT 'NOT_APPLICABLE',
    ADD CONSTRAINT chk_attempt_progress_sync CHECK (progress_sync_status IN ('NOT_APPLICABLE', 'PENDING', 'DONE'));
CREATE INDEX idx_attempts_sync_pending ON exam_attempts (submitted_at) WHERE progress_sync_status = 'PENDING';
