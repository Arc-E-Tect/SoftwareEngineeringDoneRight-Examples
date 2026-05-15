package com.arc_e_tect.book.sedr.familyties.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "server.port=0",
                "server.ssl.enabled=false"
        }
)
@ActiveProfiles("test")
class ContractHttpTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private RestTemplate restTemplate;
    private String baseUrl;
    private boolean stubbedMode;

    @BeforeEach
    void setup() {
        restTemplate = httpsRestTemplate();
        String override = resolveBaseUrlOverride();
        baseUrl = override != null ? normalizeBaseUrl(override) : "http://localhost:" + port + "/v1/familyties";
        stubbedMode = override != null;
        if (!stubbedMode) {
            cleanDatabase();
        }
    }

    @AfterEach
    void cleanup() {
        if (!stubbedMode) {
            cleanDatabase();
        }
    }

    @Test
    void addPerson_returnsCreatedWithBody() throws Exception {
        String firstName = "John";
        String lastName = "Contract";
        String payload = objectMapper.writeValueAsString(Map.of(
            "firstName", firstName,
            "lastName", lastName
        ));

        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            new HttpEntity<>(payload, jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").asText()).isNotBlank();
        assertThat(body.get("firstName").asText()).isEqualTo(firstName);
        assertThat(body.get("lastName").asText()).isEqualTo(lastName);
    }

    @Test
    void addPerson_duplicate_returnsConflict() throws Exception {
        // Works with both real app and WireMock:
        // - Real app: ensurePersonExists creates the person, duplicate POST returns 409
        // - WireMock: stub matches "Jane Conflict" and returns 409 directly
        if (!stubbedMode) {
            ensurePersonExists("Jane", "Conflict");
        }
        
        String duplicatePayload = objectMapper.writeValueAsString(Map.of(
            "firstName", "Jane",
            "lastName", "Conflict"
        ));

        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            new HttpEntity<>(duplicatePayload, jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void getPersonsByLastName_returnsList() throws Exception {
        if (!stubbedMode) {
            ensurePersonExists("John", "Smith");
            ensurePersonExists("Jane", "Smith");
        }

        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl + "/lastnames/Smith",
            HttpMethod.GET,
            new HttpEntity<>(jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.isArray()).isTrue();
        assertThat(body).isNotEmpty();
        body.forEach(node -> assertThat(node.get("lastName").asText()).isEqualTo("Smith"));
    }

    @Test
    void getPersonsByUnknownLastName_returns404() {
        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl + "/lastnames/Unknown" + UUID.randomUUID(),
            HttpMethod.GET,
            new HttpEntity<>(jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void deletePerson_returnsNoContent() throws Exception {
        String firstName = stubbedMode ? "Alice" : "Mark";
        String lastName = stubbedMode ? "Brown" : "Removable";
        if (!stubbedMode) {
            ensurePersonExists(firstName, lastName);
        }

        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl + "/lastnames/" + lastName + "?firstname=" + firstName,
            HttpMethod.DELETE,
            new HttpEntity<>(jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }

    @Test
    void deletePerson_notFound_returns404() {
        String firstName = stubbedMode ? "Missing" : UUID.randomUUID().toString();
        String lastName = stubbedMode ? "Person" : UUID.randomUUID().toString();
        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl + "/lastnames/" + lastName + "?firstname=" + firstName,
            HttpMethod.DELETE,
            new HttpEntity<>(jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void addRelationship_returnsCreated() throws Exception {
        String fromFirst = "Bob";
        String fromLast = "Johnson";
        String toFirst = "Alice";
        String toLast = "Williams";
        String payload = objectMapper.writeValueAsString(Map.of(
            "fromFirstName", fromFirst,
            "fromLastName", fromLast,
            "toFirstName", toFirst,
            "toLastName", toLast,
            "type", "spouse"
        ));

        if (!stubbedMode) {
            ensurePersonExists(fromFirst, fromLast);
            ensurePersonExists(toFirst, toLast);
        }

        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl + "/relationships",
            HttpMethod.POST,
            new HttpEntity<>(payload, jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").asText()).isNotBlank();
        assertThat(body.get("fromPersonId").asText()).isNotBlank();
        assertThat(body.get("toPersonId").asText()).isNotBlank();
        assertThat(body.get("type").asText()).isEqualToIgnoringCase("spouse");
    }

    @Test
    void getRelationshipsByType_returnsList() throws Exception {
        String lastName = "Smith";
        if (!stubbedMode) {
            String fromFirst = "Alex";
            String fromLast = "Taylor";
            String toFirst = "Ellen";
            ensurePersonExists(fromFirst, fromLast);
            ensurePersonExists(toFirst, lastName);
            String payload = objectMapper.writeValueAsString(Map.of(
                "fromFirstName", fromFirst,
                "fromLastName", fromLast,
                "toFirstName", toFirst,
                "toLastName", lastName,
                "type", "spouse"
            ));
            execute(() -> restTemplate.exchange(
                baseUrl + "/relationships",
                HttpMethod.POST,
                new HttpEntity<>(payload, jsonHeaders()),
                String.class
            ));
        }

        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl + "/relationships/spouse/lastnames/" + lastName,
            HttpMethod.GET,
            new HttpEntity<>(jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.isArray()).isTrue();
        assertThat(body).isNotEmpty();
        if (stubbedMode) {
            body.forEach(node -> assertThat(node.get("lastName").asText()).isEqualTo(lastName));
        } else {
            assertThat(body).anySatisfy(node -> assertThat(node.get("lastName").asText()).isEqualTo("Taylor"));
        }
    }

    private void ensurePersonExists(String firstName, String lastName) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "firstName", firstName,
            "lastName", lastName
        ));
        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            new HttpEntity<>(payload, jsonHeaders()),
            String.class
        ));
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private void cleanDatabase() {
        try {
            jdbcTemplate.execute("DELETE FROM relationships");
            jdbcTemplate.execute("DELETE FROM persons");
        } catch (Exception ignored) {
            // Tables might not exist yet during early init
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    private String resolveBaseUrlOverride() {
        String fromSystem = System.getProperty("familyties.base-url");
        if (fromSystem != null && !fromSystem.isBlank()) {
            return fromSystem;
        }
        String fromEnv = System.getenv("FAMILYTIES_BASE_URL");
        return (fromEnv != null && !fromEnv.isBlank()) ? fromEnv : null;
    }

    private String normalizeBaseUrl(String raw) {
        String trimmed = raw.trim();
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.endsWith("/v1/familyties")) {
            return trimmed;
        }
        if (trimmed.endsWith("/v1")) {
            return trimmed + "/familyties";
        }
        if (trimmed.endsWith("/familyties")) {
            return trimmed;
        }
        return trimmed + "/v1/familyties";
    }

    private RestTemplate httpsRestTemplate() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build();
            var sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
            var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
            CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            return new RestTemplate(requestFactory);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create HTTPS RestTemplate", e);
        }
    }

    private ResponseEntity<String> execute(java.util.function.Supplier<ResponseEntity<String>> httpCall) {
        try {
            return httpCall.get();
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                .headers(e.getResponseHeaders())
                .body(e.getResponseBodyAsString());
        }
    }

    @Test
    void addPerson_sqlInjectionInFirstName_returnsBadRequest() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "firstName", "'; DROP TABLE persons; --",
            "lastName", "Smith"
        ));

        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            new HttpEntity<>(payload, jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void addPerson_sqlInjectionInLastName_returnsBadRequest() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "firstName", "John",
            "lastName", "' OR '1'='1"
        ));

        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            new HttpEntity<>(payload, jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void lookupByLastName_sqlInjection_returnsBadRequest() throws Exception {
        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl + "/lastnames/" + "Smith' OR '1'='1",
            HttpMethod.GET,
            new HttpEntity<>(jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void addRelationship_sqlInjectionInType_returnsBadRequest() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "fromFirstName", "John",
            "fromLastName", "Smith",
            "toFirstName", "Jane",
            "toLastName", "Smith",
            "type", "parent'; DROP TABLE relationships; --"
        ));

        ResponseEntity<String> response = execute(() -> restTemplate.exchange(
            baseUrl + "/relationships",
            HttpMethod.POST,
            new HttpEntity<>(payload, jsonHeaders()),
            String.class
        ));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
