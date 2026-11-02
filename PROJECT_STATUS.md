# Project status

Last updated: 2026-10-30

## Services

| Service | Port | Database | State |
|---|---|---|---|
| discovery-server | 8761 | none | done |
| api-gateway | 8080 | none | done (routes for lessons and progress added) |
| auth-service | 8081 | vriddhi_auth | done |
| content-service | 8082 | vriddhi_content | done (+ `/internal/topics/published`, Java Interview Prep seed V5) |
| exam-service | 8083 | vriddhi_exam | done (+ Java mock papers V6) |
| learning-service | 8084 | vriddhi_learning | NEW: lessons, completion, startup + admin import |
| progress-service | 8085 | vriddhi_progress | NEW: results, Leitner revision, daily plan, streak |

## This change (Java notes, daily practice, progress)

Verified here:
- Seed migrations (content V5, exam V6) applied to a real PostgreSQL with `infra/tools/verify_seed_data.py`.
- New schemas (learning V1, progress V1) and the published-topics and streak SQL ran against a real PostgreSQL.
- All 14 lessons parse with the real `LessonFileParser`; every lesson `topicId` matches a seeded topic.
- Unit tests for `Leitner` and `LessonFileParser` pass (11 tests, run with minimal JUnit shims).
- `infra/tools/check_java_syntax.py`: 206 files, 0 errors. `npm run build` passes. Contrast check passes.

NOT verified (the build sandbox cannot reach Maven Central): `mvn compile` / `mvn test` for the whole
backend, and the full docker-compose flow. Run these locally before deploying:

1. `docker compose up --build` and watch learning-service log `Lesson bootstrap: 14 created ...`.
2. Sign in, open `/learn`, `/daily`, answer a few questions, open `/progress`.
3. `mvn -q test` in `backend/progress-service` and `backend/learning-service`.

## Known limits and next steps

- Daily plan is generated on demand (no scheduler needed). A per-learner timed "Daily Mock" built through
  exam-service is the next step.
- Exam results do not feed progress-service yet (only practice answers do).
- No question or lesson editor UI; content is added through seed builders and lesson files.
- Coding-question execution (a sandboxed runner) is not built.
- AI-drafted questions with an approval queue are planned, not built.
