package com.example.storefinder.api;

import com.example.storefinder.api.dto.AddressDto;
import com.example.storefinder.api.dto.CoordinatesDto;
import com.example.storefinder.api.dto.NearestStoreResponse;
import com.example.storefinder.api.dto.OpeningHoursDto;
import com.example.storefinder.domain.Address;
import com.example.storefinder.domain.Coordinates;
import com.example.storefinder.domain.NearestStoreFinder;
import com.example.storefinder.domain.OpeningHours;
import com.example.storefinder.domain.Store;
import com.example.storefinder.domain.StoreDistance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@Tag(name = "Stores", description = "Finding the Jumbo stores closest to a position.")
public class NearestStoresController {

    /** The assignment asks for the five closest stores, so this is not a request parameter. */
    private static final int NEAREST_STORE_COUNT = 5;

    private final NearestStoreFinder nearestStoreFinder;
    private final Clock clock;

    public NearestStoresController(NearestStoreFinder nearestStoreFinder, Clock clock) {
        this.nearestStoreFinder = nearestStoreFinder;
        this.clock = clock;
    }

    @GetMapping("/nearest")
    @Operation(
            summary = "Find the 5 stores nearest a position",
            description = """
                    Takes a position as `lat` and `lon` in decimal degrees, and returns the five \
                    Jumbo stores closest to it, nearest first, each with its distance in \
                    kilometres.

                    Set `onlyOpen=true` to consider just the stores open at the moment of the \
                    request; the five nearest **open** stores are then returned, not the open \
                    ones among the nearest five. Fewer than five results is a valid answer when \
                    too few stores qualify, and an empty array is not an error.

                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Up to five stores, ordered by ascending distance."),
            @ApiResponse(responseCode = "400",
                    description = "A coordinate is missing, not a number, or out of range.")
    })
    public List<NearestStoreResponse> findNearestStores(

            @Parameter(description = "Latitude in decimal degrees, between -90 and 90.",
                    required = true, example = "52.3676")
            @RequestParam double lat,

            @Parameter(description = "Longitude in decimal degrees, between -180 and 180.",
                    required = true, example = "4.9041")
            @RequestParam double lon,

            @Parameter(description = "Return only stores open right now.", example = "false")
            @RequestParam(defaultValue = "false") boolean onlyOpen) {

        Coordinates origin = new Coordinates(lat, lon);
        LocalTime now = LocalTime.now(clock);

        List<StoreDistance> found = onlyOpen
                ? nearestStoreFinder.findNearestOpenAt(origin, NEAREST_STORE_COUNT, now)
                : nearestStoreFinder.findNearest(origin, NEAREST_STORE_COUNT);

        return found.stream().map(storeDistance -> toResponse(storeDistance, now)).toList();
    }

    private static NearestStoreResponse toResponse(StoreDistance storeDistance, LocalTime now) {
        Store store = storeDistance.store();
        return new NearestStoreResponse(
                store.uuid(),
                roundToTwoDecimals(storeDistance.distanceInKilometers()),
                new CoordinatesDto(store.coordinates().latitude(), store.coordinates().longitude()),
                store.openingHours().isOpenAt(now),
                toOpeningHoursDto(store.openingHours()),
                toAddressDto(store.address()),
                store.locationType(),
                store.collectionPoint());
    }

    private static OpeningHoursDto toOpeningHoursDto(OpeningHours openingHours) {
        if (openingHours.isClosedAllDay()) {
            return new OpeningHoursDto(OpeningHoursDto.Status.ALL_DAY_CLOSED, null, null);
        }
        return new OpeningHoursDto(OpeningHoursDto.Status.OPEN_TODAY,
                openingHours.opensAt(), openingHours.closesAt());
    }

    private static AddressDto toAddressDto(Address address) {
        return new AddressDto(address.addressName(), address.street(), address.street2(),
                address.street3(), address.postalCode(), address.city());
    }

    /** Distance is presentational here; ordering still uses the unrounded value. */
    private static double roundToTwoDecimals(double distanceInKilometers) {
        return Math.round(distanceInKilometers * 100.0) / 100.0;
    }
}
