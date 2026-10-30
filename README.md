# Vriddhi Learn

A preparation platform (interview/coding prep to start, generic enough to add
SSC/IBPS/Railway-style exams later) built as Java/Spring Boot microservices
behind a single API gateway, with a React + TypeScript frontend.

## Architecture

```
frontend/                React + TS (Vite) — the actual app people use
backend/
  discovery-server/       Eureka registry — every service registers here by name (:8761)
  api-gateway/            Spring Cloud Gateway — single public entry point (:8080)
  auth-service/           Users, roles, permissions, JWT issuing (:8081)
  content-service/        Subjects, topics, versioned question bank (:8082)
  exam-service/           Exams, attempts, snapshot-based scoring engine (:8083)
infra/
  postgres/               Multi-database init script for local dev
docker-compose.yml        Brings up Postgres, Redis, discovery-server, and all four services
```

Design notes baked into the backend:

- **Service discovery, not hardcoded URLs.** auth-service, content-service,
  exam-service, and api-gateway all register themselves with discovery-server
  (Eureka) under their `spring.application.name` on startup. The gateway
  routes to `lb://auth-service`, `lb://content-service`, `lb://exam-service`
  instead of a fixed host:port, and exam-service's calls to content-service
  go through a `@LoadBalanced` WebClient that resolves `content-service` the
  same way. Add a second instance of any service and it's load-balanced
  automatically, no config change needed. Dashboard: http://localhost:8761.
- **JWT, verified everywhere, issued once.** auth-service signs access tokens
  with a shared secret; content-service and exam-service only *verify* —
  no synchronous call back to Auth on every request.
- **Questions are versioned.** Editing a published question creates a new
  `question_versions` row rather than mutating history, so a question that's
  already been used in an exam attempt can't change under a learner mid-way through.
- **Attempts snapshot their questions.** Starting an attempt copies the
  current question text/options/correct-answers into `attempt_questions` in
  exam-service's own database. A later edit in content-service, or even
  content-service being down, can't affect an attempt already in progress.
- **The timer is server-enforced.** `exam_attempts.expires_at` is the source
  of truth, checked on every read/write against an attempt, plus a 30-second
  background sweep for tabs that were simply closed.
- **Navigation modes are enforced server-side.** FREE / FORWARD_ONLY /
  SECTION_LOCKED are real checks in `AttemptService`, not just UI conventions.

## Running it locally

This sandbox has no network access to Maven Central, so the Java services are
written and structured correctly but **not compiled here**. To actually run
the stack:

```bash
docker compose up --build
```

This builds all four Spring Boot services (each Dockerfile does its own
`mvn package` inside the build stage, which needs normal internet access to
Maven Central) and starts Postgres + Redis alongside them. The gateway comes
up on http://localhost:8080.

### Frontend

The frontend **was** built and verified in this environment (npm's registry
is reachable here):

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173, proxies /api to the gateway on :8080
# or
npm run build    # produces frontend/dist — verified clean, no errors/warnings
```

### Creating a teacher (or the first admin)

Registration always assigns `LEARNER`. Getting a `TEACHER` or `ADMIN` account
works one of three ways:

**Option 0 — teacher application (self-service).** Someone applies at
`/teacher/register` (a 3-step form). That calls `POST /api/auth/register-teacher`, which
creates a normal `LEARNER` account plus a `PENDING` row in `teacher_applications` — it does
**not** grant `TEACHER` and returns no tokens. An admin reviews it under *Admin → Teacher
applications* (`GET /api/admin/teacher-applications`, `POST .../{id}/approve|reject`); approval
adds the `TEACHER` role and every decision is audit-logged. The applicant signs in again on
the teacher portal afterwards.

**Option A — SQL bootstrap** (needed for the very first admin, since nobody
starts as ADMIN and only an ADMIN can grant roles through the API):

```bash
docker exec -it vriddhi-postgres psql -U vriddhi -d vriddhi_auth \
  -v email="'someone@example.com'" -v role="'ADMIN'" \
  -f /infra/postgres/promote-user.sql
