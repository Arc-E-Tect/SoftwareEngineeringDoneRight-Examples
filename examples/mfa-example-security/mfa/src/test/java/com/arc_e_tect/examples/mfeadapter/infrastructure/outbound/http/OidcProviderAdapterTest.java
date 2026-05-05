package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.http;

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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OidcProviderAdapter")
@SuppressWarnings("unchecked")
class OidcProviderAdapterTest {

    @Mock
    private WebClient webClient;

    private MfeAdapterProperties props;
    private OidcProviderAdapter adapter;

    private WebClient.RequestBodyUriSpec uriSpec;
    private WebClient.RequestBodySpec bodySpec;
    private WebClient.RequestHeadersSpec headersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        props = new MfeAdapterProperties();
        props.getOidcProvider().setIssuerUri("http://wiremock-idp:8080/realms/mfa");
        props.getOidcProvider().setClientId("security-mfa-client");
        props.getOidcProvider().setClientSecret("security-mfa-secret");

        uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        bodySpec = mock(WebClient.RequestBodySpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        lenient().when(webClient.post()).thenReturn(uriSpec);
        lenient().when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        lenient().when(bodySpec.body(any())).thenReturn(headersSpec);
        lenient().when(headersSpec.retrieve()).thenReturn(responseSpec);

        adapter = new OidcProviderAdapter(webClient, props);
    }

    @Test
    @DisplayName("exchangeCodeForToken – valid response → returns UserToken with subject from JWT")
    void exchangeCodeForToken_validResponse_returnsUserToken() {
        // JWT with sub=test-user-123: header.payload.sig
        // payload (base64url): {"sub":"test-user-123","exp":9999999999}
        String jwt = "eyJhbGciOiJub25lIn0." +
                     "eyJzdWIiOiJ0ZXN0LXVzZXItMTIzIiwiZXhwIjo5OTk5OTk5OTk5fQ." +
                     "stub";

        Map<String, Object> tokenResponse = Map.of(
                "access_token", jwt,
                "expires_in", 300
        );

        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(tokenResponse));

        UserToken result = adapter.exchangeCodeForToken("auth-code-abc", "http://localhost:8080/auth/callback");

        assertThat(result).isNotNull();
        assertThat(result.tokenValue()).isEqualTo(jwt);
        assertThat(result.subject()).isEqualTo("test-user-123");
    }

    @Test
    @DisplayName("exchangeCodeForToken – null response → throws IllegalStateException")
    void exchangeCodeForToken_nullResponse_throwsException() {
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.justOrEmpty(null));

        assertThatThrownBy(() -> adapter.exchangeCodeForToken("code", "http://localhost:8080/auth/callback"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid token response");
    }

    @Test
    @DisplayName("exchangeCodeForToken – response without access_token → throws IllegalStateException")
    void exchangeCodeForToken_missingAccessToken_throwsException() {
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("token_type", "Bearer")));

        assertThatThrownBy(() -> adapter.exchangeCodeForToken("code", "http://localhost:8080/auth/callback"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid token response");
    }

    @Test
    @DisplayName("revokeToken – sends revocation request without throwing")
    void revokeToken_sendsRequest() {
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(
                org.springframework.http.ResponseEntity.ok().build()));

        UserToken token = new UserToken("tok", java.time.Instant.now().plusSeconds(300), "sub");

        adapter.revokeToken(token); // should not throw
    }

    @Test
    @DisplayName("buildAuthorizationUrl – returns URL with all required OAuth2 parameters")
    void buildAuthorizationUrl_containsRequiredParams() {
        String url = adapter.buildAuthorizationUrl("http://localhost:8080/auth/callback", "my-state");

        assertThat(url).contains("response_type=code");
        assertThat(url).contains("client_id=security-mfa-client");
        assertThat(url).contains("state=my-state");
        assertThat(url).contains("scope=openid");
        assertThat(url).contains("/protocol/openid-connect/auth");
    }

    @Test
    @DisplayName("exchangeCodeForToken – JWT with no dots → subject is 'unknown'")
    void exchangeCodeForToken_jwtWithNoDots_subjectIsUnknown() {
        Map<String, Object> tokenResponse = Map.of(
                "access_token", "nodots",
                "expires_in", 300
        );
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(tokenResponse));

        UserToken result = adapter.exchangeCodeForToken("code", "http://localhost/cb");
        assertThat(result.subject()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("exchangeCodeForToken – JWT payload without sub claim → subject is 'unknown'")
    void exchangeCodeForToken_jwtPayloadWithoutSub_subjectIsUnknown() {
        // base64url of {"no_sub":"value"} = eyJub19zdWIiOiJ2YWx1ZSJ9
        String jwt = "header.eyJub19zdWIiOiJ2YWx1ZSJ9.sig";
        Map<String, Object> tokenResponse = Map.of("access_token", jwt, "expires_in", 300);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(tokenResponse));

        UserToken result = adapter.exchangeCodeForToken("code", "http://localhost/cb");
        assertThat(result.subject()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("buildAuthorizationUrl – uses publicIssuerUri when set instead of issuerUri")
    void buildAuthorizationUrl_usesPublicIssuerUri_whenSet() {
        props.getOidcProvider().setPublicIssuerUri("http://localhost:8082/realms/mfa");

        String url = adapter.buildAuthorizationUrl("http://localhost:4200/auth/callback", "state-abc");

        assertThat(url).startsWith("http://localhost:8082/realms/mfa/protocol/openid-connect/auth");
        assertThat(url).doesNotContain("wiremock-idp");
    }

    @Test
    @DisplayName("exchangeCodeForToken – JWT payload with invalid base64 → subject is 'unknown'")
    void exchangeCodeForToken_jwtInvalidBase64_subjectIsUnknown() {
        // Invalid base64 in payload part
        String jwt = "header.!!!not-valid-base64!!!.sig";
        Map<String, Object> tokenResponse = Map.of("access_token", jwt, "expires_in", 300);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(tokenResponse));

        UserToken result = adapter.exchangeCodeForToken("code", "http://localhost/cb");
        assertThat(result.subject()).isEqualTo("unknown");
    }
}
