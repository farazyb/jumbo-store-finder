package com.example.storefinder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the running application over HTTP against the real seed file. Nothing here is faked,
 * which is what makes it worth the startup cost. Assertions read the JSON as it goes over the
 * wire rather than through the response DTO, so the serialised contract is checked too.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("The running application, against the real store data")
class StoreFinderEndToEndTest {

    private static final String NEAREST = "/api/v1/stores/nearest?lat=52.3676&lon=4.9041";

    /** Jumbo Amsterdam Stadhouderskade, the closest store in the seed file to that position. */
    private static final String NEAREST_STORE_UUID = "dhkKYx4XS0UAAAFcnMNlwJ7N";
    private static final double NEAREST_STORE_KM = 1.25;

    private static final ObjectMapper JSON = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("returns the five nearest real stores, closest first")
    void returnsTheFiveNearestRealStores() throws Exception {
        // GIVEN the application running with the seed file it ships with

        // WHEN the nearest stores to a position in Amsterdam are requested
        HttpResponse<String> response = get(NEAREST);

        // THEN five come back, closest first, and the closest is the store the data says it is.
        // A stubbed repository could not produce this uuid, so the real file was read.
        assertEquals(200, response.statusCode());

        JsonNode stores = JSON.readTree(response.body());
        assertEquals(5, stores.size());
        assertEquals(NEAREST_STORE_UUID, stores.get(0).get("uuid").asText());
        assertEquals(NEAREST_STORE_KM, stores.get(0).get("distanceKm").asDouble(), 0.01);
        assertTrue(isAscendingByDistance(stores), "distances should increase down the list");

        // AND the request passed through the logging filter, which tags every API response
        assertTrue(response.headers().firstValue("X-Request-Id").isPresent());
    }

    @Test
    @DisplayName("returns only open stores when asked for them")
    void returnsOnlyOpenStoresWhenAskedForThem() throws Exception {
        // GIVEN the application running on the real clock, so the answer depends on the hour

        // WHEN only open stores are requested
        HttpResponse<String> response = get(NEAREST + "&onlyOpen=true");

        // THEN whatever came back is open. Stated this way it holds at any time of day, including
        // at night when an empty list is the correct answer.
        assertEquals(200, response.statusCode());

        JsonNode stores = JSON.readTree(response.body());
        assertTrue(stores.size() <= 5);
        for (JsonNode store : stores) {
            assertTrue(store.get("openNow").asBoolean(),
                    store.get("uuid").asText() + " was returned but is not open");
        }
    }

    @Test
    @DisplayName("refuses a position that is not on the Earth")
    void refusesPositionThatIsNotOnTheEarth() throws Exception {
        // GIVEN a latitude beyond the pole

        // WHEN the nearest stores are requested for it
        HttpResponse<String> response = get("/api/v1/stores/nearest?lat=91&lon=4.9");

        // THEN the request is refused with a body that explains the rule
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Latitude must be between -90 and 90 degrees"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static boolean isAscendingByDistance(JsonNode stores) {
        for (int index = 1; index < stores.size(); index++) {
            if (stores.get(index).get("distanceKm").asDouble()
                    < stores.get(index - 1).get("distanceKm").asDouble()) {
                return false;
            }
        }
        return true;
    }
}
