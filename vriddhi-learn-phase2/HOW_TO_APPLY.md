# Phase 2: timed daily mock

This patch applies on top of your current GitHub main (checked against commit 3471f9e, which already contains Phase 1).

From the root of your local vriddhi-learn folder, with a clean working tree and the latest main pulled:

    git checkout -b feature/timed-daily-mock
    git apply vriddhi-learn-phase2.patch
    git add -A && git commit -m "Add timed daily mock (exam-service) and progress sync"

If you cannot use the patch, copy the files from `phase2-changed-files/` over your project (same paths).

Then:

1. Existing server: no new databases are needed (Phase 2 only adds migrations: exam-service V7, progress-service V2).
2. `docker compose up --build`, sign in, open /daily, press "Start timed mock".
3. Run `mvn -q test` in backend/exam-service (DailyExamBlueprintTest) and backend/progress-service.

PROJECT_STATUS.md lists what was verified here and what you still have to run (mvn compile/test).
