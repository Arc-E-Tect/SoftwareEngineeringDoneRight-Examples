package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter.ApiKeyValidationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the MFA.
 *
 * <p>The MFA manages its own session cookie; the underlying HTTP session
 * is therefore stateless from Spring Security's perspective.
 *
 * <p>mTLS is enforced at the server level via {@code server.ssl.client-auth: need}
 * in {@code application.yml}.  This configuration layer handles API-key
 * header validation and session-cookie-based authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final ApiKeyValidationFilter apiKeyValidationFilter;

    public SecurityConfiguration(ApiKeyValidationFilter apiKeyValidationFilter) {
        this.apiKeyValidationFilter = apiKeyValidationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // The MFA uses its own session cookie – no server-side HttpSession needed.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CSRF is not applicable for a stateless API gateway layer.
                .csrf(csrf -> csrf.disable())

                // Authorisation rules
                .authorizeHttpRequests(auth -> auth
                        // All requests are permitted – the vanilla example has no auth layer.
                        .anyRequest().permitAll())

                // Custom filter: reject requests whose API key was not validated by the AG
                .addFilterBefore(apiKeyValidationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
