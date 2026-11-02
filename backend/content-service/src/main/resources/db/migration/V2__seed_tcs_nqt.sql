-- TCS NQT (National Qualifier Test) preparation content.
-- Fixed UUIDs throughout, deliberately: exam-service's V4 migration builds
-- the mock exam's sections against these exact topic ids, and the two
-- services have separate databases with no cross-database foreign key to
-- keep them in sync any other way.
--
-- All question text below is original, written for this platform — not
-- reproduced from any TCS NQT paper or third-party question bank.

INSERT INTO categories (id, code, name, description) VALUES
    ('11111111-1111-1111-1111-111111111101', 'TCS_NQT', 'TCS NQT',
     'TCS National Qualifier Test — Numerical Ability, Verbal Ability, Reasoning Ability, and Programming Logic');

INSERT INTO subjects (id, category_id, name, description, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111201', '11111111-1111-1111-1111-111111111101',
     'Numerical Ability', 'Arithmetic, percentages, ratios, and data interpretation', 1),
    ('11111111-1111-1111-1111-111111111202', '11111111-1111-1111-1111-111111111101',
     'Verbal Ability', 'Grammar, vocabulary, and reading comprehension', 2),
    ('11111111-1111-1111-1111-111111111203', '11111111-1111-1111-1111-111111111101',
     'Reasoning Ability', 'Logical, analytical, and pattern-based reasoning', 3),
    ('11111111-1111-1111-1111-111111111204', '11111111-1111-1111-1111-111111111101',
     'Programming Logic', 'Programming fundamentals and output-prediction MCQs', 4);

INSERT INTO topics (id, subject_id, name, description, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111301', '11111111-1111-1111-1111-111111111201',
     'Arithmetic & Data Interpretation', 'Percentages, profit/loss, ratios, averages, simple data reading', 1),
    ('11111111-1111-1111-1111-111111111302', '11111111-1111-1111-1111-111111111202',
     'Reading Comprehension & Grammar', 'Short passages, sentence correction, vocabulary-in-context', 1),
    ('11111111-1111-1111-1111-111111111303', '11111111-1111-1111-1111-111111111203',
     'Logical & Analytical Reasoning', 'Series, coding-decoding, blood relations, direction sense', 1),
    ('11111111-1111-1111-1111-111111111304', '11111111-1111-1111-1111-111111111204',
     'Programming Fundamentals', 'Basics of C/Java syntax, loops, data types, and output prediction', 1);

-- A fixed "system" author id — content-service has no FK to auth-service's
-- users table (separate database), so any UUID is valid here.
-- 00000000-0000-0000-0000-000000000001

