package com.example.storefinder.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NearestStoreFinder")
class NearestStoreFinderTest {

    private static final Coordinates ORIGIN = new Coordinates(0.0, 0.0);

    private static final LocalTime NOON = LocalTime.of(12, 0);
    private static final OpeningHours OPEN = new OpeningHours(LocalTime.of(8, 0), LocalTime.of(20, 0));
    private static final OpeningHours CLOSED = OpeningHours.closedAllDay();

    @Test
    @DisplayName("returns exactly as many stores as were asked for")
    void returnsExactlyAsManyStoresAsAskedFor() {
        // GIVEN a repository holding more stores than will be requested
        StoreRepository repository = () -> List.of(
                storeAt("far", 0.4, OPEN),
                storeAt("nearest", 0.1, OPEN),
                storeAt("third", 0.3, OPEN),
                storeAt("second", 0.2, OPEN));
        NearestStoreFinder finder = new NearestStoreFinder(repository);

        // WHEN the two nearest are requested
        List<StoreDistance> found = finder.findNearest(ORIGIN, 2);

        // THEN exactly two come back
        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("returns the stores nearest first")
    void returnsStoresNearestFirst() {
        // GIVEN a repository holding stores at increasing distance, listed out of order
        StoreRepository repository = () -> List.of(
                storeAt("far", 0.4, OPEN),
                storeAt("nearest", 0.1, OPEN),
                storeAt("third", 0.3, OPEN),
                storeAt("second", 0.2, OPEN));
        NearestStoreFinder finder = new NearestStoreFinder(repository);

        // WHEN all of them are requested
        List<StoreDistance> found = finder.findNearest(ORIGIN, 4);

        // THEN they come back ordered by ascending distance
        assertEquals(List.of("nearest", "second", "third", "far"), uuidsOf(found));
        assertTrue(isAscendingByDistance(found), "distances should increase down the list");
    }

    @Test
    @DisplayName("returns what exists when fewer stores are available than requested")
    void returnsWhatExistsWhenFewerAreAvailable() {
        // GIVEN a repository holding fewer stores than will be requested
        StoreRepository repository = () -> List.of(
                storeAt("first", 0.1, OPEN),
                storeAt("second", 0.2, OPEN));
        NearestStoreFinder finder = new NearestStoreFinder(repository);

        // WHEN five stores are requested
        List<StoreDistance> found = finder.findNearest(ORIGIN, 5);

        // THEN both available stores come back, without an error
        assertEquals(2, found.size());
        assertEquals(List.of("first", "second"), uuidsOf(found));
    }

    @Test
    @DisplayName("returns an empty list when the repository holds no stores")
    void returnsEmptyListWhenRepositoryIsEmpty() {
        // GIVEN a repository holding no stores
        StoreRepository repository = List::of;
        NearestStoreFinder finder = new NearestStoreFinder(repository);

        // WHEN the nearest stores are requested
        List<StoreDistance> found = finder.findNearest(ORIGIN, 5);

        // THEN the result is empty rather than a failure
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("filters before selecting, so the nearest open stores are found rather than the open ones among the nearest")
    void filtersBeforeSelecting() {
        // GIVEN the two nearest stores are closed and the two beyond them are open
        StoreRepository repository = () -> List.of(
                storeAt("nearest-but-closed", 0.1, CLOSED),
                storeAt("second-but-closed", 0.2, CLOSED),
                storeAt("nearest-open", 0.3, OPEN),
                storeAt("second-open", 0.4, OPEN));
        NearestStoreFinder finder = new NearestStoreFinder(repository);

        // WHEN the two nearest open stores are requested
        List<StoreDistance> found = finder.findNearestOpenAt(ORIGIN, 2, NOON);

        // THEN the two open stores come back. Selecting first and filtering afterwards would
        // have taken the two closed stores and returned nothing.
        assertEquals(List.of("nearest-open", "second-open"), uuidsOf(found));
    }

    @Test
    @DisplayName("measures the distance to each store it returns")
    void measuresDistanceToEachStore() {
        // GIVEN a store one degree of longitude east of the origin
        StoreRepository repository = () -> List.of(storeAt("one-degree-east", 1.0, OPEN));
        NearestStoreFinder finder = new NearestStoreFinder(repository);

        // WHEN the nearest store is requested
        List<StoreDistance> found = finder.findNearest(ORIGIN, 1);

        // THEN it is paired with its distance, one degree at the equator being about 111 km
        assertEquals(111.19, found.get(0).distanceInKilometers(), 0.5);
    }

    @Test
    @DisplayName("rejects a request for fewer than one store")
    void rejectsRequestForFewerThanOneStore() {
        // GIVEN a repository holding stores
        StoreRepository repository = () -> List.of(storeAt("only", 0.1, OPEN));
        NearestStoreFinder finder = new NearestStoreFinder(repository);

        // WHEN no stores are asked for, filtered or not
        // THEN both entry points refuse, rather than limit(0) reporting "none found"
        assertThrows(IllegalArgumentException.class, () -> finder.findNearest(ORIGIN, 0));
        assertThrows(IllegalArgumentException.class, () -> finder.findNearestOpenAt(ORIGIN, 0, NOON));
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

    private static List<String> uuidsOf(List<StoreDistance> found) {
        return found.stream().map(storeDistance -> storeDistance.store().uuid()).toList();
    }

    private static boolean isAscendingByDistance(List<StoreDistance> found) {
        for (int index = 1; index < found.size(); index++) {
            if (found.get(index).distanceInKilometers() < found.get(index - 1).distanceInKilometers()) {
                return false;
            }
        }
        return true;
    }
}
