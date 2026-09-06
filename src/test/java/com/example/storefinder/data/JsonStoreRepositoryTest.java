package com.example.storefinder.data;

import com.example.storefinder.domain.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JsonStoreRepository")
class JsonStoreRepositoryTest {

    private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();
    private static final String SEED_FILE = "classpath:stores.json";

    private static final int STORES_IN_SEED_FILE = 587;
    private static final int STORES_WITHOUT_COLLECTION_POINT = 213;

    /** Alkmaar Duijvelshoff, one of the two stores the source marks "Gesloten". */
    private static final String CLOSED_ALL_DAY_STORE_UUID = "V7cKYx4X0QUAAAFMTmYM5CXj";

    @Test
    @DisplayName("loads every store from the seed file")
    void loadsEveryStoreFromTheSeedFile() {
        // GIVEN the seed file shipped with the application

        // WHEN the repository is built from it
        List<Store> stores = new JsonStoreRepository(RESOURCE_LOADER, SEED_FILE).findAll();

        // THEN every record is present, with its string coordinates parsed into a position
        assertEquals(STORES_IN_SEED_FILE, stores.size());
        assertTrue(stores.stream().allMatch(store -> store.coordinates() != null));
    }

    @Test
    @DisplayName("maps the literal \"Gesloten\" to hours that are closed all day")
    void mapsGeslotenToClosedAllDay() {
        // GIVEN the seed file, in which two stores carry "Gesloten" instead of a time
        List<Store> stores = new JsonStoreRepository(RESOURCE_LOADER, SEED_FILE).findAll();

        // WHEN one of those stores is looked up
        Store closedStore = stores.stream()
                .filter(store -> store.uuid().equals(CLOSED_ALL_DAY_STORE_UUID))
                .findFirst()
                .orElseThrow();

        // THEN it parsed without error and reports itself closed for the whole day
        assertTrue(closedStore.openingHours().isClosedAllDay());
    }

    @Test
    @DisplayName("reads an absent collectionPoint as false rather than leaving it unset")
    void readsAbsentCollectionPointAsFalse() {
        // GIVEN the seed file, which omits collectionPoint on the plain supermarkets

        // WHEN the repository is built from it
        List<Store> stores = new JsonStoreRepository(RESOURCE_LOADER, SEED_FILE).findAll();

        // THEN the omission became false, and street3 became null because the source leaves it blank
        assertEquals(STORES_WITHOUT_COLLECTION_POINT,
                stores.stream().filter(store -> !store.collectionPoint()).count());
        assertTrue(stores.stream().allMatch(store -> store.address().street3() == null));
    }

    @Test
    @DisplayName("skips a record it cannot read and keeps the rest")
    void skipsRecordItCannotReadAndKeepsTheRest() {
        // GIVEN a file holding one usable record and one with an out-of-range longitude

        // WHEN the repository is built from it
        List<Store> stores = new JsonStoreRepository(RESOURCE_LOADER,
                "classpath:partly-broken-stores.json").findAll();

        // THEN the good record is served and only the bad one is dropped: one unreadable row
        // must not cost the whole file
        assertEquals(1, stores.size());
        assertEquals("usable-record", stores.get(0).uuid());
    }

    @Test
    @DisplayName("refuses to start when the file is missing")
    void refusesToStartWhenTheFileIsMissing() {
        // GIVEN a location that points at no file

        // WHEN the repository is built from it
        StoreDataException thrown = assertThrows(StoreDataException.class,
                () -> new JsonStoreRepository(RESOURCE_LOADER, "classpath:no-such-file.json"));

        // THEN it fails immediately, so the application cannot come up without its data
        assertTrue(thrown.getMessage().contains("no-such-file.json"));
    }

    @Test
    @DisplayName("refuses to start when the file holds no stores")
    void refusesToStartWhenTheFileHoldsNoStores() {
        // GIVEN a file that exists but is empty

        // WHEN the repository is built from it
        StoreDataException thrown = assertThrows(StoreDataException.class,
                () -> new JsonStoreRepository(RESOURCE_LOADER, "classpath:empty-stores.json"));

        // THEN an existing but useless file is treated the same as a missing one
        assertTrue(thrown.getMessage().contains("empty-stores.json"));
    }
}
