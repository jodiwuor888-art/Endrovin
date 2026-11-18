# ============================================================
# Joash Portfolio Management System — Multi-stage Docker Build
# Stage 1: Build the Spring Boot JAR with Maven 3.9 + JDK 21
# Stage 2: Minimal JRE 21 runtime image for production
# ============================================================

# ---- BUILD STAGE ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml first to cache dependencies in Docker layer
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source code and compile fat JAR (skip tests)
COPY src ./src
RUN mvn package -DskipTests -B

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create the uploads directory structure
RUN mkdir -p /app/uploads/profiles /app/uploads/projects /app/uploads/certificates /app/uploads/cv \
    && chown -R appuser:appgroup /app/uploads

# Copy the built JAR from the build stage
COPY --from=build /app/target/portfolio-management-system-*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# Expose default application port
EXPOSE 8080

# Healthcheck for container orchestration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/actuator/health || exit 1

# Start Spring Boot application
ENTRYPOINT ["java", \
    "-server", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-XX:+UseG1GC", \
    "-XX:G1HeapRegionSize=16m", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:+ParallelRefProcEnabled", \
    "-XX:+UseStringDeduplication", \
    "-XX:+OptimizeStringConcat", \
    "-XX:+TieredCompilation", \
    "-XX:+UseCompressedOops", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.jmx.enabled=false", \
    "-Dspring.backgroundpreinitializer.ignore=true", \
    "-jar", "app.jar"]
