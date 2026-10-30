-- Vriddhi Learn — Auth Service schema
-- Users, roles, permissions and their join tables (docs section 15 & 3)

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED','PENDING','DELETED')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) NOT NULL UNIQUE,   -- LEARNER, TEACHER, ADMIN, CONTENT_REVIEWER, SUPER_ADMIN, MENTOR
    description VARCHAR(255)
);

CREATE TABLE permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(100) NOT NULL UNIQUE,  -- e.g. QUESTION_CREATE, EXAM_PUBLISH, USER_MANAGE
    description VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    issued_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- Seed roles
INSERT INTO roles (name, description) VALUES
    ('LEARNER', 'Preparation: learn, practice, mock tests, results, progress, bookmarks, revision'),
    ('TEACHER', 'Content creation: questions, explanations, mock tests, assigned content'),
    ('ADMIN', 'Platform operation: users, content, exams, publishing, reports, settings'),
    ('CONTENT_REVIEWER', 'Quality control: review/approve/reject content'),
    ('SUPER_ADMIN', 'Platform ownership: system configuration'),
    ('MENTOR', 'Guidance: learner guidance and feedback');

-- Seed core permissions
INSERT INTO permissions (code, description) VALUES
    ('QUESTION_CREATE', 'Create questions'),
    ('QUESTION_EDIT', 'Edit questions'),
    ('QUESTION_PUBLISH', 'Publish questions'),
    ('EXAM_CREATE', 'Create exams'),
    ('EXAM_PUBLISH', 'Publish exams'),
    ('USER_MANAGE', 'Manage user accounts'),
    ('REPORTS_VIEW', 'View platform reports'),
    ('SETTINGS_MANAGE', 'Manage platform settings'),
    ('CONTENT_REVIEW', 'Review and approve/reject content');

-- Wire up default role -> permission grants
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'TEACHER' AND p.code IN ('QUESTION_CREATE','QUESTION_EDIT','EXAM_CREATE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'CONTENT_REVIEWER' AND p.code IN ('CONTENT_REVIEW','QUESTION_EDIT');
