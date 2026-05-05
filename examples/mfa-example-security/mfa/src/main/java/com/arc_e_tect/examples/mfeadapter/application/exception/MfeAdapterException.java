package com.arc_e_tect.examples.mfeadapter.application.exception;

/**
 * Base runtime exception for all MFA-specific failures.
 */
public class MfeAdapterException extends RuntimeException {

    public MfeAdapterException(String message) {
        super(message);
    }

    public MfeAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
