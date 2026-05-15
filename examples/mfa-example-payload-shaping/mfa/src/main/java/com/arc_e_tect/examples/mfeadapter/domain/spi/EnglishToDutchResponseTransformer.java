package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ResponseTransformer} that renames English field names returned by the
 * Family Ties microservice to their Dutch equivalents expected by the MFE.
 *
 * <p>Field-name mapping (English → Dutch):
 * <ul>
 *   <li>{@code firstName}    → {@code voornaam}</li>
 *   <li>{@code lastName}     → {@code achternaam}</li>
 *   <li>{@code fromFirstName}→ {@code vanVoornaam}</li>
 *   <li>{@code fromLastName} → {@code vanAchternaam}</li>
 *   <li>{@code toFirstName}  → {@code naarVoornaam}</li>
 *   <li>{@code toLastName}   → {@code naarAchternaam}</li>
 *   <li>{@code type}         → {@code soort}</li>
 *   <li>{@code fromPerson}   → {@code vanPersoon}</li>
 *   <li>{@code toPerson}     → {@code naarPersoon}</li>
 * </ul>
 *
 * <p>Nested objects ({@code fromPerson} / {@code toPerson}) and JSON arrays
 * are translated recursively.  Responses with an empty body (e.g. HTTP 204)
 * are skipped via {@link #supports(ProxiedResponse)}.
 */
public class EnglishToDutchResponseTransformer implements ResponseTransformer {

    private static final Map<String, String> ENGLISH_TO_DUTCH = Map.of(
            "firstName",     "voornaam",
            "lastName",      "achternaam",
            "fromFirstName", "vanVoornaam",
            "fromLastName",  "vanAchternaam",
            "toFirstName",   "naarVoornaam",
            "toLastName",    "naarAchternaam",
            "type",          "soort",
            "fromPerson",    "vanPersoon",
            "toPerson",      "naarPersoon"
    );

    private final ObjectMapper objectMapper;

    public EnglishToDutchResponseTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(ProxiedResponse response) {
        byte[] body = response.getBody();
        return body != null && body.length > 0;
    }

    @Override
    public ProxiedResponse transform(ProxiedResponse response, ProxiedRequest originalRequest) {
        try {
            byte[] rawBody = response.getBody();
            byte[] newBody;

            String bodyString = new String(rawBody).trim();
            if (bodyString.startsWith("[")) {
                List<Map<String, Object>> list = objectMapper.readValue(
                        rawBody, new TypeReference<List<Map<String, Object>>>() {});
                List<Map<String, Object>> translated = new ArrayList<>();
                for (Map<String, Object> item : list) {
                    translated.add(renameKeys(item));
                }
                newBody = objectMapper.writeValueAsBytes(translated);
            } else {
                Map<String, Object> original = objectMapper.readValue(
                        rawBody, new TypeReference<Map<String, Object>>() {});
                newBody = objectMapper.writeValueAsBytes(renameKeys(original));
            }

            return ProxiedResponse.builder()
                    .statusCode(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(newBody)
                    .build();
        } catch (IOException e) {
            // Body is not JSON — return the response unchanged
            return response;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> renameKeys(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String newKey = ENGLISH_TO_DUTCH.getOrDefault(entry.getKey(), entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = renameKeys((Map<String, Object>) value);
            } else if (value instanceof List) {
                List<Object> translatedList = new ArrayList<>();
                for (Object item : (List<?>) value) {
                    if (item instanceof Map) {
                        translatedList.add(renameKeys((Map<String, Object>) item));
                    } else {
                        translatedList.add(item);
                    }
                }
                value = translatedList;
            }
            result.put(newKey, value);
        }
        return result;
    }
}
