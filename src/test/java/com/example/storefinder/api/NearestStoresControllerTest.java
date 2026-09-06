package com.example.storefinder.api;

import com.example.storefinder.domain.Address;
import com.example.storefinder.domain.Coordinates;
import com.example.storefinder.domain.NearestStoreFinder;
import com.example.storefinder.domain.OpeningHours;
import com.example.storefinder.domain.Store;
import com.example.storefinder.domain.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NearestStoresController.class)
@Import(NearestStoresControllerTest.FakeStoreData.class)
@DisplayName("GET /api/v1/stores/nearest")
class NearestStoresControllerTest {

    private static final String NEAREST = "/api/v1/stores/nearest";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("returns the five nearest stores, nearest first")
    void returnsFiveNearestStores() throws Exception {
        // GIVEN seven stores, the closest of which is closed all day

        // WHEN the nearest are requested without filtering
        mockMvc.perform(get(NEAREST).param("lat", "0.0").param("lon", "0.0"))

                // THEN five come back, nearest first, the closed one included
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].uuid").value("closed-nearest"))
                .andExpect(jsonPath("$[4].uuid").value("open-4"))
                .andExpect(jsonPath("$[0].distanceKm").value(11.12))
                .andExpect(jsonPath("$[0].openNow").value(false))
                .andExpect(jsonPath("$[0].openingHours.status").value("ALL_DAY_CLOSED"))
                .andExpect(jsonPath("$[0].openingHours.opensAt").doesNotExist())
                .andExpect(jsonPath("$[1].openNow").value(true))
                .andExpect(jsonPath("$[1].openingHours.status").value("OPEN_TODAY"))
                .andExpect(jsonPath("$[1].openingHours.opensAt").value("08:00"))
                .andExpect(jsonPath("$[1].openingHours.closesAt").value("20:00"))
                .andExpect(jsonPath("$[1].coordinates.latitude").value(0.0))
                .andExpect(jsonPath("$[1].address.city").value("Amsterdam"))
                .andExpect(jsonPath("$[1].address.street3").doesNotExist())
                .andExpect(jsonPath("$[1].locationType").value("Supermarkt"))
                .andExpect(jsonPath("$[1].collectionPoint").value(false));
    }

    @Test
    @DisplayName("skips stores that are closed when onlyOpen is set")
    void skipsClosedStoresWhenOnlyOpenIsSet() throws Exception {
        // GIVEN the nearest store is closed at the fixed clock time

        // WHEN only open stores are requested
        mockMvc.perform(get(NEAREST).param("lat", "0.0").param("lon", "0.0").param("onlyOpen", "true"))

                // THEN it is skipped, and five open stores further out take its place
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].uuid").value("open-1"))
                .andExpect(jsonPath("$[4].uuid").value("open-5"));
    }

    @Test
    @DisplayName("rejects a latitude outside the valid range")
    void rejectsLatitudeOutsideRange() throws Exception {
        // GIVEN a latitude beyond the pole

        // WHEN the nearest stores are requested for it
        mockMvc.perform(get(NEAREST).param("lat", "91").param("lon", "4.9"))

                // THEN the request is refused with a problem detail that states the rule
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Latitude must be between -90 and 90 degrees"));
    }

    @Test
    @DisplayName("rejects a request with no longitude")
    void rejectsRequestWithoutLongitude() throws Exception {
        // GIVEN a request carrying only a latitude

        // WHEN the nearest stores are requested
        mockMvc.perform(get(NEAREST).param("lat", "52.3676"))

                // THEN the request is refused, naming the parameter that is missing
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Parameter 'lon' is required"));
    }

    @Test
    @DisplayName("rejects a coordinate that is not a number")
    void rejectsCoordinateThatIsNotANumber() throws Exception {
        // GIVEN a latitude that is not a number

        // WHEN the nearest stores are requested
        mockMvc.perform(get(NEAREST).param("lat", "abc").param("lon", "4.9"))

                // THEN the request is refused without echoing what was sent
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Parameter 'lat' must be a number"));
    }

    @TestConfiguration
    static class FakeStoreData {

        /** 12:00 in Amsterdam, so the stores opening 08:00-20:00 are open. */
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-09-06T10:00:00Z"), ZoneId.of("Europe/Amsterdam"));
        }

        @Bean
        NearestStoreFinder nearestStoreFinder() {
            OpeningHours open = new OpeningHours(LocalTime.of(8, 0), LocalTime.of(20, 0));
            OpeningHours closed = OpeningHours.closedAllDay();

            StoreRepository repository = () -> List.of(
                    storeAt("closed-nearest", 0.1, closed),
                    storeAt("open-1", 0.2, open),
                    storeAt("open-2", 0.3, open),
                    storeAt("open-3", 0.4, open),
                    storeAt("open-4", 0.5, open),
                    storeAt("open-5", 0.6, open),
                    storeAt("open-6", 0.7, open));

            return new NearestStoreFinder(repository);
        }

        private static Store storeAt(String uuid, double longitude, OpeningHours openingHours) {
            return new Store(
                    uuid,
                    new Coordinates(0.0, longitude),
                    new Address("Jumbo " + uuid, "Kerkstraat", "1", null, "1234 AB", "Amsterdam"),
                    openingHours,
                    "Supermarkt",
                    false);
        }
    }
}
