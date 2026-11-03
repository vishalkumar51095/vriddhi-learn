---
slug: java-day-02-streams
title: Stream Basics
category: JAVA_INTERVIEW
subject: Java 8 Essentials
topic: Stream Basics
topicId: afc8fa05-df08-5ce3-b35b-46b30a809fb9
order: 2
---

## 1. How a pipeline really runs

Elements flow **one at a time** through the whole pipeline. They do not move stage by stage.

```
 [1, 2, 3, 4, 5, 6].stream().filter(even).map(square).findFirst()

 element 1 ──► filter ✗  dropped
 element 2 ──► filter ✓ ──► map ──► 4 ──► findFirst ✓  DONE
 elements 3, 4, 5, 6 are never touched   (laziness + short-circuit)
```

```
 SOURCE        INTERMEDIATE (lazy, return a Stream)       TERMINAL (starts the run)
┌────────┐    ┌────────┐   ┌────────┐   ┌────────┐       ┌──────────┐
│ List   │───►│ filter │──►│  map   │──►│ sorted │──────►│ collect  │
└────────┘    └────────┘   └────────┘   └────────┘       └──────────┘
                                          ▲ stateful: must see ALL elements before
                                            emitting, so it breaks the one-at-a-time flow
```

## 2. Creating streams

| Source | Code |
|---|---|
| Collection | `list.stream()`, `list.parallelStream()` |
| Array | `Arrays.stream(arr)`, `Stream.of(a, b, c)` |
| Range | `IntStream.range(0, n)` (end excluded), `rangeClosed(1, n)` (end included) |
| Infinite | `Stream.iterate(1, x -> x + 1)`, `Stream.generate(supplier)`. Always add `limit`. |
| String chars | `"abc".chars()` gives an `IntStream` |
| File lines | `Files.lines(path)`. Close it with try-with-resources. |

## 3. Operations

| Intermediate (lazy) | Notes |
|---|---|
| `filter(Predicate)` | stateless |
| `map(Function)`, `mapToInt/Long/Double` | stateless, 1 to 1 |
| `flatMap(Function)` | 1 to many, flattens (Day 4) |
| `distinct()`, `sorted()` | **stateful** |
| `peek(Consumer)` | debugging only |
| `limit(n)`, `skip(n)` | `limit` is short-circuiting |

| Terminal | Returns |
|---|---|
| `forEach`, `forEachOrdered` | void (`forEachOrdered` keeps order in parallel) |
| `collect(Collector)`, `toArray(String[]::new)` | collection or array |
| `count()` | `long` |
| `min/max(Comparator)`, `findFirst()`, `findAny()` | `Optional` |
| `reduce(...)` | see below |
| `anyMatch`, `allMatch`, `noneMatch` | boolean (short-circuit) |

## 4. Rules

1. Nothing runs until a terminal operation is called.
2. A stream is single-use. A second use throws `IllegalStateException`.
3. A stream never modifies its source. Do not modify the source while streaming (you risk `ConcurrentModificationException`).
4. Keep lambdas stateless and free of side effects, especially with parallel streams.
5. `sorted()` needs `Comparable` elements or a `Comparator`. Otherwise you get `ClassCastException`.

## 5. Worked examples

### Example 1: second-highest salary
```java
emps.stream().map(Employee::getSalary).distinct()
    .sorted(Comparator.reverseOrder()).skip(1).findFirst();
```
```
 [50000, 60000, 50000, 60000, 75000]
   ──distinct──► [50000, 60000, 75000]
   ──sorted desc──► [75000, 60000, 50000]
   ──skip(1)──► [60000, 50000] ──findFirst──► Optional[60000]
```
`distinct()` comes first so duplicate salaries do not count as separate ranks.

### Example 2: names starting with "A", uppercased, sorted, joined
```java
emps.stream().map(Employee::getName).filter(n -> n.startsWith("A"))
    .map(String::toUpperCase).sorted().collect(Collectors.joining(", "));
// AMIT, ANU, ARUN
```

