[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sftwnd_crayfish_common_expectation&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sftwnd_crayfish_common_expectation) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sftwnd_crayfish_common_expectation&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sftwnd_crayfish_common_expectation) ![CircleCI](https://img.shields.io/circleci/build/github/sftwnd/crayfish-common-expectation) [![License](https://img.shields.io/github/license/sftwnd/crayfish-common-expectation)](https://github.com/sftwnd/crayfish-common-expectation/blob/master/LICENSE)
# Crayfish Common Expectation
Expectation object for time-based marker creation &amp; packaging with it

## Expected Interface
Expected Interface realize object nonnull time-marker
```java
public interface Expected<T extends TemporalAccessor> {
    @Nonnull T getTick();
    default boolean happened(TemporalAccessor tick)...
```
## Expectation Interface
Extraction of nonnull time-marker from nonnull element
```java
public interface Expectation<M, T extends TemporalAccessor> extends TimeExtractor<M,T> {
@Nonnull T apply(@Nonnull M element);
...
```
## Class ExpectedPackage
Used to link object and time-marker. Implements Expected Interface
```java
public interface ExpectedPackage<M,T extends TemporalAccessor> extends Expected<T> {
    @Nonnull M getElement(); // Packaged element
    ...
```
Construct package from element with time marker
```java
    @Nonnull
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> pack(@Nonnull M element, @Nonnull T tick)
    ...
```
Construct package from element with time marker from time-marker supplier
```java
   static <M,T extends TemporalAccessor> ExpectedPackage<M,T> supply(@Nonnull M element, @Nonnull TimeSupplier<T> tick)
   ...
```
Construct package from element with time marker extracted from element
```java
    static <M,T extends TemporalAccessor> ExpectedPackage<M,T> extract(@Nonnull M element, @Nonnull TimeExtractor<M,T> extractor) {
    ...
```
## Class ExpectationPackager
Realize package with time-marker extractor from object. Convert object to ExpectedPackage with time-marker
```java
    public ExpectationPackager(@Nonnull Expectation<M,T> fireTimeExtractor) // Packager constructor with Time-marker extractor
```
Creation of ExpectedPackage from object by the timeExtractor
```java
    public @Nonnull ExpectedPackage<M, T> apply(@Nonnull M element)
```

---
Copyright ?? 2017-2022 Andrey D. Shindarev. All rights reserved.