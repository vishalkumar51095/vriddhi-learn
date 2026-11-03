---
slug: java-day-13-completablefuture
title: CompletableFuture and Parallel Streams
category: JAVA_INTERVIEW
subject: Concurrency (Java 8+)
topic: CompletableFuture & Parallel Streams
topicId: d6fba9b1-f3d3-54b0-87c1-84e7a1416c73
order: 13
---

## 1. Picture: a pipeline of async stages

```
 supplyAsync(() -> 21)   ──thenApply(x*2)──►  ──thenApply("answer="+x)──► join() → "answer=42"
   runs on another thread    transform             transform                 wait here

 thenCompose  (dependent call)          thenCombine  (independent calls, joined)
 ───────────────────────────            ────────────────────────────────────────
 fetchUser ──► fetchOrder(user)          taskA ──┐
 (second call needs the first result)            ├─► (a, b) -> a + b
                                         taskB ──┘  (both run in parallel)
```

`thenApply` is to futures what `map` is to streams. `thenCompose` is `flatMap`: use it when the next step **itself returns a future**.

## 2. The API

| Group | Methods |
|---|---|
| Start | `supplyAsync(supplier)` (returns a value), `runAsync(runnable)`, `completedFuture(v)`, `new CompletableFuture<>()` then `complete(v)` |
| Transform | `thenApply(f)`, `thenAccept(consumer)`, `thenRun(runnable)` |
| Chain | `thenCompose(f)` (f returns a CompletableFuture) |
| Combine | `thenCombine(other, biFunction)`, `thenAcceptBoth`, `allOf(f...)` (returns `CompletableFuture<Void>`), `anyOf(f...)` (returns `CompletableFuture<Object>`) |
| Errors | `exceptionally(ex -> fallback)`, `handle((v, ex) -> ...)`, `whenComplete((v, ex) -> ...)` |
| Wait | `join()` (unchecked `CompletionException`), `get()` (checked `ExecutionException`, `InterruptedException`) |
| `...Async` variants | `thenApplyAsync(f, executor)` runs the stage on another thread, possibly a custom executor |
| Java 9+ | `orTimeout(t, unit)`, `completeOnTimeout(v, t, unit)`, `delayedExecutor(...)`, `copy()` |

By default async stages run on `ForkJoinPool.commonPool()`, which uses **daemon threads**. Pass your own `Executor` for blocking or I/O-heavy work.

## 3. Error handling

```
 supplyAsync(task) ──► stage ──► stage
        │ throws
        ▼
 exceptionally(ex -> fallback)     only runs on failure, returns a fallback value
 handle((v, ex) -> ...)            always runs: value OR exception (one is null)
 whenComplete((v, ex) -> ...)      side effect only, cannot change the result
```

- An exception thrown in a stage is delivered to later stages wrapped in `CompletionException`. Use `ex.getCause()` for the original.
- `join()` throws an unchecked `CompletionException`. `get()` throws a checked `ExecutionException`.
- An exception in one stage skips the `thenApply` stages after it until a handler (`exceptionally`, `handle`) catches it.

## 4. Worked examples

```java
// parallel calls, combined
CompletableFuture<Integer> a = supplyAsync(() -> priceFromServiceA());
CompletableFuture<Integer> b = supplyAsync(() -> priceFromServiceB());
a.thenCombine(b, Math::min).join();

// dependent calls
fetchUser(id).thenCompose(u -> fetchOrder(u)).join();

// wait for many
List<CompletableFuture<Integer>> fs = ids.stream().map(i -> supplyAsync(() -> load(i))).collect(toList());
CompletableFuture.allOf(fs.toArray(new CompletableFuture[0])).join();
List<Integer> results = fs.stream().map(CompletableFuture::join).collect(toList());

// fallback
supplyAsync(() -> risky()).exceptionally(ex -> -1).join();
```

## 5. Parallel streams

```
 list.parallelStream()  ──split──►  chunk1 ─┐
                                    chunk2 ─┼─► ForkJoinPool.commonPool() ─► combine ─► result
                                    chunk3 ─┘
```

| Good fit | Bad fit |
|---|---|
| large data, CPU-bound, stateless operations | small lists (overhead beats gain) |
| easily splittable sources (`ArrayList`, arrays, ranges) | blocking I/O inside the lambda (starves the common pool) |
| associative operations (`sum`, `reduce` with a proper identity) | shared mutable state, ordered side effects, `LinkedList` or `iterate` sources |

- `forEach` on a parallel stream does not preserve order. Use `forEachOrdered`.
- `collect(toList())` and `toList()` **do** preserve encounter order.
- The `reduce` identity must be a true identity, and the accumulator must be associative and stateless.
- Measure before and after. Parallel is not automatically faster.

## 6. Traps

- Calling `join()` or `get()` inside another async stage can block pool threads and cause starvation.
- `allOf` returns `CompletableFuture<Void>`. You must `join()` each individual future for the values.
- Without a custom executor, many blocking tasks on the common pool can slow unrelated parallel streams.
- `thenApply` vs `thenApplyAsync`: the first may run on the completing thread, the second schedules a new task.
- A `CompletableFuture` never times out by itself in Java 8. Java 9 added `orTimeout`.

## 7. Interview Q&A

- **`thenApply` vs `thenCompose`?** Like `map` vs `flatMap`. Use `thenCompose` when the function returns a `CompletableFuture`.
- **`thenCombine` vs `thenCompose`?** `thenCombine` joins two independent futures. `thenCompose` chains a dependent one.
- **`join()` vs `get()`?** `join` throws unchecked `CompletionException`. `get` throws checked exceptions.
- **`exceptionally` vs `handle`?** `exceptionally` runs only on failure. `handle` runs always and sees both value and exception.
- **Which thread pool does `supplyAsync` use by default?** `ForkJoinPool.commonPool()`.
- **How do you run several tasks and wait for all?** `CompletableFuture.allOf(...).join()`.
- **How do you add a timeout?** `orTimeout` or `completeOnTimeout` (Java 9+).
- **When would you avoid `parallelStream()`?** Small data, I/O-bound or order-dependent work, or shared mutable state.
- **`Future` vs `CompletableFuture`?** `Future` can only block with `get()`. `CompletableFuture` supports chaining, combining and error handling without blocking.

## 9. Self-check (answer aloud)

1. Which method would you use if the next step returns another `CompletableFuture`?
2. Why does `allOf(...)` not give you the results?
3. What exception type reaches `handle` when `supplyAsync` throws?
4. Why can blocking calls inside `parallelStream()` hurt unrelated code?
5. How do you make `forEach` on a parallel stream respect order?
