package com.example.storefinder.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

/**
 * The store's hours for today. The source carries only today, so there is no weekly schedule.
 *
 * <p>When the status is {@code ALL_DAY_CLOSED} both times are null: the store does not open at
 * all today.
 */
@Schema(description = "The store's opening hours for today.")
public record OpeningHoursDto(

        @Schema(description = "Whether the store opens at all today.", example = "OPEN_TODAY")
        Status status,

        @Schema(description = "When the store opens, or null when closed all day.", example = "08:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime opensAt,

        @Schema(description = "When the store closes, or null when closed all day.", example = "22:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime closesAt) {

    @Schema(description = "OPEN_TODAY carries both times; ALL_DAY_CLOSED carries neither.")
    public enum Status {
        OPEN_TODAY,
        ALL_DAY_CLOSED
    }
}