### Example 3: `reduce`
```java
nums.stream().reduce(0, Integer::sum);          // identity + accumulator → int (0 if empty)
nums.stream().reduce(Integer::sum);             // no identity → Optional<Integer>
words.stream().reduce(0, (acc, w) -> acc + w.length(), Integer::sum); // 3-arg: List<String> → int, combiner for parallel
```

### Example 4: primitive streams (no boxing)
```java
emps.stream().mapToInt(Employee::getAge).sum();
emps.stream().mapToInt(Employee::getAge).average().orElse(0);   // OptionalDouble
emps.stream().mapToInt(Employee::getAge).summaryStatistics();   // count, sum, min, avg, max
IntStream.rangeClosed(1, 5).boxed().collect(Collectors.toList()); // [1,2,3,4,5]
```

### Example 5: infinite stream plus `limit`
```java
Stream.iterate(1, x -> x + 1).filter(x -> x % 2 == 0).map(x -> x * x).limit(3)
      .collect(Collectors.toList());     // [4, 16, 36]
```
Without `limit` this never ends.

### Example 6: laziness is visible with `peek`
```java
Stream.of("a", "b", "c")
      .peek(s -> System.out.println("peek " + s))
      .map(String::toUpperCase)
      .forEach(s -> System.out.println("got " + s));
// peek a, got A, peek b, got B, peek c, got C      (interleaved)
```
Add `.sorted()` before `forEach` and all the peeks print first, because `sorted` has to collect everything.

### Example 7: match operations
```java
emps.stream().anyMatch(e -> e.getAge() > 40);          // true as soon as one matches
emps.stream().allMatch(e -> e.getSalary() > 20000);    // false as soon as one fails
emps.stream().noneMatch(e -> e.getDept().equals("QA"));
```
On an empty stream: `allMatch` and `noneMatch` return **true**, and `anyMatch` returns **false**.

## 6. Traps interviewers like

- `Stream.of(intArray)` gives a `Stream<int[]>` with **one** element. Use `Arrays.stream(intArray)` to get an `IntStream`.
- `count()` returns `long`, not `int`.
- `Collectors.toList()` makes no promise about mutability. `Stream.toList()` (Java 16) returns an unmodifiable list.
- `parallelStream()` does not always help. It suits large, CPU-bound, stateless work, not small lists or I/O.
- `forEach` on a parallel stream does not guarantee order.
- `peek` may be skipped when the terminal operation can compute the result without it (for example, `count()` on a sized source in Java 9+).

## 7. Interview Q&A

- **Stream vs Collection?** A collection stores data. A stream processes it, lazily and once, without storing it.
- **Intermediate vs terminal?** Intermediate returns a Stream and is lazy. Terminal produces a result or side effect and triggers the run.
- **What does lazy mean?** Intermediate operations only record the pipeline. Work happens when the terminal operation starts.
- **Stateless vs stateful operations?** Stateless (`filter`, `map`) look at one element. Stateful (`sorted`, `distinct`) need to see more.
- **`findFirst` vs `findAny`?** The same on sequential streams. `findAny` is cheaper on parallel streams.
- **`peek` vs `forEach`?** `peek` is intermediate, for debugging. `forEach` is terminal.
- **`reduce` vs `collect`?** `reduce` is immutable reduction (combine into a value). `collect` is mutable reduction (fill a container).
- **`map` vs `mapToInt`?** `mapToInt` gives an `IntStream`, so no boxing and you get `sum()` and `average()`.
- **`limit` vs `skip`?** `limit(n)` keeps the first n. `skip(n)` discards the first n.
- **`range` vs `rangeClosed`?** `range` excludes the end. `rangeClosed` includes it.
- **Reusing a stream?** Throws `IllegalStateException: stream has already been operated upon or closed`.
- **`filter` vs `takeWhile` (Java 9)?** `filter` tests every element. `takeWhile` stops at the first failure.

## 9. Self-check (answer aloud)

1. In which order do elements move through `filter -> map -> findFirst`?
2. Why is `distinct()` placed before `sorted()` in the second-highest-salary solution?
3. What do `allMatch` and `anyMatch` return on an empty stream?
4. Why does `Stream.of(new int[]{1,2,3}).count()` return 1?
5. When would `parallelStream()` make things slower?
