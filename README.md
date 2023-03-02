[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sftwnd_crayfish_common_expectation&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sftwnd_crayfish_common_expectation) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sftwnd_crayfish_common_expectation&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sftwnd_crayfish_common_expectation) ![CircleCI](https://img.shields.io/circleci/build/github/sftwnd/crayfish-common-expectation) [![License](https://img.shields.io/github/license/sftwnd/crayfish-common-expectation)](https://github.com/sftwnd/crayfish-common-expectation/blob/master/LICENSE)
# Crayfish Common Expectation

Expectation object for time-based marker creation &amp; packaging with it

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

---
Copyright © 2017-2023 Andrey D. Shindarev. All rights reserved.