-- ===================== Numerical Ability (5 questions) =====================

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111401', '11111111-1111-1111-1111-111111111301', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111501', '11111111-1111-1111-1111-111111111401', 1,
     'A shopkeeper marks an item 40% above its cost price and then offers a 25% discount on the marked price. What is his net profit percentage?',
     'Let cost price = 100. Marked price = 140. After 25% discount: 140 × 0.75 = 105. Net profit = 5%.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111501' WHERE id = '11111111-1111-1111-1111-111111111401';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111501', '5%', true, 0),
    ('11111111-1111-1111-1111-111111111501', '10%', false, 1),
    ('11111111-1111-1111-1111-111111111501', '15%', false, 2),
    ('11111111-1111-1111-1111-111111111501', '20%', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111402', '11111111-1111-1111-1111-111111111301', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111502', '11111111-1111-1111-1111-111111111402', 1,
     'The average of 5 consecutive even numbers is 24. What is the largest of these numbers?',
     'Consecutive even numbers centered on the average: 20, 22, 24, 26, 28. Largest is 28.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111502' WHERE id = '11111111-1111-1111-1111-111111111402';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111502', '26', false, 0),
    ('11111111-1111-1111-1111-111111111502', '28', true, 1),
    ('11111111-1111-1111-1111-111111111502', '30', false, 2),
    ('11111111-1111-1111-1111-111111111502', '24', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111403', '11111111-1111-1111-1111-111111111301', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111503', '11111111-1111-1111-1111-111111111403', 1,
     'A can complete a task in 12 days and B can complete the same task in 18 days. Working together, how many days will they take?',
     'Combined rate = 1/12 + 1/18 = 3/36 + 2/36 = 5/36 per day. Time = 36/5 = 7.2 days.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111503' WHERE id = '11111111-1111-1111-1111-111111111403';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111503', '6 days', false, 0),
    ('11111111-1111-1111-1111-111111111503', '7.2 days', true, 1),
    ('11111111-1111-1111-1111-111111111503', '9 days', false, 2),
    ('11111111-1111-1111-1111-111111111503', '15 days', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111404', '11111111-1111-1111-1111-111111111301', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111504', '11111111-1111-1111-1111-111111111404', 1,
     'If the ratio of two numbers is 3:5 and their sum is 96, what is the smaller number?',
     'Total parts = 3+5 = 8. Each part = 96/8 = 12. Smaller number = 3 × 12 = 36.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111504' WHERE id = '11111111-1111-1111-1111-111111111404';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111504', '30', false, 0),
    ('11111111-1111-1111-1111-111111111504', '32', false, 1),
    ('11111111-1111-1111-1111-111111111504', '36', true, 2),
    ('11111111-1111-1111-1111-111111111504', '40', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111405', '11111111-1111-1111-1111-111111111301', 'SINGLE_CORRECT', 'HARD', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111505', '11111111-1111-1111-1111-111111111405', 1,
     'A sum of money doubles itself in 8 years at simple interest. In how many years will it become 4 times itself?',
     'Doubling in 8 years means the interest earned in 8 years equals the principal (12.5% per year). To become 4 times itself, the interest must equal 3 × principal, which takes 3 × 8 = 24 years.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111505' WHERE id = '11111111-1111-1111-1111-111111111405';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111505', '16 years', false, 0),
    ('11111111-1111-1111-1111-111111111505', '20 years', false, 1),
    ('11111111-1111-1111-1111-111111111505', '24 years', true, 2),
    ('11111111-1111-1111-1111-111111111505', '32 years', false, 3);

-- ===================== Verbal Ability (5 questions) =====================

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111406', '11111111-1111-1111-1111-111111111302', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111506', '11111111-1111-1111-1111-111111111406', 1,
     'Choose the word most nearly OPPOSITE in meaning to "FRUGAL".',
     '"Frugal" means economical or sparing. Its opposite is "extravagant" — spending resources freely.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111506' WHERE id = '11111111-1111-1111-1111-111111111406';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111506', 'Extravagant', true, 0),
    ('11111111-1111-1111-1111-111111111506', 'Economical', false, 1),
    ('11111111-1111-1111-1111-111111111506', 'Modest', false, 2),
    ('11111111-1111-1111-1111-111111111506', 'Careful', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111407', '11111111-1111-1111-1111-111111111302', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111507', '11111111-1111-1111-1111-111111111407', 1,
     'Identify the grammatically correct sentence.',
     '"Neither of the students has submitted their assignment" uses "has" correctly since "neither" takes a singular verb; option A wrongly pairs "neither" with "have".',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111507' WHERE id = '11111111-1111-1111-1111-111111111407';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111507', 'Neither of the students have submitted their assignment.', false, 0),
    ('11111111-1111-1111-1111-111111111507', 'Neither of the students has submitted their assignment.', true, 1),
    ('11111111-1111-1111-1111-111111111507', 'Neither of the student have submitted his assignment.', false, 2),
    ('11111111-1111-1111-1111-111111111507', 'Neither the students has submitted their assignment.', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111408', '11111111-1111-1111-1111-111111111302', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111508', '11111111-1111-1111-1111-111111111408', 1,
     'Read the passage and answer the question below.

What is the main idea of the passage?',
     'Passage: "Remote work has shifted from a rare perk to a mainstream expectation. While it offers employees flexibility and cuts commuting time, managers report new challenges in maintaining team cohesion and spontaneous collaboration that used to happen naturally in a shared office."',
     'The passage discusses both the benefits (flexibility, less commuting) and the challenges (cohesion, spontaneous collaboration) of remote work — it is balanced, not one-sided.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111508' WHERE id = '11111111-1111-1111-1111-111111111408';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111508', 'Remote work has both clear benefits and new challenges for organizations.', true, 0),
    ('11111111-1111-1111-1111-111111111508', 'Remote work should be banned by all companies.', false, 1),
    ('11111111-1111-1111-1111-111111111508', 'Commuting is the biggest problem employees face today.', false, 2),
    ('11111111-1111-1111-1111-111111111508', 'Managers prefer remote work over office work.', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111409', '11111111-1111-1111-1111-111111111302', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111509', '11111111-1111-1111-1111-111111111409', 1,
     'Fill in the blank: She has been working at the company _____ 2019.',
     '"Since" is used with a specific point in time (2019); "for" would be used with a duration (e.g. "for five years").',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111509' WHERE id = '11111111-1111-1111-1111-111111111409';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111509', 'for', false, 0),
    ('11111111-1111-1111-1111-111111111509', 'since', true, 1),
    ('11111111-1111-1111-1111-111111111509', 'from', false, 2),
    ('11111111-1111-1111-1111-111111111509', 'during', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111410', '11111111-1111-1111-1111-111111111302', 'SINGLE_CORRECT', 'HARD', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111510', '11111111-1111-1111-1111-111111111410', 1,
     'In the sentence "The manager insisted on that the report be submitted by Friday.", which option best replaces the words "insisted on that the report be submitted by Friday"?',
     '"Insist on" should be followed by a gerund or noun phrase ("insisted on the report being submitted"), or "insisted that" without "on" ("insisted that the report be submitted"). The original mixes both constructions incorrectly.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111510' WHERE id = '11111111-1111-1111-1111-111111111410';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111510', 'insisted that the report be submitted by Friday', true, 0),
    ('11111111-1111-1111-1111-111111111510', 'insisted on that the report was submitted by Friday', false, 1),
    ('11111111-1111-1111-1111-111111111510', 'insisted about the report submission by Friday', false, 2),
    ('11111111-1111-1111-1111-111111111510', 'insisted for the report to submit by Friday', false, 3);

-- ===================== Reasoning Ability (5 questions) =====================

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111411', '11111111-1111-1111-1111-111111111303', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111511', '11111111-1111-1111-1111-111111111411', 1,
     'Find the next number in the series: 2, 6, 12, 20, 30, ?',
     'Differences between terms are 4, 6, 8, 10, 12 — increasing by 2 each time. 30 + 12 = 42.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111511' WHERE id = '11111111-1111-1111-1111-111111111411';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111511', '36', false, 0),
    ('11111111-1111-1111-1111-111111111511', '40', false, 1),
    ('11111111-1111-1111-1111-111111111511', '42', true, 2),
    ('11111111-1111-1111-1111-111111111511', '44', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111412', '11111111-1111-1111-1111-111111111303', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111512', '11111111-1111-1111-1111-111111111412', 1,
     'In a certain code, each letter is replaced by the letter 2 positions after it in the alphabet (A→C, B→D, and so on). Under this rule, "COMPUTER" is coded as "EQORWVGT". How is "PRINTER" coded in the same language?',
     'Shift every letter forward by 2: P→R, R→T, I→K, N→P, T→V, E→G, R→T. Reading them in order gives RTKPVGT.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111512' WHERE id = '11111111-1111-1111-1111-111111111412';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111512', 'RTKPVGT', true, 0),
    ('11111111-1111-1111-1111-111111111512', 'RTKQVGT', false, 1),
    ('11111111-1111-1111-1111-111111111512', 'QSJOUFS', false, 2),
    ('11111111-1111-1111-1111-111111111512', 'RTKPVFT', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111413', '11111111-1111-1111-1111-111111111303', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111513', '11111111-1111-1111-1111-111111111413', 1,
     'Pointing to a photograph, Ravi said, "She is the daughter of my grandfather''s only son." How is the woman in the photograph related to Ravi?',
     'My grandfather''s only son is my father. So the woman is my father''s daughter — my sister.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111513' WHERE id = '11111111-1111-1111-1111-111111111413';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111513', 'Sister', true, 0),
    ('11111111-1111-1111-1111-111111111513', 'Mother', false, 1),
    ('11111111-1111-1111-1111-111111111513', 'Cousin', false, 2),
    ('11111111-1111-1111-1111-111111111513', 'Aunt', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111414', '11111111-1111-1111-1111-111111111303', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111514', '11111111-1111-1111-1111-111111111414', 1,
     'A man walks 5 km east, then turns south and walks 12 km. How far is he from the starting point?',
     'This forms a right triangle with legs 5 and 12. Hypotenuse = sqrt(5^2 + 12^2) = sqrt(169) = 13 km.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111514' WHERE id = '11111111-1111-1111-1111-111111111414';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111514', '10 km', false, 0),
    ('11111111-1111-1111-1111-111111111514', '12 km', false, 1),
    ('11111111-1111-1111-1111-111111111514', '13 km', true, 2),
    ('11111111-1111-1111-1111-111111111514', '17 km', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111415', '11111111-1111-1111-1111-111111111303', 'SINGLE_CORRECT', 'HARD', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111515', '11111111-1111-1111-1111-111111111415', 1,
     'Five friends P, Q, R, S, T are sitting in a row facing north. P is to the immediate right of Q. R is at one of the ends. S is between R and T. T is to the right of P. If Q is second from the left, who is at the left end?',
     'Q is in position 2 and P (immediately right of Q) is in position 3, leaving positions 1, 4, 5 for R, S, T. T is to the right of P, so T is in 4 or 5. R is at an end: position 1 or 5. If R were in position 5, T would have to be in 4 and S could not sit between them. So R is in position 1, with S in 4 and T in 5. R is at the left end.''s side pattern — testing R at position 1 satisfies all constraints, placing R at the left end.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111515' WHERE id = '11111111-1111-1111-1111-111111111415';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111515', 'R', true, 0),
    ('11111111-1111-1111-1111-111111111515', 'Q', false, 1),
    ('11111111-1111-1111-1111-111111111515', 'S', false, 2),
    ('11111111-1111-1111-1111-111111111515', 'T', false, 3);

-- ===================== Programming Logic (5 questions) =====================

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111416', '11111111-1111-1111-1111-111111111304', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111516', '11111111-1111-1111-1111-111111111416', 1,
     'What is the output of the following code?',
     'int x = 5;
int y = x++ + ++x;
System.out.println(y);',
     'x++ returns 5, then x becomes 6. ++x increments x to 7 and returns 7. y = 5 + 7 = 12.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111516' WHERE id = '11111111-1111-1111-1111-111111111416';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111516', '11', false, 0),
    ('11111111-1111-1111-1111-111111111516', '12', true, 1),
    ('11111111-1111-1111-1111-111111111516', '13', false, 2),
    ('11111111-1111-1111-1111-111111111516', '10', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111417', '11111111-1111-1111-1111-111111111304', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111517', '11111111-1111-1111-1111-111111111417', 1,
     'How many times will "Hello" be printed?',
     'for (int i = 0; i < 5; i++) {
    if (i == 3) continue;
    if (i == 4) break;
    System.out.println("Hello");
}',
     'i=0,1,2 print Hello (3 times). i=3 hits continue, skipping the print. i=4 hits break before printing. Total: 3.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111517' WHERE id = '11111111-1111-1111-1111-111111111417';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111517', '2', false, 0),
    ('11111111-1111-1111-1111-111111111517', '3', true, 1),
    ('11111111-1111-1111-1111-111111111517', '4', false, 2),
    ('11111111-1111-1111-1111-111111111517', '5', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111418', '11111111-1111-1111-1111-111111111304', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111518', '11111111-1111-1111-1111-111111111418', 1,
     'Which data structure follows the First-In-First-Out (FIFO) principle?',
     'A Queue processes elements in the order they arrive — the first element added is the first one removed, which is FIFO. A Stack is LIFO instead.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111518' WHERE id = '11111111-1111-1111-1111-111111111418';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111518', 'Stack', false, 0),
    ('11111111-1111-1111-1111-111111111518', 'Queue', true, 1),
    ('11111111-1111-1111-1111-111111111518', 'Tree', false, 2),
    ('11111111-1111-1111-1111-111111111518', 'Graph', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111419', '11111111-1111-1111-1111-111111111304', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111519', '11111111-1111-1111-1111-111111111419', 1,
     'What will the following code print?',
     'int a = 10;
int b = 3;
System.out.println(a % b + a / b);',
     'a % b = 10 % 3 = 1 (remainder). a / b = 10 / 3 = 3 (integer division). Sum = 1 + 3 = 4.',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111519' WHERE id = '11111111-1111-1111-1111-111111111419';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111519', '3', false, 0),
    ('11111111-1111-1111-1111-111111111519', '4', true, 1),
    ('11111111-1111-1111-1111-111111111519', '3.33', false, 2),
    ('11111111-1111-1111-1111-111111111519', '1', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111420', '11111111-1111-1111-1111-111111111304', 'SINGLE_CORRECT', 'HARD', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111520', '11111111-1111-1111-1111-111111111420', 1,
     'What is the time complexity of the following nested loop, in terms of n?',
     'for (int i = 1; i < n; i = i * 2) {
    for (int j = 0; j < n; j++) {
        // O(1) work
    }
}',
     'The outer loop runs log2(n) times (i doubles each iteration). The inner loop runs n times on each outer iteration. Total = O(n log n).',
     '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111520' WHERE id = '11111111-1111-1111-1111-111111111420';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111520', 'O(n)', false, 0),
    ('11111111-1111-1111-1111-111111111520', 'O(n log n)', true, 1),
    ('11111111-1111-1111-1111-111111111520', 'O(log n)', false, 2),
    ('11111111-1111-1111-1111-111111111520', 'O(n^2)', false, 3);

-- ===================== Additional questions (pool expanded to 8 per section) =====================

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111421', '11111111-1111-1111-1111-111111111301', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111521', '11111111-1111-1111-1111-111111111421', 1, 'What is 15% of 240?', NULL, '15% of 240 = 0.15 × 240 = 36.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111521' WHERE id = '11111111-1111-1111-1111-111111111421';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111521', '32', false, 0),
    ('11111111-1111-1111-1111-111111111521', '36', true, 1),
    ('11111111-1111-1111-1111-111111111521', '38', false, 2),
    ('11111111-1111-1111-1111-111111111521', '40', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111422', '11111111-1111-1111-1111-111111111301', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111522', '11111111-1111-1111-1111-111111111422', 1, 'A train 150 m long passes a pole in 10 seconds. What is its speed in km/h?', NULL, 'Speed = 150 m / 10 s = 15 m/s. Converting to km/h: 15 × 18/5 = 54 km/h.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111522' WHERE id = '11111111-1111-1111-1111-111111111422';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111522', '45 km/h', false, 0),
    ('11111111-1111-1111-1111-111111111522', '54 km/h', true, 1),
    ('11111111-1111-1111-1111-111111111522', '60 km/h', false, 2),
    ('11111111-1111-1111-1111-111111111522', '72 km/h', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111423', '11111111-1111-1111-1111-111111111301', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111523', '11111111-1111-1111-1111-111111111423', 1, 'The price of an item rises from ₹800 to ₹1,000. What is the percentage increase?', NULL, 'Increase = 200. Percentage increase = 200/800 × 100 = 25%.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111523' WHERE id = '11111111-1111-1111-1111-111111111423';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111523', '20%', false, 0),
    ('11111111-1111-1111-1111-111111111523', '25%', true, 1),
    ('11111111-1111-1111-1111-111111111523', '30%', false, 2),
    ('11111111-1111-1111-1111-111111111523', '12.5%', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111424', '11111111-1111-1111-1111-111111111302', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111524', '11111111-1111-1111-1111-111111111424', 1, 'Choose the correct plural of "criterion".', NULL, '"Criterion" comes from Greek and takes the plural "criteria".', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111524' WHERE id = '11111111-1111-1111-1111-111111111424';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111524', 'criterions', false, 0),
    ('11111111-1111-1111-1111-111111111524', 'criteria', true, 1),
    ('11111111-1111-1111-1111-111111111524', 'criterias', false, 2),
    ('11111111-1111-1111-1111-111111111524', 'criterion', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111425', '11111111-1111-1111-1111-111111111302', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111525', '11111111-1111-1111-1111-111111111425', 1, 'Choose the word closest in meaning to "CANDID".', NULL, '"Candid" means open and honest in speech, i.e. frank.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111525' WHERE id = '11111111-1111-1111-1111-111111111425';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111525', 'Frank', true, 0),
    ('11111111-1111-1111-1111-111111111525', 'Secretive', false, 1),
    ('11111111-1111-1111-1111-111111111525', 'Hostile', false, 2),
    ('11111111-1111-1111-1111-111111111525', 'Careless', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111426', '11111111-1111-1111-1111-111111111302', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111526', '11111111-1111-1111-1111-111111111426', 1, 'Which sentence is correctly punctuated?', NULL, '"It''s" is the contraction of "it is", and a tag question needs a comma before it and a question mark after it.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111526' WHERE id = '11111111-1111-1111-1111-111111111426';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111526', 'Its a lovely day, isn''t it?', false, 0),
    ('11111111-1111-1111-1111-111111111526', 'It''s a lovely day, isn''t it?', true, 1),
    ('11111111-1111-1111-1111-111111111526', 'Its'' a lovely day isn''t it', false, 2),
    ('11111111-1111-1111-1111-111111111526', 'It''s a lovely day isnt it?', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111427', '11111111-1111-1111-1111-111111111303', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111527', '11111111-1111-1111-1111-111111111427', 1, 'Find the odd one out: 16, 25, 36, 48, 49', NULL, '16, 25, 36 and 49 are perfect squares (4², 5², 6², 7²). 48 is not a perfect square.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111527' WHERE id = '11111111-1111-1111-1111-111111111427';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111527', '16', false, 0),
    ('11111111-1111-1111-1111-111111111527', '25', false, 1),
    ('11111111-1111-1111-1111-111111111527', '48', true, 2),
    ('11111111-1111-1111-1111-111111111527', '49', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111428', '11111111-1111-1111-1111-111111111303', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111528', '11111111-1111-1111-1111-111111111428', 1, 'If A = 1, B = 2, C = 3 and so on, what is the sum of the letter values in the word "FACE"?', NULL, 'F=6, A=1, C=3, E=5. Sum = 6 + 1 + 3 + 5 = 15.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111528' WHERE id = '11111111-1111-1111-1111-111111111428';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111528', '13', false, 0),
    ('11111111-1111-1111-1111-111111111528', '15', true, 1),
    ('11111111-1111-1111-1111-111111111528', '17', false, 2),
    ('11111111-1111-1111-1111-111111111528', '19', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111429', '11111111-1111-1111-1111-111111111303', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111529', '11111111-1111-1111-1111-111111111429', 1, 'Riya is facing north. She turns 90° to her right, then turns 180°, then turns 90° to her left. Which direction is she facing now?', NULL, 'North → 90° right = East. Turning 180° = West. Turning 90° left from West = South.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111529' WHERE id = '11111111-1111-1111-1111-111111111429';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111529', 'North', false, 0),
    ('11111111-1111-1111-1111-111111111529', 'South', true, 1),
    ('11111111-1111-1111-1111-111111111529', 'East', false, 2),
    ('11111111-1111-1111-1111-111111111529', 'West', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111430', '11111111-1111-1111-1111-111111111304', 'SINGLE_CORRECT', 'EASY', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111530', '11111111-1111-1111-1111-111111111430', 1, 'Which of the following is NOT a primitive data type in Java?', NULL, 'int, boolean and char are primitives. String is a class (a reference type), not a primitive.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111530' WHERE id = '11111111-1111-1111-1111-111111111430';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111530', 'int', false, 0),
    ('11111111-1111-1111-1111-111111111530', 'boolean', false, 1),
    ('11111111-1111-1111-1111-111111111530', 'String', true, 2),
    ('11111111-1111-1111-1111-111111111530', 'char', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111431', '11111111-1111-1111-1111-111111111304', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111531', '11111111-1111-1111-1111-111111111431', 1, 'What is the output of the following code?', 'int[] arr = {2, 4, 6, 8};
int sum = 0;
for (int i = 1; i < arr.length; i++) {
    sum += arr[i];
}
System.out.println(sum);', 'The loop starts at index 1, so it skips arr[0]. sum = 4 + 6 + 8 = 18.', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111531' WHERE id = '11111111-1111-1111-1111-111111111431';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111531', '20', false, 0),
    ('11111111-1111-1111-1111-111111111531', '18', true, 1),
    ('11111111-1111-1111-1111-111111111531', '12', false, 2),
    ('11111111-1111-1111-1111-111111111531', '14', false, 3);

INSERT INTO questions (id, topic_id, type, difficulty, status, created_by) VALUES
    ('11111111-1111-1111-1111-111111111432', '11111111-1111-1111-1111-111111111304', 'SINGLE_CORRECT', 'MEDIUM', 'PUBLISHED', '00000000-0000-0000-0000-000000000001');
INSERT INTO question_versions (id, question_id, version_number, question_text, code_snippet, explanation, created_by) VALUES
    ('11111111-1111-1111-1111-111111111532', '11111111-1111-1111-1111-111111111432', 1, 'What is the output of the following code?', 'String s = "abc";
s.concat("d");
System.out.println(s);', 'Strings are immutable. concat() returns a new String and does not modify s, and the return value is discarded here. s is still "abc".', '00000000-0000-0000-0000-000000000001');
UPDATE questions SET current_version_id = '11111111-1111-1111-1111-111111111532' WHERE id = '11111111-1111-1111-1111-111111111432';
INSERT INTO question_options (question_version_id, option_text, is_correct, sort_order) VALUES
    ('11111111-1111-1111-1111-111111111532', 'abcd', false, 0),
    ('11111111-1111-1111-1111-111111111532', 'abc', true, 1),
    ('11111111-1111-1111-1111-111111111532', 'd', false, 2),
    ('11111111-1111-1111-1111-111111111532', 'Compile error', false, 3);
