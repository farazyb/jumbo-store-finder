package com.example.storefinder.observability;

import com.example.storefinder.domain.Address;
import com.example.storefinder.domain.Coordinates;
import com.example.storefinder.domain.OpeningHours;
import com.example.storefinder.domain.Store;
import com.example.storefinder.domain.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("StoreDataHealthIndicator")
class StoreDataHealthIndicatorTest {

    @Test
    @DisplayName("is up, reporting how many stores are loaded")
    void isUpWhenStoresAreLoaded() {
        // GIVEN a repository holding two stores
        StoreRepository repository = () -> List.of(store("first"), store("second"));

        // WHEN health is checked
        Health health = new StoreDataHealthIndicator(repository).health();

        // THEN it is up, and says how many stores it found
        assertEquals(Status.UP, health.getStatus());
        assertEquals(2, health.getDetails().get("storeCount"));
    }

    @Test
    @DisplayName("is down when no stores are loaded, so the application is not marked ready")
    void isDownWhenNoStoresAreLoaded() {
        // GIVEN a repository holding no stores
        StoreRepository repository = List::of;

        // WHEN health is checked
        Health health = new StoreDataHealthIndicator(repository).health();

        // THEN it is down: there is nothing to serve
        assertEquals(Status.DOWN, health.getStatus());
    }

    private static Store store(String uuid) {
        return new Store(
                uuid,
                new Coordinates(52.3676, 4.9041),
                new Address("Jumbo " + uuid, "Kerkstraat", "1", null, "1234 AB", "Amsterdam"),
                new OpeningHours(LocalTime.of(8, 0), LocalTime.of(20, 0)),
                "Supermarkt",
                false);
    }
}
