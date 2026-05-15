package com.arc_e_tect.examples.mfeadapter.application.exception;

/**
 * Thrown when a request cannot be authenticated, for example when the
 * session cookie is missing, invalid, or expired.
 */
public class AuthenticationException extends MfeAdapterException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
