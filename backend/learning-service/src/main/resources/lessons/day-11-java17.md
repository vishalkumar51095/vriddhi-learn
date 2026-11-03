---
slug: java-day-11-java17
title: Java 17 (LTS): Sealed Classes
category: JAVA_INTERVIEW
subject: Modern Java (9 to 21)
topic: Java 17: Sealed Classes
topicId: f24592a5-1f18-545f-bfba-a8b705cb175e
order: 11
---

## 1. Picture: a sealed hierarchy is a closed family

```
 OPEN (normal interface)               CLOSED (sealed interface)
 ───────────────────────               ───────────────────────────────────────
        Shape                           sealed interface Shape
       ╱  │  ╲   ╲ …anyone, anywhere       permits Circle, Rectangle, Triangle
 Circle  Rect  ?  ?                           │         │          │
                                          Circle   Rectangle   Triangle   ← the ONLY subtypes
```

The compiler and every reader now know **all** the subtypes. That enables exhaustive checks (pattern-matching `switch` in Java 21, Day 12).

## 2. Rules

```
 sealed interface/class X permits A, B { }     ← lists the allowed direct subtypes
        │
        ├─ A must be:  final        (no more subclasses)
        │              sealed       (and list its own permits)
        │              non-sealed   (open again; anyone may extend A)
        └─ A and B must be in the same module (or the same package in the unnamed module)
```

| Rule | Detail |
|---|---|
| `permits` | may be omitted if the subtypes are in the **same source file** |
| Subtype modifier | exactly one of `final`, `sealed`, `non-sealed` (records and enums are implicitly final) |
| Applies to | classes and interfaces |
| Reflection | `Class.isSealed()`, `Class.getPermittedSubclasses()` (Java 17) |

## 3. Why use it

- Model a **closed set of alternatives** (`Ok | Err`, `Circle | Rectangle | Triangle`, payment types, AST nodes).
- Replace `abstract class` plus "hope nobody else extends it".
- Remove `default` branches that hide missing cases.

## 4. Worked example

```java
sealed interface Result permits Ok, Err { }
record Ok(int value)       implements Result { }
record Err(String message) implements Result { }

String render(Result r) {
    if (r instanceof Ok o)  return "ok:" + o.value();
    if (r instanceof Err e) return "err:" + e.message();
    throw new IllegalStateException();     // unreachable if the hierarchy is complete
}
```

In Java 21 the same method becomes an exhaustive `switch` with no `throw` and no `default`.

## 5. Other Java 17 changes

| Change | Note |
|---|---|
| Strong encapsulation of JDK internals | reflective access to `java.*` internals is blocked unless `--add-opens` is given. Old libraries may break on upgrade from 8 or 11. |
| `RandomGenerator` interface | common API for random number algorithms (`RandomGenerator.of("L64X128MixRandom")`) |
| Security Manager | deprecated for removal |
| Applet API | deprecated for removal |
| Pattern matching for `switch` | **preview** in 17, final in 21 |
| Sealed classes | final in 17 (preview in 15 and 16) |
| Records, text blocks, switch expressions, `instanceof` patterns | final earlier, all available in 17 |

## 6. Traps

- A direct subtype that is not `final`, `sealed` or `non-sealed` does not compile.
- `non-sealed` re-opens the hierarchy below that class. Use it deliberately.
- Subtypes must be in the same package (unnamed module) or module as the sealed type.
- Sealed does not mean immutable. It only restricts who can extend.
- Migrating from Java 8 to 17 often breaks libraries that use illegal reflective access. Check the dependencies.

## 7. Interview Q&A

- **What does `sealed` do?** It restricts which classes may extend or implement a type.
- **What must each permitted subclass declare?** `final`, `sealed` or `non-sealed`.
- **Sealed vs `final`?** `final` allows no subclasses. `sealed` allows a fixed list.
- **Why combine sealed types with records?** Records are concise final data carriers, so they make good alternatives of a sealed hierarchy.
- **How do you read the permitted subclasses at runtime?** `Class.getPermittedSubclasses()`.
- **What is the benefit for `switch`?** The compiler can check that every case is covered (Java 21).
- **Why do upgrades to 17 break some libraries?** Strong encapsulation blocks reflective access to JDK internals.

## 9. Self-check (answer aloud)

1. Which three modifiers can a direct subtype of a sealed class have?
2. When can you omit the `permits` clause?
3. Why is `Truck` marked `non-sealed` while `PickUp` needs no modifier?
4. Why does the final `throw` in `render` never run?
5. What Java 17 change most often breaks an old application?
