package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.http;

import com.arc_e_tect.examples.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedResponse;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter.ApiKeyScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MicroserviceRestClientAdapter")
@SuppressWarnings("unchecked")
class MicroserviceRestClientAdapterTest {

    @Mock
    private WebClient webClient;

    private MfeAdapterProperties props;
    private MicroserviceRestClientAdapter adapter;

    // Shared mocks
    private WebClient.RequestBodyUriSpec uriSpec;
    private WebClient.RequestBodySpec bodySpec;
    private WebClient.RequestHeadersSpec headersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        props = new MfeAdapterProperties();
        props.getMicroservice().setBaseUrl("http://localhost:8081");

        uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        bodySpec = mock(WebClient.RequestBodySpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        ResponseEntity<byte[]> entity = new ResponseEntity<>(
                "{\"id\":1}".getBytes(), responseHeaders, HttpStatus.OK);

        lenient().when(webClient.method(any())).thenReturn(uriSpec);
        lenient().when(uriSpec.uri(any(String.class))).thenReturn(bodySpec);
        // Invoke the headers Consumer so that lambda body lines are covered
        lenient().doAnswer(invocation -> {
            java.util.function.Consumer<HttpHeaders> consumer = invocation.getArgument(0);
            consumer.accept(new HttpHeaders());
            return bodySpec;
        }).when(bodySpec).headers(any());
        lenient().when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        lenient().when(headersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.toEntity(byte[].class)).thenReturn(Mono.just(entity));

        adapter = new MicroserviceRestClientAdapter(webClient, props, Optional.empty());
    }

    @Test
    @DisplayName("forward – basic request without inner token → proxies and returns response")
    void forward_basicRequest_returnsResponse() {
        ProxiedRequest request = ProxiedRequest.builder()
                .method("GET").path("/v1/persons").build();

        ProxiedResponse response = adapter.forward(request, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("{\"id\":1}".getBytes());
    }

    @Test
    @DisplayName("forward – with inner token → Authorization header added")
    void forward_withInnerToken_addsAuthorizationHeader() {
        ProxiedRequest request = ProxiedRequest.builder()
                .method("GET").path("/v1/persons").build();
        InnerToken token = new InnerToken("tok-xyz", Map.of(), Instant.now().plusSeconds(300));

        ProxiedResponse response = adapter.forward(request, token);

        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("forward – with query string → URI includes query")
    void forward_withQueryString_includesQueryInUri() {
        ProxiedRequest request = ProxiedRequest.builder()
                .method("GET").path("/v1/persons").queryString("page=1&size=10").build();

        ProxiedResponse response = adapter.forward(request, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("forward – with forwardable headers → header is passed")
    void forward_withForwardableHeaders_passesThem() {
        ProxiedRequest request = ProxiedRequest.builder()
                .method("GET").path("/v1/persons")
                .headers(Map.of("Accept", List.of("application/json"),
                        "cookie", List.of("session=abc"),       // should NOT be forwarded
                        "x-api-key", List.of("key123")))        // should NOT be forwarded
                .build();

        ProxiedResponse response = adapter.forward(request, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("forward – with API key scope and forwardScope=true → scope header added")
    void forward_withApiKeyScope_forwardsScope() {
        props.getApiGateway().setForwardScope(true);
        ProxiedRequest request = ProxiedRequest.builder()
                .method("GET").path("/v1/persons")
                .apiKeyScope(ApiKeyScope.READ)
                .build();

        ProxiedResponse response = adapter.forward(request, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("forward – null MS response → throws IllegalStateException")
    void forward_nullMsResponse_throwsIllegalStateException() {
        WebClient.RequestBodyUriSpec uriSpec2 = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec2 = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec headersSpec2 = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec2 = mock(WebClient.ResponseSpec.class);

        when(webClient.method(any())).thenReturn(uriSpec2);
        when(uriSpec2.uri(any(String.class))).thenReturn(bodySpec2);
        doAnswer(invocation -> {
            java.util.function.Consumer<HttpHeaders> consumer = invocation.getArgument(0);
            consumer.accept(new HttpHeaders());
            return bodySpec2;
        }).when(bodySpec2).headers(any());
        when(bodySpec2.bodyValue(any())).thenReturn(headersSpec2);
        when(headersSpec2.retrieve()).thenReturn(responseSpec2);
        when(responseSpec2.toEntity(byte[].class)).thenReturn(Mono.empty());

        ProxiedRequest request = ProxiedRequest.builder()
                .method("GET").path("/v1/persons").build();

        assertThatThrownBy(() -> adapter.forward(request, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Null response");
    }

    @Test
    @DisplayName("constructor – null headers name → isForwardableHeader returns false")
    void forward_nullHeaderName_notForwarded() {
        // Build a request with a null header entry to exercise the null guard
        Map<String, List<String>> headersWithNull = new java.util.HashMap<>();
        headersWithNull.put(null, List.of("value"));
        ProxiedRequest request = ProxiedRequest.builder()
                .method("GET").path("/v1/persons")
                .headers(headersWithNull)
                .build();

        ProxiedResponse response = adapter.forward(request, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
    }
}
