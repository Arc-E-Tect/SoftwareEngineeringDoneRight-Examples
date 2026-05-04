package com.arc_e_tect.examples.mfeadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the MFA template application.
 *
 * <p>All MFA instances share this main class. Behaviour is driven by
 * configuration ({@code application.yml}) and by providing Spring beans
 * that implement the SPI interfaces in {@code com.arc_e_tect.examples.mfeadapter.domain.spi}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class MfeAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MfeAdapterApplication.class, args);
    }
}
