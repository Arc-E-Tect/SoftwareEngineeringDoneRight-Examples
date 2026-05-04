package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiKeyScope")
class ApiKeyScopeTest {

    @Test
    @DisplayName("fromHeaderValue – known values parse correctly (case-insensitive)")
    void fromHeaderValue_knownValues() {
        assertThat(ApiKeyScope.fromHeaderValue("READ")).isEqualTo(ApiKeyScope.READ);
        assertThat(ApiKeyScope.fromHeaderValue("write")).isEqualTo(ApiKeyScope.WRITE);
        assertThat(ApiKeyScope.fromHeaderValue("Audit")).isEqualTo(ApiKeyScope.AUDIT);
    }

    @Test
    @DisplayName("fromHeaderValue – null → returns null")
    void fromHeaderValue_null_returnsNull() {
        assertThat(ApiKeyScope.fromHeaderValue(null)).isNull();
    }

    @Test
    @DisplayName("fromHeaderValue – blank → returns null")
    void fromHeaderValue_blank_returnsNull() {
        assertThat(ApiKeyScope.fromHeaderValue("  ")).isNull();
    }

    @Test
    @DisplayName("fromHeaderValue – unknown value → returns null")
    void fromHeaderValue_unknown_returnsNull() {
        assertThat(ApiKeyScope.fromHeaderValue("UNKNOWN_SCOPE")).isNull();
    }
}
