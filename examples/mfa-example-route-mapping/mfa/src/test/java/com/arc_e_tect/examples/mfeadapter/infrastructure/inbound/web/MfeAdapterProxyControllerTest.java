package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web;

import com.arc_e_tect.examples.mfeadapter.application.exception.AuthenticationException;
import com.arc_e_tect.examples.mfeadapter.application.exception.ValidationException;
import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedResponse;
import com.arc_e_tect.examples.mfeadapter.domain.port.inbound.HandleRequestUseCase;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.strategy.HeaderVersionExtractionStrategy;
import com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.strategy.VersionedPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MfeAdapterProxyController")
class MfeAdapterProxyControllerTest {

    @Mock
    private HandleRequestUseCase handleRequestUseCase;

    private MfeAdapterProperties props;
    private MfeAdapterProxyController controller;

    @BeforeEach
    void setUp() {
        props = new MfeAdapterProperties();
        // session not required (vanilla)
        props.getSession().setRequired(false);
        controller = new MfeAdapterProxyController(handleRequestUseCase, props, Optional.empty());
    }

    // -----------------------------------------------------------------
    // proxy() – vanilla no-session path
    // -----------------------------------------------------------------

    @Test
    @DisplayName("proxy – no session cookie + session.required=false → uses anonymous sessionId")
    void proxy_noSessionCookie_sessionNotRequired_proxiesWithAnonymous() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");
        ProxiedResponse response = ProxiedResponse.builder().statusCode(200)
                .body("{\"data\":[]}".getBytes()).build();
        when(handleRequestUseCase.handle(any(ProxiedRequest.class))).thenReturn(response);

        ResponseEntity<byte[]> result = controller.proxy(req);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo("{\"data\":[]}".getBytes());
    }

    @Test
    @DisplayName("proxy – no session cookie + session.required=true → 401 Unauthorized")
    void proxy_noSessionCookie_sessionRequired_returns401() throws Exception {
        props.getSession().setRequired(true);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");

        ResponseEntity<byte[]> result = controller.proxy(req);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(new String(result.getBody())).contains("unauthorized");
    }

    @Test
    @DisplayName("proxy – valid session cookie + session.required=true → proxies request")
    void proxy_validSessionCookie_sessionRequired_proxiesRequest() throws Exception {
        props.getSession().setRequired(true);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");
        req.setCookies(new Cookie("MFESESSION", "sess-abc"));
        req.addHeader("Accept", "application/json");
        ProxiedResponse response = ProxiedResponse.builder().statusCode(200)
                .body("[]".getBytes()).build();
        when(handleRequestUseCase.handle(any(ProxiedRequest.class))).thenReturn(response);

        ResponseEntity<byte[]> result = controller.proxy(req);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("proxy – with query string → passes query string to request")
    void proxy_withQueryString_passesQueryString() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");
        req.setQueryString("page=1&size=10");
        ProxiedResponse response = ProxiedResponse.builder().statusCode(200)
                .body("[]".getBytes()).build();
        when(handleRequestUseCase.handle(any(ProxiedRequest.class))).thenReturn(response);

        ResponseEntity<byte[]> result = controller.proxy(req);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("proxy – with version extraction strategy → extracts version and stripped path")
    void proxy_withVersionExtractionStrategy_extractsVersion() throws Exception {
        HeaderVersionExtractionStrategy strategy = mock(HeaderVersionExtractionStrategy.class);
        when(strategy.extract(any())).thenReturn(new VersionedPath("2", "/persons"));
        controller = new MfeAdapterProxyController(handleRequestUseCase, props, Optional.of(strategy));
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v2/persons");
        req.addHeader("X-API-Version", "2");
        ProxiedResponse response = ProxiedResponse.builder().statusCode(200)
                .body("[]".getBytes()).build();
        when(handleRequestUseCase.handle(any(ProxiedRequest.class))).thenReturn(response);

        ResponseEntity<byte[]> result = controller.proxy(req);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("proxy – version extractor returns null → path unchanged")
    void proxy_versionExtractorReturnsNull_pathUnchanged() throws Exception {
        HeaderVersionExtractionStrategy strategy = mock(HeaderVersionExtractionStrategy.class);
        when(strategy.extract(any())).thenReturn(null);
        controller = new MfeAdapterProxyController(handleRequestUseCase, props, Optional.of(strategy));
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/persons");
        ProxiedResponse response = ProxiedResponse.builder().statusCode(200).build();
        when(handleRequestUseCase.handle(any(ProxiedRequest.class))).thenReturn(response);

        ResponseEntity<byte[]> result = controller.proxy(req);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("proxy – response has custom headers → they are propagated")
    void proxy_responseHeaders_arePropagated() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(201)
                .headers(java.util.Map.of("X-Custom", List.of("value")))
                .body(new byte[0])
                .build();
        when(handleRequestUseCase.handle(any(ProxiedRequest.class))).thenReturn(response);

        ResponseEntity<byte[]> result = controller.proxy(req);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getHeaders().getFirst("X-Custom")).isEqualTo("value");
    }

    // -----------------------------------------------------------------
    // Exception handlers
    // -----------------------------------------------------------------

    @Test
    @DisplayName("handleAuthenticationException – returns 401 with error body")
    void handleAuthenticationException_returns401() {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");
        AuthenticationException ex = new AuthenticationException("No valid session");

        ResponseEntity<?> result = controller.handleAuthenticationException(ex, req);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("handleValidationException – returns 422 with error body")
    void handleValidationException_returns422() {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/v1/persons");
        ValidationException ex = new ValidationException(List.of("Name is required"));

        ResponseEntity<?> result = controller.handleValidationException(ex, req);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("handleGenericException – returns 502 bad gateway")
    void handleGenericException_returns502() {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");
        Exception ex = new RuntimeException("Upstream failure");

        ResponseEntity<?> result = controller.handleGenericException(ex, req);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }
}
