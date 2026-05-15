package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter;

import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("ApiKeyValidationFilter")
class ApiKeyValidationFilterTest {

    // -----------------------------------------------------------------
    // Filter with no validator configured (default / no-op)
    // -----------------------------------------------------------------

    @Nested
    @DisplayName("when no ApiKeyValidator is configured")
    class NoValidator {

        private final ApiKeyValidationFilter filter =
                new ApiKeyValidationFilter(Optional.empty());

        @Test
        @DisplayName("any request passes through without inspection")
        void doFilter_noValidator_alwaysPassesThrough() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
            // deliberately no validation header
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("no scope attribute is set when validator is absent")
        void doFilter_noValidator_noScopeAttribute() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            assertThat(request.getAttribute(ApiKeyValidationFilter.SCOPE_ATTRIBUTE)).isNull();
        }
    }

    // -----------------------------------------------------------------
    // Filter with StandardApiKeyValidator configured
    // -----------------------------------------------------------------

    @Nested
    @DisplayName("when StandardApiKeyValidator is configured")
    class WithStandardValidator {

        private final MfeAdapterProperties properties = new MfeAdapterProperties();
        // Defaults: header = "X-API-Key-Validated", value = "true"
        private final ApiKeyValidationFilter filter =
                new ApiKeyValidationFilter(Optional.of(new StandardApiKeyValidator(properties)));

        @Test
        @DisplayName("request with valid header → passes through filter chain")
        void doFilter_validHeader_passesThrough() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
            request.addHeader("X-API-Key-Validated", "true");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("request with valid header → scope attribute READ is set (no scope header)")
        void doFilter_validHeader_defaultScopeRead() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
            request.addHeader("X-API-Key-Validated", "true");
            // no scope header → should default to READ
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            assertThat(request.getAttribute(ApiKeyValidationFilter.SCOPE_ATTRIBUTE))
                    .isEqualTo(ApiKeyScope.READ);
        }

        @Test
        @DisplayName("request with valid header and WRITE scope → scope attribute WRITE is set")
        void doFilter_validHeader_writeScopeForwarded() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/items");
            request.addHeader("X-API-Key-Validated", "true");
            request.addHeader("X-API-Key-Scope", "WRITE");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            assertThat(request.getAttribute(ApiKeyValidationFilter.SCOPE_ATTRIBUTE))
                    .isEqualTo(ApiKeyScope.WRITE);
        }

        @Test
        @DisplayName("request without validation header → 403 returned")
        void doFilter_missingHeader_returns403() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
            verifyNoInteractions(chain);
        }

        @Test
        @DisplayName("request with header value 'false' → 403 returned")
        void doFilter_falseHeaderValue_returns403() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
            request.addHeader("X-API-Key-Validated", "false");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
            verifyNoInteractions(chain);
        }

        @Test
        @DisplayName("header value matching is case-insensitive")
        void doFilter_headerValueCaseInsensitive_passesThrough() throws Exception {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
            request.addHeader("X-API-Key-Validated", "TRUE");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    // -----------------------------------------------------------------
    // Actuator exemptions (independent of validator)
    // -----------------------------------------------------------------

    @Nested
    @DisplayName("shouldNotFilter exemptions")
    class Exemptions {

        private final ApiKeyValidationFilter filter =
                new ApiKeyValidationFilter(Optional.of(
                        new StandardApiKeyValidator(new MfeAdapterProperties())));

        @Test
        @DisplayName("actuator health endpoint is exempt from validation")
        void shouldNotFilter_actuatorHealth_returnsTrue() {
            MockHttpServletRequest healthRequest = new MockHttpServletRequest("GET", "/actuator/health");
            assertThat(filter.shouldNotFilter(healthRequest)).isTrue();
        }

        @Test
        @DisplayName("/auth/login is exempt – browser redirect carries no API-key header")
        void shouldNotFilter_authLogin_returnsTrue() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("/auth/callback is exempt – IdP redirect carries no API-key header")
        void shouldNotFilter_authCallback_returnsTrue() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/callback");
            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("/auth/logout is exempt – browser navigation carries no API-key header")
        void shouldNotFilter_authLogout_returnsTrue() {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/logout");
            assertThat(filter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("API endpoint is NOT exempt from validation")
        void shouldNotFilter_apiEndpoint_returnsFalse() {
            MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/api/customers");
            assertThat(filter.shouldNotFilter(apiRequest)).isFalse();
        }
    }
}
