# ---------- Build stage ----------
# Uses Maven to build the fat jar. Caching strategy: copy pom first to leverage cached deps.
ARG MAVEN_IMAGE=maven:3.9.4-eclipse-temurin-21
ARG BUILD_PROFILE=test
FROM ${MAVEN_IMAGE} AS build

WORKDIR /workspace

# copy only pom to leverage docker build cache for dependencies
COPY pom.xml .
# If using a multi-module project, copy parent POMs accordingly

# download dependencies using BuildKit cache mount for Maven repository
# Requires BuildKit (enabled by docker/build-push-action)
RUN --mount=type=cache,target=/root/.m2 mvn -B -f pom.xml dependency:go-offline

# copy source and package
COPY src ./src

# Build with profile support (defaults to test profile which uses H2)
# Use BuildKit cache mount for Maven local repository to speed up builds
ARG BUILD_PROFILE
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -Dspring.profiles.active=${BUILD_PROFILE} package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# create non-root user
RUN useradd --create-home --shell /bin/bash appuser

WORKDIR /app

# copy jar from build stage
COPY --from=build /workspace/target/*.jar app.jar

# copy entrypoint script
COPY docker/docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# expose application port
EXPOSE 8080

# environment defaults (override in deployment)
ENV SPRING_PROFILES_ACTIVE=default \
    JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom" \
    SERVER_PORT=8080

# run as non-root
USER appuser

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/app/docker-entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]
