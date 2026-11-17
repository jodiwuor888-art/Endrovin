package com.portfolio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PortfolioApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context initializes with all entities, repositories, and security beans
    }
}
