#!/usr/bin/env python3
"""
Builds java_interview.json (category JAVA_INTERVIEW: Java 8 to 21 interview prep) for gen_seed_sql.py.

    python3 infra/seed-data/build_java_interview.py
    python3 infra/tools/gen_seed_sql.py infra/seed-data/java_interview.json \
        --content-out backend/content-service/src/main/resources/db/migration/V5__seed_java_interview.sql \
        --exam-out    backend/exam-service/src/main/resources/db/migration/V6__seed_java_interview_papers.sql

Each question is written as (difficulty, question, correct answer, [wrong answers], explanation[, code]).
The correct answer is always written first; the builder shuffles the options with a seed derived from the
question text, so the output is stable and the correct answer is not always option A.
Every "what does this print/return" question was checked by running the code on a real JDK.
"""
import hashlib, json, os, random

def Q(d, q, correct, wrong, why, code=None):
    opts = [correct] + list(wrong)
    rnd = random.Random(hashlib.md5(q.encode()).hexdigest())
    order = list(range(len(opts)))
    rnd.shuffle(order)
    item = {'d': d, 'q': q, 'options': [opts[i] for i in order], 'answer': order.index(0), 'why': why,
            'tags': ['Java Interview Prep']}
    if code:
        item['code'] = code
    return item

E, M, H = 'EASY', 'MEDIUM', 'HARD'
SAME = "Same behaviour as the Java language specification describes."

TOPICS = {}

TOPICS['Lambda & Functional Interfaces'] = ('Lambdas, functional interfaces, comparators', [
    Q(E, 'What makes an interface a functional interface?', 'It has exactly one abstract method',
      ['It has no methods at all', 'It contains only default methods', 'It must be annotated with @FunctionalInterface'],
      'A functional interface has exactly one abstract method; default and static methods do not count. @FunctionalInterface is optional: it only makes the compiler enforce the rule.'),
    Q(E, 'Which built-in functional interface converts a value of type T into a value of type R?', 'Function<T, R>',
      ['Predicate<T>', 'Consumer<T>', 'Supplier<T>'],
      'Function<T, R> has apply(T) returning R. Predicate returns boolean, Consumer returns nothing, Supplier takes no input.'),
    Q(E, 'What does Predicate<T>.test(t) return?', 'boolean', ['T', 'void', 'Optional<T>'],
      'A Predicate is a yes/no test, so test() returns a primitive boolean. It is what filter() expects.'),
    Q(M, 'Inside a lambda expression, what does `this` refer to?', 'The enclosing class instance',
      ['The lambda object itself', 'The functional interface type', 'The thread that runs the lambda'],
      'A lambda does not introduce a new scope for this. In an anonymous class, this is the anonymous object, which is a key difference.'),
    Q(M, 'What happens when this code is compiled?', 'Compile error: a local variable used in a lambda must be final or effectively final',
      ['count ends up equal to list.size()', 'It compiles but throws an exception at runtime', 'It compiles and count stays 0'],
      'Lambdas capture a copy of local variables, so they must never be reassigned. Use an array, an AtomicInteger, or a stream operation such as count() instead.',
      'int count = 0;\nlist.forEach(x -> count++);'),
    Q(M, 'Which comparator sorts employees by salary descending and breaks ties by name ascending?',
      'Comparator.comparingDouble(Employee::getSalary).reversed().thenComparing(Employee::getName)',
      ['Comparator.comparingDouble(Employee::getSalary).thenComparing(Employee::getName).reversed()',
       'Comparator.comparing(Employee::getName).thenComparing(Employee::getSalary)',
       'Comparator.naturalOrder()'],
      'reversed() applies to the comparator built so far (salary), then thenComparing adds the name tie-break. Putting reversed() last would reverse the name order too.'),
    Q(M, 'Which statement about @FunctionalInterface is correct?',
      'It is optional; it makes the compiler reject the interface if it gets a second abstract method',
      ['It is required for a lambda to be assigned to the interface', 'It forbids default methods', 'It makes the interface thread-safe'],
      'Any interface with one abstract method can be the target of a lambda. The annotation just documents the intent and adds a compile-time check.'),
    Q(H, 'Comparator declares equals(Object) as an abstract method, yet it is a functional interface. Why?',
      'Methods that match public methods of Object do not count as abstract methods for this purpose',
      ['equals is a default method in Comparator', 'The compiler ignores the first abstract method', 'Comparator is a class, not an interface'],
      'Every class already inherits equals from Object, so an interface redeclaring it does not add a method an implementer must write.'),
    Q(M, 'How are lambdas compiled compared with anonymous classes?',
      'Lambdas use invokedynamic and do not produce a separate .class file for each lambda',
      ['Each lambda generates its own .class file, like an anonymous class', 'Lambdas are converted to inner enums at compile time', 'Lambdas are only evaluated at class-loading time'],
      'The compiler emits an invokedynamic call and the JVM creates the implementation at runtime, which avoids a class file per lambda.'),
])

