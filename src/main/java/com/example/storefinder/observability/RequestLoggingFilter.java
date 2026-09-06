package com.example.storefinder.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_KEY = "requestId";

    private static final String API_PATH_PREFIX = "/api/";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /** Only the service API is worth a line. Swagger, actuator and static assets are not. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(API_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = requestIdOf(request);
        MDC.put(REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startedAt = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long millisTaken = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("{} {} -> {} in {}ms", request.getMethod(), request.getRequestURI(),
                    response.getStatus(), millisTaken);

            // Threads are pooled, so leaving this behind would mislabel the next request.
            MDC.remove(REQUEST_ID_KEY);
        }
    }

    private static String requestIdOf(HttpServletRequest request) {
        String inbound = request.getHeader(REQUEST_ID_HEADER);
        return inbound == null || inbound.isBlank() ? UUID.randomUUID().toString() : inbound;
    }
}
