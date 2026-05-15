package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.outbound.http;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedResponse;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.MicroserviceClientPort;
import com.arc_e_tect.book.sedr.mfeadapter.domain.spi.PassThroughRoutingStrategy;
import com.arc_e_tect.book.sedr.mfeadapter.domain.spi.RoutingStrategy;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.config.MfeAdapterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter that forwards HTTP requests to the associated microservice using a
 * mTLS-enabled {@link WebClient}.
 *
 * <p>The inner token is attached as a {@code Bearer} token in the
 * {@code Authorization} header.  The response body and headers are mapped
 * to a {@link ProxiedResponse} and returned to the calling application
 * service.
 *
 * <p>When a {@link RoutingStrategy} bean is present, the forwarding path is
 * resolved through it; otherwise {@link PassThroughRoutingStrategy} is used.
 *
 * <p>When {@code mfe-adapter.api-gateway.forward-scope=true} (the default) and
 * the request carries a non-null API-key scope, the scope is added to the
 * forwarded request as the header configured in
 * {@code mfe-adapter.api-gateway.scope-header}.
 */
@Component
public class MicroserviceRestClientAdapter implements MicroserviceClientPort {

    private static final Logger log = LoggerFactory.getLogger(MicroserviceRestClientAdapter.class);

    private final WebClient webClient;
    private final MfeAdapterProperties mfeAdapterProperties;
    private final RoutingStrategy routingStrategy;

    public MicroserviceRestClientAdapter(@Qualifier("microserviceWebClient") WebClient webClient,
                                          MfeAdapterProperties mfeAdapterProperties,
                                          Optional<RoutingStrategy> routingStrategy) {
        this.webClient = webClient;
        this.mfeAdapterProperties = mfeAdapterProperties;
        this.routingStrategy = routingStrategy.orElseGet(PassThroughRoutingStrategy::new);
        log.info("Routing strategy: {}", this.routingStrategy.getClass().getSimpleName());
    }

    @Override
    public ProxiedResponse forward(ProxiedRequest request, InnerToken innerToken) {
        String uri = buildUri(request);
        log.debug("Forwarding {} {} to MS: {}", request.getMethod(), request.getPath(), uri);

        MfeAdapterProperties.ApiGateway ag = mfeAdapterProperties.getApiGateway();
        boolean forwardScope = ag.isForwardScope() && request.getApiKeyScope() != null;

        ResponseEntity<byte[]> msResponse = webClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(uri)
                .headers(headers -> {
                    if (innerToken != null) {
                        headers.set("Authorization", "Bearer " + innerToken.tokenValue());
                    }
                    request.getHeaders().forEach(
                            (name, values) -> {
                                if (isForwardableHeader(name)) {
                                    headers.addAll(name, values);
                                }
                            });
                    if (forwardScope) {
                        headers.set(ag.getScopeHeader(), request.getApiKeyScope().name());
                    }
                })
                .bodyValue(request.getBody())
                .retrieve()
                .toEntity(byte[].class)
                .block();

        if (msResponse == null) {
            throw new IllegalStateException("Null response from microservice");
        }

        Map<String, List<String>> responseHeaders = new java.util.LinkedHashMap<>();
        msResponse.getHeaders().forEach(responseHeaders::put);

        return ProxiedResponse.builder()
                .statusCode(msResponse.getStatusCode().value())
                .headers(responseHeaders)
                .body(msResponse.getBody())
                .build();
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private String buildUri(ProxiedRequest request) {
        String path = routingStrategy.resolvePath(request);
        String query = request.getQueryString();
        return query != null && !query.isBlank() ? path + "?" + query : path;
    }

    private boolean isForwardableHeader(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        // Hop-by-hop, MFA-internal, and scope headers that must not be forwarded from inbound
        return switch (lower) {
            case "connection", "keep-alive", "proxy-authenticate",
                 "proxy-authorization", "te", "trailer", "transfer-encoding",
                 "upgrade", "cookie", "authorization",
                 "x-api-key", "x-api-key-validated", "x-api-key-scope" -> false;
            default -> true;
        };
    }
}
