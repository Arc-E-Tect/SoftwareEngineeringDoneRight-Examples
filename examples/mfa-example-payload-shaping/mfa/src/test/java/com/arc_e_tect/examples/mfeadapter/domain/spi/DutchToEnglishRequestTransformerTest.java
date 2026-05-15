package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DutchToEnglishRequestTransformer")
class DutchToEnglishRequestTransformerTest {

    private DutchToEnglishRequestTransformer transformer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        transformer = new DutchToEnglishRequestTransformer(objectMapper);
    }

    // -------------------------------------------------------------------------
    // supports()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("supports() returns false for empty body")
    void supports_emptyBody_returnsFalse() {
        ProxiedRequest request = ProxiedRequest.builder()
                .method("POST")
                .path("/v1/familyties/persons")
                .body(new byte[0])
                .build();

        assertThat(transformer.supports(request)).isFalse();
    }

    @Test
    @DisplayName("supports() returns false for null body")
    void supports_nullBody_returnsFalse() {
        ProxiedRequest request = ProxiedRequest.builder()
                .method("POST")
                .path("/v1/familyties/persons")
                .body(null)
                .build();

        assertThat(transformer.supports(request)).isFalse();
    }

    @Test
    @DisplayName("supports() returns true for non-empty body")
    void supports_nonEmptyBody_returnsTrue() {
        ProxiedRequest request = ProxiedRequest.builder()
                .method("POST")
                .path("/v1/familyties/persons")
                .body("{\"voornaam\":\"Jan\"}".getBytes(StandardCharsets.UTF_8))
                .build();

        assertThat(transformer.supports(request)).isTrue();
    }

    // -------------------------------------------------------------------------
    // transform() — person request
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transform() translates voornaam and achternaam to firstName and lastName")
    void transform_personRequest_renamesDutchFieldsToEnglish() throws Exception {
        String dutch = "{\"voornaam\":\"Jan\",\"achternaam\":\"Smit\"}";
        ProxiedRequest request = ProxiedRequest.builder()
                .method("POST")
                .path("/v1/familyties/persons")
                .body(dutch.getBytes(StandardCharsets.UTF_8))
                .build();

        ProxiedRequest result = transformer.transform(request);

        @SuppressWarnings("unchecked")
        var body = objectMapper.readValue(result.getBody(), java.util.Map.class);
        assertThat(body).containsEntry("firstName", "Jan")
                        .containsEntry("lastName", "Smit")
                        .doesNotContainKey("voornaam")
                        .doesNotContainKey("achternaam");
    }

    // -------------------------------------------------------------------------
    // transform() — relationship request
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transform() translates relationship Dutch fields to English")
    void transform_relationshipRequest_renamesAllDutchFields() throws Exception {
        String dutch = "{\"vanVoornaam\":\"Jan\",\"vanAchternaam\":\"Smit\"," +
                       "\"naarVoornaam\":\"Piet\",\"naarAchternaam\":\"Bakker\",\"soort\":\"parent\"}";
        ProxiedRequest request = ProxiedRequest.builder()
                .method("POST")
                .path("/v1/familyties/relationships")
                .body(dutch.getBytes(StandardCharsets.UTF_8))
                .build();

        ProxiedRequest result = transformer.transform(request);

        @SuppressWarnings("unchecked")
        var body = objectMapper.readValue(result.getBody(), java.util.Map.class);
        assertThat(body)
                .containsEntry("fromFirstName", "Jan")
                .containsEntry("fromLastName", "Smit")
                .containsEntry("toFirstName", "Piet")
                .containsEntry("toLastName", "Bakker")
                .containsEntry("type", "parent")
                .doesNotContainKey("vanVoornaam")
                .doesNotContainKey("vanAchternaam")
                .doesNotContainKey("naarVoornaam")
                .doesNotContainKey("naarAchternaam")
                .doesNotContainKey("soort");
    }

    // -------------------------------------------------------------------------
    // transform() — unknown field passthrough
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transform() passes through unknown field names unchanged")
    void transform_unknownField_passesThrough() throws Exception {
        String json = "{\"unknownField\":\"value\",\"voornaam\":\"Jan\"}";
        ProxiedRequest request = ProxiedRequest.builder()
                .method("POST")
                .path("/v1/familyties/persons")
                .body(json.getBytes(StandardCharsets.UTF_8))
                .build();

        ProxiedRequest result = transformer.transform(request);

        @SuppressWarnings("unchecked")
        var body = objectMapper.readValue(result.getBody(), java.util.Map.class);
        assertThat(body)
                .containsEntry("unknownField", "value")
                .containsEntry("firstName", "Jan");
    }

    // -------------------------------------------------------------------------
    // transform() — non-JSON body (graceful degradation)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transform() returns original request when body is not valid JSON")
    void transform_nonJsonBody_returnsOriginalRequest() {
        byte[] nonJson = "not-json".getBytes(StandardCharsets.UTF_8);
        ProxiedRequest request = ProxiedRequest.builder()
                .method("POST")
                .path("/v1/familyties/persons")
                .body(nonJson)
                .build();

        ProxiedRequest result = transformer.transform(request);

        assertThat(result.getBody()).isEqualTo(nonJson);
    }
}
