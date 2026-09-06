package com.example.storefinder.config;

import com.example.storefinder.StoreFinderApplication;
import com.example.storefinder.domain.NearestStoreFinder;
import com.example.storefinder.domain.StoreRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Wires the framework-free domain into the application.
 *
 * <p>Kept out of {@link StoreFinderApplication} so that slice tests, which always process the
 * application class, can supply their own clock and finder without colliding with these.
 */
@Configuration
public class StoreFinderConfiguration {

    /** Every store in the data is Dutch, so "open now" is answered in Dutch local time. */
    private static final ZoneId STORE_ZONE = ZoneId.of("Europe/Amsterdam");

    @Bean
    Clock storeClock() {
        return Clock.system(STORE_ZONE);
    }

    @Bean
    NearestStoreFinder nearestStoreFinder(StoreRepository storeRepository) {
        return new NearestStoreFinder(storeRepository);
    }
}
