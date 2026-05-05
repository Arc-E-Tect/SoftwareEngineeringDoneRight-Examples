package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link RequestTransformer} that renames Dutch field names in the MFE request
 * body to their English equivalents expected by the Family Ties microservice.
 *
 * <p>Field-name mapping (Dutch → English):
 * <ul>
 *   <li>{@code voornaam}     → {@code firstName}</li>
 *   <li>{@code achternaam}   → {@code lastName}</li>
 *   <li>{@code vanVoornaam}  → {@code fromFirstName}</li>
 *   <li>{@code vanAchternaam}→ {@code fromLastName}</li>
 *   <li>{@code naarVoornaam} → {@code toFirstName}</li>
 *   <li>{@code naarAchternaam}→ {@code toLastName}</li>
 *   <li>{@code soort}        → {@code type}</li>
 *   <li>{@code vanPersoon}   → {@code fromPerson}</li>
 *   <li>{@code naarPersoon}  → {@code toPerson}</li>
 * </ul>
 *
 * <p>Unknown field names are passed through unchanged.
 * Requests with an empty body are skipped via {@link #supports(ProxiedRequest)}.
 */
public class DutchToEnglishRequestTransformer implements RequestTransformer {

    private static final Map<String, String> DUTCH_TO_ENGLISH = Map.of(
            "voornaam",      "firstName",
            "achternaam",    "lastName",
            "vanVoornaam",   "fromFirstName",
            "vanAchternaam", "fromLastName",
            "naarVoornaam",  "toFirstName",
            "naarAchternaam","toLastName",
            "soort",         "type",
            "vanPersoon",    "fromPerson",
            "naarPersoon",   "toPerson"
    );

    private final ObjectMapper objectMapper;

    public DutchToEnglishRequestTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(ProxiedRequest request) {
        byte[] body = request.getBody();
        return body != null && body.length > 0;
    }

    @Override
    public ProxiedRequest transform(ProxiedRequest request) {
        try {
            Map<String, Object> original = objectMapper.readValue(
                    request.getBody(),
                    new TypeReference<Map<String, Object>>() {});
            Map<String, Object> translated = renameKeys(original);
            byte[] newBody = objectMapper.writeValueAsBytes(translated);
            return ProxiedRequest.builder()
                    .method(request.getMethod())
                    .path(request.getPath())
                    .queryString(request.getQueryString())
                    .headers(request.getHeaders())
                    .body(newBody)
                    .sessionId(request.getSessionId())
                    .apiKeyScope(request.getApiKeyScope())
                    .version(request.getVersion())
                    .build();
        } catch (IOException e) {
            // Body is not JSON — return the request unchanged
            return request;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> renameKeys(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String newKey = DUTCH_TO_ENGLISH.getOrDefault(entry.getKey(), entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = renameKeys((Map<String, Object>) value);
            }
            result.put(newKey, value);
        }
        return result;
    }
}
