#!/bin/sh

set -e

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ]; then
    JAVA_EXECUTABLE="$JAVA_HOME/bin/java"
else
    JAVA_EXECUTABLE="java"
fi

exec "$JAVA_EXECUTABLE" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
