# Build stage: compiles the application with the Maven wrapper (tests run in CI, not in the image build)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Dependencies are resolved in their own layer so they are cached while only the sources change
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/
RUN ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# Runtime stage: only the JRE and the executable jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /workspace/target/starwars-api-*.jar app.jar

# Tuned for small containers (512 MB, a fraction of a CPU): the heap leaves room for the JVM's own memory,
# the serial collector has the lowest overhead, and C1-only compilation shortens startup
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
