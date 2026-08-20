package com.soham.railway_reservation_engine.common.exception;

/**
 * Base "resource missing" exception (→ HTTP 404 by convention). Domain-specific
 * subclasses like {@code BookingNotFoundException} specialise the message while
 * keeping a single handler path.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}
