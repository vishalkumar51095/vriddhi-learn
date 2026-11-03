---
slug: java-day-01-lambda
title: Lambdas and Functional Interfaces
category: JAVA_INTERVIEW
subject: Java 8 Essentials
topic: Lambda & Functional Interfaces
topicId: 5a078db8-068c-5f3e-b27d-61a41ab65a7c
order: 1
---

## 1. Why lambdas?

A lambda is a method without a name. It removes the ceremony around passing behaviour.

```
BEFORE (Java 7)                              AFTER (Java 8)
┌────────────────────────────────┐           ┌─────────────────────────────┐
│ new Comparator<String>() {     │           │                             │
│   public int compare(          │   ───►    │ (a, b) -> a.compareTo(b)    │
│     String a, String b) {      │           │                             │
│     return a.compareTo(b);     │           └─────────────────────────────┘
│   }                            │
│ }                              │           5 lines of noise → 1 line of intent
└────────────────────────────────┘
```

## 2. Anatomy of a lambda

```
     (a, b)     ->     a + b
       │         │       │
   parameters  arrow    body
```

Shortcuts:

```
 (int a, int b) -> { return a + b; }     full form
 (a, b)         -> { return a + b; }     types inferred
 (a, b)         -> a + b                 one expression: no braces, no return
 a              -> a * 2                 one parameter: no parentheses
 ()             -> System.out.println()  no parameters: empty parentheses
```

## 3. A lambda needs a functional interface

A **functional interface** has exactly **one abstract method**. The lambda becomes its implementation.

```
 interface Validator<T> {                    Validator<Integer> isAdult =
     boolean validate(T value);   ◄────────      age -> age >= 18;
 }                                                │
        ▲                                         └── this IS the validate() body
        │
  one abstract method (default and static methods don't count)
```

`@FunctionalInterface` is optional. It makes the compiler reject the interface if a second abstract method is added.

## 4. The four built-in interfaces you must know

```
 ┌────────────┬───────────────────┬────────────────────────────┐
 │ Interface  │ Method            │ Think of it as             │
 ├────────────┼───────────────────┼────────────────────────────┤
 │ Predicate  │ T  ──► boolean    │ FILTER (yes/no test)       │
 │ Function   │ T  ──► R          │ CONVERTER (map)            │
 │ Consumer   │ T  ──► void       │ ACTION (print, save)       │
 │ Supplier   │ ()  ──► T         │ FACTORY (gives a value)    │
 └────────────┴───────────────────┴────────────────────────────┘
 Also: BiFunction (2 inputs), UnaryOperator<T> (T→T), BinaryOperator<T> ((T,T)→T)
```

## 5. Worked examples

### Example 1: Predicate (filter)

```java
Predicate<Integer> isEven = n -> n % 2 == 0;
isEven.test(4);   // true
isEven.test(7);   // false
```

### Example 2: Function (convert)

```java
Function<String, Integer> length = s -> s.length();
length.apply("Infosys");   // 7
```

### Example 3: Consumer and Supplier

```java
Consumer<String> printer = s -> System.out.println("Hello, " + s);
printer.accept("Ravi");                       // prints: Hello, Ravi

Supplier<Double> randomValue = () -> Math.random();
randomValue.get();                            // some number between 0 and 1
```

### Example 4: Comparator chain (very common in interviews)

Sort by salary high-to-low, and break ties by name A-Z.

```
 Input:   Ravi 50000 | Anu 60000 | Sita 50000 | Amit 60000

 Step 1: sort by salary DESC     →  Anu 60000, Amit 60000, Ravi 50000, Sita 50000
 Step 2: ties broken by name ASC →  Amit 60000, Anu 60000, Ravi 50000, Sita 50000
```

```java
emps.sort(Comparator.comparingDouble(Employee::getSalary).reversed()
                    .thenComparing(Employee::getName));
```

Pitfall: `Comparator.comparing(e -> e.getSalary()).reversed()` often fails to compile because the type of `e` cannot be inferred. Use a method reference or `comparingDouble(Employee::getSalary)`.

### Example 5: Combining predicates

```java
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<String> longName = s -> s.length() > 4;

notEmpty.and(longName).test("Vishal");   // true
notEmpty.and(longName).test("Anu");      // false
longName.negate().test("Anu");           // true
longName.or(notEmpty).test("A");         // true
```

```
        "Vishal" ──► notEmpty ──► true ─┐
                                        ├─ and ──► true
        "Vishal" ──► longName ──► true ─┘
```

## 6. Interview questions

**Q1. What is a functional interface?**
An interface with exactly one abstract method. It can still have default and static methods.

**Q2. Lambda vs anonymous class: what are the differences?**

```
 ┌───────────────────────┬──────────────────────────┬───────────────────────────┐
 │                       │ Anonymous class          │ Lambda                    │
 ├───────────────────────┼──────────────────────────┼───────────────────────────┤
 │ `this` refers to      │ the anonymous object     │ the enclosing class       │
 │ Works with            │ any interface or class   │ functional interfaces only│
 │ Compiled form         │ separate .class file     │ invokedynamic             │
 │ Can have its own state│ yes (fields)             │ no                        │
 └───────────────────────┴──────────────────────────┴───────────────────────────┘
```

**Q3. Why does this not compile?**

```java
int count = 0;
list.forEach(x -> count++);     // ERROR
```

A local variable used inside a lambda must be **final or effectively final** (never reassigned).

```
   local variable  count ─────X────► lambda (cannot change it)
   instance field / array element ──► lambda (allowed, but be careful with threads)
```

**Q4. Can a functional interface extend another interface?**
Yes, as long as the total number of abstract methods stays at one.

**Q5. Is `Comparator` a functional interface even though it has `equals()`?**
Yes. Methods that match `Object` public methods do not count as abstract methods.

## 8. Self-check (answer aloud)

1. What makes an interface "functional"?
2. Which built-in interface would you use to filter, to convert, to print, to create a value?
3. What does `this` mean inside a lambda?
4. Why can't you modify a local variable inside a lambda?
5. Write a comparator for "salary descending, then name ascending" from memory.
