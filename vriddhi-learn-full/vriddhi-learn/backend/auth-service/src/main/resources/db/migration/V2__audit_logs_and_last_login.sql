-- Audit trail (sign-ins, registrations, and every admin action) + last sign-in time.
CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    actor_id     UUID,                       -- null for failed sign-ins by an unknown email
    actor_email  VARCHAR(255),
    event_type   VARCHAR(50) NOT NULL,       -- LOGIN_SUCCESS, LOGIN_FAILURE, LOGIN_DENIED, REGISTER, LOGOUT,
                                             -- PROFILE_UPDATE, USER_STATUS_CHANGED, USER_ROLES_CHANGED
    target_type  VARCHAR(50),
    target_id    VARCHAR(100),
    details      TEXT,
    ip_address   VARCHAR(64),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_created ON audit_logs (created_at DESC);
CREATE INDEX idx_audit_event   ON audit_logs (event_type);
CREATE INDEX idx_audit_actor   ON audit_logs (actor_id);

ALTER TABLE users ADD COLUMN last_login_at TIMESTAMPTZ;
