package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EnglishToDutchResponseTransformer")
class EnglishToDutchResponseTransformerTest {

    private EnglishToDutchResponseTransformer transformer;
    private ObjectMapper objectMapper;
    private ProxiedRequest dummyRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        transformer = new EnglishToDutchResponseTransformer(objectMapper);
        dummyRequest = ProxiedRequest.builder()
                .method("GET")
                .path("/v1/familyties/persons")
                .build();
    }

    // -------------------------------------------------------------------------
    // supports()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("supports() returns false for empty body (HTTP 204 pattern)")
    void supports_emptyBody_returnsFalse() {
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(204)
                .body(new byte[0])
                .build();

        assertThat(transformer.supports(response)).isFalse();
    }

    @Test
    @DisplayName("supports() returns false for null body")
    void supports_nullBody_returnsFalse() {
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(204)
                .body(null)
                .build();

        assertThat(transformer.supports(response)).isFalse();
    }

    @Test
    @DisplayName("supports() returns true for non-empty body")
    void supports_nonEmptyBody_returnsTrue() {
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(200)
                .body("{\"firstName\":\"Jan\"}".getBytes(StandardCharsets.UTF_8))
                .build();

        assertThat(transformer.supports(response)).isTrue();
    }

    // -------------------------------------------------------------------------
    // transform() — person response (object)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transform() translates firstName and lastName to voornaam and achternaam")
    void transform_personResponse_renamesEnglishFieldsToDutch() throws Exception {
        String english = "{\"firstName\":\"Jan\",\"lastName\":\"Smit\"}";
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(200)
                .body(english.getBytes(StandardCharsets.UTF_8))
                .build();

        ProxiedResponse result = transformer.transform(response, dummyRequest);

        @SuppressWarnings("unchecked")
        var body = objectMapper.readValue(result.getBody(), Map.class);
        assertThat(body)
                .containsEntry("voornaam", "Jan")
                .containsEntry("achternaam", "Smit")
                .doesNotContainKey("firstName")
                .doesNotContainKey("lastName");
    }

    // -------------------------------------------------------------------------
    // transform() — relationship response with nested persons
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transform() translates nested fromPerson and toPerson fields recursively")
    void transform_relationshipResponseWithNestedPersons_renamesAllFields() throws Exception {
        String english = "{\"id\":1," +
                         "\"fromPerson\":{\"firstName\":\"Jan\",\"lastName\":\"Smit\"}," +
                         "\"toPerson\":{\"firstName\":\"Piet\",\"lastName\":\"Bakker\"}," +
                         "\"type\":\"parent\"}";
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(200)
                .body(english.getBytes(StandardCharsets.UTF_8))
                .build();

        ProxiedResponse result = transformer.transform(response, dummyRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(result.getBody(), Map.class);
        assertThat(body)
                .containsEntry("id", 1)
                .containsEntry("soort", "parent")
                .doesNotContainKey("type")
                .doesNotContainKey("fromPerson")
                .doesNotContainKey("toPerson")
                .containsKey("vanPersoon")
                .containsKey("naarPersoon");

        @SuppressWarnings("unchecked")
        Map<String, Object> vanPersoon = (Map<String, Object>) body.get("vanPersoon");
        assertThat(vanPersoon)
                .containsEntry("voornaam", "Jan")
                .containsEntry("achternaam", "Smit");

        @SuppressWarnings("unchecked")
        Map<String, Object> naarPersoon = (Map<String, Object>) body.get("naarPersoon");
        assertThat(naarPersoon)
                .containsEntry("voornaam", "Piet")
                .containsEntry("achternaam", "Bakker");
    }

    // -------------------------------------------------------------------------
    // transform() — JSON array response
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transform() translates all items in a JSON array response")
    void transform_arrayResponse_renamesFieldsInAllItems() throws Exception {
        String english = "[{\"firstName\":\"Jan\",\"lastName\":\"Smit\"}," +
                         "{\"firstName\":\"Piet\",\"lastName\":\"Bakker\"}]";
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(200)
                .body(english.getBytes(StandardCharsets.UTF_8))
                .build();

        ProxiedResponse result = transformer.transform(response, dummyRequest);

        List<Map<String, Object>> body = objectMapper.readValue(
                result.getBody(), new TypeReference<List<Map<String, Object>>>() {});
        assertThat(body).hasSize(2);
        assertThat(body.get(0))
                .containsEntry("voornaam", "Jan")
                .containsEntry("achternaam", "Smit");
        assertThat(body.get(1))
                .containsEntry("voornaam", "Piet")
                .containsEntry("achternaam", "Bakker");
    }

    @Test
    @DisplayName("transform() translates fields inside a list that is a value within a map")
    void transform_mapWithListFieldValue_renamesFieldsInsideList() throws Exception {
        // A map where one field value is itself a list — exercises the
        // else-if (value instanceof List) branch inside renameKeys(). Both the
        // Map-item branch and the non-Map-item (primitive) branch are exercised.
        String english = "{\"persons\":[{\"firstName\":\"Jan\",\"lastName\":\"Smit\"},\"literal\"]," +
                         "\"type\":\"family\"}";
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(200)
                .body(english.getBytes(StandardCharsets.UTF_8))
                .build();

        ProxiedResponse result = transformer.transform(response, dummyRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(result.getBody(), Map.class);
        @SuppressWarnings("unchecked")
        List<Object> persons = (List<Object>) body.get("persons");
        assertThat(persons).hasSize(2);
        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) persons.get(0);
        assertThat(first)
                .containsEntry("voornaam", "Jan")
                .containsEntry("achternaam", "Smit")
                .doesNotContainKey("firstName");
        // Non-Map item passes through unchanged
        assertThat(persons.get(1)).isEqualTo("literal");
    }

    // -------------------------------------------------------------------------
    // transform() — non-JSON body (graceful degradation)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transform() returns original response when body is not valid JSON")
    void transform_nonJsonBody_returnsOriginalResponse() {
        byte[] nonJson = "not-json".getBytes(StandardCharsets.UTF_8);
        ProxiedResponse response = ProxiedResponse.builder()
                .statusCode(200)
                .body(nonJson)
                .build();

        ProxiedResponse result = transformer.transform(response, dummyRequest);

        assertThat(result.getBody()).isEqualTo(nonJson);
    }
}
