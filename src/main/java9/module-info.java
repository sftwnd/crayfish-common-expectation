// the spotbugs annotations jar is an automatic module: it has no module descriptor, only an Automatic-Module-Name
@SuppressWarnings("requires-automatic")
open module com.github.sftwnd.crayfish_common_expectation {
    // CLASS-retention marker annotations, needed neither to compile against the module nor to run it
    requires static com.github.spotbugs.annotations;
    exports com.github.sftwnd.crayfish.common.expectation;
}
