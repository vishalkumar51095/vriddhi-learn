---
slug: java-day-10-java14-16
title: Java 14, 15 and 16
category: JAVA_INTERVIEW
subject: Modern Java (9 to 21)
topic: Java 14 to 16
topicId: 1ff323e6-ec75-55cd-bc04-e3340b277e4b
order: 10
---

## 1. Picture: when each feature became final

```
 Feature                       preview        FINAL
 ───────────────────────────   ────────────   ─────────
 switch expressions            12, 13         Java 14
 text blocks                   13, 14         Java 15
 records                       14, 15         Java 16
 pattern matching instanceof   14, 15         Java 16
 Stream.toList()               –              Java 16
 helpful NullPointerExceptions –              Java 14 (on by default from 15)
```

## 2. Switch expressions (Java 14)

```
 OLD statement                          NEW expression
 ─────────────────────────              ─────────────────────────────────────
 int d;                                 int d = switch (month) {
 switch (month) {                           case "FEB"             -> leap ? 29 : 28;
   case "FEB": d = 28; break;               case "APR", "JUN"      -> 30;
   case "APR":                              default                -> 31;
   case "JUN": d = 30; break;           };
   default:    d = 31;
 }                                      no fall-through, no break, returns a value
```

- Arrow form (`->`) never falls through. Several labels share one case with commas.
- A block returns a value with `yield`.
- A switch **expression** must be exhaustive. For an enum with all constants covered, no `default` is needed. Otherwise add `default`.
- Old `case X:` labels still work, and with `yield` you can use them inside a switch expression too.

## 3. Text blocks (Java 15)

```java
String json = """
              {"name": "%s", "age": %d}
              """.formatted(name, age);
```

- The opening `"""` must be followed by a newline.
- Incidental indentation is removed, based on the **least-indented line, including the closing `"""`**.
- A trailing newline exists only if the closing delimiter is on its own line.
- `\` at the end of a line suppresses the newline. `\s` keeps a trailing space.
- `"` does not need escaping inside, but `"""` does.
- `String.formatted(...)` (Java 15) is the instance version of `String.format`.

## 4. Records (Java 16)

```
 record Money(long amount, String currency) { }
          │
          ├─ private final fields amount, currency
          ├─ canonical constructor Money(long, String)
          ├─ accessors amount() and currency()      (not getAmount!)
          └─ equals(), hashCode(), toString()  →  "Money[amount=100, currency=INR]"
```

| Rule | Detail |
|---|---|
| Fields | implicitly `private final`. Records are immutable (shallowly). |
| Inheritance | implicitly `final`. Cannot extend a class. **Can** implement interfaces. |
| Constructors | compact form validates: `Range { if (lo > hi) throw new IllegalArgumentException(); }` |
| Extra members | static methods and fields, instance methods, and additional constructors are allowed |
| No | extra instance fields, `abstract` |
| Use for | DTOs, value objects, map keys, multiple return values |

A record is **shallowly** immutable: a `List` component can still be modified.

## 5. Pattern matching for `instanceof` (Java 16)

```java
if (o instanceof String s && !s.isEmpty()) {   // s is already cast and in scope
    return s.length();
}
```

The binding is in scope only where the compiler can prove the match succeeded. In `!(o instanceof String s)` the variable is in scope after an early `return`.

## 6. `Stream.toList()` (Java 16)

`stream.toList()` returns an **unmodifiable** list and is shorter than `collect(Collectors.toList())`. It allows null elements, while `List.of` does not.

## 7. Traps

- Record accessors are `name()`, not `getName()`. Frameworks that expect JavaBean getters (older Jackson or JPA setups) may need configuration.
- A record cannot be a JPA `@Entity` (it needs a no-argument constructor and mutable fields).
- In a text block, moving the closing `"""` left or right changes the indentation of the whole block.
- A switch expression with no matching case and no `default` over a non-enum type will not compile.
- Pattern variables cannot shadow other variables in scope.

## 8. Interview Q&A

- **Statement vs expression switch?** An expression produces a value, uses `->` with no fall-through, and must be exhaustive.
- **What does `yield` do?** It returns a value from a block inside a switch expression.
- **What does a record give you automatically?** Private final fields, a canonical constructor, accessors, `equals`, `hashCode` and `toString`.
- **Can a record extend a class?** No. It can implement interfaces.
- **Class vs record?** A record is a transparent immutable data carrier. A class can hold mutable state and hide its representation.
- **How are text blocks indented?** The minimal indentation of the content lines and the closing delimiter is stripped.
- **`Stream.toList()` vs `collect(Collectors.toList())`?** `toList()` is unmodifiable and allows nulls. `Collectors.toList()` makes no guarantees about the list type.
- **What do helpful NullPointerExceptions do?** The message names the variable or method call that was null.

## 10. Self-check (answer aloud)

1. Why does a switch expression not need `break`?
2. What does the compiler check for an enum switch expression with no `default`?
3. Why is `record Person(List<String> tags)` only shallowly immutable?
4. Why is `name()` not `getName()` on a record?
5. What decides the indentation in a text block?
