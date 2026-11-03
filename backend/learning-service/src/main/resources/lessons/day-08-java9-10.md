---
slug: java-day-08-java9-10
title: Java 9 and Java 10
category: JAVA_INTERVIEW
subject: Modern Java (9 to 21)
topic: Java 9 & 10
topicId: 350a5f9e-1e65-5740-a9e5-881274fd7127
order: 8
---

## 1. Picture: what changed

```
 Java 8                         Java 9                          Java 10
 ───────────────────            ────────────────────────        ────────────────────
 Arrays.asList(..) (mutable     List.of / Set.of / Map.of       List.copyOf
   elements, null OK)             (immutable, no nulls)         Collectors.toUnmodifiable*
 Stream: filter, limit          takeWhile / dropWhile           var (local only)
 Optional: map, orElse          or / ifPresentOrElse / stream   Optional.orElseThrow()
 default + static in interface  private interface methods
 classpath                      modules (JPMS)
```

## 2. Java 9 features

| Feature | Notes |
|---|---|
| `List.of`, `Set.of`, `Map.of`, `Map.ofEntries` | immutable. No nulls (NPE). `Set.of` and `Map.of` throw `IllegalArgumentException` on duplicates. Iteration order of `Set.of`/`Map.of` is **unspecified**. |
| `takeWhile`, `dropWhile` | work on the prefix of the stream. They stop or start at the first failing element. |
| `Stream.iterate(seed, hasNext, next)` | finite version of `iterate`, like a `for` loop |
| `Stream.ofNullable(x)` | empty stream for `null`, one element otherwise |
| `Optional.or`, `ifPresentOrElse`, `stream` | see Day 6 |
| Private interface methods | share code between default methods |
| Modules (JPMS) | `module-info.java` with `requires` and `exports`. Strong encapsulation. |
| Try-with-resources on an existing variable | `try (r) { ... }` if `r` is effectively final |
| `Objects.requireNonNullElse(x, def)` | null-safe default |
| `InputStream.transferTo`, `ProcessHandle`, Flow (reactive streams) API | minor additions |
| G1 | became the default garbage collector |

## 3. Java 10 features

| Feature | Notes |
|---|---|
| `var` | local variables only: not fields, parameters, return types or `var x = null;`. The type is still static, just inferred. |
| `List.copyOf`, `Set.copyOf`, `Map.copyOf` | immutable snapshot |
| `Collectors.toUnmodifiableList/Set/Map` | collectors that return unmodifiable results |
| `Optional.orElseThrow()` | no-argument version, preferred over `get()` |

## 4. `takeWhile` vs `filter`

```
 [1, 2, 3, 10, 1, 2]

 filter(n -> n < 5)      → [1, 2, 3, 1, 2]     tests EVERY element
 takeWhile(n -> n < 5)   → [1, 2, 3]           stops at 10
 dropWhile(n -> n < 5)   → [10, 1, 2]          starts at 10
```

On an unordered stream, `takeWhile` and `dropWhile` have no guaranteed result.

## 5. Worked examples

```java
List.of(1, 2, 3).add(4);                      // UnsupportedOperationException
List.of(1, null);                             // NullPointerException
Arrays.asList(1, null);                       // fine; Arrays.asList is fixed-size, not immutable

var original = new ArrayList<>(List.of(1, 2));
var copy = List.copyOf(original);
original.add(3);                              // copy is still [1, 2]

Stream.iterate(1, x -> x <= 100, x -> x * 2); // 1, 2, 4, ..., 64

opt.ifPresentOrElse(v -> use(v), () -> useDefault());
firstChoice.or(() -> secondChoice);
Stream.ofNullable(list).flatMap(List::stream).count();
```

## 6. Traps

- `List.of` returns an immutable list, but `Arrays.asList` is only fixed-size: you can `set` but not `add`. Neither is the same as `Collections.unmodifiableList`, which is a read-only **view** of a list that can still change underneath.
- `List.copyOf` makes an independent snapshot, so later changes to the source do not appear in the copy.
- `Set.of` and `Map.of` iterate in an order that can change between runs. Do not depend on it.
- `var` does not make Java dynamically typed. `var list = new ArrayList<>();` infers `ArrayList<Object>`.
- `var` is not allowed for fields, method parameters or return types.
- `takeWhile` is not `limit`: it depends on a condition, not a count.

## 7. Interview Q&A

- **`List.of` vs `Arrays.asList`?** `List.of` is immutable and rejects nulls. `Arrays.asList` is fixed-size, allows `set` and nulls, and is backed by the array.
- **`List.copyOf` vs `Collections.unmodifiableList`?** `copyOf` is an immutable snapshot. `unmodifiableList` is a read-only view of a list that can still change.
- **`takeWhile` vs `filter`?** `takeWhile` stops at the first failure. `filter` tests all elements.
- **Why were private interface methods added?** To remove duplicated code between default methods without exposing a helper.
- **Where can `var` be used?** Local variables, loop variables and try-with-resources variables. Java 11 also allows it for lambda parameters.
- **What are modules?** A way to group packages and declare which are exported and which modules are required (JPMS).
- **What does `Optional.or` do?** Returns the same Optional if present, otherwise one supplied lazily.

## 9. Self-check (answer aloud)

1. Why can `List.of(1, null)` fail while `Arrays.asList(1, null)` works?
2. What is the difference between `copyOf` and `unmodifiableList`?
3. Where can you not use `var`?
4. For `[1, 2, 3, 10, 1]`, what do `takeWhile(n -> n < 5)` and `filter(n -> n < 5)` return?
5. What does `Set.of` guarantee about iteration order?
