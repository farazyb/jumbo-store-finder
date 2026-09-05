package com.example.storefinder.domain;

/**
 * The single exception the domain throws.
 *
 * <p>Messages state the rule that was broken, never the value that broke it. This text reaches
 * the log file, and a customer's request coordinates must not.
 */
public class StoreFinderException extends RuntimeException {

    public StoreFinderException(String message) {
        super(message);
    }

    public StoreFinderException(String message, Throwable cause) {
        super(message, cause);
    }
}
