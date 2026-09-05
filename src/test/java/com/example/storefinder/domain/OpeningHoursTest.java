package com.example.storefinder.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OpeningHours")
class OpeningHoursTest {

    private static final LocalTime OPENS_AT = LocalTime.of(8, 0);
    private static final LocalTime CLOSES_AT = LocalTime.of(20, 0);

    @ParameterizedTest(name = "at {0} is open: {1}")
    @CsvSource({
            "07:59, false",
            "08:00, true",
            "19:59, true",
            "20:00, false"
    })
    @DisplayName("is open from the opening time up to but not including the closing time")
    void isOpenBetweenOpeningAndClosingTime(LocalTime time, boolean expectedToBeOpen) {
        // GIVEN a store open from 08:00 to 20:00
        OpeningHours openingHours = new OpeningHours(OPENS_AT, CLOSES_AT);

        // WHEN the store is asked whether it is open at each side of both boundaries
        boolean open = openingHours.isOpenAt(time);

        // THEN the answer treats the opening time as open and the closing time as closed
        assertEquals(expectedToBeOpen, open);
    }

    @Test
    @DisplayName("is never open when closed all day")
    void isNeverOpenWhenClosedAllDay() {
        // GIVEN a store that is closed all day
        OpeningHours openingHours = OpeningHours.closedAllDay();

        // WHEN the store is asked whether it is open during what would be business hours
        boolean open = openingHours.isOpenAt(LocalTime.of(12, 0));

        // THEN it is closed
        assertFalse(open);
    }

    @Test
    @DisplayName("reports closed all day only when it has no hours")
    void reportsClosedAllDayOnlyWithoutHours() {
        // GIVEN one store closed all day and one with normal opening hours
        OpeningHours closed = OpeningHours.closedAllDay();
        OpeningHours open = new OpeningHours(OPENS_AT, CLOSES_AT);

        // WHEN each is asked whether it is closed all day
        // THEN only the one without hours says so
        assertTrue(closed.isClosedAllDay());
        assertFalse(open.isClosedAllDay());
    }

    @Test
    @DisplayName("rejects a store that closes before it opens")
    void rejectsClosingBeforeOpening() {
        // GIVEN a closing time at or before the opening time

        // WHEN opening hours are constructed from them
        // THEN construction fails rather than producing a store that is never open
        assertThrows(IllegalArgumentException.class, () -> new OpeningHours(CLOSES_AT, OPENS_AT));
        assertThrows(IllegalArgumentException.class, () -> new OpeningHours(OPENS_AT, OPENS_AT));
    }

    @Test
    @DisplayName("rejects one time without the other, which is not a state a store can be in")
    void rejectsOneTimeWithoutTheOther() {
        // GIVEN only an opening time, or only a closing time

        // WHEN opening hours are constructed from the half-pair
        // THEN construction fails rather than producing a store that opens but never closes
        assertThrows(IllegalArgumentException.class, () -> new OpeningHours(OPENS_AT, null));
        assertThrows(IllegalArgumentException.class, () -> new OpeningHours(null, CLOSES_AT));
    }
}
