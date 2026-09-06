package com.example.storefinder.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A store near the searched position, with how far away it is.")
public record NearestStoreResponse(

        @Schema(description = "The store's identifier.", example = "EOgKYx4XFiQAAAFJa_YYZ4At")
        String uuid,

        @Schema(description = "Great-circle distance from the searched position, in kilometres, "
                + "rounded to two decimals. Results are ordered by this value, nearest first.",
                example = "1.25")
        double distanceKm,

        CoordinatesDto coordinates,

        @Schema(description = "Whether the store is open at the moment the request was handled.",
                example = "true")
        boolean openNow,

        OpeningHoursDto openingHours,

        AddressDto address,

        @Schema(description = "Kind of location: Supermarkt, PuP (pickup point) or SupermarktPuP.",
                example = "SupermarktPuP")
        String locationType,

        @Schema(description = "Whether orders can be collected at this store.", example = "true")
        boolean collectionPoint) {
}
