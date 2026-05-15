package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web;

import com.arc_e_tect.book.sedr.mfeadapter.application.exception.AuthenticationException;
import com.arc_e_tect.book.sedr.mfeadapter.application.exception.ValidationException;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedResponse;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.inbound.HandleRequestUseCase;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.config.MfeAdapterProperties;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.dto.ErrorResponse;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.filter.ApiKeyScope;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.filter.ApiKeyValidationFilter;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.strategy.VersionExtractionStrategy;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.strategy.VersionedPath;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catch-all controller that proxies every inbound HTTP request to the
 * associated microservice.
 *
 * <p>The controller is intentionally generic: it maps {@code /**} and
 * delegates all routing decisions to the application service layer.
 * MFA-specific behaviour is injected through the SPI interfaces
 * ({@link com.arc_e_tect.book.sedr.mfeadapter.domain.spi.RequestTransformer},
 * {@link com.arc_e_tect.book.sedr.mfeadapter.domain.spi.ResponseTransformer},
 * {@link com.arc_e_tect.book.sedr.mfeadapter.domain.spi.RequestValidator}).
 *
 * <p>When a {@link VersionExtractionStrategy} bean is present, the API version
 * is extracted from the request and stored on {@link ProxiedRequest#getVersion()}.
 */
@RestController
public class MfeAdapterProxyController {

    private static final Logger log = LoggerFactory.getLogger(MfeAdapterProxyController.class);

    private final HandleRequestUseCase handleRequestUseCase;
    private final MfeAdapterProperties mfeAdapterProperties;
    private final VersionExtractionStrategy versionExtractionStrategy;

    public MfeAdapterProxyController(HandleRequestUseCase handleRequestUseCase,
                               MfeAdapterProperties mfeAdapterProperties,
                               Optional<VersionExtractionStrategy> versionExtractionStrategy) {
        this.handleRequestUseCase = handleRequestUseCase;
        this.mfeAdapterProperties = mfeAdapterProperties;
        this.versionExtractionStrategy = versionExtractionStrategy.orElse(null);
        if (this.versionExtractionStrategy == null) {
            log.info("No VersionExtractionStrategy bean configured – version extraction is disabled");
        } else {
            log.info("Version extraction enabled via {}", this.versionExtractionStrategy.getClass().getSimpleName());
        }
    }

    /**
     * Proxy handler – matches all paths and HTTP methods that reach this
     * controller (everything except the auth and actuator paths).
     */
    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest httpRequest) throws IOException {
        String sessionId = extractSessionId(httpRequest);
        if (sessionId == null) {
            if (mfeAdapterProperties.getSession().isRequired()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"unauthorized\",\"message\":\"No session cookie\"}".getBytes());
        }
            sessionId = "anonymous";
        }

        ApiKeyScope scope = (ApiKeyScope) httpRequest.getAttribute(ApiKeyValidationFilter.SCOPE_ATTRIBUTE);

        String path = httpRequest.getRequestURI();
        String version = null;
        if (versionExtractionStrategy != null) {
            VersionedPath vp = versionExtractionStrategy.extract(httpRequest);
            if (vp != null) {
                version = vp.version();
                path = vp.strippedPath();
            }
        }

        ProxiedRequest request = ProxiedRequest.builder()
                .method(httpRequest.getMethod())
                .path(path)
                .queryString(httpRequest.getQueryString())
                .headers(extractHeaders(httpRequest))
                .body(StreamUtils.copyToByteArray(httpRequest.getInputStream()))
                .sessionId(sessionId)
                .apiKeyScope(scope)
                .version(version)
                .build();

        log.debug("Proxying {} {}", request.getMethod(), request.getPath());

        ProxiedResponse response = handleRequestUseCase.handle(request);

        HttpHeaders responseHeaders = new HttpHeaders();
        response.getHeaders().forEach(responseHeaders::addAll);

        return ResponseEntity
                .status(response.getStatusCode())
                .headers(responseHeaders)
                .body(response.getBody());
    }

    // -----------------------------------------------------------------
    // Exception handlers
    // -----------------------------------------------------------------

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, "unauthorized", ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        log.warn("Validation failure: {}", ex.getErrors());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(422, "unprocessable_entity", ex.getErrors(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error handling request to '{}'", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of(502, "bad_gateway", "Upstream service error",
                        request.getRequestURI()));
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private String extractSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String cookieName = mfeAdapterProperties.getSession().getCookieName();
        return Arrays.stream(cookies)
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private Map<String, List<String>> extractHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                headers.put(name, Collections.list(request.getHeaders(name)));
            }
        }
        return headers;
    }
}
