package com.example.storefinder.domain;

/**
 * Great-circle distance between two positions, treating the Earth as a sphere.
 *
 * <p>Accurate to roughly 0.5% against the true ellipsoid, which is far finer than needed to rank
 * supermarkets by proximity.
 */
public final class Haversine {

    /** Mean Earth radius, in kilometres. */
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private Haversine() {
    }

    public static double distanceInKilometers(Coordinates from, Coordinates to) {
        double fromLatitude = Math.toRadians(from.latitude());
        double toLatitude = Math.toRadians(to.latitude());
        double latitudeDifference = toLatitude - fromLatitude;
        double longitudeDifference = Math.toRadians(to.longitude() - from.longitude());

        double halfVersine = square(Math.sin(latitudeDifference / 2))
                + square(Math.sin(longitudeDifference / 2)) * Math.cos(fromLatitude) * Math.cos(toLatitude);

        // For near-antipodal positions rounding can push the term just above 1, where asin
        // returns NaN. Clamping keeps the result at half the circumference instead.
        double centralAngle = 2 * Math.asin(Math.sqrt(Math.min(1.0, halfVersine)));

        return EARTH_RADIUS_KM * centralAngle;
    }

    private static double square(double value) {
        return value * value;
    }
}
