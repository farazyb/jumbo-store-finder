package com.example.storefinder.domain;

/**
 * A validated position in decimal degrees.
 *
 * <p>Range is checked on construction, so every {@code Coordinates} in the system is usable
 * without further guarding.
 *
 * @param latitude  degrees north of the equator, -90 to 90
 * @param longitude degrees east of the prime meridian, -180 to 180
 */
public record Coordinates(double latitude, double longitude) {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    public Coordinates {
        if (isOutside(latitude, MIN_LATITUDE, MAX_LATITUDE)) {
            throw new StoreFinderException("Latitude must be between -90 and 90 degrees");
        }
        if (isOutside(longitude, MIN_LONGITUDE, MAX_LONGITUDE)) {
            throw new StoreFinderException("Longitude must be between -180 and 180 degrees");
        }
    }

    /**
     * NaN is tested explicitly because every comparison against it is false, so a NaN would
     * otherwise pass both bounds checks.
     */
    private static boolean isOutside(double value, double minimum, double maximum) {
        return Double.isNaN(value) || value < minimum || value > maximum;
    }
}
