package com.arc_e_tect.examples.familyties.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cucumber test suite configuration for E2E tests.
 * 
 * <p>E2E tests run against a production-like Docker Compose stack.</p>
 * 
 * <h2>E2E Testing Workflow:</h2>
 * <p>Tests connect as REST clients to a containerized application managed by Gradle:</p>
 * <ol>
 *   <li><b>Automatic lifecycle:</b> {@code ./gradlew testE2E} (start → test → cleanup)</li>
 * </ol>
 * 
 * <p>Or use manual Docker Compose:</p>
 * <pre>
 *   cd app/src/testE2E/resources
 *   docker compose -f docker-compose.e2e.yml --env-file .env up -d --build
 *   cd ../../..
 *   ./gradlew testE2E
 *   docker compose -f app/src/testE2E/resources/docker-compose.e2e.yml down -v
 * </pre>
 * 
 * <h2>Architecture:</h2>
 * <ul>
 *   <li><b>Production-like:</b> Tests run against containerized application (not embedded)</li>
 *   <li><b>Full stack:</b> PostgreSQL + Family Ties app both in containers</li>
 *   <li><b>REST client tests:</b> No embedded server, pure external API testing</li>
 *   <li><b>Plugin lifecycle:</b> Gradle dockerCompose plugin manages startup/teardown</li>
 * </ul>
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("e2e")
public class CucumberSpringConfiguration {
    
    static {
        System.out.println("╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         E2E TEST CONFIGURATION                           ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Tests connect to:                                                       ║");
        System.out.println("║    Application: http://localhost:8081 (containerized)                    ║");
        System.out.println("║    PostgreSQL:  localhost:5432 (containerized)                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
    }
}

