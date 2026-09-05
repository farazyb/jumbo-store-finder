package com.example.storefinder.domain;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * Finds the stores closest to a given position.
 */
public class NearestStoreFinder {

    private final StoreRepository storeRepository;

    public NearestStoreFinder(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /**
     * Returns at most {@code howMany} stores, nearest to {@code origin} first. Returning fewer
     * than requested is a valid answer, not a failure.
     *
     * @throws IllegalArgumentException if {@code howMany} is less than 1
     */
    public List<StoreDistance> findNearest(Coordinates origin, int howMany) {
        return nearestAmong(storeRepository.findAll(), origin, howMany);
    }

    /**
     * Returns at most {@code howMany} stores open at {@code time}, nearest to {@code origin}
     * first.
     *
     * <p>Stores are filtered <em>before</em> the nearest are selected, so this yields the nearest
     * open stores rather than whichever of the nearest few happen to be open.
     *
     * <p>The caller passes the time rather than this class reading a clock, which would drag time
     * handling into the domain.
     *
     * @throws IllegalArgumentException if {@code howMany} is less than 1
     */
    public List<StoreDistance> findNearestOpenAt(Coordinates origin, int howMany, LocalTime time) {
        List<Store> openStores = storeRepository.findAll().stream()
                .filter(store -> store.openingHours().isOpenAt(time))
                .toList();

        return nearestAmong(openStores, origin, howMany);
    }

    /** Ranks the given stores by distance from {@code origin} and keeps the nearest few. */
    private static List<StoreDistance> nearestAmong(List<Store> stores, Coordinates origin, int howMany) {
        if (howMany < 1) {
            throw new IllegalArgumentException("howMany must be at least 1, was " + howMany);
        }

        return stores.stream()
                .map(store -> new StoreDistance(store,
                        Haversine.distanceInKilometers(origin, store.coordinates())))
                .sorted(Comparator.comparingDouble(StoreDistance::distanceInKilometers))
                .limit(howMany)
                .toList();
    }
}
