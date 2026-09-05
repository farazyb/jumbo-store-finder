package com.example.storefinder.domain;

/**
 * A store's postal address.
 *
 * @param addressName the store's display name, such as "Jumbo Amsterdam Bos en Lommerplein"
 * @param street      street name
 * @param street2     house number
 * @param street3     house number addition, null when the source leaves it blank
 * @param postalCode  Dutch postal code, such as "1055 EK"
 * @param city        city name
 */
public record Address(String addressName, String street, String street2, String street3,
                      String postalCode, String city) {
}
