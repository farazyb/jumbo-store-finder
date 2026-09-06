package com.example.storefinder.data;

/**
 * The store data as a whole is unusable, so the application cannot start.
 *
 * <p>A single unreadable record is not this: those are skipped and logged. This is raised only
 * when the file is missing, cannot be parsed, or leaves nothing to serve.
 */
public class StoreDataException extends RuntimeException {

    public StoreDataException(String message) {
        super(message);
    }

    public StoreDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
