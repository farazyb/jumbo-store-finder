# Jumbo Store Finder

Finds the 5 Jumbo stores closest to a position, so a customer can decide where to shop or
collect an order.

## Running it

Two ways. Pick whichever is easier for you.

### Option 1: Maven, if you have JDK 17 or newer

Nothing to build, no Docker needed.

```bash
./mvnw spring-boot:run
```

On Windows PowerShell or cmd, use `mvnw.cmd spring-boot:run`.

If port 8080 is already in use, choose another one:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

On Windows, the same with `mvnw.cmd`.

### Option 2: Docker Compose

This builds the image and starts it for you.

```bash
docker compose up --build
```

Port 8080 is a common one, so if it is busy, create a file named `.env` next to `compose.yaml`
with this line in it, then run the command above again:

```
PORT=9090
```

## Trying it out

The examples use port 8080. If you changed it, use your port instead.

### Swagger

Open the docs and use "Try it out" on the endpoint:

http://localhost:8080/swagger-ui.html

It describes the input, the output and the behaviour, so it is the quickest way to explore.

### curl

The 5 closest stores to a position in Amsterdam:

```bash
curl "http://localhost:8080/api/v1/stores/nearest?lat=52.3676&lon=4.9041"
```

The same, but only stores that are open right now:

```bash
curl "http://localhost:8080/api/v1/stores/nearest?lat=52.3676&lon=4.9041&onlyOpen=true"
```

In PowerShell, use `curl.exe` instead of `curl`, so the built-in alias does not get in the way.

### Tests

```bash
./mvnw verify
```

On Windows, `mvnw.cmd verify`.

## Tech stack

- Java 17
- Spring Boot 4.1.1, Web MVC and Actuator
- Maven
- springdoc-openapi for Swagger
- Micrometer with Prometheus
- JUnit 5
- Docker
- GitHub Actions

## Design decisions

1. No auth, no Kubernetes, no Terraform. The focus was building the API, not deploying it or
   provisioning infrastructure. This is a small project.
2. Five closest stores was set in the assignment, so there is no limit request parameter. The
   code takes a `howMany` argument, so widening it later is easy.
3. No PostGIS. For 600 stores and very few requests it adds complexity without adding value.
   Spatial indexing becomes necessary at larger scale, and the search sits behind the
   `StoreRepository` port and `NearestStoreFinder`, so that change stays contained.
4. A custom filter logs request latency and attaches a request id, so a failure can be traced.
5. Spring Actuator for health, so it is clear when the service is ready to serve.
6. Swagger for the API docs. It states the input, output and behaviour, and doubles as an easy
   way to test.
7. Prometheus is enabled, but only four metrics are exported: requests, log events, JVM heap and
   CPU usage. Four is a deliberate starting set, small enough to stay readable, and easy to widen
   when there is a reason to.
8. A two stage Dockerfile. It keeps the image small and leaves build tools and sources out of it.
9. GitHub Actions runs on every push. One job runs the tests, another builds the Docker image and
   checks that the container starts and reports healthy.
10. A record that cannot be read is skipped and logged with its id, and loading fails only when no
    usable record is left. Whether that is right depends on the business, and failing on any bad
    record is a fair rule too. Either way the log names the record that is wrong, so the data can
    be fixed.
11. Haversine is hand written, since the curvature matters.
12. The domain has no Spring or Jackson in it, which keeps the layering honest.

## Time spent

- 1-2h understanding the requirements, comparing approaches, and writing CLAUDE.md before any
  code.
- 30m designing the architecture.
- 45m planning.
- 2h implementation,validation, code review.

