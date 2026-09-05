package com.example.storefinder.domain;

import java.time.LocalTime;

/**
 * A store's opening hours for today. The source data carries only today, so there is no weekly
 * schedule to model.
 *
 * <p>A store closed all day has both fields absent; {@link #closedAllDay()} builds that state and
 * {@link #isClosedAllDay()} recognises it. One time without the other is not a valid state.
 *
 * @param opensAt  when the store opens, or null when closed all day
 * @param closesAt when the store closes, or null when closed all day
 */
public record OpeningHours(LocalTime opensAt, LocalTime closesAt) {

    private static final OpeningHours CLOSED_ALL_DAY = new OpeningHours(null, null);

    // A public record cannot hide its canonical constructor, so every rule lives here. There is
    // no other way in, and no way to reach an invalid state through it.
    public OpeningHours {
        if ((opensAt == null) != (closesAt == null)) {
            throw new IllegalArgumentException(
                    "Opening and closing time must both be present or both be absent");
        }
        if (opensAt != null && !opensAt.isBefore(closesAt)) {
            throw new IllegalArgumentException("Opening time must be before closing time");
        }
    }

    /** Names the state that {@code new OpeningHours(null, null)} does not express. */
    public static OpeningHours closedAllDay() {
        return CLOSED_ALL_DAY;
    }

    public boolean isClosedAllDay() {
        return opensAt == null;
    }

    /**
     * Whether the store is open at the given time. The opening time counts as open, the
     * closing time does not.
     */
    public boolean isOpenAt(LocalTime time) {
        return !isClosedAllDay() && !time.isBefore(opensAt) && time.isBefore(closesAt);
    }
}
