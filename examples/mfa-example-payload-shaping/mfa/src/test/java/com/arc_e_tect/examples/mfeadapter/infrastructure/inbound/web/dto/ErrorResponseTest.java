package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse")
class ErrorResponseTest {

    @Test
    @DisplayName("of(int, String, List, String) – all fields set correctly")
    void of_withList_setsAllFields() {
        ErrorResponse er = ErrorResponse.of(422, "Unprocessable Entity",
                List.of("Name required", "Email invalid"), "/v1/persons");

        assertThat(er.status()).isEqualTo(422);
        assertThat(er.error()).isEqualTo("Unprocessable Entity");
        assertThat(er.messages()).containsExactly("Name required", "Email invalid");
        assertThat(er.path()).isEqualTo("/v1/persons");
        assertThat(er.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("of(int, String, String, String) – single message is wrapped in list")
    void of_withString_wrapsSingleMessage() {
        ErrorResponse er = ErrorResponse.of(401, "Unauthorized", "No session", "/v1/persons");

        assertThat(er.status()).isEqualTo(401);
        assertThat(er.messages()).containsExactly("No session");
    }
}
