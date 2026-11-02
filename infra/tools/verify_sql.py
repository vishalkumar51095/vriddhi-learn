#!/usr/bin/env python3
"""
Extracts every SQL statement tagged with `// test-params: {...}` from the Java DAOs/services,
runs it against a real (throwaway) PostgreSQL that has all Flyway migrations applied plus
sample data, and asserts a few behaviours. The SQL is single-sourced: what runs here is
exactly the text the Java code sends.

    pip install pgserver psycopg2-binary
    python3 infra/tools/verify_sql.py
"""
import glob, json, os, re, sys, tempfile
import pgserver, psycopg2

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..', 'backend'))
srv = pgserver.get_server(tempfile.mkdtemp(prefix='vriddhi_sql_'), cleanup_mode='delete')
base = srv.get_uri()
def uri(db): return re.sub(r'/[^/?]*(\?|$)', f'/{db}\\1', base, count=1)

DBS = {'auth-service': 'v_auth', 'content-service': 'v_content', 'exam-service': 'v_exam'}
conn = {}
for svc, db in DBS.items():
    c = psycopg2.connect(base); c.autocommit = True; c.cursor().execute(f'CREATE DATABASE {db}'); c.close()
    conn[svc] = psycopg2.connect(uri(db)); conn[svc].autocommit = True
    for f in sorted(glob.glob(f'{ROOT}/{svc}/src/main/resources/db/migration/V*.sql')):
        conn[svc].cursor().execute(open(f).read())
print("migrations applied for", ", ".join(DBS))

# ---------------- sample data ----------------
a = conn['auth-service'].cursor()
for i, (email, name, status, roles, days) in enumerate([
        ('admin@x.io', 'Ada Admin', 'ACTIVE', ['ADMIN'], 20), ('t1@x.io', 'Tara Teacher', 'ACTIVE', ['TEACHER', 'LEARNER'], 5),
        ('s1@x.io', 'Sam Student', 'ACTIVE', ['LEARNER'], 0), ('s2@x.io', "O'Neil Student", 'SUSPENDED', ['LEARNER'], 2)]):
    a.execute("INSERT INTO users (email,password_hash,full_name,status,created_at,last_login_at) VALUES (%s,'h',%s,%s,now()-%s*interval '1 day', now()) RETURNING id",
              (email, name, status, days))
    uid = a.fetchone()[0]
    for r in roles:
        a.execute("INSERT INTO user_roles SELECT %s, id FROM roles WHERE name=%s", (uid, r))
    a.execute("INSERT INTO audit_logs (actor_id,actor_email,event_type,target_type,target_id,details,ip_address) VALUES (%s,%s,'LOGIN_SUCCESS','USER',%s,'Signed in','10.0.0.%s')", (uid, email, str(uid), i))
a.execute("INSERT INTO audit_logs (actor_email,event_type,details) VALUES ('ghost@x.io','LOGIN_FAILURE','Wrong email or password')")

e = conn['exam-service'].cursor()
e.execute("SELECT id FROM exams ORDER BY id LIMIT 1"); exam_id = e.fetchone()[0]
for st, score, d in [('SUBMITTED', 12, 0), ('SUBMITTED', 8, 1), ('AUTO_SUBMITTED', 5, 1), ('IN_PROGRESS', None, 0)]:
    e.execute("INSERT INTO exam_attempts (exam_id,user_id,status,started_at,expires_at,score) VALUES (%s,gen_random_uuid(),%s,now()-%s*interval '1 day',now()+interval '1 hour',%s)", (exam_id, st, d, score))

# ---------------- run every tagged statement ----------------
pat = re.compile(r'//\s*test-params:\s*(\{.*?\})\s*\n\s*(?:private\s+)?(?:static\s+)?(?:final\s+)?String\s+(\w+)\s*=\s*(?:"""\n(.*?)"""|"(.*?)");', re.S)
def to_psycopg(sql): return re.sub(r'(?<!:):([A-Za-z_]\w*)', r'%(\1)s', sql.replace('%', '%%'))

