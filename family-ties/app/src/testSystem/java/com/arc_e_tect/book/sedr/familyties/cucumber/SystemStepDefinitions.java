package com.arc_e_tect.book.sedr.familyties.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RestTemplate restTemplate;
    private ResponseEntity<String> response;
    private String baseUrl;
    private boolean stubbedMode;
    private final Map<String, UUID> personIds = new HashMap<>();
    private Exception lastException;

    @Before
    public void setup() {
        restTemplate = httpsRestTemplate();
        String override = resolveBaseUrlOverride();
        baseUrl = override != null ? normalizeBaseUrl(override) : "https://localhost:" + port + "/v1/familyties";
        stubbedMode = override != null;
        personIds.clear();
        lastException = null;
        cleanDatabase();
    }

    @After
    public void cleanup() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        if (stubbedMode) {
            return;
        }
        try {
            jdbcTemplate.execute("DELETE FROM relationships");
            jdbcTemplate.execute("DELETE FROM persons");
        } catch (Exception e) {
            // Tables might not exist yet, ignore
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
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
            var sslContext = SSLContextBuilder.create()
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

    // Helper method to handle HTTP errors that RestTemplate throws for non-2xx responses
    private <T> ResponseEntity<T> safeExecute(java.util.function.Supplier<ResponseEntity<T>> httpCall) {
        try {
            return httpCall.get();
        } catch (HttpStatusCodeException e) {
            // RestTemplate throws exceptions for 4xx/5xx, but we want to capture the response
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body((T) e.getResponseBodyAsString());
        }
    }

    private String toHttpBaseUrl() {
        return baseUrl.replaceFirst("^https://", "http://");
    }

    // ===== Person Management Step Definitions =====

    @Given("the person with first name {string} and lastname {string} is not yet known to Family Ties")
    public void thePersonIsNotInDatabase(String firstName, String lastName) {
        if (stubbedMode) {
            personIds.remove(firstName + " " + lastName);
            return;
        }
        try {
            String sql = "DELETE FROM persons WHERE first_name = ? AND last_name = ?";
            jdbcTemplate.update(sql, firstName, lastName);
        } catch (Exception e) {
            // Table might not exist yet, ignore
        }
    }

    @Given("the person with first name {string} and lastname {string} is already known to Family Ties")
    public void thePersonExistsInDatabase(String firstName, String lastName) {
        if (stubbedMode) {
            personIds.computeIfAbsent(firstName + " " + lastName, k -> UUID.randomUUID());
            return;
        }
        String checkSql = "SELECT id FROM persons WHERE first_name = ? AND last_name = ?";
        List<UUID> ids = jdbcTemplate.query(checkSql, 
            (rs, rowNum) -> (UUID) rs.getObject("id"), 
            firstName, lastName);
        
        if (ids.isEmpty()) {
            UUID id = UUID.randomUUID();
            String insertSql = "INSERT INTO persons (id, first_name, last_name) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, id, firstName, lastName);
            personIds.put(firstName + " " + lastName, id);
        } else {
            personIds.put(firstName + " " + lastName, ids.get(0));
        }
    }

    @Given("the following persons are known to Family Ties:")
    public void theFollowingPersonsExistInDatabase(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            String firstName = row.get("firstName");
            String lastName = row.get("lastName");
            thePersonExistsInDatabase(firstName, lastName);
        }
    }

    @Given("no persons with lastname {string} are known to Family Ties")
    public void noPersonsWithLastnameExist(String lastName) {
        if (stubbedMode) {
            personIds.entrySet().removeIf(e -> e.getKey().endsWith(" " + lastName));
            return;
        }
        try {
            jdbcTemplate.update("DELETE FROM persons WHERE last_name = ?", lastName);
        } catch (Exception e) {
            // Table might not exist yet, ignore
        }
    }

    @Given("{int} persons with lastname {string} are known to Family Ties")
    public void nPersonsWithLastnameExist(int count, String lastName) {
        for (int i = 1; i <= count; i++) {
            String firstName = "Person" + i;
            thePersonExistsInDatabase(firstName, lastName);
        }
    }

    @Given("{int} persons with first name prefix {string} and lastname {string} are known to Family Ties")
    public void nPersonsWithPrefixExist(int count, String prefix, String lastName) {
        for (int i = 1; i <= count; i++) {
            String firstName = prefix + i;
            thePersonExistsInDatabase(firstName, lastName);
        }
    }

    @When("I register the person with first name {string} and lastname {string}")
    public void iAddPerson(String firstName, String lastName) {
        String requestBody = String.format(
            "{\"firstName\":\"%s\",\"lastName\":\"%s\"}",
            firstName, lastName
        );
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, jsonHeaders());
        response = safeExecute(() -> restTemplate.postForEntity(baseUrl, request, String.class));
    }

    @When("I look up family members with lastname {string}")
    public void iRetrieveFamilyMembersByLastname(String lastName) {
        String url = baseUrl + "/lastnames/" + lastName;
        response = safeExecute(() -> restTemplate.getForEntity(url, String.class));
    }

    @When("I look up family members with lastname {string} with page {int} and size {int}")
    public void iRetrieveFamilyMembersByLastnameWithPagination(String lastName, int page, int size) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/lastnames/" + lastName)
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
        response = safeExecute(() -> restTemplate.getForEntity(url, String.class));
    }

    @When("I remove the person with first name {string} and lastname {string}")
    public void iDeletePerson(String firstName, String lastName) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/lastnames/" + lastName)
                .queryParam("firstname", firstName)
                .toUriString();
        response = safeExecute(() -> restTemplate.exchange(url, HttpMethod.DELETE, null, String.class));
    }

    @When("I attempt to register the person with first name {string} and lastname {string} over http")
    public void iRegisterPersonOverHttp(String firstName, String lastName) {
        String requestBody = String.format(
            "{\"firstName\":\"%s\",\"lastName\":\"%s\"}",
            firstName, lastName
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, jsonHeaders());
        String httpBase = toHttpBaseUrl();
        try {
            lastException = null;
            response = restTemplate.postForEntity(httpBase, request, String.class);
        } catch (ResourceAccessException | HttpStatusCodeException e) {
            // Capture both connection-level and HTTP error responses when hitting the non-TLS endpoint
            lastException = e;
            response = null;
        }
    }

    @When("I attempt to look up family members with lastname {string} over http")
    public void iLookUpFamilyMembersOverHttp(String lastName) {
        String httpBase = toHttpBaseUrl();
        try {
            lastException = null;
            response = restTemplate.getForEntity(httpBase + "/lastnames/" + lastName, String.class);
        } catch (ResourceAccessException | HttpStatusCodeException e) {
            lastException = e;
            response = null;
        }
    }

    @When("I attempt to remove the person with first name {string} and lastname {string} over http")
    public void iRemovePersonOverHttp(String firstName, String lastName) {
        String url = UriComponentsBuilder.fromUriString(toHttpBaseUrl() + "/lastnames/" + lastName)
                .queryParam("firstname", firstName)
                .toUriString();
        try {
            lastException = null;
            response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (ResourceAccessException | HttpStatusCodeException e) {
            lastException = e;
            response = null;
        }
    }

    @Then("the person with first name {string} and lastname {string} is now registered")
    public void thePersonIsAddedSuccessfully(String firstName, String lastName) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Verify person actually exists in database
        if (!stubbedMode) {
            String sql = "SELECT COUNT(*) FROM persons WHERE first_name = ? AND last_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, firstName, lastName);
            assertThat(count).isEqualTo(1);
        }
    }

    @Then("the person with first name {string} and lastname {string} is no longer listed")
    public void thePersonIsDeleted(String firstName, String lastName) {
        if (stubbedMode) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            return;
        }
        String sql = "SELECT COUNT(*) FROM persons WHERE first_name = ? AND last_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, firstName, lastName);
        assertThat(count).isZero();
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expectedStatus) {
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("the response contains a conflict error message")
    public void theResponseContainsConflictErrorMessage() {
        assertThat(response.getBody()).isNotEmpty();
    }

    @Then("I am told the person already exists")
    public void iAmToldPersonAlreadyExists() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Then("I see {int} people in the family list")
    public void theResponseContainsNPersons(int expectedCount) {
        String body = response.getBody();
        assertThat(body).isNotNull();
        // Count occurrences of "firstName" to count persons in JSON array
        int count = body.split("\"firstName\"").length - 1;
        assertThat(count).isEqualTo(expectedCount);
    }

    @Then("the family list includes person {string}")
    @Then("I see related people including {string}")
    public void theResponseContainsPerson(String fullName) {
        String[] parts = fullName.split(" ");
        String firstName = parts[0];
        String lastName = parts[1];
        
        String body = response.getBody();
        assertThat(body).contains("\"firstName\":\"" + firstName + "\"");
        assertThat(body).contains("\"lastName\":\"" + lastName + "\"");
    }

    // ===== Relationship Management Step Definitions =====

    @Given("a {string} relationship exists from {string} to {string}")
    public void aRelationshipExists(String relationshipType, String fromPerson, String toPerson) {
        // Ensure both persons exist (in-memory for stubbed mode, in database for non-stubbed)
        String[] fromParts = fromPerson.split(" ");
        String[] toParts = toPerson.split(" ");
        thePersonExistsInDatabase(fromParts[0], fromParts[1]);
        thePersonExistsInDatabase(toParts[0], toParts[1]);
        
        UUID fromId = personIds.get(fromPerson);
        UUID toId = personIds.get(toPerson);

        if (stubbedMode) {
            return;
        }

        String sql = "INSERT INTO relationships (id, from_person_id, to_person_id, type) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, UUID.randomUUID(), fromId, toId, relationshipType.toUpperCase());
    }

    @Given("{int} {string} relationships exist to {string}")
    public void nRelationshipsExistTo(int count, String relationshipType, String toPerson) {
        // Ensure target person exists (in-memory for stubbed mode, in database for non-stubbed)
        String[] toParts = toPerson.split(" ");
        thePersonExistsInDatabase(toParts[0], toParts[1]);
        UUID toId = personIds.get(toPerson);
        
        for (int i = 1; i <= count; i++) {
            String fromPerson = "GrandParent" + i + " Smith";
            thePersonExistsInDatabase("GrandParent" + i, "Smith");
            UUID fromId = personIds.get(fromPerson);
            
            if (!stubbedMode) {
                String sql = "INSERT INTO relationships (id, from_person_id, to_person_id, type) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(sql, UUID.randomUUID(), fromId, toId, relationshipType.toUpperCase());
            }
        }
    }

    @When("I record a {string} relationship from {string} to {string}")
    public void iAddRelationship(String relationshipType, String fromPerson, String toPerson) {
        String[] fromParts = fromPerson.split(" ");
        String[] toParts = toPerson.split(" ");
        
        String requestBody = String.format(
            "{\"fromFirstName\":\"%s\",\"fromLastName\":\"%s\",\"toFirstName\":\"%s\",\"toLastName\":\"%s\",\"type\":\"%s\"}",
            fromParts[0], fromParts[1], toParts[0], toParts[1], relationshipType.toLowerCase()
        );
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, jsonHeaders());
        response = safeExecute(() -> restTemplate.postForEntity(baseUrl + "/relationships", request, String.class));
    }

            @When("I attempt to record a {string} relationship from {string} to {string} over http")
            public void iAddRelationshipOverHttp(String relationshipType, String fromPerson, String toPerson) {
                String[] fromParts = fromPerson.split(" ");
                String[] toParts = toPerson.split(" ");

                String requestBody = String.format(
                    "{\"fromFirstName\":\"%s\",\"fromLastName\":\"%s\",\"toFirstName\":\"%s\",\"toLastName\":\"%s\",\"type\":\"%s\"}",
                    fromParts[0], fromParts[1], toParts[0], toParts[1], relationshipType.toLowerCase()
                );

                HttpEntity<String> request = new HttpEntity<>(requestBody, jsonHeaders());
                try {
                    lastException = null;
                    response = restTemplate.postForEntity(toHttpBaseUrl() + "/relationships", request, String.class);
                } catch (ResourceAccessException | HttpStatusCodeException e) {
                    lastException = e;
                    response = null;
                }
            }

    @When("I look up {string} relationships for lastname {string}")
    public void iRetrieveRelationshipsByLastname(String relationshipType, String lastName) {
        String url = baseUrl + "/relationships/" + relationshipType + "/lastnames/" + lastName;
        response = safeExecute(() -> restTemplate.getForEntity(url, String.class));
    }

    @When("I attempt to look up {string} relationships for lastname {string} over http")
    public void iRetrieveRelationshipsByLastnameOverHttp(String relationshipType, String lastName) {
        String url = toHttpBaseUrl() + "/relationships/" + relationshipType + "/lastnames/" + lastName;
        try {
            lastException = null;
            response = restTemplate.getForEntity(url, String.class);
        } catch (ResourceAccessException | HttpStatusCodeException e) {
            lastException = e;
            response = null;
        }
    }

    @Then("the relationship is recorded successfully")
    public void theRelationshipIsCreatedSuccessfully() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Verify relationship actually exists in database
        if (!stubbedMode) {
            // At least one relationship should exist after successful creation
            String sql = "SELECT COUNT(*) FROM relationships";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            assertThat(count).isGreaterThan(0);
        }
    }

    @Then("the request is rejected because http is not supported")
    public void theRequestIsRejectedBecauseHttpIsNotSupported() {
        assertThat(response).isNull();
        assertThat(lastException).isNotNull();
    }

    @Then("the response contains error message {string}")
    public void theResponseContainsErrorMessage(String expectedMessage) {
        String actualMessage = response.getBody();
        assertThat(actualMessage).contains(expectedMessage);
    }

    @Then("I am told a person can have at most two parents")
    public void iAmToldAtMostTwoParents() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("at most two parents");
    }

    @Then("I am told a person can have at most four grandparents")
    public void iAmToldAtMostFourGrandparents() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("at most four grandparents");
    }

    @Then("I am told a person can only have one spouse")
    public void iAmToldOnlyOneSpouse() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("only have one spouse");
    }

    @Then("I am told the person does not exist")
    public void iAmToldPersonDoesNotExist() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Then("I am told no family members are known with lastname {string}")
    public void iAmToldNoFamilyMembersKnown(String lastName) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Then("I am told no people are known with lastname {string}")
    public void iAmToldNoPeopleKnown(String lastName) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ===== SQL Injection Prevention Step Definitions =====

    @When("I attempt to register a person with firstname {string} and lastname {string}")
    public void iAttemptToRegisterPersonWithFirstnameAndLastname(String firstName, String lastName) {
        String requestBody = String.format(
            "{\"firstName\":\"%s\",\"lastName\":\"%s\"}",
            firstName, lastName
        );
        HttpEntity<String> request = new HttpEntity<>(requestBody, jsonHeaders());
        response = safeExecute(() -> restTemplate.postForEntity(baseUrl, request, String.class));
    }

    @When("I attempt to look up family members with lastname {string}")
    public void iAttemptToLookupWithLastname(String lastName) {
        String url = baseUrl + "/lastnames/" + lastName;
        response = safeExecute(() -> restTemplate.getForEntity(url, String.class));
    }

    @When("I attempt to record a relationship with type {string} from {string} to {string}")
    public void iAttemptToRecordRelationshipWithType(String type, String fromPerson, String toPerson) {
        String[] fromParts = fromPerson.split(" ");
        String[] toParts = toPerson.split(" ");
        
        String requestBody = String.format(
            "{\"fromFirstName\":\"%s\",\"fromLastName\":\"%s\",\"toFirstName\":\"%s\",\"toLastName\":\"%s\",\"type\":\"%s\"}",
            fromParts[0], fromParts[1], toParts[0], toParts[1], type
        );
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, jsonHeaders());
        response = safeExecute(() -> restTemplate.postForEntity(baseUrl + "/relationships", request, String.class));
    }

    @Then("the request is rejected with bad request")
    public void theRequestIsRejectedWithBadRequest() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
