# Crayfish Common Expectation

[![Maven Central](https://img.shields.io/maven-central/v/com.github.sftwnd.crayfish.common/crayfish-common-expectation)](https://central.sonatype.com/artifact/com.github.sftwnd.crayfish.common/crayfish-common-expectation)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-blue)](#java-modules)
[![JPMS](https://img.shields.io/badge/JPMS-named%20module-blue)](#java-modules)
[![OSGi](https://img.shields.io/badge/OSGi-bundle-blue)](#osgi)
[![javadoc](https://javadoc.io/badge2/com.github.sftwnd.crayfish.common/crayfish-common-expectation/javadoc.svg)](https://javadoc.io/doc/com.github.sftwnd.crayfish.common/crayfish-common-expectation)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sftwnd_crayfish_common_expectation&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sftwnd_crayfish_common_expectation)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sftwnd_crayfish_common_expectation&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sftwnd_crayfish_common_expectation)
[![Build](https://github.com/sftwnd/crayfish-common-expectation/actions/workflows/build.yml/badge.svg)](https://github.com/sftwnd/crayfish-common-expectation/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/sftwnd/crayfish-common-expectation)](https://github.com/sftwnd/crayfish-common-expectation/blob/master/LICENSE)

**Zero-dependency time markers for Java 8+: objects that know the moment they are due, and packages that attach such
a moment to any element.**

An `Expected` has a tick, a point in time of any `java.time` type that converts to an `Instant` (`Instant`,
`ZonedDateTime`, `OffsetDateTime`, …), and knows whether it has happened. An `ExpectedPackage` attaches a tick to an
element: a given one, one of a supplier or one extracted from the element, taken once, at the packaging.

## Features

- **Any `java.time` moment** — ticks of different types are compared as instants: an `Instant` and a `ZonedDateTime`
  of the same moment are equal
- **Happened or not** — `happened()` against the current time, `happened(moment)` against any moment
- **Ordering** — `Expected.comparator(...)` orders by the tick, then by the given comparator; without one, objects of
  the same tick by the natural order of their class if it is comparable with itself, and otherwise by an arbitrary
  order that does not change while they live: a total order, and a sorted set keeps every distinct object of a tick;
  `null` first
- **Packages** — `ExpectedPackage.pack`, `supply` and `extract` attach a tick to an element without a class of
  its own: immutable, equal if their elements and ticks are equal; a null or a tick without an instant
  (`LocalDateTime`, `LocalDate`, …) is rejected at the packaging
- **Functional interfaces** — `Expected`, `TemporalSupplier` and `TemporalExtractor` are lambdas
- **Zero runtime dependencies** — the SpotBugs annotations of the API are optional, needed neither to compile against
  the library nor to run it
- **Java 8+, JPMS, OSGi** — Java 8 bytecode, a named module on Java 9+ and an OSGi bundle, see
  [Java modules](#java-modules) and [OSGi](#osgi)

## Installation

Maven:

```xml
<dependency>
    <groupId>com.github.sftwnd.crayfish.common</groupId>
    <artifactId>crayfish-common-expectation</artifactId>
    <version>1.4.0</version>
</dependency>
```

Gradle:

```groovy
implementation 'com.github.sftwnd.crayfish.common:crayfish-common-expectation:1.4.0'
```

1.3.x requires Java 11; 1.4.0 runs on Java 8 again, as 1.2.0 and earlier did, and is the same named module on Java 9+
as 1.3.x.

## Quick start

```java
Instant now = Instant.now();
// a tick given, a tick of a supplier and a tick extracted from the element: each is taken once, at the packaging
ExpectedPackage<String, Instant> report = ExpectedPackage.pack("report", now.plus(Duration.ofHours(1)));
ExpectedPackage<String, ZonedDateTime> backup = ExpectedPackage.supply("backup",
        () -> ZonedDateTime.now(ZoneId.of("Europe/Moscow")).minusMinutes(5));
ExpectedPackage<Order, Instant> order = ExpectedPackage.extract(new Order("42", now.plusSeconds(30)), Order::deadline);

report.happened();                                   // false: in an hour
backup.happened();                                   // true: five minutes ago
report.happened(now.plus(Duration.ofHours(2)));      // true: by then

// the earliest tick first; ticks of different types are compared as instants
Queue<ExpectedPackage<?, ?>> queue = new PriorityQueue<>(Expected.comparator(null));
queue.add(report);
queue.add(backup);
queue.add(order);
while (!queue.isEmpty() && queue.peek().happened()) {
    System.out.println("due: " + queue.poll().getElement());   // due: backup
}
System.out.println("next: " + queue.peek().getElement());      // next: Order 42
```

`Order` stands for any class of an application, here with a method `Instant deadline()`.

## Expected

An object with a tick, not null; a functional interface:

```java
@FunctionalInterface
public interface Expected<T extends TemporalAccessor> {

    @NonNull T getTick();

    // whether the tick is not after the moment; null is the current time
    default boolean happened(@Nullable TemporalAccessor moment);

    // whether the tick is not after the current time
    default boolean happened();

    // the order by the ticks, see below
    static <M extends Expected<? extends TemporalAccessor>> Comparator<M> comparator(@Nullable Comparator<? super M> objectComparator);

    // two moments compared as instants
    static int compareTemporalAccessor(@NonNull TemporalAccessor first, @NonNull TemporalAccessor second);
}
```

A tick without an instant (`LocalDateTime`, `LocalDate`, …) makes `happened` and the comparisons throw a
`DateTimeException`.

`Expected.comparator(objectComparator)` orders by the ticks, the earliest first, and puts `null` before every object.
The objects of the same tick are ordered by `objectComparator`, and its result is final; without it:

- objects of the same class that is comparable with itself (it implements `Comparable` of the class or of a supertype of
  it, itself or by a superclass or an interface): by their natural order;
- other objects of the same class: by an arbitrary order of the objects;
- objects of different classes: by an arbitrary order of their classes.

An arbitrary order does not change while the objects live, and in it an object is equal only to itself: the comparator
is a total order, and a `TreeSet` or a `PriorityQueue` keeps every distinct object of a tick, unless the natural order of
its class makes two of them equal.

## ExpectedPackage

An element with a tick; an `ExpectedPackage` is an `Expected`:

```java
public interface ExpectedPackage<M, T extends TemporalAccessor> extends Expected<T> {

    @NonNull M getElement();

    // a package of an element and a tick
    static <M, T extends TemporalAccessor> ExpectedPackage<M, T> pack(@NonNull M element, @NonNull T tick);

    // a package of an element and the tick of a supplier, called once, at the packaging
    static <M, T extends TemporalAccessor> ExpectedPackage<M, T> supply(@NonNull M element, @NonNull TemporalSupplier<T> temporalSupplier);

    // a package of an element and the tick that an extractor gives for it, once, at the packaging
    static <M, T extends TemporalAccessor> ExpectedPackage<M, T> extract(@NonNull M element, @NonNull TemporalExtractor<M, T> extractor);

    // ExpectedPackage(<tick>,<element>), the tick as an Instant if it has one; "null" for a null package
    static String toString(@Nullable ExpectedPackage<?, ?> expectedPackage);
}
```

The packages of `pack`, `supply` and `extract` are immutable. At the packaging they reject a null element, supplier,
extractor or tick with a `NullPointerException` that names the method and the argument, and a tick without an instant
with a `DateTimeException`. Two of them are equal if their elements and their ticks are equal (by `equals` of the ticks:
an `Instant` and a `ZonedDateTime` of the same moment are different ticks). Their `toString()` is
`ExpectedPackage(<instant>,<element>)` and does not throw.

## TemporalSupplier and TemporalExtractor

The functional interfaces of the ticks of `supply` and `extract`, which give ticks that are not null:

```java
@FunctionalInterface
public interface TemporalSupplier<T extends TemporalAccessor> extends Supplier<T> {
    @Override @NonNull T get();
}

@FunctionalInterface
public interface TemporalExtractor<E, T extends TemporalAccessor> extends Function<E, T> {
    @Override @NonNull T apply(@NonNull E element);
}
```

## Java modules

The library runs on Java 8+: the classes are Java 8 bytecode. On Java 9+ it is the named module
`com.github.sftwnd.crayfish_common_expectation`, which exports the package `com.github.sftwnd.crayfish.common.expectation`:

```java
module my.application {
    requires com.github.sftwnd.crayfish_common_expectation;
}
```

The module descriptor is in `META-INF/versions/9` of the multi-release jar, so Java 8 ignores it. The sources jar has
its source at the same place. The package is exported, not open: the implementation classes of the packages and of the
order are not public, and a framework that needs deep reflection on them needs `--add-opens`.

## OSGi

The jar is an OSGi bundle with the symbolic name `com.github.sftwnd.crayfish.common.expectation`. It exports the
package `com.github.sftwnd.crayfish.common.expectation` with the version of the release, imports no package (the
SpotBugs annotations of the API are not needed at run time) and requires Java 8 (`osgi.ee` JavaSE 1.8):

```
Bundle-SymbolicName: com.github.sftwnd.crayfish.common.expectation
Export-Package: com.github.sftwnd.crayfish.common.expectation;version="1.4.0"
Require-Capability: osgi.ee;filter:="(&(osgi.ee=JavaSE)(version=1.8))"
```

The build checks the bundle in Apache Felix on Java 8 and Java 21: the framework resolves it, and a bundle that imports
the package runs.

## Building

The project builds with the Maven Wrapper (Maven 3.10.0) on JDK 8 to 25:

```shell
./mvnw verify
```

The classes are compiled for Java 8 on every JDK; the module descriptor is built on JDK 9+ only, so a release has to be
built on JDK 9+. On JDK 11+ the build runs SpotBugs, and a finding fails it. The tests have to cover at least 90% of the
instructions and of the branches of the classes: JaCoCo fails the build otherwise.

Two builds of the same sources with the same version of the JDK give the same jars byte for byte:
`project.build.outputTimestamp` gives their entries a fixed time, which `release:prepare` sets to the time of the
release. Another version of the JDK compiles slightly different classes and JavaDoc and writes its version into the
manifest (`Build-Jdk-Spec`), so a release is reproduced with the JDK version it was built with.

---

Copyright © 2017-2026 Andrey D. Shindarev. All rights reserved.
