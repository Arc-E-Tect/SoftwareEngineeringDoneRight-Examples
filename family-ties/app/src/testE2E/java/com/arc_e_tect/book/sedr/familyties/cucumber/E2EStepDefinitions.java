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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class E2EStepDefinitions {

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Autowired
    private DataSource dataSource;

    private static RestTemplate restTemplate; // Shared across all scenarios
    private ResponseEntity<String> response;
    private String baseUrl;
    private final Map<String, UUID> personIds = new HashMap<>();
    private long lastResponseTimeMillis;
    private final List<Long> recordedResponseTimes = new ArrayList<>();
    private Exception lastException;
    private String lastRelationshipFromFirstName;
    private String lastRelationshipFromLastName;
    private String lastRelationshipToFirstName;
    private String lastRelationshipToLastName;
    private String lastRelationshipType;

    @Before
    public void setup() {
        // Initialize RestTemplate once, reuse across scenarios
        if (restTemplate == null) {
            restTemplate = httpsRestTemplate();
        }
        baseUrl = appBaseUrl + "/v1/familyties";
        personIds.clear();
        recordedResponseTimes.clear();
        lastResponseTimeMillis = 0L;
        lastException = null;
        lastRelationshipFromFirstName = null;
        lastRelationshipFromLastName = null;
        lastRelationshipToFirstName = null;
        lastRelationshipToLastName = null;
        lastRelationshipType = null;
        cleanDatabase();
    }

    @After
    public void cleanup() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        // Clean database using direct JDBC calls to ensure proper test isolation
        // This avoids using the system under test to set fixtures
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Delete in correct order due to foreign key constraints
            // Relationships reference persons, so delete relationships first
            stmt.executeUpdate("DELETE FROM relationships");
            stmt.executeUpdate("DELETE FROM persons");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to clean database for test isolation", e);
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
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

    private <T> ResponseEntity<T> timedExecute(java.util.function.Supplier<ResponseEntity<T>> httpCall) {
        long start = System.nanoTime();
        ResponseEntity<T> result = httpCall.get();
        lastResponseTimeMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();
        recordedResponseTimes.add(lastResponseTimeMillis);
        return result;
    }

    private String toHttpBaseUrl() {
        return baseUrl.replaceFirst("^https://", "http://");
    }

    // ===== Person Management Step Definitions =====

    @Given("the person with first name {string} and lastname {string} is not yet known to Family Ties")
    public void thePersonIsNotInDatabase(String firstName, String lastName) {
        // Ensure person does NOT exist in database - delete if present
        String deleteSql = "DELETE FROM persons WHERE first_name = ? AND last_name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.executeUpdate();
            // Also remove from in-memory tracking
            personIds.remove(firstName + " " + lastName);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to ensure person does not exist in database", e);
        }
    }

    @Given("the person with first name {string} and lastname {string} is already known to Family Ties")
    public void thePersonExistsInDatabase(String firstName, String lastName) {
        // Insert person directly into database via JDBC (not using system under test)
        UUID id = UUID.randomUUID();
        try (Connection conn = dataSource.getConnection()) {
            // Check if person already exists
            String checkSql = "SELECT id FROM persons WHERE first_name = ? AND last_name = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, firstName);
                checkStmt.setString(2, lastName);
                var rs = checkStmt.executeQuery();
                if (rs.next()) {
                    // Person exists, get the ID
                    id = UUID.fromString(rs.getString("id"));
                    personIds.put(firstName + " " + lastName, id);
                    return;
                }
            }
            
            // Person doesn't exist, insert it
            String insertSql = "INSERT INTO persons (id, first_name, last_name) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setObject(1, id);
                insertStmt.setString(2, firstName);
                insertStmt.setString(3, lastName);
                insertStmt.executeUpdate();
                personIds.put(firstName + " " + lastName, id);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert person fixture into database", e);
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
        // Delete all persons with the given lastname from database
        String deleteSql = "DELETE FROM persons WHERE last_name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, lastName);
            stmt.executeUpdate();
            // Also remove from in-memory tracking
            personIds.entrySet().removeIf(e -> e.getKey().endsWith(" " + lastName));
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete persons with lastname from database", e);
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
        response = timedExecute(() -> safeExecute(() -> restTemplate.postForEntity(baseUrl, request, String.class)));
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

    @When("I look up family members with lastname {string}")
    public void iRetrieveFamilyMembersByLastname(String lastName) {
        String url = baseUrl + "/lastnames/" + lastName;
        response = timedExecute(() -> safeExecute(() -> restTemplate.getForEntity(url, String.class)));
    }

    @When("I look up family members with lastname {string} with page {int} and size {int}")
    public void iRetrieveFamilyMembersByLastnameWithPagination(String lastName, int page, int size) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/lastnames/" + lastName)
                .queryParam("page", page)
                .queryParam("size", size)
                .toUriString();
        response = timedExecute(() -> safeExecute(() -> restTemplate.getForEntity(url, String.class)));
    }

    @When("I remove the person with first name {string} and lastname {string}")
    public void iDeletePerson(String firstName, String lastName) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/lastnames/" + lastName)
                .queryParam("firstname", firstName)
                .toUriString();
        response = timedExecute(() -> safeExecute(() -> restTemplate.exchange(url, HttpMethod.DELETE, null, String.class)));
    }

    @Then("the person with first name {string} and lastname {string} is now registered")
    public void thePersonIsAddedSuccessfully(String firstName, String lastName) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Verify person actually exists in database (not via API)
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) as cnt FROM persons WHERE first_name = ? AND last_name = ?")) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            var rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt("cnt");
            assertThat(count).isEqualTo(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to verify person in database", e);
        }
    }

    @Then("the person with first name {string} and lastname {string} is no longer listed")
    public void thePersonIsDeleted(String firstName, String lastName) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify person actually removed from database (not via API)
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) as cnt FROM persons WHERE first_name = ? AND last_name = ?")) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            var rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt("cnt");
            assertThat(count).isEqualTo(0);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to verify person deleted from database", e);
        }
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
        // Ensure both persons exist in database
        String[] fromParts = fromPerson.split(" ");
        String[] toParts = toPerson.split(" ");
        thePersonExistsInDatabase(fromParts[0], fromParts[1]);
        thePersonExistsInDatabase(toParts[0], toParts[1]);
        
        UUID fromId = personIds.get(fromPerson);
        UUID toId = personIds.get(toPerson);
        
        // Insert relationship directly into database via JDBC (not using system under test)
        String insertSql = "INSERT INTO relationships (id, from_person_id, to_person_id, type) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setObject(1, UUID.randomUUID());
            stmt.setObject(2, fromId);
            stmt.setObject(3, toId);
            stmt.setString(4, relationshipType.toUpperCase());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert relationship fixture into database", e);
        }
    }

    @Given("{int} {string} relationships exist to {string}")
    public void nRelationshipsExistTo(int count, String relationshipType, String toPerson) {
        // Ensure target person exists
        String[] toParts = toPerson.split(" ");
        thePersonExistsInDatabase(toParts[0], toParts[1]);
        UUID toId = personIds.get(toPerson);
        
        // Create multiple relationships directly in database (not using system under test)
        String insertSql = "INSERT INTO relationships (id, from_person_id, to_person_id, type) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            
            for (int i = 1; i <= count; i++) {
                String fromFirstName = "GrandParent" + i;
                String fromLastName = "Smith";
                String fromPerson = fromFirstName + " " + fromLastName;
                
                // Ensure from person exists
                thePersonExistsInDatabase(fromFirstName, fromLastName);
                UUID fromId = personIds.get(fromPerson);
                
                // Insert relationship
                stmt.setObject(1, UUID.randomUUID());
                stmt.setObject(2, fromId);
                stmt.setObject(3, toId);
                stmt.setString(4, relationshipType.toUpperCase());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert relationship fixtures into database", e);
        }
    }

    @When("I record a {string} relationship from {string} to {string}")
    public void iAddRelationship(String relationshipType, String fromPerson, String toPerson) {
        String[] fromParts = fromPerson.split(" ");
        String[] toParts = toPerson.split(" ");
        
        // Track for verification
        lastRelationshipFromFirstName = fromParts[0];
        lastRelationshipFromLastName = fromParts[1];
        lastRelationshipToFirstName = toParts[0];
        lastRelationshipToLastName = toParts[1];
        lastRelationshipType = relationshipType.toUpperCase();
        
        String requestBody = String.format(
            "{\"fromFirstName\":\"%s\",\"fromLastName\":\"%s\",\"toFirstName\":\"%s\",\"toLastName\":\"%s\",\"type\":\"%s\"}",
            fromParts[0], fromParts[1], toParts[0], toParts[1], relationshipType.toLowerCase()
        );
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, jsonHeaders());
        response = timedExecute(() -> safeExecute(() -> restTemplate.postForEntity(baseUrl + "/relationships", request, String.class)));
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
        response = timedExecute(() -> safeExecute(() -> restTemplate.getForEntity(url, String.class)));
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

    @When("I measure response times for retrieving family members with lastname {string} over {int} requests")
    public void iMeasureResponseTimesForRetrieval(String lastName, int requestCount) {
        recordedResponseTimes.clear();
        lastResponseTimeMillis = 0L;
        String url = baseUrl + "/lastnames/" + lastName;
        for (int i = 0; i < requestCount; i++) {
            response = timedExecute(() -> safeExecute(() -> restTemplate.getForEntity(url, String.class)));
        }
    }

    @Then("the relationship is recorded successfully")
    public void theRelationshipIsCreatedSuccessfully() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Verify relationship actually exists in database (not via API)
        // Check for the specific relationship that was just created
        try (Connection conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(
                 "SELECT COUNT(*) as cnt FROM relationships r " +
                 "JOIN persons p1 ON r.from_person_id = p1.id " +
                 "JOIN persons p2 ON r.to_person_id = p2.id " +
                 "WHERE p1.first_name = ? AND p1.last_name = ? " +
                 "AND p2.first_name = ? AND p2.last_name = ? " +
                 "AND r.type = ?")) {
            stmt.setString(1, lastRelationshipFromFirstName);
            stmt.setString(2, lastRelationshipFromLastName);
            stmt.setString(3, lastRelationshipToFirstName);
            stmt.setString(4, lastRelationshipToLastName);
            stmt.setString(5, lastRelationshipType);
            var rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt("cnt");
            assertThat(count).isEqualTo(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to verify relationship in database", e);
        }
    }

    @Then("the request is rejected because http is not supported")
    public void theRequestIsRejectedBecauseHttpIsNotSupported() {
        assertThat(response).isNull();
        assertThat(lastException).isNotNull();
    }

    @Then("the response time is below {int} milliseconds")
    public void theResponseTimeIsBelow(int thresholdMillis) {
        assertThat(lastResponseTimeMillis).isLessThan(thresholdMillis);
    }

    @Then("the average response time is below {int} milliseconds")
    public void theAverageResponseTimeIsBelow(int thresholdMillis) {
        assertThat(recordedResponseTimes).isNotEmpty();
        double average = recordedResponseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(Double.NaN);
        assertThat(average).isLessThan(thresholdMillis);
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
        response = timedExecute(() -> safeExecute(() -> restTemplate.postForEntity(baseUrl, request, String.class)));
    }

    @When("I attempt to look up family members with lastname {string}")
    public void iAttemptToLookupWithLastname(String lastName) {
        String url = baseUrl + "/lastnames/" + lastName;
        response = timedExecute(() -> safeExecute(() -> restTemplate.getForEntity(url, String.class)));
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
        response = timedExecute(() -> safeExecute(() -> restTemplate.postForEntity(baseUrl + "/relationships", request, String.class)));
    }

    @Then("the request is rejected with bad request")
    public void theRequestIsRejectedWithBadRequest() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