TOPICS['Stream Basics'] = ('Stream pipelines, terminal and intermediate operations', [
    Q(E, 'Which of these is a terminal stream operation?', 'count()', ['filter()', 'map()', 'sorted()'],
      'Terminal operations such as count(), collect(), forEach() and findFirst() start the pipeline. filter, map and sorted are intermediate and lazy.'),
    Q(E, 'What happens if you call a terminal operation on a stream that has already been consumed?',
      'IllegalStateException is thrown', ['The stream restarts from the beginning', 'An empty result is returned', 'NullPointerException is thrown'],
      'A stream can be traversed only once. Create a new stream from the source if you need another pass.'),
    Q(M, 'What does this code print?', 'Nothing, because there is no terminal operation',
      ['Lowercase a, b, c on separate lines', 'Uppercase A, B, C on separate lines', 'It throws IllegalStateException'],
      'Intermediate operations are lazy. Without a terminal operation the pipeline never runs, so peek() prints nothing.',
      'Stream.of("a", "b", "c")\n    .peek(System.out::println)\n    .map(String::toUpperCase);'),
    Q(M, 'What does this expression return?', 'Optional[4]', ['Optional[2]', '4', 'Optional[56]'],
      'Elements flow one at a time: 1 fails the filter, 2 passes and becomes 4, and findFirst() stops the whole pipeline. The result is wrapped in an Optional.',
      'Stream.of(1, 2, 3, 4, 5, 6)\n    .filter(n -> n % 2 == 0)\n    .map(n -> n * n)\n    .findFirst()'),
    Q(M, 'Which pipeline returns the second-highest distinct salary?',
      'salaries.stream().distinct().sorted(Comparator.reverseOrder()).skip(1).findFirst()',
      ['salaries.stream().sorted(Comparator.reverseOrder()).skip(1).findFirst()',
       'salaries.stream().sorted().skip(1).findFirst()',
       'salaries.stream().max(Comparator.naturalOrder())'],
      'distinct() must come first. Without it, a duplicated top salary would be returned as the "second highest".'),
    Q(M, 'What does allMatch(...) return on an empty stream?', 'true', ['false', 'An empty Optional', 'It throws NoSuchElementException'],
      'allMatch is vacuously true when there is no element to violate the predicate. anyMatch is false and noneMatch is true on an empty stream.'),
    Q(M, 'What is n?', '1', ['3', '0', 'The code does not compile'],
      'Stream.of(int[]) treats the whole array as a single element (a Stream<int[]>). Use Arrays.stream(arr) or IntStream.of(arr) to stream the numbers.',
      'int[] arr = {1, 2, 3};\nlong n = Stream.of(arr).count();'),
    Q(M, 'What does IntStream.range(1, 5).sum() return?', '10', ['15', '14', '5'],
      'range excludes the end value, so it sums 1 + 2 + 3 + 4 = 10. rangeClosed(1, 5) would give 15.'),
    Q(E, 'What is the return type of Stream.count()?', 'long', ['int', 'Integer', 'Optional<Long>'],
      'count() returns a primitive long, so compare with 3L or cast when you need an int.'),
])

TOPICS['Collectors'] = ('groupingBy, partitioningBy, toMap, summarizing and more', [
    Q(E, 'What is the type of emps.stream().collect(Collectors.groupingBy(Employee::getDept, Collectors.counting()))?',
      'Map<String, Long>', ['Map<String, Integer>', 'Map<Employee, Long>', 'Map<String, List<Employee>>'],
      'groupingBy keys by the classifier result (the department name) and counting() produces a Long per group.'),
    Q(E, 'How does Collectors.partitioningBy differ from groupingBy?',
      'It always produces exactly two groups, keyed true and false',
      ['It sorts the elements before grouping', 'It returns a Set instead of a Map', 'It can only be used on numeric streams'],
      'partitioningBy takes a predicate and the resulting map always has both a true and a false key, even if one group is empty.'),
    Q(M, 'What happens when Collectors.toMap(keyFn, valueFn) meets a duplicate key?',
      'IllegalStateException is thrown unless a merge function is supplied',
      ['The last value silently wins', 'The first value silently wins', 'The entry is dropped'],
      'Use the three-argument form, for example toMap(k, v, Integer::sum), to say how duplicates are combined.'),
    Q(M, 'Which collector returns count, sum, min, average and max in a single pass?',
      'Collectors.summarizingInt (or summarizingDouble)', ['Collectors.counting', 'Collectors.reducing', 'Collectors.joining'],
      'summarizingInt/Long/Double return an IntSummaryStatistics-style object with getCount, getSum, getMin, getAverage and getMax.'),
    Q(M, 'How do you make groupingBy return its keys in sorted order?',
      'Pass TreeMap::new as the map factory: groupingBy(f, TreeMap::new, downstream)',
      ['Call sorted() before collect()', 'groupingBy always returns a sorted map', 'Use Collectors.toSortedMap()'],
      'The default result is a HashMap with no ordering guarantee. The three-argument groupingBy lets you choose TreeMap or LinkedHashMap.'),
    Q(M, 'What is the result type of Collectors.counting()?', 'Long', ['int', 'Integer', 'Optional<Long>'],
      'counting() produces a Long. That is why comparisons such as entry.getValue() > 1 work but equals(1) would not match a Long.'),
    Q(H, 'What happens if a value is null in Collectors.toMap(...)?', 'NullPointerException is thrown',
      ['The entry is stored with a null value', 'The entry is skipped', 'IllegalStateException is thrown'],
      'toMap is built on HashMap.merge, which rejects null values. Filter nulls first or use a different collector.'),
    Q(H, 'What does Collectors.teeing (Java 12) do?',
      'Runs two collectors over the same stream in one pass and merges their results',
      ['Splits a stream into two streams', 'Collects into two lists without merging', 'Runs the pipeline on two threads'],
      'For example teeing(minBy, maxBy, (min, max) -> ...) computes both in a single traversal.'),
    Q(M, 'Which statement about reduce and collect is correct?',
      'reduce combines elements into an immutable value; collect accumulates into a mutable container',
      ['collect is only for parallel streams', 'reduce always returns a List', 'They are identical'],
      'reduce is immutable reduction (for example summing). collect is mutable reduction (filling a list, map or StringBuilder).'),
])