```

**Option B — the admin API**, once you have at least one admin:

```bash
curl -X PATCH http://localhost:8080/api/admin/users/{userId}/roles \
  -H "Authorization: Bearer $ADMIN_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"roles": ["LEARNER", "TEACHER"]}'
```

Either way, log out and back in afterwards — roles are baked into the JWT at
issue time, not looked up per-request, so an existing session won't pick up
the change until it gets a fresh token.

### Creating your first exam

The seed data only populates categories/subjects/topics. To get an exam
listed in the frontend:

1. Register a user, then promote it to TEACHER (see above).
2. `POST /api/questions` (as TEACHER/ADMIN) to create a few questions, then
   `POST /api/questions/{id}/submit-for-review` and `POST /api/questions/{id}/review`
   (as ADMIN/CONTENT_REVIEWER) to publish them.
3. `POST /api/exams` (as TEACHER/ADMIN) with sections pointing at topic IDs,
   then `POST /api/exams/{id}/publish`.
4. Log into the frontend as a learner — the exam now appears under **Exams**
   and can be started for real, hitting the live attempt API end to end.

## TCS NQT papers

Three ready-to-take mock papers ship pre-seeded (published, no setup needed),
under a new `TCS_NQT` category:

| Paper | Sections | Time | Navigation |
|---|---|---|---|
| Full Mock Paper 1 | Numerical, Verbal, Reasoning, Programming (5 each) | 60 min | Section-locked, like the real test |
| Foundation Aptitude Sprint | Numerical, Verbal, Reasoning (4 each) | 30 min | Free |
| Reasoning & Programming Sprint | Reasoning (5), Programming (6) | 25 min | Forward-only |

There is no negative marking, matching the real test. Each of the four topics has a pool of
**8 questions** and every section draws at random from its pool, so re-taking a paper
gives a different mix. All 32 questions are original, written for this platform,
not copied from any TCS paper or third-party bank. Every question has an explanation,
shown in Practice mode after answering.

Where it lives: `content-service/.../V2__seed_tcs_nqt.sql` (category, subjects, topics,
questions) and `exam-service/.../V3__seed_tcs_nqt_papers.sql` (the three papers).
They use fixed UUIDs on purpose: the two services have separate databases, so the
exam papers reference the content topics by literal id.

**Adding more questions or papers.** Add rows following the same pattern, then run:

```bash
pip install pgserver psycopg2-binary
python3 infra/tools/verify_seed_data.py
```

It applies every migration to a throwaway PostgreSQL and checks that each single-correct
question has exactly one correct option, every exam section points at a real topic, and no
section asks for more questions than its pool holds. (The coding round of the real
TCS NQT isn't covered: coding questions here are manually graded.)

## UGC NET papers

Three more published mock papers under a `UGC_NET` category, no negative marking (as in the real test):

| Paper | Sections | Time | Navigation |
|---|---|---|---|
| Paper 1 Mock (Teaching & Research Aptitude) | 5 topics × 3 questions | 20 min | Free |
| Paper 2 Mock (Computer Science & Applications) | 6 topics × 4 questions | 30 min | Free |
| Paper 1 + Paper 2 Combined Mock | 10 from Paper 1, 20 from Paper 2 | 36 min | Free |

The bank has **39 questions** (15 for Paper 1, 24 for Paper 2). **These are original, NET-style
practice questions written for this platform. They are NOT actual previous-year papers** and are
tagged `NET-style practice` rather than a year. Real past papers are published by the NTA and can be
loaded with the generator below, tagged with their exam session.

**Adding real previous-year questions.** Questions live in `infra/seed-data/ugc_net.json`
(category → subjects → topics → questions; per-question `tags` such as `"UGC NET Dec 2023"`).
Put new questions in a *new* JSON file that reuses the same subject/topic names, then generate a
questions-only migration with the next version number (never regenerate a migration that has
already been applied — Flyway checksums it):

```bash
python3 infra/tools/gen_seed_sql.py infra/seed-data/ugc_net_pyq_dec2023.json --questions-only \
    --content-out backend/content-service/src/main/resources/db/migration/V5__ugc_net_pyq_dec2023.sql
