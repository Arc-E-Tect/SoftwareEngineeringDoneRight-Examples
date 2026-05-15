package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

/**
 * Kafka consumer configuration for the MFA reference-data cache.
 *
 * <p>A dedicated consumer group is used so that every MFA instance
 * independently receives all reference-data events and can maintain its
 * own local cache.
 *
 * <p>Deserializer settings are driven by {@code spring.kafka.consumer.*}
 * properties in {@code application.yml}; Spring Boot auto-configures the
 * underlying {@link ConsumerFactory} accordingly.
 */
@Configuration
public class KafkaConfiguration {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    referenceDataKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
