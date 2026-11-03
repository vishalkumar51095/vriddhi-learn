---
slug: java-day-06-optional-methodrefs
title: Optional and Method References
category: JAVA_INTERVIEW
subject: Java 8 Essentials
topic: Optional & Method References
topicId: 1104fd37-3e49-5d7a-a524-8273d199fa61
order: 6
---

## 1. Picture: Optional is a box that may be empty

```
   Optional<String>
   ┌──────────────┐          ┌──────────────┐
   │   "Vishal"   │    or    │   (empty)    │        the box itself is never null
   └──────────────┘          └──────────────┘

   Without Optional                    With Optional
   ─────────────────                   ────────────────────────────────────────────
   u.getAddress().getCity()  ► 💥 NPE   Optional.ofNullable(u).map(User::getAddress)
                                                .map(Address::getCity).orElse("UNKNOWN")
```

Each `map` step is skipped once the box is empty, so the chain is null-safe.

## 2. API at a glance

| Group | Methods |
|---|---|
| Create | `Optional.of(x)` (NPE if null), `ofNullable(x)`, `empty()` |
| Transform | `map(f)`, `flatMap(f)` (f already returns an Optional), `filter(p)` |
| Unwrap | `orElse(v)`, `orElseGet(supplier)`, `orElseThrow(supplier)`, `get()` (avoid) |
| Act | `ifPresent(consumer)`, `isPresent()` |
| Java 9 | `or(supplier)`, `ifPresentOrElse(c, runnable)`, `stream()` |
| Java 10 and 11 | `orElseThrow()` (no argument), `isEmpty()` |
| Primitive versions | `OptionalInt`, `OptionalLong`, `OptionalDouble` |

## 3. `orElse` vs `orElseGet`

```
 Optional.of("x").orElse( expensive() )          expensive() RUNS even though the box is full
 Optional.of("x").orElseGet( () -> expensive() ) expensive() never runs (supplier is lazy)
```

Use `orElse` for constants. Use `orElseGet` when the default costs something (a DB call, an object creation).

## 4. `map` vs `flatMap`

```
 user.getEmail() returns Optional<String>

 Optional.of(user).map(User::getEmail)         → Optional<Optional<String>>   (nested)
 Optional.of(user).flatMap(User::getEmail)     → Optional<String>             (flat)
```

## 5. Method references: four kinds

| Kind | Example | Same as lambda |
|---|---|---|
| Static | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Bound instance (a specific object) | `System.out::println`, `sink::add` | `x -> sink.add(x)` |
| Unbound instance (receiver is the first argument) | `String::toUpperCase` | `s -> s.toUpperCase()` |
| Constructor | `User::new`, `ArrayList::new`, `String[]::new` | `n -> new User(n)` |

Use a method reference when the lambda only calls one existing method. It is shorter and easier to read.

## 6. Worked examples

```java
Optional.of("hi").map(String::length).orElse(0);                    // 2
Optional.ofNullable(null).map(Object::toString).orElse("none");     // none
Optional.of(4).filter(n -> n % 2 == 0);                             // Optional[4]
Optional.ofNullable(name).orElseThrow(() -> new IllegalArgumentException("name required"));

list.stream().map(Integer::parseInt)                                // static
list.stream().map(String::toUpperCase)                              // unbound
names.stream().map(User::new)                                       // constructor
src.forEach(sink::add)                                              // bound
```

## 7. Anti-patterns

- `if (opt.isPresent()) return opt.get();` is just `orElse` written the long way. Prefer `map`/`orElse`.
- Calling `get()` on an empty Optional throws `NoSuchElementException`.
- Do not use Optional for fields, method parameters, or collections. It is meant for **return types**. It is also not `Serializable`.
- Do not return `null` from a method whose return type is `Optional`.
- `Optional.of(null)` throws NPE. Use `ofNullable` when the value may be null.

## 8. Interview Q&A

- **`of` vs `ofNullable`?** `of` throws NPE on null. `ofNullable` returns an empty Optional.
- **`orElse` vs `orElseGet`?** `orElse` always evaluates its argument. `orElseGet` evaluates the supplier only when empty.
- **`map` vs `flatMap` on Optional?** `flatMap` is for functions that already return an Optional, which avoids nesting.
- **Why not use Optional as a field or parameter?** It adds allocation and noise, is not serializable, and does not stop callers from passing `null` for the Optional itself.
- **What does `orElseThrow()` with no argument do? (Java 10)** It throws `NoSuchElementException` when empty and is the preferred replacement for `get()`.
- **Name the four method reference types.** Static, bound instance, unbound instance, constructor.
- **Difference between bound and unbound references?** Bound has a fixed receiver (`sink::add`). Unbound takes the receiver as the first argument (`String::toUpperCase`).

## 10. Self-check (answer aloud)

1. What does `Optional.ofNullable(null).map(String::length)` return?
2. When is the argument of `orElse(...)` evaluated?
3. Why would `map(User::getEmail)` give `Optional<Optional<String>>`?
4. Which kind of method reference is `String::toUpperCase`, and what is its lambda?
5. Why is `Optional` a poor choice for a class field?
