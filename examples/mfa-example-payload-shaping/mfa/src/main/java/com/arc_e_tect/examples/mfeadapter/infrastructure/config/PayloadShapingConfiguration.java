package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import com.arc_e_tect.examples.mfeadapter.domain.spi.DutchToEnglishRequestTransformer;
import com.arc_e_tect.examples.mfeadapter.domain.spi.EnglishToDutchResponseTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring configuration that registers the payload-shaping transformer beans.
 *
 * <p>Active only when the {@code payload-shaper} Spring profile is set.
 * Both beans are picked up by the MFA framework's proxy pipeline via the
 * {@link com.arc_e_tect.examples.mfeadapter.domain.spi.RequestTransformer} and
 * {@link com.arc_e_tect.examples.mfeadapter.domain.spi.ResponseTransformer} SPI contracts.
 */
@Configuration
@Profile("payload-shaper")
public class PayloadShapingConfiguration {

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
}