TOPICS['flatMap & Laziness'] = ('flatMap, distinct, skip, limit and evaluation order', [
    Q(E, 'What is the difference between map and flatMap on a Stream?',
      'map is one-to-one; flatMap maps each element to a stream and flattens the results',
      ['flatMap is the lazy version of map', 'map works on collections and flatMap works on arrays', 'flatMap always returns a List'],
      'Use flatMap when each element produces zero or more results, such as the words in a sentence.'),
    Q(E, 'If nested is a List<List<Integer>>, what is the type of nested.stream().flatMap(List::stream)?',
      'Stream<Integer>', ['Stream<List<Integer>>', 'List<Integer>', 'Stream<Stream<Integer>>'],
      'flatMap replaces each inner list with its elements and concatenates them into one Stream<Integer>.'),
    Q(M, 'What is printed by this pipeline (no sorted)?', 'pa fA pb fB', ['pa pb fA fB', 'fA fB pa pb', 'fa pa fb pb'],
      'Elements travel through the whole pipeline one at a time, so the peek and forEach outputs interleave.',
      'Stream.of("a", "b")\n    .peek(s -> System.out.print("p" + s + " "))\n    .map(String::toUpperCase)\n    .forEach(s -> System.out.print("f" + s + " "));'),
    Q(M, 'What is printed by this pipeline (with sorted)?', 'pb pa fa fb', ['pb fb pa fa', 'pa pb fa fb', 'fa fb pb pa'],
      'sorted() is a stateful barrier: it must see every element before emitting the first one, so all peek output comes first.',
      'Stream.of("b", "a")\n    .peek(s -> System.out.print("p" + s + " "))\n    .sorted()\n    .forEach(s -> System.out.print("f" + s + " "));'),
    Q(M, 'What does IntStream.rangeClosed(1, 10).skip(3).limit(3) produce?', '4, 5, 6', ['3, 4, 5', '1, 2, 3', '4, 5, 6, 7'],
      'skip(3) drops 1, 2, 3 and limit(3) keeps the next three values. This is the pagination pattern: skip((page - 1) * size).limit(size).'),
    Q(M, 'Which two stream operations are stateful?', 'sorted and distinct', ['filter and map', 'peek and forEach', 'limit and findFirst'],
      'Stateful operations may need to remember or buffer elements. filter and map handle each element independently.'),
    Q(M, 'How do you find the third-highest DISTINCT number in a List<Integer>?',
      'nums.stream().distinct().sorted(Comparator.reverseOrder()).skip(2).findFirst()',
      ['nums.stream().sorted().skip(2).findFirst()', 'nums.stream().distinct().sorted().limit(3)', 'nums.stream().max(Comparator.naturalOrder()).skip(2)'],
      'Deduplicate, sort descending, skip the top two and take the next. The result is an Optional because the list may have fewer than three distinct values.'),
    Q(H, 'Why is the (long) cast used in skip((long) (page - 1) * size)?', 'To avoid int overflow when the page offset is large',
      ['Because skip only accepts negative numbers', 'To make the stream parallel', 'Because limit requires a long start index'],
      'skip() takes a long. Multiplying two ints first could overflow before the value is widened, so cast one operand to long.'),
])

