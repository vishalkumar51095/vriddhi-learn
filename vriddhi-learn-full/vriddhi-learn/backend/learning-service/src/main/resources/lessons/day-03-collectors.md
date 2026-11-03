---
slug: java-day-03-collectors
title: Collectors
category: JAVA_INTERVIEW
subject: Java 8 Essentials
topic: Collectors
topicId: 117952d7-68bf-5d69-aa95-0f651102bce5
order: 3
---

## 1. Picture: `groupingBy` sorts elements into buckets

```
 Employees: [Ravi-IT, Anu-HR, Sita-IT, Amit-IT, Arun-HR]
                         │  groupingBy(Employee::getDept)
              ┌──────────┴───────────┐
              ▼                      ▼
        IT ─► [Ravi, Sita, Amit]   HR ─► [Anu, Arun]
              │                      │   downstream collector runs per bucket
              ▼                      ▼   (counting → 3 and 2)
```

`partitioningBy` is the same idea with exactly two buckets, `true` and `false`. Both keys always exist, even when a bucket is empty.

## 2. What a Collector is

`collect()` does a **mutable reduction**. A Collector has four parts:

```
 supplier()     ─► create the container          () -> new ArrayList<>()
 accumulator()  ─► add one element               (list, e) -> list.add(e)
 combiner()     ─► merge two containers (parallel)
 finisher()     ─► convert to the final result   (optional)
```

## 3. The collectors to know

| Collector | Result |
|---|---|
| `toList()`, `toSet()`, `toCollection(TreeSet::new)` | collection |
| `toMap(k, v)`, `toMap(k, v, merge)`, `toMap(k, v, merge, LinkedHashMap::new)` | map |
| `groupingBy(f)`, `groupingBy(f, downstream)`, `groupingBy(f, TreeMap::new, downstream)` | `Map<K, List<T>>` or downstream result |
| `partitioningBy(pred)`, `partitioningBy(pred, downstream)` | `Map<Boolean, ...>` |
| `counting()` | `Long` |
| `summingInt/Long/Double`, `averagingInt/Long/Double` | number (`Double` for averages, 0.0 if empty) |
| `minBy`, `maxBy` | `Optional<T>` |
| `summarizingInt/Long/Double` | count, sum, min, avg, max in one pass |
| `joining()`, `joining(", ")`, `joining(", ", "[", "]")` | `String` |
| `mapping(f, downstream)` | transform before collecting |
| `filtering(pred, downstream)`, `flatMapping(f, downstream)` | Java 9 |
| `collectingAndThen(downstream, finisher)` | post-process the result |
| `reducing(identity, op)` | custom reduction |
| `teeing(c1, c2, merger)` | Java 12: two collectors, one pass |

## 4. Worked examples

```java
// count per department
emps.stream().collect(groupingBy(Employee::getDept, counting()));        // {IT=3, HR=2}

// highest-paid per department (unwrap the Optional with collectingAndThen)
emps.stream().collect(groupingBy(Employee::getDept,
    collectingAndThen(maxBy(comparingDouble(Employee::getSalary)), o -> o.get().getName())));

// names per department, departments sorted
emps.stream().collect(groupingBy(Employee::getDept, TreeMap::new,
    mapping(Employee::getName, toList())));

// two buckets
emps.stream().collect(partitioningBy(e -> e.getAge() >= 30));

// word count: merge function resolves duplicate keys
words.stream().collect(toMap(w -> w, w -> 1, Integer::sum));

// keep insertion order
list.stream().collect(toMap(k -> k, v -> v, (a, b) -> a, LinkedHashMap::new));

// stats in one pass
DoubleSummaryStatistics s = emps.stream().collect(summarizingDouble(Employee::getSalary));
s.getMin(); s.getMax(); s.getAverage(); s.getSum(); s.getCount();
```

(`import static java.util.stream.Collectors.*;` is assumed in the snippets above.)

## 5. Traps

- `toMap(k, v)` with a duplicate key throws `IllegalStateException: Duplicate key`. Pass a merge function.
- `toMap` throws `NullPointerException` if a **value** is null.
- `groupingBy` returns a `HashMap` by default, so key order is unspecified. Pass `TreeMap::new` or `LinkedHashMap::new` for order.
- `counting()` returns `Long`, so compare with `3L`, not `3`.
- `Collectors.toList()` makes no promise about the list type or mutability.
- `maxBy` and `minBy` return `Optional` because the stream may be empty.

## 6. Interview Q&A

- **`groupingBy` vs `partitioningBy`?** `groupingBy` makes any number of buckets from a classifier. `partitioningBy` makes exactly two from a predicate, and both keys are always present.
- **`reduce` vs `collect`?** `reduce` is immutable reduction. `collect` is mutable reduction into a container.
- **What is a downstream collector?** The second argument of `groupingBy`/`partitioningBy`. It decides what each bucket becomes.
- **How do you handle duplicate keys in `toMap`?** Use the 3-argument form with a merge function.
- **How do you get a sorted map out of `groupingBy`?** `groupingBy(f, TreeMap::new, downstream)`.
- **How do you get min, max, sum and average in one pass?** `summarizingInt/Long/Double`.
- **What does `teeing` do?** Runs two collectors on the same stream and merges their results (Java 12).

## 8. Self-check (answer aloud)

1. Why does `collectingAndThen` appear in "highest-paid per department"?
2. What does `partitioningBy` return for a predicate that is never true?
3. How would you make `groupingBy` return keys in sorted order?
4. What exception does `toMap` throw on duplicate keys, and how do you avoid it?
5. Why is the type of `counting()` result `Long`?
