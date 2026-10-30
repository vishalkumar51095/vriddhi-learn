-- Vriddhi Learn — Content Service schema
-- Preparation categories -> subjects -> topics -> questions (doc section 3 & 15)
-- Questions are versioned: editing a published question creates a new
-- question_versions row rather than mutating history, so an exam attempt
-- that snapshots a version never changes underneath the learner.

CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(50) NOT NULL UNIQUE,   -- SOFTWARE_DEVELOPER, SSC_CGL, IBPS_PO, RAILWAY
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE subjects (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    sort_order  INT NOT NULL DEFAULT 0
);

CREATE TABLE topics (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id  UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    sort_order  INT NOT NULL DEFAULT 0
);

CREATE TABLE questions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id            UUID NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    type                VARCHAR(30) NOT NULL
                            CHECK (type IN ('SINGLE_CORRECT','MULTIPLE_CORRECT','TRUE_FALSE','FILL_BLANK','CODING','SUBJECTIVE')),
    difficulty          VARCHAR(10) NOT NULL CHECK (difficulty IN ('EASY','MEDIUM','HARD')),
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT','PENDING_REVIEW','PUBLISHED','REJECTED','ARCHIVED')),
    current_version_id  UUID,  -- FK added after question_versions exists
    created_by          UUID NOT NULL,  -- teacher/admin user id (from Auth service, no local FK)
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE question_versions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id       UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    version_number    INT NOT NULL,
    question_text     TEXT NOT NULL,
    code_snippet      TEXT,                 -- shown above coding/SQL questions
    explanation       TEXT,
    fill_blank_answer VARCHAR(500),         -- reference answer for FILL_BLANK (manually graded)
    created_by        UUID NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (question_id, version_number)
);

ALTER TABLE questions
    ADD CONSTRAINT fk_questions_current_version
    FOREIGN KEY (current_version_id) REFERENCES question_versions(id);

CREATE TABLE question_options (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_version_id UUID NOT NULL REFERENCES question_versions(id) ON DELETE CASCADE,
    option_text         VARCHAR(1000) NOT NULL,
    is_correct          BOOLEAN NOT NULL DEFAULT false,
    sort_order          INT NOT NULL DEFAULT 0
);

CREATE TABLE tags (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE question_tags (
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    tag_id      UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (question_id, tag_id)
);

CREATE INDEX idx_questions_topic ON questions(topic_id);
CREATE INDEX idx_questions_status ON questions(status);
CREATE INDEX idx_question_versions_question ON question_versions(question_id);
CREATE INDEX idx_topics_subject ON topics(subject_id);
CREATE INDEX idx_subjects_category ON subjects(category_id);

-- Seed: preparation categories + the Java Backend subject/topic tree used
-- throughout the prototypes.
INSERT INTO categories (code, name, description) VALUES
    ('SOFTWARE_DEVELOPER', 'Software Developer', 'Java, DSA, SQL, Spring Boot, System Design'),
    ('SSC_CGL', 'SSC CGL', 'Quant, Reasoning, English, General Awareness'),
    ('IBPS_PO', 'IBPS PO', 'Quant, Reasoning, English, Banking Awareness'),
    ('RAILWAY', 'Railway', 'Configured through the same generic content model');

INSERT INTO subjects (category_id, name, description, sort_order)
SELECT id, 'Core Java', 'OOP, Collections, Exceptions, Multithreading', 1 FROM categories WHERE code = 'SOFTWARE_DEVELOPER';
INSERT INTO subjects (category_id, name, description, sort_order)
SELECT id, 'DSA', 'Arrays, Trees, Graphs, Dynamic Programming', 2 FROM categories WHERE code = 'SOFTWARE_DEVELOPER';
INSERT INTO subjects (category_id, name, description, sort_order)
SELECT id, 'SQL', 'Joins, Subqueries, Indexing', 3 FROM categories WHERE code = 'SOFTWARE_DEVELOPER';
INSERT INTO subjects (category_id, name, description, sort_order)
SELECT id, 'Spring Boot', 'Spring Boot, Spring Security, Spring Data', 4 FROM categories WHERE code = 'SOFTWARE_DEVELOPER';

INSERT INTO topics (subject_id, name, sort_order)
SELECT id, 'Multithreading', 1 FROM subjects WHERE name = 'Core Java';
INSERT INTO topics (subject_id, name, sort_order)
SELECT id, 'Collections', 2 FROM subjects WHERE name = 'Core Java';
INSERT INTO topics (subject_id, name, sort_order)
SELECT id, 'Spring Security', 1 FROM subjects WHERE name = 'Spring Boot';
