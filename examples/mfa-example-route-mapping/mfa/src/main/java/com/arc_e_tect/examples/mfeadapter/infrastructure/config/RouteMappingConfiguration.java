package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import com.arc_e_tect.examples.mfeadapter.domain.spi.DutchToEnglishRequestTransformer;
import com.arc_e_tect.examples.mfeadapter.domain.spi.EnglishToDutchResponseTransformer;
import com.arc_e_tect.examples.mfeadapter.domain.spi.RegexRoutingStrategy;
import com.arc_e_tect.examples.mfeadapter.domain.spi.RoutingStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring configuration that registers the route-mapping beans.
 *
 * <p>Active only when the {@code route-mapping} Spring profile is set.
 * Registers the {@link com.arc_e_tect.examples.mfeadapter.domain.spi.RegexRoutingStrategy}
 * which rewrites the inbound path using a configurable regex pattern and replacement,
 * as well as the payload-shaping transformer beans for Dutch ⇔ English field-name translation.
 */
@Configuration
@Profile("route-mapping")
@EnableConfigurationProperties(RouteMappingProperties.class)
public class RouteMappingConfiguration {

    /**
     * Explicit Jackson 2 {@link ObjectMapper} bean.
     *
     * <p>Spring Boot 4 auto-configures a Jackson 3 ({@code tools.jackson}) mapper,
     * which is a different class. The payload transformers depend on the Jackson 2
     * API ({@code com.fasterxml.jackson}), so we register the mapper explicitly.
     */
    @Bean
    public RoutingStrategy routingStrategy(RouteMappingProperties routeMappingProperties) {
        return new RegexRoutingStrategy(routeMappingProperties);
    }

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
}
