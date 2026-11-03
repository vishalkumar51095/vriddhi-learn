---
slug: java-day-07-default-datetime
title: Default and Static Interface Methods, Date-Time API
category: JAVA_INTERVIEW
subject: Java 8 Essentials
topic: Default Methods & Date-Time API
topicId: 10aaa661-67a6-5294-a2bc-08f6ef8b0bd1
order: 7
---

## 1. Default methods and the diamond problem

```
        ┌──────────────┐        ┌───────────────┐
        │ Walker       │        │ Swimmer       │
        │ default      │        │ default       │
        │ move()="walk"│        │ move()="swim" │
        └──────┬───────┘        └───────┬───────┘
               └──────────┬─────────────┘
                          ▼
                  class Duck implements Walker, Swimmer
                  ✗ compile error unless Duck overrides move()
                  ✓ return Walker.super.move() + "+" + Swimmer.super.move();
```

**Resolution rules**, in order:
1. A method declared in the **class** (or a superclass) wins over any interface default.
2. Otherwise the **most specific interface** wins (a sub-interface beats its parent).
3. Otherwise you must override and choose explicitly with `InterfaceName.super.method()`.

## 2. Interface methods in Java 8 and 9

| Kind | Rules |
|---|---|
| `default` | has a body, inherited by implementers, can be overridden. Added so interfaces can evolve without breaking old classes (this is how `Collection.stream()` and `List.sort()` arrived). |
| `static` | called as `Interface.method()`. **Not inherited** by implementing classes or sub-interfaces. |
| `private` (Java 9) | shares code between default methods. Not visible outside the interface. |
| Abstract | still exactly one for a functional interface. Defaults and statics do not count. |

An interface has no state. It cannot have instance fields, only constants.

## 3. Date-Time API (`java.time`)

```
  LocalDate        2026-10-29              (date only)
  LocalTime        15:30:00                (time only)
  LocalDateTime    2026-10-29T15:30        (no zone)
  ZonedDateTime    2026-10-29T15:30+05:30[Asia/Kolkata]
  Instant          a point on the UTC timeline (machine time)

  Period    = date-based amount  (years, months, days)   Period.between(a, b)
  Duration  = time-based amount  (hours, minutes, seconds, nanos)   Duration.between(t1, t2)
```

| Task | Code |
|---|---|
| now | `LocalDate.now()`, `LocalDateTime.now()`, `Instant.now()` |
| build | `LocalDate.of(2026, 10, 29)` (months are **1-based**) |
| arithmetic | `d.plusDays(5)`, `d.minusMonths(1)` (returns a **new** object) |
| compare | `isBefore`, `isAfter`, `isEqual` |
| difference | `ChronoUnit.DAYS.between(a, b)`, `Period.between(a, b)` |
| format and parse | `DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH)`, `format(...)`, `LocalDate.parse(s, f)` |
| adjusters | `d.with(TemporalAdjusters.lastDayOfMonth())`, `next(DayOfWeek.FRIDAY)` |
| zone conversion | `ldt.atZone(ZoneId.of("Asia/Kolkata")).withZoneSameInstant(ZoneOffset.UTC)` |
| leap year | `Year.isLeap(2024)`, `d.isLeapYear()` |

## 4. Why it replaced `Date`, `Calendar` and `SimpleDateFormat`

| Old API | Problem |
|---|---|
| `java.util.Date` | mutable, months start at 0, years offset from 1900, mixes date and time |
| `Calendar` | mutable, verbose |
| `SimpleDateFormat` | **not thread-safe** |
| `java.time` | immutable, thread-safe, clear types, fluent API, built-in zones |

## 5. Worked examples

```java
Period.between(LocalDate.of(1995, 8, 15), LocalDate.of(2026, 10, 29)).getYears();   // 31
ChronoUnit.DAYS.between(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1));          // 59
LocalDate.of(2026, 1, 31).plusMonths(1);                                              // 2026-02-28 (clamped)
Duration.between(LocalTime.of(9, 15), LocalTime.of(17, 45)).toMinutes();             // 510
LocalDate.of(2026, 10, 29).with(TemporalAdjusters.next(DayOfWeek.FRIDAY));           // 2026-10-30
```

## 6. Traps

- `plusMonths` on the 29th to 31st clamps to the last valid day. Jan 31 plus one month is Feb 28 or 29.
- `next(DayOfWeek.FRIDAY)` is strictly after the date. Use `nextOrSame` to include today.
- `Period` and `Duration` are different: `Period.between` takes dates, `Duration.between` takes times (or instants).
- `Period.getDays()` is only the "days" component, not the total. Use `ChronoUnit.DAYS.between` for the total.
- A `DateTimeFormatter` pattern uses `MMM` for month names and `mm` for **minutes**. `MM` is the month number. Pass a `Locale` for predictable names.
- `LocalDateTime` has no zone, so it cannot be converted to an instant without one.

## 7. Interview Q&A

- **Why were default methods added?** To add methods to existing interfaces (such as `Iterable.forEach`) without breaking every implementer.
- **How is the diamond problem solved?** The class overrides the method and chooses with `Interface.super.method()`.
- **Are static interface methods inherited?** No. Call them on the interface name.
- **Can an interface with default methods be a functional interface?** Yes, if it has exactly one abstract method.
- **Abstract class vs interface with defaults?** An abstract class can hold state and constructors. An interface cannot, but a class can implement many interfaces.
- **Why is `java.time` better than `Date`?** It is immutable and thread-safe, and it separates date, time, zone and machine time.
- **`Period` vs `Duration`?** `Period` is date-based (years, months, days). `Duration` is time-based (hours, minutes, seconds).
- **Is `DateTimeFormatter` thread-safe?** Yes. `SimpleDateFormat` is not.

## 9. Self-check (answer aloud)

1. What happens if two interfaces give the same default method to one class?
2. Why does `LocalDate.of(2026, 1, 31).plusMonths(1)` not throw an exception?
3. What is the difference between `MM` and `mm` in a format pattern?
4. When would you pick `Instant` over `LocalDateTime`?
5. Why is a static interface method not inherited?
