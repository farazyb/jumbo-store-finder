# ---- Stage 1: build ----------------------------------------------------------
# Needs a full JDK: javac compiles the sources here.
FROM eclipse-temurin:17-jdk AS build

WORKDIR /build

# Copy only what resolves dependencies first, so this layer is cached and Maven
# re-downloads nothing when just the sources change.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B --no-transfer-progress dependency:go-offline

COPY src/ src/
# Tests already run in CI on every push; re-running them here only slows the image build.
RUN ./mvnw -B --no-transfer-progress -DskipTests package

# ---- Stage 2: run ------------------------------------------------------------
# Only a JRE: no compiler, no Maven, no sources in the shipped image.
FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Run as a non-root user rather than root.
RUN useradd --system --uid 1001 --create-home appuser
USER appuser

COPY --from=build /build/target/store-finder-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
