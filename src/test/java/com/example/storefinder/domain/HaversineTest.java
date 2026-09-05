package com.example.storefinder.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Haversine")
class HaversineTest {

    private static final Coordinates AMSTERDAM = new Coordinates(52.3676, 4.9041);
    private static final Coordinates ROTTERDAM = new Coordinates(51.9244, 4.4777);

    private static final double AMSTERDAM_TO_ROTTERDAM_KM = 57.23;
    private static final double HALF_EARTH_CIRCUMFERENCE_KM = 20015.11;
    private static final double TOLERANCE_KM = 0.5;

    @Test
    @DisplayName("measures a known distance between two cities")
    void measuresKnownDistanceBetweenCities() {
        // GIVEN the centres of Amsterdam and Rotterdam

        // WHEN the distance between them is measured
        double distance = Haversine.distanceInKilometers(AMSTERDAM, ROTTERDAM);

        // THEN it matches the great-circle distance for that pair
        assertEquals(AMSTERDAM_TO_ROTTERDAM_KM, distance, TOLERANCE_KM);
    }

    @Test
    @DisplayName("measures exactly zero between a position and itself")
    void measuresZeroBetweenPositionAndItself() {
        // GIVEN one position used as both endpoints

        // WHEN the distance from it to itself is measured
        double distance = Haversine.distanceInKilometers(AMSTERDAM, AMSTERDAM);

        // THEN it is exactly zero, with no rounding drift
        assertEquals(0.0, distance);
    }

    @Test
    @DisplayName("measures the same distance in either direction")
    void measuresSameDistanceInEitherDirection() {
        // GIVEN two distinct positions

        // WHEN the distance is measured in both directions
        double thereDistance = Haversine.distanceInKilometers(AMSTERDAM, ROTTERDAM);
        double backDistance = Haversine.distanceInKilometers(ROTTERDAM, AMSTERDAM);

        // THEN the two measurements agree
        assertEquals(thereDistance, backDistance);
    }

    @Test
    @DisplayName("returns half the circumference for antipodal positions rather than NaN")
    void returnsHalfCircumferenceForAntipodalPositions() {
        // GIVEN two positions on exactly opposite sides of the Earth, where rounding can push
        // the half-versine past 1 and make asin return NaN
        Coordinates position = new Coordinates(0.0, 0.0);
        Coordinates opposite = new Coordinates(0.0, 180.0);

        // WHEN the distance between them is measured
        double distance = Haversine.distanceInKilometers(position, opposite);

        // THEN the clamp holds and the result is half the Earth's circumference
        assertFalse(Double.isNaN(distance), "antipodal positions must not produce NaN");
        assertEquals(HALF_EARTH_CIRCUMFERENCE_KM, distance, TOLERANCE_KM);
    }
}
