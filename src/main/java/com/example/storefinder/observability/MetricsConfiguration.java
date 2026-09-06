package com.example.storefinder.observability;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class MetricsConfiguration {

    private static final Set<String> EXPORTED_METRICS = Set.of(
            "http.server.requests",
            "logback.events",
            "jvm.memory.used",
            "process.cpu.usage");

    @Bean
    MeterFilter exportOnlyEssentialMetrics() {
        return MeterFilter.denyUnless(meterId -> EXPORTED_METRICS.contains(meterId.getName()));
    }
}