TOPICS['String & Character Streams'] = ('chars(), frequency maps, palindromes, anagrams', [
    Q(E, 'What does "abc".chars() return?', 'An IntStream of char values', ['A Stream<Character>', 'A char[]', 'A Stream<String>'],
      'chars() returns an IntStream. Convert with mapToObj(c -> (char) c) when you need Character objects.'),
    Q(E, 'Why use a LinkedHashMap when finding the first non-repeating character with streams?',
      'It keeps first-seen order so the first entry with count 1 is the answer',
      ['It makes counting faster', 'HashMap cannot store Character keys', 'It sorts keys alphabetically'],
      'A HashMap has no ordering guarantee. LinkedHashMap preserves the order characters first appeared.'),
    Q(M, 'What is n when counting the letter s in "swiss"?', '3', ['2', '1', '5'],
      'chars() yields the code units of "swiss"; the letter s appears three times (s, w, i, s, s) and count() returns 3.',
      'long n = "swiss".chars().filter(c -> c == \'s\').count();'),
    Q(M, 'What is n when "a  b" (two spaces) is split on a single space?', '3', ['2', '1', '4'],
      'split(" ") on a string with two consecutive spaces produces an empty string between them: ["a", "", "b"]. Use split("\\\\s+") to avoid that.',
      'long n = Arrays.stream("a  b".split(" ")).count();'),
    Q(M, 'How can you check whether two strings are anagrams using streams?',
      'Sort the characters of both strings and compare the results',
      ['Compare their hash codes', 'Compare their lengths only', 'Use distinct() on both and compare the sizes'],
      'Anagrams have identical sorted character sequences: Arrays.equals(a.chars().sorted().toArray(), b.chars().sorted().toArray()).'),
    Q(M, 'Which characters of "programming" appear more than once?', 'g, m, r', ['p, o, a', 'r, o, g', 'm, i, n'],
      'In p-r-o-g-r-a-m-m-i-n-g the letters r, g and m each occur twice.'),
    Q(M, 'Which method correctly streams code points, including emoji outside the Basic Multilingual Plane?',
      'String.codePoints()', ['String.chars()', 'String.toCharArray()', 'String.getBytes()'],
      'chars() returns UTF-16 code units, so a supplementary character appears as two surrogate halves. codePoints() returns whole characters.'),
    Q(H, 'Why should you compare two Long objects, such as map values, with equals instead of ==?',
      '== compares references, and Long values outside the small cache range are different objects',
      ['== is not allowed for wrapper types', 'equals is faster', '== compares only the low 8 bits'],
      'Long.valueOf caches -128 to 127, so 100L == 100L can be true while 1000L == 1000L is false. A comparison like value == 1 against an int literal unboxes and is fine.'),
])

TOPICS['Optional & Method References'] = ('Optional API and the four kinds of method reference', [
    Q(E, 'What does Optional.of(null) do?', 'Throws NullPointerException', ['Returns an empty Optional', 'Returns Optional[null]', 'Returns null'],
      'of() requires a non-null value. Use ofNullable() when the value may be null.'),
    Q(E, 'What does Optional.ofNullable(null) return?', 'An empty Optional', ['NullPointerException', 'Optional[null]', 'null'],
      'ofNullable wraps a value that may be null: null becomes Optional.empty().'),
    Q(M, 'What is the difference between orElse(x) and orElseGet(supplier)?',
      'orElse always evaluates its argument; orElseGet calls the supplier only when the Optional is empty',
      ['orElseGet throws if the Optional is present', 'orElse is lazy; orElseGet is eager', 'They are identical'],
      'orElse(expensive()) runs expensive() even when a value is present. orElseGet(() -> expensive()) skips it.'),
    Q(M, 'user.getEmail() returns Optional<String>. Which call gives an Optional<String> without nesting?',
      'Optional.of(user).flatMap(User::getEmail)',
      ['Optional.of(user).map(User::getEmail)', 'Optional.of(user).filter(User::getEmail)', 'Optional.of(user).orElse(User::getEmail)'],
      'map would produce Optional<Optional<String>>. flatMap is for functions that already return an Optional.'),
    Q(M, 'What kind of method reference is String::toUpperCase?', 'Unbound instance method reference',
      ['Static method reference', 'Bound instance method reference', 'Constructor reference'],
      'The receiver is supplied later as the first argument: s -> s.toUpperCase().'),
    Q(M, 'What kind of method reference is System.out::println?', 'Bound instance method reference',
      ['Static method reference', 'Unbound instance method reference', 'Constructor reference'],
      'The receiver (System.out) is fixed when the reference is created: x -> System.out.println(x).'),
    Q(M, 'What kind of method reference is User::new?', 'Constructor reference',
      ['Static method reference', 'Unbound instance method reference', 'Bound instance method reference'],
      'User::new calls a constructor, for example name -> new User(name). ArrayList::new and String[]::new work the same way.'),
    Q(H, 'Why is Optional a poor choice for a class field or method parameter?',
      'It adds allocation and noise, is not Serializable, and does not stop callers passing null for the Optional itself',
      ['It is not allowed by the compiler', 'It makes the class immutable', 'It only works with primitive types'],
      'Optional is designed for return types that may have no value. For fields and parameters, plain nullable values or overloads are clearer.'),
    Q(E, 'What does calling get() on an empty Optional throw?', 'NoSuchElementException',
      ['NullPointerException', 'IllegalStateException', 'IllegalArgumentException'],
      'Prefer orElse, orElseGet, orElseThrow or ifPresent. Since Java 10, orElseThrow() with no argument is the clear replacement for get().'),
])

