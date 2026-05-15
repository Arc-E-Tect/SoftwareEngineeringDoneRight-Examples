package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.strategy;

import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Version extraction strategies")
class VersionExtractionStrategiesTest {

    // -----------------------------------------------------------------
    // HeaderVersionExtractionStrategy
    // -----------------------------------------------------------------

    @Test
    @DisplayName("Header strategy – header present → returns versioned path with original URI")
    void header_headerPresent_returnsVersionedPath() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getVersioning().setVersionHeader("X-API-Version");
        HeaderVersionExtractionStrategy strategy = new HeaderVersionExtractionStrategy(props);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");
        req.addHeader("X-API-Version", "1");

        VersionedPath vp = strategy.extract(req);

        assertThat(vp).isNotNull();
        assertThat(vp.version()).isEqualTo("1");
        assertThat(vp.strippedPath()).isEqualTo("/v1/persons");
    }

    @Test
    @DisplayName("Header strategy – header absent → returns null")
    void header_headerAbsent_returnsNull() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        HeaderVersionExtractionStrategy strategy = new HeaderVersionExtractionStrategy(props);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");

        VersionedPath vp = strategy.extract(req);

        assertThat(vp).isNull();
    }

    @Test
    @DisplayName("Header strategy – header blank → returns null")
    void header_headerBlank_returnsNull() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        HeaderVersionExtractionStrategy strategy = new HeaderVersionExtractionStrategy(props);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");
        req.addHeader("X-API-Version", "   ");

        VersionedPath vp = strategy.extract(req);

        assertThat(vp).isNull();
    }

    // -----------------------------------------------------------------
    // PathVersionExtractionStrategy
    // -----------------------------------------------------------------

    @Test
    @DisplayName("Path strategy – /v1/persons → version=1, channel 'persons' stripped")
    void path_versionOnly_extracted() {
        PathVersionExtractionStrategy strategy = new PathVersionExtractionStrategy();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1/persons");

        VersionedPath vp = strategy.extract(req);

        assertThat(vp).isNotNull();
        assertThat(vp.version()).isEqualTo("1");
        // 'persons' is parsed as a channel segment (all-alpha, no sub-path), stripped path is /
        assertThat(vp.strippedPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("Path strategy – /v2/web/customers → version=2, channel stripped, stripped=/customers")
    void path_versionAndChannel_channelStripped() {
        PathVersionExtractionStrategy strategy = new PathVersionExtractionStrategy();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v2/web/customers");

        VersionedPath vp = strategy.extract(req);

        assertThat(vp).isNotNull();
        assertThat(vp.version()).isEqualTo("2");
        assertThat(vp.strippedPath()).isEqualTo("/customers");
    }

    @Test
    @DisplayName("Path strategy – /v1 (no path after version) → version=1, stripped=/")
    void path_versionNoRemainder_strippedIsRoot() {
        PathVersionExtractionStrategy strategy = new PathVersionExtractionStrategy();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v1");

        VersionedPath vp = strategy.extract(req);

        assertThat(vp).isNotNull();
        assertThat(vp.version()).isEqualTo("1");
        assertThat(vp.strippedPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("Path strategy – non-versioned path → returns null")
    void path_nonVersionedPath_returnsNull() {
        PathVersionExtractionStrategy strategy = new PathVersionExtractionStrategy();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/persons");

        VersionedPath vp = strategy.extract(req);

        assertThat(vp).isNull();
    }

    // -----------------------------------------------------------------
    // VersionedPath record
    // -----------------------------------------------------------------

    @Test
    @DisplayName("VersionedPath – constructor and accessors work correctly")
    void versionedPath_accessors() {
        VersionedPath vp = new VersionedPath("3", "/api/v3/items");

        assertThat(vp.version()).isEqualTo("3");
        assertThat(vp.strippedPath()).isEqualTo("/api/v3/items");
    }
}
