package com.arc_e_tect.examples.mfeadapter.application.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exception classes")
class ExceptionClassesTest {

    @Test
    @DisplayName("MfeAdapterException(String) – carries message")
    void mfeAdapterException_withMessage() {
        MfeAdapterException ex = new MfeAdapterException("oops");
        assertThat(ex.getMessage()).isEqualTo("oops");
    }

    @Test
    @DisplayName("MfeAdapterException(String, Throwable) – carries message and cause")
    void mfeAdapterException_withMessageAndCause() {
        Throwable cause = new RuntimeException("root");
        MfeAdapterException ex = new MfeAdapterException("wrapped", cause);
        assertThat(ex.getMessage()).isEqualTo("wrapped");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("AuthenticationException(String) – carries message")
    void authenticationException_withMessage() {
        AuthenticationException ex = new AuthenticationException("unauthorized");
        assertThat(ex.getMessage()).isEqualTo("unauthorized");
    }

    @Test
    @DisplayName("ValidationException(List) – errors accessible via getErrors()")
    void validationException_withErrors() {
        ValidationException ex = new ValidationException(List.of("Name is required", "Email invalid"));
        assertThat(ex.getErrors()).containsExactly("Name is required", "Email invalid");
    }
}