TOPICS['Default Methods & Date-Time API'] = ('Interface default/static methods and java.time', [
    Q(E, 'Why were default methods added to interfaces in Java 8?',
      'So interfaces can gain new methods without breaking existing implementations',
      ['To allow multiple inheritance of state', 'To replace abstract classes', 'To make interfaces instantiable'],
      'This is how Collection.stream(), List.sort() and Iterable.forEach were added without breaking every existing class.'),
    Q(M, 'Class Duck implements two interfaces that both define a default method move(). What must Duck do?',
      'Override move() and, if needed, call Walker.super.move() or Swimmer.super.move()',
      ['Nothing; the first interface wins', 'Nothing; the compiler picks the alphabetically first', 'Declare move() final in both interfaces'],
      'This is the diamond problem. The compiler reports an error until the class overrides the method and chooses explicitly.'),
    Q(M, 'Are static methods declared in an interface inherited by implementing classes?',
      'No; they must be called on the interface name', ['Yes, like instance methods', 'Yes, but only if the interface is public', 'Only when the class also overrides them'],
      'Static interface methods belong to the interface alone: Shape.square(3), not an instance or implementing class.'),
    Q(M, 'What does LocalDate.of(2026, 1, 31).plusMonths(1) return?', '2026-02-28', ['2026-03-03', '2026-02-31', 'It throws DateTimeException'],
      'When the day does not exist in the target month, java.time clamps to the last valid day of that month.'),
    Q(M, 'What is the difference between Period and Duration?',
      'Period is date-based (years, months, days); Duration is time-based (hours, minutes, seconds)',
      ['Period is for time zones; Duration is for leap years', 'They are the same', 'Period is mutable; Duration is immutable'],
      'Period.between takes two dates. Duration.between takes two times or instants.'),
    Q(M, 'In a DateTimeFormatter pattern, what does mm stand for?', 'Minutes (the month is MM)',
      ['Month (minutes is MM)', 'Milliseconds', 'Midnight marker'],
      'Lower-case mm is minutes and upper-case MM is the month number. MMM gives the month name.'),
    Q(M, 'What does ChronoUnit.DAYS.between(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1)) return?', '59', ['60', '58', '61'],
      'January has 31 days and February 2026 has 28 (2026 is not a leap year), so 31 + 28 = 59.'),
    Q(H, 'Why is java.time preferred over Date and SimpleDateFormat?',
      'java.time types are immutable and thread-safe, while SimpleDateFormat is not thread-safe',
      ['java.time is faster only on Linux', 'SimpleDateFormat cannot format dates', 'Date cannot store milliseconds'],
      'DateTimeFormatter and the java.time value types can be shared freely between threads. SimpleDateFormat has mutable internal state.'),
])

TOPICS['Java 9 & 10'] = ('List.of, takeWhile, private interface methods, var', [
    Q(E, 'What does List.of(1, 2, 3).add(4) do?', 'Throws UnsupportedOperationException',
      ['Adds 4 and returns true', 'Returns false', 'Throws IllegalArgumentException'], 'List.of returns an immutable list.'),
    Q(E, 'Where can var be used?', 'Local variables, including loop variables', ['Fields', 'Method parameters', 'Method return types'],
      'var is local-variable type inference. It does not make Java dynamically typed: the type is still fixed at compile time.'),
    Q(M, 'What does List.of(1, null) do?', 'Throws NullPointerException',
      ['Creates a list with a null element', 'Creates a list and ignores the null', 'Throws IllegalArgumentException'],
      'The List.of family rejects nulls. Arrays.asList(1, null) is allowed.'),
    Q(M, 'Which elements remain after takeWhile(n -> n < 5)?', '1, 2, 3', ['1, 2, 3, 1', '10', '1, 2, 3, 10'],
      'takeWhile stops at the first element that fails the predicate (10), so the trailing 1 is not included. filter would keep both 1s.',
      'Stream.of(1, 2, 3, 10, 1).takeWhile(n -> n < 5)'),
    Q(M, 'Which elements remain after dropWhile(n -> n < 5)?', '10, 1', ['1, 2, 3', '10', '1, 1'],
      'dropWhile discards elements while the predicate holds and then keeps everything that follows, including the trailing 1.',
      'Stream.of(1, 2, 3, 10, 1).dropWhile(n -> n < 5)'),
    Q(M, 'How does List.copyOf(list) differ from Collections.unmodifiableList(list)?',
      'copyOf makes an immutable snapshot; unmodifiableList is a read-only view that changes when the source changes',
      ['They are identical', 'copyOf returns a view; unmodifiableList returns a copy', 'copyOf allows null elements'],
      'Changing the original list shows through an unmodifiable view but never through a copyOf snapshot.'),
    Q(M, 'Which Java version added private methods in interfaces?', 'Java 9', ['Java 8', 'Java 10', 'Java 11'],
      'Private interface methods let default methods share code without exposing a helper.'),
    Q(H, 'What does Map.of("a", 1, "a", 2) do?', 'Throws IllegalArgumentException because of the duplicate key',
      ['Keeps the last value', 'Keeps the first value', 'Throws NullPointerException'],
      'Map.of and Set.of reject duplicates at creation time. Their iteration order is also unspecified.'),
])

