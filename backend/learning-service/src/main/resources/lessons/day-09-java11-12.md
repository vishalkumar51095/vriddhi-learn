---
slug: java-day-09-java11-12
title: Java 11 and Java 12
category: JAVA_INTERVIEW
subject: Modern Java (9 to 21)
topic: Java 11 & 12
topicId: eb83643d-4845-525e-a202-655dc3a8d993
order: 9
---

## 1. Picture: what changed

```
 Java 11 (LTS, 2018)                                Java 12
 ──────────────────────────────────────             ─────────────────────────────
 String:  isBlank  strip  stripLeading              Collectors.teeing
          stripTrailing  lines  repeat              String.indent / transform
 Files.readString / writeString                     switch expressions (preview)
 Path.of(...)                                       Files.mismatch
 java.net.http.HttpClient (HTTP/1.1 + HTTP/2)
 var in lambda parameters
 Predicate.not, Optional.isEmpty
 Collection.toArray(IntFunction)
 java Hello.java  (run a single source file)
```

## 2. String additions (Java 11 and 12)

| Method | Does | Note |
|---|---|---|
| `isBlank()` | true if empty or only whitespace | `isEmpty()` is true only for length 0 |
| `strip()`, `stripLeading()`, `stripTrailing()` | remove whitespace | Unicode-aware. `trim()` removes only characters up to U+0020. |
| `lines()` | `Stream<String>` split on line terminators | no trailing empty string |
| `repeat(n)` | concatenate n times | `repeat(0)` gives `""` |
| `indent(n)` (12) | adds or removes n leading spaces on each line, normalizes line endings, adds a trailing newline | |
| `transform(f)` (12) | applies a function to the string | enables fluent chains |

## 3. Other Java 11 additions

```java
Files.writeString(path, "text");   String s = Files.readString(path);
Path p = Path.of("dir", "file.txt");                 // replaces Paths.get
list.stream().filter(Predicate.not(String::isBlank)) // instead of s -> !s.isBlank()
(var a, var b) -> a + b                              // allows annotations on lambda parameters
String[] arr = list.toArray(String[]::new);
optional.isEmpty();
```

**HTTP Client** (`java.net.http`, standardized in 11): `HttpClient.newHttpClient()`, `HttpRequest.newBuilder(URI.create(url)).build()`, then `client.send(req, BodyHandlers.ofString())`, or `sendAsync(...)` returning a `CompletableFuture`. It supports HTTP/2 and replaces `HttpURLConnection` for new code.

Also in 11: run a single file with `java File.java`, TLS 1.3, Flight Recorder open-sourced, Epsilon (no-op) GC, ZGC experimental. The Java EE and CORBA modules (JAXB, JAX-WS) were removed from the JDK.

## 4. `Collectors.teeing` (Java 12)

```
              ┌─► minBy ──┐
 stream ──────┤           ├─► merger((min, max) -> min + "-" + max) ─► result
              └─► maxBy ──┘
         one pass over the data, two collectors
```

```java
nums.stream().collect(teeing(
    minBy(naturalOrder()), maxBy(naturalOrder()),
    (min, max) -> min.get() + "-" + max.get()));
```

Typical use: count and sum in one pass, then compute an average.

## 5. Traps

- `"\u2003".trim()` leaves the character in place, but `strip()` removes it.
- `"".isBlank()` and `"".isEmpty()` are both true, but `" ".isBlank()` is true while `" ".isEmpty()` is false.
- `indent` always ends the result with `\n`.
- `lines()` splits on `\n`, `\r` and `\r\n`.
- `Files.readString` and `writeString` use UTF-8 by default and load the whole file into memory. For large files use `Files.lines` or a reader.

## 6. Interview Q&A

- **`trim()` vs `strip()`?** `strip()` uses `Character.isWhitespace` (Unicode-aware). `trim()` removes only characters up to U+0020.
- **`isBlank()` vs `isEmpty()`?** `isEmpty()` is length zero. `isBlank()` also covers whitespace-only strings.
- **What is new in the Java 11 HTTP client?** A modern API with HTTP/2, async support through `CompletableFuture`, and builders.
- **What does `var` in a lambda parameter allow?** Annotations on the parameter, while keeping type inference.
- **What does `teeing` do?** Runs two collectors on one pass and merges the results.
- **Why is Java 11 important?** It is a long-term support release that many companies moved to from Java 8.
- **How do you run a one-file program in Java 11?** `java Hello.java`, with no separate `javac` step.

## 8. Self-check (answer aloud)

1. Which string would `isBlank()` call blank but `isEmpty()` not?
2. Why can `trim()` fail on text copied from a web page?
3. How would you compute min and max in one pass?
4. What replaced `Paths.get(...)`?
5. Which Java 11 feature lets you skip `javac` for a small program?
