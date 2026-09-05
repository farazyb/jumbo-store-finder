package com.example.storefinder.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Coordinates")
class CoordinatesTest {

    private static final double VALID_LATITUDE = 52.3676;
    private static final double VALID_LONGITUDE = 4.9041;

    @ParameterizedTest(name = "latitude {0}")
    @ValueSource(doubles = {91.0, -91.0, Double.NaN})
    @DisplayName("rejects a latitude above the range, below it, or not a number")
    void rejectsLatitudeOutsideRange(double invalidLatitude) {
        // GIVEN a latitude outside the valid range, paired with a valid longitude

        // WHEN the coordinates are constructed
        StoreFinderException thrown = assertThrows(StoreFinderException.class,
                () -> new Coordinates(invalidLatitude, VALID_LONGITUDE));

        // THEN construction fails, naming the latitude as the reason
        assertTrue(thrown.getMessage().contains("Latitude"));
    }

    @ParameterizedTest(name = "longitude {0}")
    @ValueSource(doubles = {181.0, -181.0})
    @DisplayName("rejects a longitude above or below the range")
    void rejectsLongitudeOutsideRange(double invalidLongitude) {
        // GIVEN a longitude outside the valid range, paired with a valid latitude

        // WHEN the coordinates are constructed
        StoreFinderException thrown = assertThrows(StoreFinderException.class,
                () -> new Coordinates(VALID_LATITUDE, invalidLongitude));

        // THEN construction fails, naming the longitude as the reason
        assertTrue(thrown.getMessage().contains("Longitude"));
    }

    @ParameterizedTest(name = "({0}, {1})")
    @CsvSource({"90.0, 180.0", "-90.0, -180.0"})
    @DisplayName("accepts the extremes of both ranges, which are real positions")
    void acceptsPositionAtRangeBounds(double latitude, double longitude) {
        // GIVEN a position at the very edge of both valid ranges

        // WHEN the coordinates are constructed
        Coordinates coordinates = new Coordinates(latitude, longitude);

        // THEN the bounds are treated as inside the range, and both values are preserved
        assertEquals(latitude, coordinates.latitude());
        assertEquals(longitude, coordinates.longitude());
    }

    @Test
    @DisplayName("states the rule without repeating the rejected position, which would reach the log file")
    void keepsRejectedPositionOutOfTheMessage() {
        // GIVEN a position whose latitude is out of range and recognisable as text
        double outOfRangeLatitude = 91.5;

        // WHEN the coordinates are constructed
        StoreFinderException thrown = assertThrows(StoreFinderException.class,
                () -> new Coordinates(outOfRangeLatitude, VALID_LONGITUDE));

        // THEN the message explains the rule and mentions neither half of the position
        assertTrue(thrown.getMessage().contains("-90"), "message should state the rule");
        assertFalse(thrown.getMessage().contains("91.5"), "message must not echo the latitude");
        assertFalse(thrown.getMessage().contains("4.9041"), "message must not echo the longitude");
    }
}
