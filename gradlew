#!/bin/sh

# Setup environment
APP_HOME=$(cd "$(dirname "$0")" && pwd)

CLASSPATH=""
for f in "$APP_HOME"/gradle/wrapper/*.jar; do
    [ -f "$f" ] && CLASSPATH="$f"
done

# Download wrapper jar if missing
if [ -z "$CLASSPATH" ]; then
    mkdir -p "$APP_HOME/gradle/wrapper"
    JAR_URL="https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
    if command -v curl >/dev/null 2>&1; then
        curl -sLo "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$JAR_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -qO "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$JAR_URL"
    fi
    CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
fi

exec java -jar "$CLASSPATH" "$@"
