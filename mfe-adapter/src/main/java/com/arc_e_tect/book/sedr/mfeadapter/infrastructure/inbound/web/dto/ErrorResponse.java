package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standardised error response body returned for all MFA error conditions.
 *
 * @param timestamp ISO-8601 timestamp of the error
 * @param status    HTTP status code
 * @param error     short error label
 * @param messages  list of human-readable error descriptions
 * @param path      request path that triggered the error
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        List<String> messages,
        String path) {

    public static ErrorResponse of(int status, String error, List<String> messages, String path) {
        return new ErrorResponse(Instant.now(), status, error, messages, path);
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return of(status, error, List.of(message), path);
    }
}
