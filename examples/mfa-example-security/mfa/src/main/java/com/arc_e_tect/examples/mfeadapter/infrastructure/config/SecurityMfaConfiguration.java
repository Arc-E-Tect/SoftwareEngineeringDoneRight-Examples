package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import com.arc_e_tect.examples.mfeadapter.domain.spi.DutchToEnglishRequestTransformer;
import com.arc_e_tect.examples.mfeadapter.domain.spi.EnglishToDutchResponseTransformer;
import com.arc_e_tect.examples.mfeadapter.domain.spi.RegexRoutingStrategy;
import com.arc_e_tect.examples.mfeadapter.domain.spi.RoutingStrategy;
import com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter.ApiKeyValidator;
import com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter.StandardApiKeyValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring configuration that registers the security-mfa beans.
 *
 * <p>Active only when the {@code security-mfa} Spring profile is set.
 * Registers:
 * <ul>
 *   <li>The {@link RegexRoutingStrategy} for path rewriting</li>
 *   <li>Payload-shaping transformers for Dutch ⇔ English field-name translation</li>
 *   <li>The {@link ApiKeyValidator} bean wired to MFA gateway properties</li>
 * </ul>
 */
@Configuration
@Profile("security-mfa")
@EnableConfigurationProperties(SecurityMfaProperties.class)
public class SecurityMfaConfiguration {

    /**
     * Routing strategy that rewrites inbound paths using a configurable regex pattern
     * and replacement string sourced from {@link SecurityMfaProperties}.
     */
    @Bean
    public RoutingStrategy routingStrategy(SecurityMfaProperties securityMfaProperties) {
        return new RegexRoutingStrategy(securityMfaProperties);
    }

    /**
     * Explicit Jackson 2 {@link ObjectMapper} bean.
     *
     * <p>Spring Boot 4 auto-configures a Jackson 3 ({@code tools.jackson}) mapper,
     * which is a different class. The payload transformers depend on the Jackson 2
     * API ({@code com.fasterxml.jackson}), so we register the mapper explicitly.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public DutchToEnglishRequestTransformer dutchToEnglishRequestTransformer(ObjectMapper objectMapper) {
        return new DutchToEnglishRequestTransformer(objectMapper);
    }

    @Bean
    public EnglishToDutchResponseTransformer englishToDutchResponseTransformer(ObjectMapper objectMapper) {
        return new EnglishToDutchResponseTransformer(objectMapper);
    }

    /**
     * Standard header-based {@link ApiKeyValidator} that reads the validated-header
     * and scope-header names from {@link MfeAdapterProperties}.
     */
    @Bean
    public ApiKeyValidator apiKeyValidator(MfeAdapterProperties mfeAdapterProperties) {
        return new StandardApiKeyValidator(mfeAdapterProperties);
    }
}