TOPICS['Java 11 & 12'] = ('String methods, Files, teeing, single-file launch', [
    Q(E, 'What does " ".isBlank() return?', 'true', ['false', 'It throws NullPointerException', 'An empty Optional'],
      'isBlank() is true for empty or whitespace-only strings. isEmpty() is true only for length 0.'),
    Q(E, 'What does "ab".repeat(3) return?', '"ababab"', ['"ab3"', '"aabbab"', '"ab ab ab"'], 'repeat(n) concatenates the string n times.'),
    Q(M, 'How does String.strip() differ from trim()?',
      'strip() removes all Unicode whitespace; trim() removes only characters up to U+0020',
      ['They are identical', 'trim() is Unicode-aware; strip() is not', 'strip() only removes leading whitespace'],
      'Text copied from web pages often contains non-ASCII spaces that trim() leaves behind.'),
    Q(M, 'In which Java version was Collectors.teeing added?', 'Java 12', ['Java 8', 'Java 11', 'Java 17'],
      'teeing runs two collectors in one pass and combines their results.'),
    Q(M, 'What is Predicate.not(String::isBlank) used for?', 'Negating a method reference, for example keeping only non-blank strings in a filter',
      ['Creating a Predicate that always returns false', 'Converting a Predicate to a Function', 'Making a predicate asynchronous'],
      'It replaces s -> !s.isBlank() with a cleaner reference: lines.filter(Predicate.not(String::isBlank)).'),
    Q(M, 'Which charset do Files.readString(path) and Files.writeString(path, text) use by default?', 'UTF-8',
      ['The platform default charset', 'ISO-8859-1', 'US-ASCII'], 'Both methods default to UTF-8 and load the whole file into memory.'),
    Q(M, 'How can you run a single-file Java program in Java 11 without a separate javac step?', 'java Hello.java',
      ['javac -run Hello.java', 'java -single Hello', 'jrun Hello.java'], 'The source-file launcher compiles in memory and runs the file.'),
    Q(H, 'What does "a\\nb".indent(2) produce?', 'Both lines indented by two spaces, with a trailing newline added',
      ['Only the first line indented by two spaces', 'Two blank lines added before the text', 'The text unchanged; indent only works on text blocks'],
      'indent(n) adds n leading spaces to every line, normalizes line endings and ends the result with a newline.'),
])

TOPICS['Java 14 to 16'] = ('Switch expressions, text blocks, records, pattern instanceof', [
    Q(E, 'What is the accessor for a record component named name?', 'name()', ['getName()', 'component()', 'name'],
      'Record accessors use the component name, not JavaBean-style getters.'),
    Q(E, 'In a switch expression that uses ->, what happens after a case matches?', 'No fall-through; only that case runs',
      ['Execution falls through to the next case', 'The switch exits only if break is used', 'All remaining cases run'],
      'Arrow cases never fall through and the switch expression produces a value.'),
    Q(M, 'What is r?', '15', ['10', '5', '0'], 'case 3 runs the block and yield returns t * 3 = 15.',
      'int r = switch (3) {\n    case 1, 2 -> 10;\n    case 3 -> { int t = 5; yield t * 3; }\n    default -> 0;\n};'),
    Q(M, 'Can a record extend another class?', 'No; records are implicitly final and extend java.lang.Record, but they can implement interfaces',
      ['Yes, any non-final class', 'Yes, but only abstract classes', 'Only if it is declared sealed'],
      'A record is a transparent immutable data carrier, so it cannot have a superclass other than Record.'),
    Q(M, 'What does Stream.toList() (Java 16) return?', 'An unmodifiable list', ['A mutable ArrayList', 'A LinkedList', 'A Set'],
      'Unlike Collectors.toList(), toList() guarantees an unmodifiable list and allows null elements.'),
    Q(M, 'In a text block, what determines how much indentation is stripped?',
      'The least-indented content line, including the closing delimiter', ['Always four spaces', 'Only the first line', 'Nothing is stripped'],
      'Moving the closing triple quotes left or right changes the indentation of the whole block.'),
    Q(M, 'What does if (o instanceof String s && !s.isEmpty()) give you?',
      'A pattern variable s, already cast to String, usable in the rest of the condition and the true branch',
      ['A compile error; the cast must be explicit', 'A variable s of type Object', 'A String shared across threads'],
      'Pattern matching for instanceof (final in Java 16) removes the separate cast.'),
    Q(H, 'Why is record Person(List<String> tags) only shallowly immutable?',
      'The reference to the list is final, but the list itself can still be modified',
      ['Records cannot hold collections', 'Record fields are not final', 'equals() compares references'],
      'Make a defensive copy (List.copyOf) in a compact constructor if you need deep immutability.'),
])

