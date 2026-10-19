#!/bin/bash
# Checks the jar built on JDK 9+ (./mvnw package): the module descriptor, a modular consumer on the module path
# with the JDK of JAVA_HOME, and, if the home of a Java 8 JDK is given, a consumer on the class path of Java 8.
# usage: .github/scripts/check-modular-jar.sh [java-8-home]
set -euo pipefail

MODULE=com.github.sftwnd.crayfish_common_expectation
JAVA8_HOME=${1:-}
JAR=$(ls target/crayfish-common-expectation-*.jar | grep -vE -- '-(sources|javadoc)\.jar$')
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

fail() {
    echo "FAILED: $*" >&2
    exit 1
}

# the module descriptor, in META-INF/versions/9 of a multi-release jar
DESCRIPTOR=$("$JAVA_HOME/bin/jar" --describe-module --file "$JAR" --release 9)
echo "$DESCRIPTOR"
for line in "$MODULE@" \
            "exports com.github.sftwnd.crayfish.common.expectation" \
            "requires com.github.spotbugs.annotations static"; do
    grep -qF -- "$line" <<< "$DESCRIPTOR" || fail "no '$line' in the module descriptor"
done

EXPECTED="ExpectedPackage(1970-01-01T00:00:00Z,check)"
mkdir -p "$WORK/modular/expectation.check/expectation/check" "$WORK/plain/expectation/check" "$WORK/plain/out"
cat > "$WORK/modular/expectation.check/module-info.java" <<JAVA
module expectation.check {
    requires $MODULE;
}
JAVA
cat > "$WORK/modular/expectation.check/expectation/check/Main.java" <<'JAVA'
package expectation.check;

import com.github.sftwnd.crayfish.common.expectation.ExpectedPackage;

import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        Module module = ExpectedPackage.class.getModule();
        System.out.println(module.getName() + (module.getDescriptor().isAutomatic() ? " automatic" : " named")
                + " " + ExpectedPackage.pack("check", Instant.EPOCH));
    }
}
JAVA
cat > "$WORK/plain/expectation/check/Main.java" <<'JAVA'
package expectation.check;

import com.github.sftwnd.crayfish.common.expectation.ExpectedPackage;

import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        System.out.println(System.getProperty("java.version") + " " + ExpectedPackage.pack("check", Instant.EPOCH));
    }
}
JAVA

# a named module on the module path, without the optional spotbugs annotations
"$JAVA_HOME/bin/javac" -d "$WORK/modular/out" --module-path "$JAR" \
    --module-source-path "$WORK/modular" --module expectation.check
OUT=$("$JAVA_HOME/bin/java" --module-path "$JAR:$WORK/modular/out/expectation.check" \
    --module expectation.check/expectation.check.Main)
echo "module path: $OUT"
[[ "$OUT" = "$MODULE named $EXPECTED" ]] || fail "expected '$MODULE named $EXPECTED' on the module path"

# Java 8 ignores the descriptor
if [[ -n "$JAVA8_HOME" ]]; then
    "$JAVA8_HOME/bin/javac" -d "$WORK/plain/out" -cp "$JAR" "$WORK/plain/expectation/check/Main.java"
    OUT=$("$JAVA8_HOME/bin/java" -cp "$JAR:$WORK/plain/out" expectation.check.Main)
    echo "class path: $OUT"
    [[ "$OUT" == 1.8.* && "$OUT" == *" $EXPECTED" ]] || fail "expected '$EXPECTED' on the class path of Java 8"
fi
echo "OK"
