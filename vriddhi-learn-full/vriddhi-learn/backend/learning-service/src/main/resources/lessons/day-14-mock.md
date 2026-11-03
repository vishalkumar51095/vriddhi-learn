---
slug: java-day-14-mock
title: Rapid-Fire Review and Interview Strategy
category: JAVA_INTERVIEW
subject: Interview Strategy
order: 14
---

## 1. How to attack a stream problem in an interview

```
 1. Clarify      null? empty? duplicates? ties? sorted output? case-insensitive?
 2. Shape        input type ─► [filter ─► map ─► sort ─►] terminal ─► output type
 3. Pick the terminal   one value → reduce/max/findFirst   many values → collect
                        group → groupingBy   split in two → partitioningBy
 4. Edge cases   empty list, one element, all equal, ties
 5. Complexity   sorted is O(n log n); groupingBy is O(n); contains on a List inside a loop is O(n²)
```

## 2. Rapid-fire theory (answer each in one sentence)

| Day | Question | Short answer |
|---|---|---|
| 1 | What is a functional interface? | exactly one abstract method |
| 1 | Lambda vs anonymous class? | `this` differs, lambdas use `invokedynamic`, lambdas only for functional interfaces |
| 1 | Why must captured variables be effectively final? | the lambda captures a copy of the value |
| 2 | Intermediate vs terminal operations? | intermediate is lazy and returns a stream, terminal triggers the run |
| 2 | Can you reuse a stream? | no, `IllegalStateException` |
| 2 | `findFirst` vs `findAny`? | same sequentially, `findAny` is cheaper in parallel |
| 2 | `peek` vs `forEach`? | `peek` is intermediate (debugging), `forEach` is terminal |
| 3 | `groupingBy` vs `partitioningBy`? | n buckets from a classifier vs exactly two from a predicate |
| 3 | `toMap` with duplicate keys? | `IllegalStateException` unless a merge function is given |
| 3 | `reduce` vs `collect`? | immutable vs mutable reduction |
| 4 | `map` vs `flatMap`? | 1 to 1 vs 1 to many, flattened |
| 4 | Which operations are stateful? | `sorted`, `distinct` |
| 5 | Why `chars()` plus a cast? | it returns an `IntStream` of code units |
| 6 | `orElse` vs `orElseGet`? | argument always evaluated vs lazy supplier |
| 6 | `of` vs `ofNullable`? | NPE on null vs empty Optional |
| 6 | Four method reference kinds? | static, bound, unbound, constructor |
| 7 | Diamond problem with defaults? | override and call `Interface.super.method()` |
| 7 | `Period` vs `Duration`? | date-based vs time-based |
| 7 | Why `java.time` over `Date`? | immutable, thread-safe, clear types |
| 8 | `List.of` vs `Arrays.asList`? | immutable and null-hostile vs fixed-size and null-friendly |
| 8 | `takeWhile` vs `filter`? | stops at first failure vs tests all |
| 8 | Where can `var` be used? | local variables only |
| 9 | `trim` vs `strip`? | ASCII-ish vs Unicode-aware |
| 9 | What does `teeing` do? | two collectors, one pass, merged result |
| 10 | Switch expression vs statement? | returns a value, no fall-through, exhaustive |
| 10 | What does a record generate? | fields, constructor, accessors, equals, hashCode, toString |
| 11 | Sealed class permitted subtypes must be? | `final`, `sealed` or `non-sealed` |
| 12 | What are virtual threads? | JVM-managed lightweight threads for blocking work |
| 12 | Why can a pattern switch skip `default`? | a sealed hierarchy is exhaustive |
| 13 | `thenApply` vs `thenCompose`? | map vs flatMap for futures |
| 13 | `join` vs `get`? | unchecked vs checked exceptions |
| 13 | When avoid `parallelStream`? | small data, blocking I/O, shared state |

## 3. Final readiness checklist

- [ ] I can explain lazy evaluation using the `peek` example.
- [ ] I can write `groupingBy` with a downstream collector without looking.
- [ ] I can solve "second highest salary" three ways.
- [ ] I know the `orElse` trap and the `toMap` duplicate-key trap.
- [ ] I can name what is new in Java 8, 11, 17 and 21 in one minute each.
- [ ] I can explain records, sealed classes and virtual threads with one example each.
