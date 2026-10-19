# Crayfish Common Expectation

[![Maven Central](https://img.shields.io/maven-central/v/com.github.sftwnd.crayfish.common/crayfish-common-expectation)](https://central.sonatype.com/artifact/com.github.sftwnd.crayfish.common/crayfish-common-expectation)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-blue)](#java-modules)
[![JPMS](https://img.shields.io/badge/JPMS-named%20module-blue)](#java-modules)
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
- **Happened or not** — `happened()` against the current time, `happened(tick)` against any moment
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
- **Java 8+, JPMS** — Java 8 bytecode, a named module on Java 9+, see [Java modules](#java-modules)

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

## Expected

An object with a non-null time marker:

```java
public interface Expected<T extends TemporalAccessor> {
    @NonNull T getTick();
    default boolean happened(TemporalAccessor tick)...
```

## ExpectedPackage

Links an element and a time marker; an `ExpectedPackage` is an `Expected`:

```java
public interface ExpectedPackage<M,T extends TemporalAccessor> extends Expected<T> {
    @NonNull M getElement(); // Packaged element
    ...
```

A package of an element with a time marker:

```java
    @NonNull
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> pack(@NonNull M element, @NonNull T tick)
    ...
```

A package of an element with a time marker from a supplier, called once, at the packaging:

```java
    @NonNull
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> supply(@NonNull M element, @NonNull TemporalSupplier<T> temporalSupplier)
    ...
```

A package of an element with a time marker extracted from the element, once, at the packaging:

```java
    @NonNull
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> extract(@NonNull M element, @NonNull TemporalExtractor<M,T> extractor)
    ...
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

## Building

The project builds with the Maven Wrapper (Maven 3.10.0) on JDK 8 to 25:

```shell
./mvnw verify
```

The classes are compiled for Java 8 on every JDK; the module descriptor is built on JDK 9+ only, so a release has to be
built on JDK 9+. On JDK 11+ the build runs SpotBugs, and a finding fails it.

Two builds of the same sources give the same jars byte for byte: `project.build.outputTimestamp` gives their entries a
fixed time, which `release:prepare` sets to the time of the release.

---

Copyright © 2017-2026 Andrey D. Shindarev. All rights reserved.
