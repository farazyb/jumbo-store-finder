package com.example.storefinder.domain;

/**
 * A Jumbo store.
 *
 * @param uuid            the store's identifier
 * @param coordinates     where the store is
 * @param address         where to post a letter to it
 * @param openingHours    today's opening hours
 * @param locationType    "Supermarkt", "PuP" or "SupermarktPuP" in the current data; kept as text
 *                        so an unseen value cannot break startup
 * @param collectionPoint whether orders can be collected here
 */
public record Store(String uuid, Coordinates coordinates, Address address,
                    OpeningHours openingHours, String locationType, boolean collectionPoint) {
}
