package com.example.storefinder.data;

import com.example.storefinder.domain.Address;
import com.example.storefinder.domain.Coordinates;
import com.example.storefinder.domain.OpeningHours;
import com.example.storefinder.domain.Store;
import com.example.storefinder.domain.StoreFinderException;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the stores from a JSON file once, at startup.
 *
 * <p>This is where the source format is absorbed: coordinates arrive as strings, opening hours
 * use the literal "Gesloten" for a store closed all day, {@code collectionPoint} is absent rather
 * than false, and {@code street3} is always blank. Everything downstream sees only domain types.
 *
 * <p>If the file is missing, unreadable or holds no stores, construction fails and the
 * application does not start.
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
            throw new StoreFinderException("No store data found at " + location);
        }

        StoresFile storesFile;
        try (InputStream contents = resource.getInputStream()) {
            storesFile = new ObjectMapper().readValue(contents, StoresFile.class);
        } catch (IOException unreadable) {
            throw new StoreFinderException("Store data at " + location + " could not be read", unreadable);
        }

        if (storesFile == null || storesFile.stores() == null || storesFile.stores().isEmpty()) {
            throw new StoreFinderException("Store data at " + location + " holds no stores");
        }

        List<Store> loaded = new ArrayList<>(storesFile.stores().size());
        for (StoreRecord record : storesFile.stores()) {
            loaded.add(toStore(record));
        }
        return List.copyOf(loaded);
    }

    private static Store toStore(StoreRecord record) {
        try {
            return new Store(
                    record.uuid(),
                    new Coordinates(Double.parseDouble(record.latitude()),
                            Double.parseDouble(record.longitude())),
                    new Address(record.addressName(), record.street(), record.street2(),
                            blankToNull(record.street3()), record.postalCode(), record.city()),
                    toOpeningHours(record.todayOpen(), record.todayClose()),
                    record.locationType(),
                    // Absent on the 213 supermarkets that are not collection points.
                    Boolean.TRUE.equals(record.collectionPoint()));
        } catch (RuntimeException unusable) {
            log.error("Cannot parse store record {}: {}", record.uuid(), unusable.getMessage());
            throw new StoreFinderException("Store data holds a record that cannot be read", unusable);
        }
    }

    private static OpeningHours toOpeningHours(String todayOpen, String todayClose) {
        if (CLOSED_ALL_DAY.equals(todayOpen) || CLOSED_ALL_DAY.equals(todayClose)) {
            return OpeningHours.closedAllDay();
        }
        return new OpeningHours(LocalTime.parse(todayOpen), LocalTime.parse(todayClose));
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
