package com.example.storefinder.data;

import com.example.storefinder.domain.Address;
import com.example.storefinder.domain.Coordinates;
import com.example.storefinder.domain.OpeningHours;
import com.example.storefinder.domain.Store;
import com.example.storefinder.domain.StoreRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the stores from a JSON file once, at startup.
 *
 * <p>This is where the source format is absorbed: coordinates arrive as strings, opening hours
 * use the literal "Gesloten" for a store closed all day, {@code collectionPoint} is absent rather
 * than false, and {@code street3} is always blank. Everything downstream sees only domain types.
 *
 * <p>A record that cannot be read is skipped and logged, so one bad row does not cost the other
 * 586. Construction fails only when the file is missing, unparseable, or leaves nothing to serve.
 */
@Component
public class JsonStoreRepository implements StoreRepository {

    private static final Logger log = LoggerFactory.getLogger(JsonStoreRepository.class);

    /** The source writes this in place of a time when a store does not open at all today. */
    private static final String CLOSED_ALL_DAY = "Gesloten";

    private final List<Store> stores;

    public JsonStoreRepository(ResourceLoader resourceLoader,
                               @Value("${stores.data-location:classpath:stores.json}") String location) {
        this.stores = load(resourceLoader, location);
        log.info("Loaded {} stores from {}", stores.size(), location);
    }

    @Override
    public List<Store> findAll() {
        return stores;
    }

    private static List<Store> load(ResourceLoader resourceLoader, String location) {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new StoreDataException("Store data file not found: " + location);
        }

        StoresFile storesFile;
        try (InputStream contents = resource.getInputStream()) {
            storesFile = new ObjectMapper().readValue(contents, StoresFile.class);
        } catch (IOException unreadable) {
            throw new StoreDataException("Store data file could not be parsed: " + location, unreadable);
        }

        List<StoreRecord> records = storesFile == null || storesFile.stores() == null
                ? List.of()
                : storesFile.stores();

        List<Store> loaded = new ArrayList<>(records.size());
        int skipped = 0;
        for (StoreRecord record : records) {
            try {
                loaded.add(toStore(record));
            } catch (RuntimeException unusable) {
                skipped++;
                log.warn("Skipped store record {}: {}", record.uuid(), reasonOf(unusable));
            }
        }

        if (loaded.isEmpty()) {
            throw new StoreDataException("Store data file contains no usable stores: " + location);
        }
        if (skipped > 0) {
            log.warn("Skipped {} of {} store records", skipped, records.size());
        }
        return List.copyOf(loaded);
    }

    private static Store toStore(StoreRecord record) {
        return new Store(
                record.uuid(),
                new Coordinates(parseCoordinate(record.latitude(), "latitude"),
                        parseCoordinate(record.longitude(), "longitude")),
                new Address(record.addressName(), record.street(), record.street2(),
                        blankToNull(record.street3()), record.postalCode(), record.city()),
                toOpeningHours(record.todayOpen(), record.todayClose()),
                record.locationType(),
                // Absent on the 213 supermarkets that are not collection points.
                Boolean.TRUE.equals(record.collectionPoint()));
    }

    private static OpeningHours toOpeningHours(String todayOpen, String todayClose) {
        if (CLOSED_ALL_DAY.equals(todayOpen) || CLOSED_ALL_DAY.equals(todayClose)) {
            return OpeningHours.closedAllDay();
        }
        return new OpeningHours(parseTime(todayOpen, "todayOpen"), parseTime(todayClose, "todayClose"));
    }

    // The JDK's own parse failures name neither the field nor, for a null, anything useful:
    // LocalTime.parse(null) reports only "text". These say which field and what was in it.

    private static double parseCoordinate(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is missing");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException notANumber) {
            throw new IllegalArgumentException(field + " is not a number: " + value);
        }
    }

    private static LocalTime parseTime(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is missing");
        }
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException notATime) {
            throw new IllegalArgumentException(field + " is not a time: " + value);
        }
    }

    /** Some exceptions carry no message at all, and "null" in the log helps nobody. */
    private static String reasonOf(RuntimeException unusable) {
        String message = unusable.getMessage();
        return message == null || message.isBlank() ? unusable.getClass().getSimpleName() : message;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StoresFile(List<StoreRecord> stores) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StoreRecord(String uuid, String latitude, String longitude,
                               String addressName, String street, String street2, String street3,
                               String postalCode, String city,
                               String todayOpen, String todayClose,
                               String locationType, Boolean collectionPoint) {
    }
}
