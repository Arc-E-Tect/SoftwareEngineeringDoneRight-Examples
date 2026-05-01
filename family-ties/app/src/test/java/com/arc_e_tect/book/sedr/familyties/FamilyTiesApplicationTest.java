package com.arc_e_tect.book.sedr.familyties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FamilyTiesApplicationTest {

    @Test
    @DisplayName("loads the Spring application context")
    void contextLoads() {
        // This test ensures the Spring context can be loaded, which covers the @SpringBootApplication annotation
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("invokes SpringApplication.run from main")
    void mainMethodStartsSpringApplication() {
        // Test that main method can be invoked
        try (var mockStatic = mockStatic(SpringApplication.class)) {
            FamilyTiesApplication.main(new String[]{});
            mockStatic.verify(() -> SpringApplication.run(FamilyTiesApplication.class, new String[]{}));
        }
    }
}
