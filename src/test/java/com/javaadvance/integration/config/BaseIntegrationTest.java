package com.javaadvance.integration.config;

import com.javaadvance.JavaadvanceApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = JavaadvanceApplication.class
)
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

}