package com.arc_e_tect.examples.mfeadapter.application.exception;

import java.util.List;

/**
 * Thrown when one or more {@link com.arc_e_tect.examples.mfeadapter.domain.spi.RequestValidator}
 * implementations reject the inbound request.
 */
public class ValidationException extends MfeAdapterException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Request validation failed: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
