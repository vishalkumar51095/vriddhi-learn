-- Tracks how far into the exam (by global question sort_order) the learner
-- has progressed, so FORWARD_ONLY can be enforced server-side rather than
-- trusted to the client.
ALTER TABLE exam_attempts
    ADD COLUMN max_sort_order_reached INT NOT NULL DEFAULT 0;
