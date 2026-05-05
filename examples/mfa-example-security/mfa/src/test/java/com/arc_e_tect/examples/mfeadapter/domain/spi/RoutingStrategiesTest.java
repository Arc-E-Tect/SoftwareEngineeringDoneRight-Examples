package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.SecurityMfaProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Routing strategies")
class RoutingStrategiesTest {

    private ProxiedRequest requestWith(String path) {
        return ProxiedRequest.builder().method("GET").path(path).build();
    }

    // -----------------------------------------------------------------
    // PassThroughRoutingStrategy
    // -----------------------------------------------------------------

    @Test
    @DisplayName("PassThrough – returns original path unchanged")
    void passThrough_returnsOriginalPath() {
        PassThroughRoutingStrategy strategy = new PassThroughRoutingStrategy();
        assertThat(strategy.resolvePath(requestWith("/v1/persons"))).isEqualTo("/v1/persons");
    }

    // -----------------------------------------------------------------
    // PrefixStrippingRoutingStrategy
    // -----------------------------------------------------------------

    @Test
    @DisplayName("PrefixStripping – strips configured context-path prefix")
    void prefixStripping_stripsPrefix() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getMicroservice().setContextPath("/api");
        PrefixStrippingRoutingStrategy strategy = new PrefixStrippingRoutingStrategy(props);

        assertThat(strategy.resolvePath(requestWith("/api/persons"))).isEqualTo("/persons");
    }

    @Test
    @DisplayName("PrefixStripping – stripped path empty → returns /")
    void prefixStripping_emptyRemainder_returnsRoot() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getMicroservice().setContextPath("/api");
        PrefixStrippingRoutingStrategy strategy = new PrefixStrippingRoutingStrategy(props);

        assertThat(strategy.resolvePath(requestWith("/api"))).isEqualTo("/");
    }

    @Test
    @DisplayName("PrefixStripping – path does not start with prefix → returned unchanged")
    void prefixStripping_noMatch_returnsOriginal() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getMicroservice().setContextPath("/api");
        PrefixStrippingRoutingStrategy strategy = new PrefixStrippingRoutingStrategy(props);

        assertThat(strategy.resolvePath(requestWith("/other/path"))).isEqualTo("/other/path");
    }

    @Test
    @DisplayName("PrefixStripping – blank context-path → path unchanged")
    void prefixStripping_blankContextPath_returnsOriginal() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getMicroservice().setContextPath("  ");
        PrefixStrippingRoutingStrategy strategy = new PrefixStrippingRoutingStrategy(props);

        assertThat(strategy.resolvePath(requestWith("/v1/persons"))).isEqualTo("/v1/persons");
    }

    // -----------------------------------------------------------------
    // PathMappingRoutingStrategy
    // -----------------------------------------------------------------

    @Test
    @DisplayName("PathMapping – matches longest prefix and replaces it")
    void pathMapping_longestPrefixWins() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getRouting().setPathMappings(Map.of(
                "/api/v1", "/v1",
                "/api/v1/persons", "/persons"
        ));
        PathMappingRoutingStrategy strategy = new PathMappingRoutingStrategy(props);

        // /api/v1/persons should match the longer prefix
        assertThat(strategy.resolvePath(requestWith("/api/v1/persons/123")))
                .isEqualTo("/persons/123");
    }

    @Test
    @DisplayName("PathMapping – no prefix match → path returned unchanged")
    void pathMapping_noMatch_returnsOriginal() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getRouting().setPathMappings(Map.of("/api/v1", "/v1"));
        PathMappingRoutingStrategy strategy = new PathMappingRoutingStrategy(props);

        assertThat(strategy.resolvePath(requestWith("/other/path"))).isEqualTo("/other/path");
    }

    @Test
    @DisplayName("PathMapping – empty mappings → path returned unchanged")
    void pathMapping_emptyMappings_returnsOriginal() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        PathMappingRoutingStrategy strategy = new PathMappingRoutingStrategy(props);

        assertThat(strategy.resolvePath(requestWith("/v1/persons"))).isEqualTo("/v1/persons");
    }

    @Test
    @DisplayName("PathMapping – null mappings → path returned unchanged")
    void pathMapping_nullMappings_returnsOriginal() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getRouting().setPathMappings(null);
        PathMappingRoutingStrategy strategy = new PathMappingRoutingStrategy(props);

        assertThat(strategy.resolvePath(requestWith("/v1/persons"))).isEqualTo("/v1/persons");
    }

    // -----------------------------------------------------------------
    // RegexRoutingStrategy
    // -----------------------------------------------------------------

    private RegexRoutingStrategy regexStrategy(String pattern, String replacement) {
        SecurityMfaProperties props = new SecurityMfaProperties();
        props.getRouting().setPathPattern(pattern);
        props.getRouting().setPathReplacement(replacement);
        return new RegexRoutingStrategy(props);
    }

    @Test
    @DisplayName("Regex – strips dutch segment between version and resource")
    void regex_stripsDutchSegment() {
        RegexRoutingStrategy strategy = regexStrategy("^(/v\\d+)/dutch(/.+)$", "$1$2");

        assertThat(strategy.resolvePath(requestWith("/v1/dutch/familyties"))).isEqualTo("/v1/familyties");
    }

    @Test
    @DisplayName("Regex – preserves query-less path with sub-resource")
    void regex_preservesSubResource() {
        RegexRoutingStrategy strategy = regexStrategy("^(/v\\d+)/dutch(/.+)$", "$1$2");

        assertThat(strategy.resolvePath(requestWith("/v1/dutch/familyties/lastnames/Smith")))
                .isEqualTo("/v1/familyties/lastnames/Smith");
    }

    @Test
    @DisplayName("Regex – non-matching path returned unchanged (e.g. /actuator/health)")
    void regex_noMatch_returnsOriginal() {
        RegexRoutingStrategy strategy = regexStrategy("^(/v\\d+)/dutch(/.+)$", "$1$2");

        assertThat(strategy.resolvePath(requestWith("/actuator/health"))).isEqualTo("/actuator/health");
    }

    @Test
    @DisplayName("Regex – pattern matches highest available version segment")
    void regex_highVersionNumber() {
        RegexRoutingStrategy strategy = regexStrategy("^(/v\\d+)/dutch(/.+)$", "$1$2");

        assertThat(strategy.resolvePath(requestWith("/v12/dutch/familyties"))).isEqualTo("/v12/familyties");
    }
}
