#!/usr/bin/env bash
set -e

# Allow passing JVM options via JAVA_OPTS env var and append them to java command
# Also allow optional config substitution or runtime templating steps here.

# If we want to inject runtime config via environment variables into a file, do it here.
# Example: replace placeholders in a config-template.yml -> application.yml (if you use templates)

# Start the app
exec java $JAVA_OPTS -jar app.jar
