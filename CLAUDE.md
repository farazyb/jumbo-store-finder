# Jumbo Store Finder

Find the Jumbo stores closest to a given position, so a customer can
decide where to shop or collect an order.

Backend service: returns the N Jumbo stores nearest a given lat/lon

## Commands

    ./mvnw verify          # build + tests
    ./mvnw spring-boot:run # start on :8080

    curl "http://localhost:8080/api/v1/stores/nearest?lat=52.3676&lon=4.9041"

Docs: http://localhost:8080/swagger-ui.html

## Layout
   api/     controllers, DTOs, exception handler
   domain/  Store, Coordinates, Haversine, NearestStoreFinder, exceptions(domain related)
   data/    StoreRepository (port) + JsonStoreRepository

## Rules
- you can not guss, Ask questions.
- Keep it simple, do not over engineering.
- domain/ imports nothing from Spring or Jackson. Keep it framework-free.
- Store data is immutable and loaded once at startup. Fail fast if absent.
- Distance is Haversine, in `domain/Haversine.java`. Do not swap in a library.
- No database, no caching, no auth. Deliberate — do not add.
- Use meaningful and descriptive names for variables, functions, classes, and other code elements to convey their purpose and functionality.
- Keep documentation concise to communicate only essential information without unnecessary verbosity or redundancy.
- Follow consistent formatting conventions, including indentation, line breaks, and spacing, to improve readability and maintainability.

## Api rules:
- Use openAPI swagger for documenting the api
- api documents must explain which input,output and behavior

## Data(src/main/resources/stores.json)
- todayOpen/todayClose can be the literal "Gesloten"
- collectionPoint is absent, not false, on 213 records.
- lat/lon are strings, not numbers
- Only today's hours exist — no weekly schedule

##LOGGING
- request's coordinates can not be in the log file(security reason), Only log when the request faced with exception.
- Never log per-request in the happy path.
- Log the cause, not just that something failed: include the record UUID.
- Use parameterised logging.


##Tests
- use Junit
- do not use mockito , use fake lambda
- test must be written in GIVEN,WHEN,THEN style.
- use @DisplayName
- use Parameterized when you want to test method multiple times with different arguments.


##STOP AND REPORTING
- when you done with the Unit of work , one logic you have to stop and report the changes .




