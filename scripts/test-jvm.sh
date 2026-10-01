#!/usr/bin/env bash
# Run configuration and key-timing tests using existing jars, without Gradle or downloads.
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_JAR:?Set ANDROID_JAR to an existing SDK platform android.jar}"
: "${JUNIT_JAR:?Set JUNIT_JAR to junit-4.13.2.jar}"
: "${HAMCREST_JAR:?Set HAMCREST_JAR to hamcrest-core-1.3.jar}"
for jar in "$ANDROID_JAR" "$JUNIT_JAR" "$HAMCREST_JAR"; do
    test -f "$jar" || { echo "Missing jar: $jar" >&2; exit 1; }
done
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}"
classes=$(mktemp -d "${TMPDIR:-/tmp}/pixelgesture-tests.XXXXXX")
trap 'rm -rf "$classes"' EXIT
classpath="$ANDROID_JAR:$JUNIT_JAR:$HAMCREST_JAR"
"${java_bin}javac" --release 8 -cp "$classpath" -d "$classes" \
    app/src/main/java/com/pixelgesture/AppConfig.java \
    app/src/main/java/com/pixelgesture/ConfigBackup.java \
    app/src/main/java/com/pixelgesture/VolumeGestureState.java \
    app/src/main/java/com/pixelgesture/OverlayGeometry.java \
    app/src/main/java/com/pixelgesture/BillingGateway.java \
    app/src/main/java/com/pixelgesture/BillingController.java \
    app/src/main/java/com/pixelgesture/ProfileStore.java \
    app/src/test/java/com/pixelgesture/*.java
"${java_bin}java" -cp "$classes:$classpath" org.junit.runner.JUnitCore \
    com.pixelgesture.AppConfigTest com.pixelgesture.ConfigBackupTest com.pixelgesture.VolumeGestureStateTest com.pixelgesture.BillingControllerTest com.pixelgesture.ProfileStoreTest com.pixelgesture.OverlayGeometryTest
