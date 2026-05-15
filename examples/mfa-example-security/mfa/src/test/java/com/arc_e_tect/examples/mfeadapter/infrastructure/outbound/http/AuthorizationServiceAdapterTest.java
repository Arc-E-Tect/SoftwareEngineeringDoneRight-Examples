package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.http;

import com.arc_e_tect.examples.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationServiceAdapter")
@SuppressWarnings("unchecked")
class AuthorizationServiceAdapterTest {

    @Mock
    private WebClient webClient;

    private MfeAdapterProperties props;
    private AuthorizationServiceAdapter adapter;

    private WebClient.RequestBodyUriSpec uriSpec;
    private WebClient.RequestBodySpec bodySpec;
    private WebClient.RequestHeadersSpec headersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        props = new MfeAdapterProperties();
        props.getAuthorizationService().setBaseUrl("http://wiremock-authz:8080");
        props.getAuthorizationService().setTokenSwapPath("/authz/token/swap");

        uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        bodySpec = mock(WebClient.RequestBodySpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        lenient().when(webClient.post()).thenReturn(uriSpec);
        lenient().when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        lenient().when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        lenient().when(bodySpec.retrieve()).thenReturn(responseSpec);
        lenient().when(headersSpec.retrieve()).thenReturn(responseSpec);

        adapter = new AuthorizationServiceAdapter(webClient, props);
    }

    @Test
    @DisplayName("swapForInnerToken – valid response → returns InnerToken with claims")
    void swapForInnerToken_validResponse_returnsInnerToken() {
        Map<String, Object> claims = Map.of("roles", java.util.List.of("USER", "READ_WRITE"), "sub", "test-user-123");
        Map<String, Object> response = Map.of(
                "token", "inner-token-abc123",
                "expires_in", 300,
                "claims", claims
        );

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(response));

        UserToken userToken = new UserToken("user-tok", Instant.now().plusSeconds(300), "test-user-123");
        InnerToken result = adapter.swapForInnerToken(userToken);

        assertThat(result).isNotNull();
        assertThat(result.tokenValue()).isEqualTo("inner-token-abc123");
        assertThat(result.claims()).containsKey("roles");
    }

    @Test
    @DisplayName("swapForInnerToken – null response → throws IllegalStateException")
    void swapForInnerToken_nullResponse_throwsException() {
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.justOrEmpty(null));

        UserToken userToken = new UserToken("user-tok", Instant.now().plusSeconds(300), "sub");

        assertThatThrownBy(() -> adapter.swapForInnerToken(userToken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid inner-token response");
    }

    @Test
    @DisplayName("swapForInnerToken – response without token field → throws IllegalStateException")
    void swapForInnerToken_missingTokenField_throwsException() {
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("other_field", "value")));

        UserToken userToken = new UserToken("user-tok", Instant.now().plusSeconds(300), "sub");

        assertThatThrownBy(() -> adapter.swapForInnerToken(userToken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid inner-token response");
    }

    @Test
    @DisplayName("swapForInnerToken – response without claims → returns InnerToken with empty claims")
    void swapForInnerToken_noClaims_returnsTokenWithEmptyClaims() {
        Map<String, Object> response = Map.of(
                "token", "inner-token-no-claims",
                "expires_in", 60
        );

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(response));

        UserToken userToken = new UserToken("user-tok", Instant.now().plusSeconds(300), "sub");
        InnerToken result = adapter.swapForInnerToken(userToken);

        assertThat(result.tokenValue()).isEqualTo("inner-token-no-claims");
        assertThat(result.claims()).isEmpty();
    }
}
