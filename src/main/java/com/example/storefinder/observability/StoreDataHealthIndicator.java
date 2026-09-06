package com.example.storefinder.observability;

import com.example.storefinder.domain.StoreRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Reports whether the store data is loaded. Joins the readiness group, so the application is
 * only ready to serve once it has stores to serve.
 */
@Component
public class StoreDataHealthIndicator implements HealthIndicator {

    private final StoreRepository storeRepository;

    public StoreDataHealthIndicator(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public Health health() {
        int storeCount = storeRepository.findAll().size();

        return storeCount == 0
                ? Health.down().withDetail("storeCount", storeCount).build()
                : Health.up().withDetail("storeCount", storeCount).build();
    }
}
