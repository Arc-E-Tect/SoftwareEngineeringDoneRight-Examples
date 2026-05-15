package com.arc_e_tect.examples.mfeadapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("vanilla")
@DisplayName("MfeAdapterApplication – context loads with vanilla profile")
class MfeAdapterApplicationTest {

    @Test
    @DisplayName("Spring context starts without errors")
    void contextLoads() {
        // If the context loads, the test passes.
    }
}
