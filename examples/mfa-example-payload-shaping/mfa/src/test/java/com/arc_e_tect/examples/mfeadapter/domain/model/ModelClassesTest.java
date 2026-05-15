package com.arc_e_tect.examples.mfeadapter.domain.model;

import com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter.ApiKeyScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domain model classes")
class ModelClassesTest {

    // -----------------------------------------------------------------
    // InnerToken
    // -----------------------------------------------------------------

    @Test
    @DisplayName("InnerToken – accessors and isExpired for future token")
    void innerToken_notExpired() {
        InnerToken token = new InnerToken("tok-abc", Map.of("role", "admin"),
                Instant.now().plusSeconds(300));

        assertThat(token.tokenValue()).isEqualTo("tok-abc");
        assertThat(token.getClaim("role")).isEqualTo("admin");
        assertThat(token.getClaim("missing")).isNull();
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("InnerToken – isExpired returns true for past expiry")
    void innerToken_expired() {
        InnerToken token = new InnerToken("tok-old", Map.of(), Instant.now().minusSeconds(1));
        assertThat(token.isExpired()).isTrue();
    }

    // -----------------------------------------------------------------
    // UserToken
    // -----------------------------------------------------------------

    @Test
    @DisplayName("UserToken – isExpired returns false for future expiry")
    void userToken_notExpired() {
        UserToken token = new UserToken("raw-jwt", Instant.now().plusSeconds(600), "user@example.com");
        assertThat(token.isExpired()).isFalse();
        assertThat(token.subject()).isEqualTo("user@example.com");
    }

    // -----------------------------------------------------------------
    // ProxiedRequest.Builder
    // -----------------------------------------------------------------

    @Test
    @DisplayName("ProxiedRequest.Builder – apiKeyScope and version are set")
    void proxiedRequest_apiKeyScopeAndVersion() {
        ProxiedRequest req = ProxiedRequest.builder()
                .method("POST").path("/v2/persons")
                .version("2")
                .apiKeyScope(ApiKeyScope.WRITE)
                .build();

        assertThat(req.getVersion()).isEqualTo("2");
        assertThat(req.getApiKeyScope()).isEqualTo(ApiKeyScope.WRITE);
    }

    @Test
    @DisplayName("ProxiedRequest.Builder – body is copied defensively")
    void proxiedRequest_bodyDefensiveCopy() {
        byte[] original = "hello".getBytes();
        ProxiedRequest req = ProxiedRequest.builder().method("GET").path("/x").body(original).build();
        original[0] = 'X';

        assertThat(req.getBody()).startsWith((byte) 'h');
    }

    // -----------------------------------------------------------------
    // ValidationResult
    // -----------------------------------------------------------------

    @Test
    @DisplayName("ValidationResult.failure(String) – valid=false with single error")
    void validationResult_failureWithSingleMessage() {
        ValidationResult result = ValidationResult.failure("Required field missing");
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("Required field missing");
    }

    @Test
    @DisplayName("ValidationResult.failure(List) – valid=false with multiple errors")
    void validationResult_failureWithList() {
        ValidationResult result = ValidationResult.failure(List.of("E1", "E2"));
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).containsExactly("E1", "E2");
    }

    // -----------------------------------------------------------------
    // ProxiedResponse
    // -----------------------------------------------------------------

    @Test
    @DisplayName("ProxiedResponse – headers accessible via getHeaders()")
    void proxiedResponse_headersAccessible() {
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(200)
                .headers(Map.of("Content-Type", List.of("application/json")))
                .body("{}".getBytes())
                .build();

        assertThat(response.getHeaders()).containsKey("Content-Type");
        assertThat(response.getStatusCode()).isEqualTo(200);
    }
}
