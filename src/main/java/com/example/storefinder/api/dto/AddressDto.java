package com.example.storefinder.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The store's postal address.")
public record AddressDto(

        @Schema(description = "The store's display name.", example = "Jumbo Amsterdam Stadhouderskade")
        String addressName,

        @Schema(description = "Street name.", example = "Stadhouderskade")
        String street,

        @Schema(description = "House number.", example = "55")
        String street2,

        @Schema(description = "House number addition. Omitted when the store has none.", example = "A")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String street3,

        @Schema(description = "Dutch postal code.", example = "1072 AB")
        String postalCode,

        @Schema(description = "City name.", example = "Amsterdam")
        String city) {
}
