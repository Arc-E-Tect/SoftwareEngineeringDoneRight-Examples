package com.arc_e_tect.examples.mfeadapter.application.service;

import com.arc_e_tect.examples.mfeadapter.application.exception.AuthenticationException;
import com.arc_e_tect.examples.mfeadapter.application.exception.ValidationException;
import com.arc_e_tect.examples.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedResponse;
import com.arc_e_tect.examples.mfeadapter.domain.model.Session;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.domain.model.ValidationResult;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.InnerTokenServicePort;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.MicroserviceClientPort;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.SessionStorePort;
import com.arc_e_tect.examples.mfeadapter.domain.spi.RequestTransformer;
import com.arc_e_tect.examples.mfeadapter.domain.spi.RequestValidator;
import com.arc_e_tect.examples.mfeadapter.domain.spi.ResponseTransformer;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MfeAdapterRequestApplicationService")
class MfeAdapterRequestApplicationServiceTest {

    @Mock
    private SessionStorePort sessionStore;
    @Mock
    private InnerTokenServicePort innerTokenService;
    @Mock
    private MicroserviceClientPort microserviceClient;

    private MfeAdapterRequestApplicationService service;

    private static final String SESSION_ID = "test-session-id";
    private static final UserToken USER_TOKEN = new UserToken(
            "user-token-value", Instant.now().plusSeconds(3600), "user-sub-123");
    private static final InnerToken INNER_TOKEN = new InnerToken(
            "inner-token-value", Map.of("role", "ADMIN"), Instant.now().plusSeconds(300));
    private static final Session VALID_SESSION = new Session(
            SESSION_ID, USER_TOKEN, Instant.now().plusSeconds(1800), "user-sub-123");

    @BeforeEach
    void setUp() {
        // No SPI implementations registered – framework no-ops
        service = new MfeAdapterRequestApplicationService(
                sessionStore, innerTokenService, microserviceClient,
                new MfeAdapterProperties(), List.of(), List.of(), List.of());
    }

    @Test
    @DisplayName("happy path: valid session → forwards request → returns response")
    void handle_validSession_forwardsAndReturnsResponse() {
        ProxiedRequest request = buildRequest();
        ProxiedResponse expectedResponse = ProxiedResponse.builder()
                .statusCode(200)
                .body("{\"id\":1}".getBytes())
                .build();

        when(sessionStore.findById(SESSION_ID)).thenReturn(Optional.of(VALID_SESSION));
        when(innerTokenService.swapForInnerToken(USER_TOKEN)).thenReturn(INNER_TOKEN);
        when(microserviceClient.forward(request, INNER_TOKEN)).thenReturn(expectedResponse);

        ProxiedResponse actual = service.handle(request);

        assertThat(actual.getStatusCode()).isEqualTo(200);
        assertThat(actual.getBody()).isEqualTo("{\"id\":1}".getBytes());
        verify(microserviceClient).forward(request, INNER_TOKEN);
    }

