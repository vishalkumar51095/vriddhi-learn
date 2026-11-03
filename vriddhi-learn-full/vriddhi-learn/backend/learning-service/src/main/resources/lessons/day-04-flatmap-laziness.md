---
slug: java-day-04-flatmap-laziness
title: flatMap, distinct, skip, limit and Laziness
category: JAVA_INTERVIEW
subject: Java 8 Essentials
topic: flatMap & Laziness
topicId: bbbff4cb-3be2-5707-834f-8f9eb56816ba
order: 4
---

## 1. Picture: `map` vs `flatMap`

```
 input:  [ [1,2], [3,4], [5] ]

 map(list -> list.stream())        flatMap(list -> list.stream())
 ──────────────────────────        ──────────────────────────────
 Stream< Stream<Integer> >         Stream<Integer>
 [ s1, s2, s3 ]  (still nested)    [ 1, 2, 3, 4, 5 ]   (flattened)
```

`flatMap` = map each element to a **stream**, then concatenate those streams into one.

```
 "the quick" ──split──► the, quick ─┐
 "the lazy"  ──split──► the, lazy  ─┼─► the, quick, the, lazy ──distinct──► the, quick, lazy
```

## 2. The operations

| Operation | Use |
|---|---|
| `flatMap(f)` | 1 to many, result is a `Stream<R>` |
| `flatMapToInt/Long/Double` | same, giving a primitive stream |
| `distinct()` | removes duplicates using `equals`/`hashCode`. Stateful. |
| `sorted()`, `sorted(cmp)` | stateful barrier |
| `skip(n)` | drops the first n |
| `limit(n)` | keeps the first n. Short-circuiting. |
| `mapMulti` (Java 16) | like `flatMap` but pushes elements into a consumer (less allocation) |
| `Optional.stream()` (Java 9) | `flatMap(Optional::stream)` drops empty Optionals |

## 3. Worked examples

```java
// unique words across sentences
sentences.stream().flatMap(s -> Arrays.stream(s.split(" ")))
         .map(String::toLowerCase).distinct().sorted().collect(toList());

// flatten nested lists
nested.stream().flatMap(List::stream).collect(toList());

// third-highest distinct number
nums.stream().distinct().sorted(reverseOrder()).skip(2).findFirst();

// pagination: page is 1-based
list.stream().skip((long) (page - 1) * size).limit(size).collect(toList());

// cartesian product
as.stream().flatMap(a -> bs.stream().map(b -> a + "" + b)).collect(toList());

// distinct by a property: keep the first element for each key
emps.stream().collect(toMap(Employee::getDept, e -> e, (a, b) -> a, LinkedHashMap::new)).values();
```

## 4. Laziness and evaluation order

```
 Stream.of("a","b").peek(p).map(upper).forEach(f)       Stream.of("b","a").peek(p).sorted().forEach(f)

 p-a  f-A  p-b  f-B   (one element at a time)           p-b  p-a  f-a  f-b   (sorted must see ALL first)
```

- Intermediate operations only build the pipeline. The terminal operation starts the run.
- Stateless operations (`filter`, `map`) pass elements through one at a time.
- Stateful operations (`sorted`, `distinct`) may need to buffer before emitting. `sorted` always buffers everything.
- `limit` and `findFirst` stop the whole pipeline early, which is why infinite streams work with them.

## 5. Traps

- `distinct()` on your own class needs correct `equals` and `hashCode`.
- `skip` and `limit` take a `long`. Cast before multiplying to avoid `int` overflow in pagination.
- `limit` after `sorted` still sorts everything. Put `filter` and `distinct` earlier to shrink the data.
- `distinct()` keeps the first occurrence, so it preserves encounter order on ordered streams.
- A `filter` with a stateful predicate (`seen::add`) works sequentially but is unsafe in parallel streams.

## 6. Interview Q&A

- **`map` vs `flatMap`?** `map` is 1 to 1. `flatMap` is 1 to many, flattened into one stream.
- **How would you flatten `List<List<Integer>>`?** `list.stream().flatMap(List::stream)`.
- **Which operations are stateful?** `sorted`, `distinct`, and the ordering parts of `limit`/`skip` on parallel streams.
- **Is `peek` safe for business logic?** No. It is for debugging, and it may not run if the result can be computed without it.
- **How do you implement `distinctBy(property)`?** `toMap(key, identity, (a, b) -> a, LinkedHashMap::new).values()`.
- **`findFirst` vs `limit(1)`?** `findFirst` is terminal and returns an `Optional`. `limit(1)` is intermediate and returns a stream.
- **How do you drop empty Optionals from a stream?** `flatMap(Optional::stream)` (Java 9), or `filter(Optional::isPresent).map(Optional::get)` in Java 8.

## 8. Self-check (answer aloud)

1. What type does `list.stream().map(l -> l.stream())` produce, and why is it usually wrong?
2. Why does `sorted()` change the order of `peek` output?
3. How do you prevent `int` overflow when computing a page offset?
4. Which line of a pipeline would you move earlier to make it faster: `sorted` or `distinct`?
5. How would you get distinct employees by department?