TOPICS['Java 17: Sealed Classes'] = ('Sealed classes, permits, non-sealed, Java 17 changes', [
    Q(E, 'What does the permits clause of a sealed class do?', 'Lists the only classes allowed to extend or implement it directly',
      ['Lists the classes it is forbidden to extend', 'Makes the class abstract', 'Grants file access'],
      'A sealed hierarchy is closed: the compiler and readers know every direct subtype.'),
    Q(E, 'Which modifiers can a direct subclass of a sealed class have?', 'final, sealed or non-sealed',
      ['static, final or abstract', 'public, private or protected', 'default, final or open'],
      'Records and enums are implicitly final, so they satisfy the rule automatically.'),
    Q(M, 'When can the permits clause be omitted?', 'When the permitted subclasses are in the same source file',
      ['Never', 'When the class is abstract', 'When the subclasses are in another module'],
      'The compiler infers the permitted subclasses declared in the same compilation unit.'),
    Q(M, 'What common effect does Java 17\'s strong encapsulation of JDK internals have on old applications?',
      'Reflective access to JDK internals fails unless opened with --add-opens',
      ['Applications run faster without changes', 'All reflection is removed', 'Serialization stops working'],
      'This is why upgrading from Java 8 or 11 to 17 often exposes libraries that rely on illegal reflective access.'),
    Q(M, 'What was the status of pattern matching for switch in Java 17?', 'Preview feature (finalized in Java 21)',
      ['Final feature', 'Removed', 'Incubator module'], 'Java 17 finalized sealed classes, but switch patterns stayed in preview until Java 21.'),
    Q(M, 'Which method returns the permitted subclasses of a sealed type at runtime?', 'Class.getPermittedSubclasses()',
      ['Class.getSubclasses()', 'Class.getSealedTypes()', 'Class.permits()'], 'Class.isSealed() tells you whether a type is sealed.'),
    Q(M, 'What does marking a permitted subclass non-sealed do?', 'Reopens the hierarchy below it, so anyone may extend that subclass',
      ['Makes it final', 'Makes it immutable', 'Makes it abstract'], 'Use it deliberately where you want extension beyond the closed set.'),
    Q(H, 'Which list contains only long-term support (LTS) releases?', '8, 11, 17 and 21',
      ['9, 10, 12 and 13', '8, 10, 14 and 18', '11, 13, 15 and 17'], 'LTS releases get years of updates. Java 25 is the latest LTS after 21.'),
])

TOPICS['Java 21'] = ('Virtual threads, pattern switch, record patterns, sequenced collections', [
    Q(E, 'What is the status of virtual threads in Java 21?', 'Final (standard) feature', ['Preview', 'Incubator', 'Removed'],
      'Virtual threads were previewed in Java 19 and 20 and finalized in Java 21.'),
    Q(E, 'Which method returns the last element of a List in Java 21?', 'getLast()', ['last()', 'tail()', 'peekLast()'],
      'List now implements SequencedCollection, which adds getFirst(), getLast(), reversed() and more.'),
    Q(M, 'Should you pool virtual threads in a fixed-size thread pool?', 'No; create one per task because they are cheap',
      ['Yes, always cap them at the number of CPU cores', 'Yes, otherwise they leak', 'Only on Windows'],
      'Use Executors.newVirtualThreadPerTaskExecutor() and limit concurrency with a Semaphore if needed.'),
    Q(M, 'Virtual threads help most with:', 'Many concurrent tasks that spend their time blocked on I/O',
      ['CPU-bound number crunching', 'Reducing the heap usage of a single task', 'Replacing garbage collection'],
      'A blocked virtual thread unmounts from its carrier thread, letting the carrier run other work.'),
    Q(M, 'What is this syntax called?', 'A record pattern', ['A lambda expression', 'A text block', 'A sealed permit'],
      'Record patterns deconstruct a record in switch or instanceof and were finalized in Java 21.',
      'case Circle(double r) -> Math.PI * r * r;'),
    Q(M, 'In a pattern switch, where must case Integer i when i > 10 appear relative to case Integer i?',
      'Before it; otherwise the guarded case is dominated and does not compile', ['After it', 'It does not matter', 'In a separate switch'],
      'The compiler rejects a case that an earlier, more general case would always catch.'),
    Q(M, 'In Java 21, what is thread pinning?',
      'A virtual thread blocked inside a synchronized block stays attached to its carrier thread, reducing scalability',
      ['Assigning a thread to a CPU core', 'Setting thread priority to maximum', 'Pausing the JVM for garbage collection'],
      'Prefer ReentrantLock over synchronized around blocking calls when using virtual threads on Java 21.'),
    Q(H, 'What was the status of string templates in Java 21?', 'Preview feature',
      ['Final feature', 'Removed in 21', 'Incubator module'], 'Do not rely on preview features in production code.'),
])

