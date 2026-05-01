package com.arc_e_tect.book.sedr.mfeadapter.domain.model;

import java.util.List;

/**
 * Immutable result of a validation operation.
 *
 * @param valid   {@code true} when the validated value passed all checks
 * @param errors  human-readable error descriptions; empty when {@code valid} is {@code true}
 */
public record ValidationResult(boolean valid, List<String> errors) {

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, List.copyOf(errors));
    }

    public static ValidationResult failure(String error) {
        return new ValidationResult(false, List.of(error));
    }
}
