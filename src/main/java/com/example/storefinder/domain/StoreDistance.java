package com.example.storefinder.domain;

/**
 * A store paired with how far it is from the position that was searched for.
 *
 * @param store                 the store found
 * @param distanceInKilometers  great-circle distance from the searched position
 */
public record StoreDistance(Store store, double distanceInKilometers) {
}