python3 infra/tools/verify_seed_data.py     # applies every migration to a throwaway PostgreSQL and checks them
```

Ids are `uuid5` of the names, so topic ids are identical across runs and the exam-service migration
can reference them without a shared database. A section draws from the *combined* pool of its topics.

## UGC NET — June 2025 previous-year paper

`UGC NET Computer Science — June 2025 (95 of 100 questions)` is a published mock: 120 minutes,
free navigation, no negative marking, one section per subject area (14 sections). Its questions
are the real June 2025 Paper 2 questions, tagged `UGC NET June 2025`, and live in their own subject
(**Paper 2 — Previous Year: June 2025**) so the paper is served whole every time and each area can
also be practised on its own.

* **Answer keys were re-checked.** Every key was re-derived independently (the computational
  ones — LCS, disk scheduling, SJF/round-robin, perceptron, etc. — were also run in code) and
  matched the supplied key. A few questions are ambiguous in the source (Q46 write operation,
  Q59 XML, Q79 cache coherence, Q89 I/O sequence); the supplied key is used and the explanation says so.
* **Explanations are original**, written for this platform, not copied from the source solutions.
* **5 of 100 are not included:** Q50 and Q75 need automata diagrams; Q95 needs matrix images (its
  options B and C were identical in the source); Q57 and Q92 were withdrawn by UGC and only exist in
  the source in a rewritten form. Question text now keeps its line breaks (`whitespace-pre-line`) so
  statement lists and match-the-following read properly.

Regenerate from source with `python3 infra/seed-data/build_ugc_net_june2025.py`, then
`python3 infra/tools/gen_seed_sql.py infra/seed-data/ugc_net_june2025.json --skip-category ...`
(only before the migration has been applied — Flyway checksums migrations).

## Design system, portals & admin console

A large pass addressing five specific problems: the home page had no real numbers, the
login page was visually bare, student/teacher sign-in weren't separated, colour contrast
was weak, the About page was static text, and there was no admin console.

**Theme system.** Light/dark + three accent colours (amber/indigo/teal), set via
`data-theme`/`data-accent` on `<html>`, applied before first paint (inline script in
`index.html`, no flash), controlled by the switcher in the header. Every token pair is
checked against WCAG AA by `infra/tools/check_contrast.py` — 246 pairs across all 6
theme combinations, 0 failures. Semantic colours (`bg-green`, `bg-amber`, …) intentionally
invert between light/dark for use as text-on-soft-background; solid fills meant to hold
white text (palette cells, badges) use the separate `solid-*` tokens that stay saturated
in both themes — mixing the two up is exactly the kind of dark-mode bug that's easy to
ship, so the checker and a manual sweep both exist to catch it.

**Three separate portals**, not one form with a dropdown: `/login` (student),
`/teacher/login`, `/admin/login`, each with its own brand panel, copy and gradient. All
three call the same `POST /api/auth/login`, now with an optional `portal` field —
auth-service checks the password first (so a wrong password never reveals which roles an
account holds), then checks the account has the matching role, returning a clear 403
("This isn't a student account…") rather than a generic failure.

**Home page now shows real numbers** — active learners/teachers, published questions,
mock exam papers, attempts completed — from three new unauthenticated `/api/public/**`
endpoints (one per service), fetched in parallel so one being slow doesn't block the
others.

**About page is interactive**: tabbed sections, a clickable palette demo (cycles through
the same states the real exam engine uses), a negative-marking calculator running the
exact formula `AttemptService` uses server-side, and an FAQ accordion.

**Admin console** (`/admin/**`, ADMIN-only): a dashboard with live entry counts (users,
questions, exams) pulled from all three services, 14-day registration/attempt charts,
users-by-role and top-exams breakdowns, and a recent-activity feed — plus dedicated pages
for user management (search/filter, suspend/reactivate, edit roles), the full audit log
(searchable, paginated), exam oversight, and content oversight. Backing it: a new
`audit_logs` table in auth-service (every login success/failure/denial, registration,
profile edit, and admin action is recorded with actor, target, IP and details) and three
new `AdminQueryDao`/`ExamStatsDao`/`ContentStatsDao` classes — plain SQL by design, run
and asserted correct by `infra/tools/verify_sql.py` against a real PostgreSQL loaded with
sample data (18 behavioural assertions: role filters actually filter, search is
apostrophe-safe, averages exclude in-progress attempts, and so on).

**Two real bugs fixed along the way:**
- exam-service's call to content-service used a fake static bearer token
  (`CONTENT_SERVICE_INTERNAL_TOKEN`) that content-service's JWT filter would never have
  accepted — a token that looks like auth but silently isn't. Replaced with
  `InternalTokenProvider`, which mints a real short-lived JWT (same shared secret, signed,
  5-minute expiry, cached and rotated) carrying the `EXAM_SERVICE` role.
- content-service's security rules had the learner-facing GET routes for
  `/api/questions/**` listed *after* the catch-all teacher/admin rule on the same prefix —
  Spring Security matches top-down, so the broad rule always won first and would have
  required TEACHER/ADMIN for practice mode. Reordered, and split so the authoring view
  (which includes correct answers) is TEACHER/ADMIN-only while `/practice` and `/random`
  stay learner-accessible.

**How this was checked**, given the sandbox still can't run Maven or a browser:
- `infra/tools/check_java_syntax.py` — every `.java` file parsed with a real parser
  (tree-sitter), catching unbalanced braces/bad syntax that eyeballing misses (142 files).
- A cross-file import check confirming every internal `com.vriddhi.*` import resolves to
  a class that actually exists (0 unresolved).
- `infra/tools/verify_sql.py` — every tagged SQL statement executed against a real
  PostgreSQL 16 with representative sample data, with assertions on the results.
- `infra/tools/check_contrast.py` — every colour pair, every theme combination, WCAG AA.
- `npm run build` — the frontend actually compiles and bundles (112 modules, no errors),
  since this sandbox can reach npm's registry.
- I could not compile the Java or open a browser, so layout/spacing on real rendered
  pages and full request/response wiring end-to-end are still unverified by me.

## What's still prototype-only

The four static HTML files delivered earlier (public site, learner app,
mock-exam UI, teacher/admin panel) remain useful as a fuller visual/UX
reference. The React app now covers: auth across three portals, a public
home page with real stats, an interactive About page, practice mode,
the full exam-taking flow, profile editing, a teacher dashboard (view-only —
question authoring is still API-only, `POST /api/questions`), and a full
admin console (users, roles, audit log, exam and content oversight). Still
missing: revision queues, bookmarks, and a question/exam *editor* UI for
teachers (creating and publishing content is still curl/API only).

## Recently added

- **Theme system, three portals, real home-page stats, interactive About page, and a full
  admin console.** See "Design system, portals & admin console" above.

- **TCS NQT papers** — see the section above. Also fixed two bugs found while
  wiring them up: exam-service's question fetch ignored the topic filter
  entirely (every section would have drawn from *all* questions), and it
  serialized the topic list as one bracketed string instead of repeated query params.

- **Role management API** — `PATCH /api/admin/users/{id}/roles` (ADMIN-only)
  replaces a user's role set, e.g. to make an existing learner a TEACHER.
  See "Creating a teacher" above; the bootstrap SQL script for the very first
  admin lives at `infra/postgres/promote-user.sql`.
- **Discovery server** (Eureka) — see the architecture section above. All
  four services now find each other by name via `discovery-server`; static
  `AUTH_SERVICE_URL`/`CONTENT_SERVICE_URL`/`EXAM_SERVICE_URL` env vars are
  gone from docker-compose, replaced by each service registering itself and
  the gateway/exam-service looking others up through the registry.
- **Home page** (`/`) — public marketing landing page, unauthenticated.
- **Practice mode** (`/practice`) — pick category → subject → topic, get a
  random published question, answer it, and get graded instantly. Backed by
  two new content-service endpoints: `GET /api/questions/random` and
  `POST /api/questions/{id}/practice/check` (stateless — no exam attempt is
  created; MCQ/multi-correct are graded on the spot, fill-blank/coding/
  subjective come back flagged for manual review, same as in a real exam).
- **Profile page** (`/profile`) — edit your display name for real, via a new
  `PATCH /api/auth/me` endpoint on auth-service.
- **Categories endpoint** (`GET /api/categories`) on content-service, so the
  practice picker isn't hardcoded to one preparation category.
