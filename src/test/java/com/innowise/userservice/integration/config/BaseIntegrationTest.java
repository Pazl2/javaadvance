package com.innowise.userservice.integration.config;

import com.innowise.userservice.UserServiceApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.annotation.PostConstruct;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = UserServiceApplication.class
)
@ActiveProfiles("test")
@Import({TestcontainersConfig.class, TestSecurityConfig.class})
public abstract class BaseIntegrationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @PostConstruct
    void setup() {
        restTemplate.getRestTemplate().getInterceptors().add(
                (request, body, execution) -> {
                    request.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    request.getHeaders().set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    return execution.execute(request, body);
                }
        );
    }
}