TOPICS['CompletableFuture & Parallel Streams'] = ('Async pipelines and parallel streams', [
    Q(E, 'Which CompletableFuture method is the equivalent of Stream.map?', 'thenApply', ['thenCompose', 'thenAccept', 'allOf'],
      'thenApply transforms the result. thenCompose is the flatMap equivalent.'),
    Q(E, 'How do join() and get() differ?',
      'join() throws the unchecked CompletionException; get() throws the checked ExecutionException and InterruptedException',
      ['join() waits; get() does not', 'get() is only for completed futures', 'They are identical'],
      'join() is convenient inside lambdas because it needs no try/catch.'),
    Q(M, 'When should you use thenCompose instead of thenApply?', 'When the next step itself returns a CompletableFuture',
      ['When the next step returns void', 'When running on a custom executor', 'When the first future failed'],
      'thenApply would give CompletableFuture<CompletableFuture<T>>. thenCompose flattens it.'),
    Q(M, 'Which pool does supplyAsync(supplier) use by default?', 'ForkJoinPool.commonPool()',
      ['A new thread per call', 'The main thread', 'A fixed pool of 10 threads'], 'Pass your own Executor for blocking or I/O-heavy tasks.'),
    Q(M, 'What does CompletableFuture.allOf(f1, f2) return?', 'CompletableFuture<Void>; you read the values from f1 and f2 yourself',
      ['CompletableFuture<List<T>> of all results', 'The first completed result', 'An array of results'],
      'Call join() on allOf to wait, then join() each individual future for its value.'),
    Q(M, 'An exception is thrown inside supplyAsync. What does handle((v, ex) -> ...) receive as ex?',
      'A CompletionException wrapping the original exception', ['The original exception unwrapped', 'null', 'An ExecutionException'],
      'Use ex.getCause() to reach the original exception. This was verified by running the code on a JDK.'),
    Q(M, 'What does parallelStream().forEach(...) guarantee about order?', 'No order guarantee; use forEachOrdered if order matters',
      ['Encounter order is always preserved', 'Elements arrive in reverse order', 'Elements arrive sorted'],
      'collect(toList()) preserves encounter order, but forEach does not on a parallel stream.'),
    Q(H, 'When is parallelStream() a bad choice?', 'Small data, blocking I/O inside the lambda, or shared mutable state',
      ['Large CPU-bound stateless work', 'Summing a big range of numbers', 'Splittable sources such as arrays'],
      'Parallel streams use the common pool, so blocking tasks can starve unrelated work.'),
])

for _t, (_d, _qs) in TOPICS.items():
    _texts = [x['q'] for x in _qs]
    assert len(_texts) == len(set(_texts)), 'duplicate question text in topic: ' + _t

SUBJECTS = [
    ('Java 8 Essentials', 'Lambdas, streams, collectors, Optional, default methods and java.time',
     ['Lambda & Functional Interfaces', 'Stream Basics', 'Collectors', 'flatMap & Laziness', 'String & Character Streams',
      'Optional & Method References', 'Default Methods & Date-Time API']),
    ('Modern Java (9 to 21)', 'Features added from Java 9 to the Java 21 LTS release',
     ['Java 9 & 10', 'Java 11 & 12', 'Java 14 to 16', 'Java 17: Sealed Classes', 'Java 21']),
    ('Concurrency (Java 8+)', 'CompletableFuture and parallel streams',
     ['CompletableFuture & Parallel Streams']),
]

data = {
    'category': {'code': 'JAVA_INTERVIEW', 'name': 'Java Interview Prep',
                 'description': 'Java 8 to 21 interview preparation: concepts, output-prediction questions and mock tests'},
    'subjects': [{'name': n, 'description': d,
                  'topics': [{'name': t, 'description': TOPICS[t][0], 'questions': TOPICS[t][1]} for t in ts]}
                 for n, d, ts in SUBJECTS],
    'exams': [
        {'title': 'Java 8 Mock Test', 'duration_minutes': 30, 'navigation_mode': 'FREE', 'negative_marking': 0,
         'sections': [{'name': t, 'topics': [t], 'count': 3} for t in SUBJECTS[0][2]]},
        {'title': 'Modern Java (9 to 21) Mock Test', 'duration_minutes': 25, 'navigation_mode': 'FREE', 'negative_marking': 0,
         'sections': [{'name': t, 'topics': [t], 'count': 3} for t in SUBJECTS[1][2]]},
        {'title': 'Java Full Mock Test', 'duration_minutes': 25, 'navigation_mode': 'FREE', 'negative_marking': 0,
         'sections': [{'name': 'Java 8', 'topics': SUBJECTS[0][2], 'count': 8},
                      {'name': 'Modern Java', 'topics': SUBJECTS[1][2], 'count': 6},
                      {'name': 'Concurrency', 'topics': SUBJECTS[2][2], 'count': 2}]},
    ],
}

out = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'java_interview.json')
with open(out, 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=1, ensure_ascii=False)
total = sum(len(t['questions']) for s in data['subjects'] for t in s['topics'])
print('wrote', out, '-', total, 'questions in', sum(len(s['topics']) for s in data['subjects']), 'topics')
