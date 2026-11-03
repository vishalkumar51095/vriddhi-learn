# Project status

Last updated: 2026-11-03

## Services

| Service | Port | Database | State |
|---|---|---|---|
| discovery-server | 8761 | none | done |
| api-gateway | 8080 | none | done (routes for lessons and progress added) |
| auth-service | 8081 | vriddhi_auth | done |
| content-service | 8082 | vriddhi_content | done (+ `/internal/topics/published`, Java Interview Prep seed V5) |
| exam-service | 8083 | vriddhi_exam | done (+ Java mock papers V6, timed daily mock + progress sync V7) |
| learning-service | 8084 | vriddhi_learning | NEW: lessons, completion, startup + admin import |
| progress-service | 8085 | vriddhi_progress | NEW: results, Leitner revision, daily plan, streak, internal results (V2) |

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

## Phase 2: timed daily mock

Verified here: migrations exam V7 and progress V2 apply on a real PostgreSQL; the unique "one daily exam per
learner/category/day" index, owner/date and kind CHECKs, `progress_sync_status` CHECK, the `FOR UPDATE` row-lock
query, listing filters (daily exams hidden) and the idempotency table all behave correctly; `verify_sql.py` passes
(its hard-coded seed counts now include the Java content); 7 unit tests for `DailyExamBlueprint` pass (run with minimal
JUnit shims); syntax check 225 files, 0 errors; frontend build passes.

NOT verified: `mvn compile` / `mvn test` and the running services (Maven Central is blocked in the build sandbox).
Please test this flow locally: sign in, open `/daily`, press "Start timed mock", answer, submit, then check `/progress`
(the mock's answers should appear within a minute) and press "View result" on the card.

## Known limits and next steps

- Daily plan and daily mock are generated on demand (no scheduler needed).
- A due-for-revision question is represented by its topic in the mock (random question from that topic), not pinned.
- The Dashboard still shows illustrative subject-progress numbers; progress-service can now supply real ones.
- No question or lesson editor UI; content is added through seed builders and lesson files.
- Coding-question execution (a sandboxed runner) is not built.
- AI-drafted questions with an approval queue are planned, not built.