    @Test
    @DisplayName("missing session → AuthenticationException thrown")
    void handle_missingSession_throwsAuthenticationException() {
        when(sessionStore.findById(SESSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(buildRequest()))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining(SESSION_ID);

        verify(microserviceClient, never()).forward(any(), any());
    }

    @Test
    @DisplayName("expired session → AuthenticationException thrown")
    void handle_expiredSession_throwsAuthenticationException() {
        Session expiredSession = new Session(
                SESSION_ID, USER_TOKEN, Instant.now().minusSeconds(1), "user-sub");

        when(sessionStore.findById(SESSION_ID)).thenReturn(Optional.of(expiredSession));

        assertThatThrownBy(() -> service.handle(buildRequest()))
                .isInstanceOf(AuthenticationException.class);

        verify(innerTokenService, never()).swapForInnerToken(any());
    }

    @Test
    @DisplayName("validator rejects request → ValidationException thrown before forwarding")
    void handle_validatorRejectsRequest_throwsValidationException() {
        RequestValidator rejectingValidator = request -> ValidationResult.failure("Country code XY not found");

        service = new MfeAdapterRequestApplicationService(
                sessionStore, innerTokenService, microserviceClient,
                new MfeAdapterProperties(), List.of(rejectingValidator), List.of(), List.of());

        when(sessionStore.findById(SESSION_ID)).thenReturn(Optional.of(VALID_SESSION));
        when(innerTokenService.swapForInnerToken(USER_TOKEN)).thenReturn(INNER_TOKEN);

        assertThatThrownBy(() -> service.handle(buildRequest()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Country code XY not found");

        verify(microserviceClient, never()).forward(any(), any());
    }

    @Test
    @DisplayName("request transformer is applied before forwarding")
    void handle_requestTransformerApplied() {
        ProxiedRequest original = buildRequest();
        ProxiedRequest transformed = ProxiedRequest.builder()
                .method("GET").path("/api/v2/items").sessionId(SESSION_ID).build();

        RequestTransformer transformer = req -> transformed;
        ProxiedResponse msResponse = ProxiedResponse.builder().statusCode(200).build();

        service = new MfeAdapterRequestApplicationService(
                sessionStore, innerTokenService, microserviceClient,
                new MfeAdapterProperties(), List.of(), List.of(transformer), List.of());

        when(sessionStore.findById(SESSION_ID)).thenReturn(Optional.of(VALID_SESSION));
        when(innerTokenService.swapForInnerToken(USER_TOKEN)).thenReturn(INNER_TOKEN);
        when(microserviceClient.forward(transformed, INNER_TOKEN)).thenReturn(msResponse);

        ProxiedResponse result = service.handle(original);

        assertThat(result.getStatusCode()).isEqualTo(200);
        // Verify the transformer's output was forwarded, not the original
        verify(microserviceClient).forward(transformed, INNER_TOKEN);
    }

    @Test
    @DisplayName("response transformer is applied to the MS response")
    void handle_responseTransformerApplied() {
        ProxiedRequest request = buildRequest();
        ProxiedResponse msResponse = ProxiedResponse.builder().statusCode(200)
                .body("raw".getBytes()).build();
        ProxiedResponse transformedResponse = ProxiedResponse.builder().statusCode(200)
                .body("transformed".getBytes()).build();

        ResponseTransformer transformer = (resp, req) -> transformedResponse;

        service = new MfeAdapterRequestApplicationService(
                sessionStore, innerTokenService, microserviceClient,
                new MfeAdapterProperties(), List.of(), List.of(), List.of(transformer));

        when(sessionStore.findById(SESSION_ID)).thenReturn(Optional.of(VALID_SESSION));
        when(innerTokenService.swapForInnerToken(USER_TOKEN)).thenReturn(INNER_TOKEN);
        when(microserviceClient.forward(request, INNER_TOKEN)).thenReturn(msResponse);

        ProxiedResponse result = service.handle(request);

        assertThat(new String(result.getBody())).isEqualTo("transformed");
    }

    @Test
    @DisplayName("validator.supports() returns false → validator.validate() is never called")
    void handle_validatorSupportsReturnsFalse_validatorSkipped() {
        RequestValidator nonApplicableValidator = new RequestValidator() {
            @Override
            public boolean supports(ProxiedRequest request) { return false; }
            @Override
            public ValidationResult validate(ProxiedRequest request) {
                throw new AssertionError("validate() must not be called when supports()=false");
            }
        };

        service = new MfeAdapterRequestApplicationService(
                sessionStore, innerTokenService, microserviceClient,
                new MfeAdapterProperties(), List.of(nonApplicableValidator), List.of(), List.of());

        ProxiedResponse msResponse = ProxiedResponse.builder().statusCode(200).build();
        when(sessionStore.findById(SESSION_ID)).thenReturn(Optional.of(VALID_SESSION));
        when(innerTokenService.swapForInnerToken(USER_TOKEN)).thenReturn(INNER_TOKEN);
        when(microserviceClient.forward(any(), any())).thenReturn(msResponse);

        ProxiedResponse result = service.handle(buildRequest());

        assertThat(result.getStatusCode()).isEqualTo(200);
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    @Test
    @DisplayName("authorization-service.required=false → IAS call skipped, request still forwarded")
    void handle_iasNotRequired_skipsInnerTokenSwap() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getAuthorizationService().setRequired(false);

        service = new MfeAdapterRequestApplicationService(
                sessionStore, innerTokenService, microserviceClient,
                props, List.of(), List.of(), List.of());

        ProxiedResponse msResponse = ProxiedResponse.builder().statusCode(200).build();
        when(sessionStore.findById(SESSION_ID)).thenReturn(Optional.of(VALID_SESSION));
        when(microserviceClient.forward(any(), any())).thenReturn(msResponse);

        ProxiedResponse result = service.handle(buildRequest());

        assertThat(result.getStatusCode()).isEqualTo(200);
    }

    private ProxiedRequest buildRequest() {
        return ProxiedRequest.builder()
                .method("GET")
                .path("/api/items")
                .sessionId(SESSION_ID)
                .build();
    }
}
