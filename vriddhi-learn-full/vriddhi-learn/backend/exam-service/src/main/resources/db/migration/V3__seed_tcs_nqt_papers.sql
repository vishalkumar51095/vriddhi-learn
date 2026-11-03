-- TCS NQT mock papers.
-- The topic ids below are the fixed ids seeded by content-service's
-- V2__seed_tcs_nqt.sql. The two services have separate databases, so there is
-- no foreign key to enforce this — the literals must match.
--
--   ...301 Arithmetic & Data Interpretation   (Numerical Ability)
--   ...302 Reading Comprehension & Grammar    (Verbal Ability)
--   ...303 Logical & Analytical Reasoning     (Reasoning Ability)
--   ...304 Programming Fundamentals           (Programming Logic)
--
-- Each section draws its questions at random from that topic's pool (8 per
-- topic), so repeated attempts at the same paper see different questions.
-- TCS NQT has no negative marking, so negative_marking is 0 on all three.

-- ---------- Paper 1: Full Mock (4 sections, section-locked like the real test) ----------
INSERT INTO exams (id, title, category_code, duration_minutes, negative_marking, navigation_mode, status, created_by) VALUES
    ('22222222-2222-2222-2222-222222222201', 'TCS NQT — Full Mock Paper 1', 'TCS_NQT', 60, 0, 'SECTION_LOCKED', 'PUBLISHED',
     '00000000-0000-0000-0000-000000000001');
INSERT INTO exam_sections (id, exam_id, name, sort_order, question_count) VALUES
    ('22222222-2222-2222-2222-222222222301', '22222222-2222-2222-2222-222222222201', 'Numerical Ability', 0, 5),
    ('22222222-2222-2222-2222-222222222302', '22222222-2222-2222-2222-222222222201', 'Verbal Ability', 1, 5),
    ('22222222-2222-2222-2222-222222222303', '22222222-2222-2222-2222-222222222201', 'Reasoning Ability', 2, 5),
    ('22222222-2222-2222-2222-222222222304', '22222222-2222-2222-2222-222222222201', 'Programming Logic', 3, 5);
INSERT INTO exam_section_topics (section_id, topic_id) VALUES
    ('22222222-2222-2222-2222-222222222301', '11111111-1111-1111-1111-111111111301'),
    ('22222222-2222-2222-2222-222222222302', '11111111-1111-1111-1111-111111111302'),
    ('22222222-2222-2222-2222-222222222303', '11111111-1111-1111-1111-111111111303'),
    ('22222222-2222-2222-2222-222222222304', '11111111-1111-1111-1111-111111111304');

-- ---------- Paper 2: Foundation Aptitude Sprint (3 sections, free navigation) ----------
INSERT INTO exams (id, title, category_code, duration_minutes, negative_marking, navigation_mode, status, created_by) VALUES
    ('22222222-2222-2222-2222-222222222202', 'TCS NQT — Foundation Aptitude Sprint', 'TCS_NQT', 30, 0, 'FREE', 'PUBLISHED',
     '00000000-0000-0000-0000-000000000001');
INSERT INTO exam_sections (id, exam_id, name, sort_order, question_count) VALUES
    ('22222222-2222-2222-2222-222222222311', '22222222-2222-2222-2222-222222222202', 'Numerical Ability', 0, 4),
    ('22222222-2222-2222-2222-222222222312', '22222222-2222-2222-2222-222222222202', 'Verbal Ability', 1, 4),
    ('22222222-2222-2222-2222-222222222313', '22222222-2222-2222-2222-222222222202', 'Reasoning Ability', 2, 4);
INSERT INTO exam_section_topics (section_id, topic_id) VALUES
    ('22222222-2222-2222-2222-222222222311', '11111111-1111-1111-1111-111111111301'),
    ('22222222-2222-2222-2222-222222222312', '11111111-1111-1111-1111-111111111302'),
    ('22222222-2222-2222-2222-222222222313', '11111111-1111-1111-1111-111111111303');

-- ---------- Paper 3: Reasoning & Programming Sprint (2 sections, forward-only) ----------
INSERT INTO exams (id, title, category_code, duration_minutes, negative_marking, navigation_mode, status, created_by) VALUES
    ('22222222-2222-2222-2222-222222222203', 'TCS NQT — Reasoning & Programming Sprint', 'TCS_NQT', 25, 0, 'FORWARD_ONLY', 'PUBLISHED',
     '00000000-0000-0000-0000-000000000001');
INSERT INTO exam_sections (id, exam_id, name, sort_order, question_count) VALUES
    ('22222222-2222-2222-2222-222222222321', '22222222-2222-2222-2222-222222222203', 'Reasoning Ability', 0, 5),
    ('22222222-2222-2222-2222-222222222322', '22222222-2222-2222-2222-222222222203', 'Programming Logic', 1, 6);
INSERT INTO exam_section_topics (section_id, topic_id) VALUES
    ('22222222-2222-2222-2222-222222222321', '11111111-1111-1111-1111-111111111303'),
    ('22222222-2222-2222-2222-222222222322', '11111111-1111-1111-1111-111111111304');
