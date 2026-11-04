#!/bin/bash
cd "$(dirname "$0")"
if [ -x jre/bin/java ]; then JAVA=jre/bin/java; else JAVA=java; fi
exec "$JAVA" -jar app.jar
