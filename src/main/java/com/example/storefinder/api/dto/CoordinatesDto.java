package com.example.storefinder.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Where the store is, in decimal degrees.")
public record CoordinatesDto(

        @Schema(description = "Degrees north of the equator.", example = "52.3554")
        double latitude,

        @Schema(description = "Degrees east of the prime meridian.", example = "4.8846")
        double longitude) {
}
