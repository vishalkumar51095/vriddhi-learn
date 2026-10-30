#!/usr/bin/env python3
"""
Applies every Flyway migration for content-service and exam-service to a
throwaway PostgreSQL and checks the seed data is internally consistent.

Run this after adding questions or exam papers:

    pip install pgserver psycopg2-binary
    python3 infra/tools/verify_seed_data.py

It catches the mistakes that are easy to make when hand-writing seed SQL:
  * a migration that doesn't apply
  * a SINGLE_CORRECT question without exactly one correct option
  * a question with no current version
  * an exam section pointing at a topic id that doesn't exist in content-service
    (the two services have separate databases, so nothing else enforces this)
  * a section asking for more questions than its topics' combined pool contains
"""
import glob, os, re, sys, tempfile
import pgserver, psycopg2

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', 'backend'))
srv = pgserver.get_server(tempfile.mkdtemp(prefix='vriddhi_pg_'), cleanup_mode='delete')
base_uri = srv.get_uri()
def uri_for(db): return re.sub(r'/[^/?]*(\?|$)', f'/{db}\\1', base_uri, count=1)

def fresh_db(db):
    c = psycopg2.connect(base_uri); c.autocommit = True
    c.cursor().execute(f"DROP DATABASE IF EXISTS {db}"); c.cursor().execute(f"CREATE DATABASE {db}"); c.close()
    c = psycopg2.connect(uri_for(db)); c.autocommit = True
    return c

problems = []
conns = {}
for db, svc in [('vriddhi_content', 'content-service'), ('vriddhi_exam', 'exam-service')]:
    conns[db] = fresh_db(db)
    for f in sorted(glob.glob(f'{ROOT}/{svc}/src/main/resources/db/migration/V*.sql')):
        try:
            conns[db].cursor().execute(open(f).read()); print(f"OK    {svc}/{os.path.basename(f)}")
        except Exception as e:
            print(f"FAIL  {svc}/{os.path.basename(f)}: {e}"); sys.exit(1)

cc, ce = conns['vriddhi_content'].cursor(), conns['vriddhi_exam'].cursor()

cc.execute("select count(*) from questions where current_version_id is null")
if cc.fetchone()[0]: problems.append("some questions have no current version")

cc.execute("""select q.id from questions q join question_versions v on v.id = q.current_version_id
              left join question_options o on o.question_version_id = v.id
              where q.type = 'SINGLE_CORRECT'
              group by q.id having count(*) filter (where o.is_correct) <> 1""")
for (qid,) in cc.fetchall(): problems.append(f"question {qid}: SINGLE_CORRECT needs exactly one correct option")

cc.execute("select id from topics"); topics = {r[0] for r in cc.fetchall()}
cc.execute("select topic_id, count(*) from questions where status = 'PUBLISHED' group by topic_id"); pool = dict(cc.fetchall())
# A section draws its questions from the UNION of its topics' pools (see AttemptService), so the pool check
# is per section, not per topic.
ce.execute("""select e.title, s.id, s.name, s.question_count, st.topic_id from exams e
              join exam_sections s on s.exam_id = e.id join exam_section_topics st on st.section_id = s.id""")
sections = {}
for title, sid, section, asked, tid in ce.fetchall():
    entry = sections.setdefault(sid, {'label': f"{title} / {section}", 'asked': asked, 'available': 0})
    if tid not in topics: problems.append(f"{title} / {section}: topic {tid} does not exist in content-service")
    else: entry['available'] += pool.get(tid, 0)
for e in sections.values():
    if e['available'] < e['asked']:
        problems.append(f"{e['label']}: asks for {e['asked']} but its topics hold only {e['available']} published questions")

print()
if problems:
    print("PROBLEMS FOUND:"); [print("  -", p) for p in problems]; sys.exit(1)
print("All migrations apply and seed data is consistent.")
