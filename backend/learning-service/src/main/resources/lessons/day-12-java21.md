---
slug: java-day-12-java21
title: Java 21 (LTS)
category: JAVA_INTERVIEW
subject: Modern Java (9 to 21)
topic: Java 21
topicId: 5593be01-536a-53a6-8780-9bedbe35f58d
order: 12
---

## 1. Picture: what Java 21 finalized

```
 Java 17 (LTS)  ──►  18  19  20  ──►  Java 21 (LTS)
                      │  │  │
 pattern switch   (preview in 17-20) ───────────►  FINAL
 record patterns  (preview in 19, 20) ──────────►  FINAL
 virtual threads  (preview in 19, 20) ──────────►  FINAL
 sequenced collections ─────────────────────────►  NEW and FINAL
 generational ZGC ──────────────────────────────►  NEW
```

Still **preview** in 21 (do not rely on them): string templates, unnamed patterns and variables, unnamed classes and instance `main`, structured concurrency, scoped values.

## 2. Pattern matching for `switch`

```java
static String describe(Object o) {
    return switch (o) {
        case null                  -> "null";
        case Integer i when i > 10 -> "big int " + i;     // guard with 'when'
        case Integer i             -> "int " + i;
        case String s              -> "str " + s.length();
        default                    -> "other";
    };
}
```

- `case null` is allowed. Without it, a `null` selector throws `NullPointerException`.
- Order matters: put the more specific (guarded) case **before** the general one, or it will not compile ("dominated").
- Over a sealed type, a switch is **exhaustive** without `default` and the compiler enforces it.

## 3. Record patterns

```java
// destructure a record in switch or instanceof
case Circle(double r)               -> Math.PI * r * r;
case Rectangle(double w, double h)  -> w * h;

// nested
if (line instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) { ... }
```

```
 sealed Shape ─► Circle(r) | Rectangle(w, h) | Triangle(b, h)
 + record patterns + exhaustive switch  =  algebraic-data-type style code, checked by the compiler
```

## 4. Virtual threads

```
 PLATFORM threads (old)                    VIRTUAL threads (Java 21)
 ─────────────────────────                 ───────────────────────────────────────────
 1 Java thread = 1 OS thread               many virtual threads ──mounted on──► few carrier OS threads
 expensive (~MBs of stack),                cheap (grow on the heap), millions possible
 thousands at most                         when one BLOCKS (I/O, sleep) it unmounts; the carrier runs another
```

```java
Thread.ofVirtual().start(task);
Thread.startVirtualThread(task);
try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {   // ExecutorService is AutoCloseable (19+)
    Future<Integer> f = ex.submit(() -> 2 * 21);
}
```

| Do | Don't |
|---|---|
| use for many **blocking** tasks (HTTP, DB, file I/O) | use for CPU-bound work (no speedup over cores) |
| create one per task | **pool** them. They are cheap, so pooling defeats the purpose. |
| limit concurrency with a `Semaphore` | assume `synchronized` is free: in Java 21, blocking inside `synchronized` **pins** the carrier thread. Prefer `ReentrantLock`. |

Spring Boot 3.2 and later can use virtual threads with `spring.threads.virtual.enabled=true`.

## 5. Sequenced collections

New interfaces `SequencedCollection`, `SequencedSet`, `SequencedMap` give every ordered collection the same methods:

```
 getFirst()  getLast()  addFirst()  addLast()  removeFirst()  removeLast()  reversed()
 SequencedMap: firstEntry()  lastEntry()  putFirst()  putLast()  pollFirstEntry()  reversed()
```

`List`, `Deque`, `LinkedHashSet`, `SortedSet`, `LinkedHashMap` and `SortedMap` all implement them. Before 21 the last element of a list was `list.get(list.size() - 1)`.

## 6. Small additions

`Math.clamp(value, min, max)`, `StringBuilder.repeat(CharSequence, int)`, `String.indexOf(ch, from, to)`, `Character.isEmoji(...)`, `List.reversed()`.

Java 18 to 20 also brought: UTF-8 as the default charset (18), a simple web server `jwebserver` (18), and the Vector API (incubator).

## 7. Traps

- Putting `case Integer i` before `case Integer i when i > 10` is a compile error because the second case is dominated.
- A pattern `switch` over a non-sealed type needs a `default` (or `case Object o`).
- Virtual threads are not faster than platform threads for CPU work. They improve **throughput for blocking work**.
- Do not store `ThreadLocal` data casually in millions of virtual threads. It can use a lot of memory.
- `list.reversed()` is a **view**. Copy it (`new ArrayList<>(list.reversed())`) if you need an independent list.

## 8. Interview Q&A

- **What are virtual threads?** Lightweight threads managed by the JVM and scheduled onto a small pool of carrier OS threads.
- **When do they help?** Many concurrent tasks that mostly wait on I/O.
- **Should you pool virtual threads?** No. Create one per task.
- **What is thread pinning?** A virtual thread stuck to its carrier thread while blocked (for example inside `synchronized` in Java 21), which reduces scalability.
- **What does a record pattern do?** Deconstructs a record's components directly in `switch` or `instanceof`.
- **Why is a sealed hierarchy plus pattern `switch` powerful?** The compiler checks every subtype is handled.
- **What are sequenced collections?** A common API for collections with a defined encounter order: first, last and reversed.
- **Which Java versions are LTS?** 8, 11, 17, 21 (and 25).

## 10. Self-check (answer aloud)

1. Why can a pattern `switch` over a sealed type omit `default`?
2. What does the `when` keyword do in a `case`?
3. Why should you not create a pool of virtual threads?
4. What is pinning and how do you avoid it in Java 21?
5. What did you have to write before `getLast()` existed?
