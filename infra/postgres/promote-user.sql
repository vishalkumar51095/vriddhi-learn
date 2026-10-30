-- Bootstrap helper: grants a role to an existing user.
--
-- This is needed for the very first ADMIN on a fresh install (nobody starts
-- as ADMIN, and only an ADMIN can grant roles through the API — see
-- POST /api/admin/users/{id}/roles), and is a fine shortcut for TEACHER too
-- if you don't want to go through an admin account at all.
--
-- Usage:
--   docker exec -it vriddhi-postgres psql -U vriddhi -d vriddhi_auth \
--     -v email="'teacher@example.com'" -v role="'TEACHER'" \
--     -f /path/to/promote-user.sql

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = :email
  AND r.name = :role
ON CONFLICT DO NOTHING;
