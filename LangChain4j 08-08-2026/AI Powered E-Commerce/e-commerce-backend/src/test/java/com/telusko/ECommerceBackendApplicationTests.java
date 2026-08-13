package com.telusko;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Docker Compose support is skipped inside tests by default, which leaves the context without a
// datasource. The app has no other database configuration, so re-enable it for the context test.
@SpringBootTest
@TestPropertySource(properties = "spring.docker.compose.skip.in-tests=false")
class ECommerceBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
