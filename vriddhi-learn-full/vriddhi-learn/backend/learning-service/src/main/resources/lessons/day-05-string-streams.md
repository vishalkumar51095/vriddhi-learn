---
slug: java-day-05-string-streams
title: String and Character Streams
category: JAVA_INTERVIEW
subject: Java 8 Essentials
topic: String & Character Streams
topicId: 8776ce3e-1f20-5494-a087-927978004ac3
order: 5
---

## 1. Picture: turning a String into a stream of characters

```
 "swiss" ──chars()──► IntStream 115,119,105,115,115      (ints, not chars!)
                          │ mapToObj(c -> (char) c)
                          ▼
                    Stream<Character>  s, w, i, s, s
                          │ groupingBy(identity, LinkedHashMap::new, counting())
                          ▼
                    {s=3, w=1, i=1}   ──filter(count==1)──► w   (first non-repeating)
```

## 2. The toolkit

| Need | Code |
|---|---|
| chars as ints | `s.chars()` (`IntStream`). Use `s.codePoints()` for full Unicode. |
| chars as `Character` | `s.chars().mapToObj(c -> (char) c)` |
| words | `Arrays.stream(s.split(" "))`, or `split("\\s+")` for repeated spaces |
| frequency | `groupingBy(Function.identity(), counting())` |
| keep order | `groupingBy(identity(), LinkedHashMap::new, counting())` |
| join back | `collect(Collectors.joining(" "))` |
| index loop as a stream | `IntStream.range(0, n)` |
| sorted chars | `s.chars().sorted().toArray()` |

## 3. Worked examples

```java
// character frequency
s.chars().mapToObj(c -> (char) c)
 .collect(groupingBy(Function.identity(), LinkedHashMap::new, counting()));

// first non-repeating character
...frequency map....entrySet().stream().filter(e -> e.getValue() == 1)
 .map(Map.Entry::getKey).findFirst().orElse(null);

// palindrome (no extra memory)
IntStream.range(0, t.length() / 2).allMatch(i -> t.charAt(i) == t.charAt(t.length() - 1 - i));

// anagram: sort both and compare
Arrays.equals(a.chars().sorted().toArray(), b.chars().sorted().toArray());

// reverse each word
Arrays.stream(s.split(" ")).map(w -> new StringBuilder(w).reverse().toString()).collect(joining(" "));

// longest word
Arrays.stream(s.split(" ")).max(Comparator.comparingInt(String::length));

// vowel count
s.toLowerCase().chars().filter(c -> "aeiou".indexOf(c) >= 0).count();
```

## 4. Traps

- `chars()` returns an `IntStream`. Without `(char)` you will group by numbers, not characters.
- `HashMap` has no order guarantee. Use `LinkedHashMap` when order matters ("first non-repeating").
- `e.getValue() == 1` works because `Long` unboxes against an `int` literal. Comparing two `Long` objects with `==` is a bug. Use `equals`.
- `String.split(" ")` on text with repeated spaces creates empty strings. Use `"\\s+"` there.
- Case matters. Call `toLowerCase()` first for palindrome, anagram and vowel checks, if the question says to ignore case.
- `chars()` splits supplementary characters (such as many emojis) into two surrogate halves. `codePoints()` does not.

## 5. Interview Q&A

- **`chars()` vs `toCharArray()`?** `chars()` gives an `IntStream` for pipelines. `toCharArray()` gives a `char[]` for loops.
- **Why can't you stream a `char[]` directly?** There is no `CharStream`. Use `new String(arr).chars()` or loop over indices.
- **How do you find duplicates in a string?** Group with `counting()`, then filter counts above 1.
- **How do you remove duplicate characters but keep order?** `s.chars().distinct().mapToObj(c -> String.valueOf((char) c)).collect(joining())`.
- **`StringBuilder.reverse()` vs a stream for reversing?** `StringBuilder.reverse()` is simpler and faster. Streams add nothing for a single reversal.
- **How would you count words case-insensitively?** `map(String::toLowerCase)` before `groupingBy`.
- **`Collectors.joining` vs `String.join`?** `String.join` is for existing collections of strings. `joining` terminates a stream pipeline.

## 7. Self-check (answer aloud)

1. Why does `"abc".chars().mapToObj(c -> c)` produce `Stream<Integer>`?
2. Why must the frequency map be a `LinkedHashMap` for "first non-repeating"?
3. How do you make the palindrome check ignore case?
4. What does `split(" ")` do with two consecutive spaces?
5. When would you use `codePoints()` instead of `chars()`?
