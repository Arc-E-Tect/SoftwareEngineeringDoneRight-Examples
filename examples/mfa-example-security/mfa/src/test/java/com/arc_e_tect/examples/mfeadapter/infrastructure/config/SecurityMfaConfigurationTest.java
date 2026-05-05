package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import com.arc_e_tect.examples.mfeadapter.domain.spi.RoutingStrategy;
import com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter.ApiKeyValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityMfaConfiguration")
class SecurityMfaConfigurationTest {

    private SecurityMfaConfiguration config;
    private MfeAdapterProperties mfeAdapterProperties;
    private SecurityMfaProperties securityMfaProperties;

    @BeforeEach
    void setUp() {
        mfeAdapterProperties = new MfeAdapterProperties();
        mfeAdapterProperties.getApiGateway().setValidatedHeader("X-API-Key-Validated");
        mfeAdapterProperties.getApiGateway().setValidatedValue("true");
        mfeAdapterProperties.getApiGateway().setScopeHeader("X-API-Key-Scope");

        securityMfaProperties = new SecurityMfaProperties();
        securityMfaProperties.getRouting().setPathPattern("^(/v\\d+)/dutch(/.+)$");
        securityMfaProperties.getRouting().setPathReplacement("$1$2");

        config = new SecurityMfaConfiguration();
    }

    @Test
    @DisplayName("routingStrategy – creates a RoutingStrategy from SecurityMfaProperties")
    void routingStrategy_createsBean() {
        RoutingStrategy strategy = config.routingStrategy(securityMfaProperties);
        assertThat(strategy).isNotNull();
    }

    @Test
    @DisplayName("objectMapper – creates a non-null ObjectMapper")
    void objectMapper_createsBean() {
        ObjectMapper mapper = config.objectMapper();
        assertThat(mapper).isNotNull();
    }

    @Test
    @DisplayName("dutchToEnglishRequestTransformer – creates transformer bean")
    void dutchToEnglishRequestTransformer_createsBean() {
        ObjectMapper mapper = config.objectMapper();
        assertThat(config.dutchToEnglishRequestTransformer(mapper)).isNotNull();
    }

    @Test
    @DisplayName("englishToDutchResponseTransformer – creates transformer bean")
    void englishToDutchResponseTransformer_createsBean() {
        ObjectMapper mapper = config.objectMapper();
        assertThat(config.englishToDutchResponseTransformer(mapper)).isNotNull();
    }

    @Test
    @DisplayName("apiKeyValidator – creates an ApiKeyValidator bean")
    void apiKeyValidator_createsBean() {
        ApiKeyValidator validator = config.apiKeyValidator(mfeAdapterProperties);
        assertThat(validator).isNotNull();
    }
}