results, failures = {}, 0
for f in sorted(glob.glob(f'{ROOT}/**/*.java', recursive=True)):
    svc = next((s for s in DBS if f'/{s}/' in f), None)
    src = open(f).read()
    for m in pat.finditer(src):
        params, name, block, single = json.loads(m.group(1)), m.group(2), m.group(3), m.group(4)
        sql = to_psycopg(block if block is not None else single)
        cur = conn[svc].cursor()
        try:
            cur.execute(sql, params)
            rows = cur.fetchall() if cur.description else []
            results[(svc, name)] = rows
            print(f"OK    {svc:16} {name:22} -> {len(rows) if cur.description else cur.rowcount} row(s)")
        except Exception as ex:
            failures += 1; print(f"FAIL  {svc:16} {name}: {str(ex).splitlines()[0]}")

# ---------------- behavioural assertions ----------------
def check(cond, msg):
    global failures
    if not cond: failures += 1; print("ASSERT FAILED:", msg)
    else: print("ok    ", msg)

def run(svc, sql, params=None):
    cur = conn[svc].cursor(); cur.execute(to_psycopg(sql), params or {}); return cur.fetchall()

users = results[('auth-service', 'SEARCH_USERS')]
check(len(users) == 4 and users[0][1] == 's1@x.io', "user search: 4 users, newest first")
check(any(r[6] == 'LEARNER,TEACHER' for r in users), "user search: multi-role users aggregate to 'LEARNER,TEACHER'")
check(results[('auth-service', 'COUNT_USERS')][0][0] == 4, "user count = 4")
tot = results[('auth-service', 'USER_TOTALS')][0]
check(tot[:4] == (4, 3, 1, 0), f"user totals total/active/suspended/pending = {tot[:4]}")
check(len(results[('auth-service', 'REGISTRATIONS_PER_DAY')]) == 14, "registrations-per-day returns exactly 14 rows (zero-filled)")
check(sum(r[1] for r in results[('auth-service', 'REGISTRATIONS_PER_DAY')]) == 3, "registrations-per-day counts only users inside the 14-day window (the 20-day-old admin is excluded)")
check(results[('auth-service', 'LOGIN_ACTIVITY')][0] == (4, 1), "login activity: 4 ok / 1 failed in 24h")
check(len(results[('auth-service', 'SEARCH_AUDIT')]) == 5, "audit search returns all 5 rows")
pub = results[('auth-service', 'PUBLIC_USER_COUNTS')][0]
check(pub == (2, 1), f"public counts count ACTIVE users only: learners/teachers = {pub}")

# filters really filter
src = open(f'{ROOT}/auth-service/src/main/java/com/vriddhi/auth/dao/AdminQueryDao.java').read()
search = pat.search(src).group(3)   # raw; run() converts it
r = run('auth-service', search, {"q": "", "role": "TEACHER", "status": "", "limit": 20, "offset": 0})
check(len(r) == 1 and r[0][6] == 'LEARNER,TEACHER', "role filter keeps the user's other roles in the aggregate")
r = run('auth-service', search, {"q": "o'neil", "role": "", "status": "", "limit": 20, "offset": 0})
check(len(r) == 1, "search is case-insensitive and safe with an apostrophe")
r = run('auth-service', search, {"q": "", "role": "", "status": "SUSPENDED", "limit": 20, "offset": 0})
check(len(r) == 1 and r[0][3] == 'SUSPENDED', "status filter works")
r = run('auth-service', search, {"q": "", "role": "", "status": "", "limit": 2, "offset": 2})
check(len(r) == 2, "pagination (limit 2, offset 2) returns the last 2")

ex = results[('exam-service', 'ATTEMPT_TOTALS')][0]
check(ex[:4] == (4, 1, 2, 1), f"attempt totals total/in-progress/submitted/auto = {ex[:4]}")
check(abs(float(ex[4]) - 8.3333) < 0.01, f"avg score ignores in-progress attempts = {float(ex[4]):.2f}")
check(results[('exam-service', 'PUBLIC_COUNTS')][0][1] == 3, "public attempts_taken excludes in-progress")
check(len(results[('exam-service', 'PUBLIC_EXAMS')]) == 7, "public exam list shows the 7 seeded papers (3 TCS NQT + 3 UGC NET practice + UGC NET June 2025)")
check(sum(1 for r in results[('exam-service', 'ADMIN_EXAMS')] if r[9] > 0) == 1, "admin exam list carries per-exam attempt counts")
check(results[('content-service', 'PUBLIC_COUNTS')][0][0] == 166, "public content stats: 166 published questions (32 TCS NQT + 39 UGC NET practice + 95 June 2025)")

print()
sys.exit(1 if failures else 0) if failures else print("All SQL statements run and behave correctly against PostgreSQL.")
