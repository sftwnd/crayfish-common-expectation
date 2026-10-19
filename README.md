[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sftwnd_crayfish_common_expectation&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sftwnd_crayfish_common_expectation) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sftwnd_crayfish_common_expectation&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sftwnd_crayfish_common_expectation) [![build](https://github.com/sftwnd/crayfish-common-expectation/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/sftwnd/crayfish-common-expectation/actions/workflows/build.yml) [![License](https://img.shields.io/github/license/sftwnd/crayfish-common-expectation)](https://github.com/sftwnd/crayfish-common-expectation/blob/master/LICENSE)

# Crayfish Common Expectation

Expectation object for time-based marker creation &amp; packaging with it

## Java versions

The library runs on Java 8 and later: the classes are Java 8 bytecode. On Java 9+ the jar is the named module
`com.github.sftwnd.crayfish_common_expectation` (the module descriptor is in `META-INF/versions/9` of a multi-release
jar), which exports `com.github.sftwnd.crayfish.common.expectation`:

```java
module my.application {
    requires com.github.sftwnd.crayfish_common_expectation;
}
```

The build and the tests run on JDK 8, 11, 17, 21 and 25.

## Expected Interface

Expected Interface realize object nonnull time-marker

```java
public interface Expected<T extends TemporalAccessor> {
    @NonNull T getTick();
    default boolean happened(TemporalAccessor tick)...
```

## Class ExpectedPackage

Used to link object and time-marker. Implements Expected Interface

```java
public interface ExpectedPackage<M,T extends TemporalAccessor> extends Expected<T> {
    @NonNull M getElement(); // Packaged element
    ...
```

Construct package from element with time marker

```java
    @NonNull
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> pack(@NonNull M element, @NonNull T tick)
    ...
```

Construct package from element with time marker from time-marker supplier

```java
   static <M,T extends TemporalAccessor> ExpectedPackage<M,T> supply(@NonNull M element, @NonNull TimeSupplier<T> tick)
   ...
```

Construct package from element with time marker extracted from element

```java
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> extract(@NonNull M element, @NonNull TimeExtractor<M,T> extractor) {
    ...
```

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
Copyright © 2017-2023 Andrey D. Shindarev. All rights reserved.
