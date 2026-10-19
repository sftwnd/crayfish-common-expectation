#!/bin/bash
# Checks the OSGi bundle of ./mvnw package: its headers, and that Apache Felix resolves it without the optional
# SpotBugs annotations and runs a bundle that imports its package, with the JDK of JAVA_HOME (9+) and, if the home of a
# Java 8 JDK is given, with Java 8.
# usage: .github/scripts/check-osgi-bundle.sh [java-8-home]
set -euo pipefail

SYMBOLIC_NAME=com.github.sftwnd.crayfish.common.expectation
PACKAGE=com.github.sftwnd.crayfish.common.expectation
FELIX=org.apache.felix:org.apache.felix.framework:7.0.5
JAVA8_HOME=${1:-}
JAR=$(ls target/crayfish-common-expectation-*.jar | grep -vE -- '-(sources|javadoc)\.jar$')
VERSION=${JAR##*/crayfish-common-expectation-}
VERSION=${VERSION%.jar}
VERSION=${VERSION%-SNAPSHOT}
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

fail() {
    echo "FAILED: $*" >&2
    exit 1
}

# the headers of the manifest, with their continuation lines joined
HEADERS=$(unzip -p "$JAR" META-INF/MANIFEST.MF | tr -d '\r' \
    | awk '/^ / { line = line substr($0, 2); next } { if (NR > 1) print line; line = $0 } END { print line }')
grep -E '^(Bundle-SymbolicName|Bundle-Version|Export-Package|Import-Package|Require-Capability):' <<< "$HEADERS"
for header in "Bundle-ManifestVersion: 2" \
              "Bundle-SymbolicName: $SYMBOLIC_NAME" \
              "Export-Package: $PACKAGE;version=\"$VERSION\"" \
              'Require-Capability: osgi.ee;filter:="(&(osgi.ee=JavaSE)(version=1.8))"' \
              "Automatic-Module-Name: com.github.sftwnd.crayfish_common_expectation" \
              "Multi-Release: true"; do
    grep -qxF -- "$header" <<< "$HEADERS" || fail "no '$header' in the manifest"
done
# the CLASS-retention SpotBugs annotations are not imported: the bundle imports no package
if grep -q '^Import-Package:' <<< "$HEADERS"; then
    fail "the bundle imports packages"
fi

# Apache Felix, from Maven Central
./mvnw -B -ntp -q dependency:copy -Dartifact="$FELIX" -DoutputDirectory="$WORK"
FELIX_JAR=$(ls "$WORK"/org.apache.felix.framework-*.jar)

# a bundle that imports the package of the release, and a launcher that installs the two bundles and starts them
mkdir -p "$WORK/consumer/expectation/check" "$WORK/consumer-classes" "$WORK/launcher/expectation/launch" "$WORK/launcher-classes"
cat > "$WORK/consumer/expectation/check/Activator.java" <<'JAVA'
package expectation.check;

import com.github.sftwnd.crayfish.common.expectation.ExpectedPackage;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.time.Instant;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) {
        // the bundle that the class of the package comes from, and a package of it
        System.out.println(FrameworkUtil.getBundle(ExpectedPackage.class).getSymbolicName() + " "
                + ExpectedPackage.pack("check", Instant.EPOCH));
    }

    @Override
    public void stop(BundleContext context) {
        // nothing to stop
    }
}
JAVA
cat > "$WORK/consumer.mf" <<MANIFEST
Bundle-ManifestVersion: 2
Bundle-SymbolicName: expectation.check
Bundle-Version: 1.0.0
Bundle-Activator: expectation.check.Activator
Import-Package: $PACKAGE;version="[${VERSION%.*},$((${VERSION%%.*} + 1)))",org.osgi.framework
MANIFEST
cat > "$WORK/launcher/expectation/launch/Launcher.java" <<'JAVA'
package expectation.launch;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

// usage: Launcher <storage> <bundle of the release> <bundle that imports its package>
public class Launcher {
    public static void main(String[] args) throws Exception {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(Constants.FRAMEWORK_STORAGE, args[0]);
        configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        Framework framework = ServiceLoader.load(FrameworkFactory.class).iterator().next().newFramework(configuration);
        framework.start();
        try {
            BundleContext context = framework.getBundleContext();
            Bundle library = context.installBundle(new File(args[1]).toURI().toString());
            Bundle consumer = context.installBundle(new File(args[2]).toURI().toString());
            library.start();
            consumer.start();
            System.out.println(library.getSymbolicName() + " " + library.getVersion() + " "
                    + (library.getState() == Bundle.ACTIVE ? "ACTIVE" : "state " + library.getState()));
        } finally {
            framework.stop();
            framework.waitForStop(10_000);
        }
    }
}
JAVA
"$JAVA_HOME/bin/javac" --release 8 -Xlint:-options -d "$WORK/consumer-classes" -cp "$FELIX_JAR:$JAR" "$WORK/consumer/expectation/check/Activator.java"
"$JAVA_HOME/bin/jar" --create --file "$WORK/consumer.jar" --manifest "$WORK/consumer.mf" -C "$WORK/consumer-classes" .
"$JAVA_HOME/bin/javac" --release 8 -Xlint:-options -d "$WORK/launcher-classes" -cp "$FELIX_JAR" "$WORK/launcher/expectation/launch/Launcher.java"

check() {
    local java_home=$1 storage=$2 out
    out=$("$java_home/bin/java" -cp "$FELIX_JAR:$WORK/launcher-classes" expectation.launch.Launcher "$storage" "$JAR" "$WORK/consumer.jar")
    echo "$("$java_home/bin/java" -version 2>&1 | head -1): $(tr '\n' ';' <<< "$out")"
    grep -qxF "$SYMBOLIC_NAME ExpectedPackage(1970-01-01T00:00:00Z,check)" <<< "$out" || fail "the bundle that imports the package did not run"
    grep -qE "^$SYMBOLIC_NAME $VERSION(\.SNAPSHOT)? ACTIVE$" <<< "$out" || fail "the bundle is not active"
}
check "$JAVA_HOME" "$WORK/felix"
if [[ -n "$JAVA8_HOME" ]]; then
    check "$JAVA8_HOME" "$WORK/felix-8"
fi
echo "OK"
