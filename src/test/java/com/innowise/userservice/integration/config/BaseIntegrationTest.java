package com.innowise.userservice.integration.config;

import com.innowise.userservice.UserServiceApplication;
import com.innowise.userservice.security.AuthServiceClient;
import com.innowise.userservice.security.ValidateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = UserServiceApplication.class
)
@ActiveProfiles("test")
@Import({TestcontainersConfig.class, TestSecurityConfig.class})
public abstract class BaseIntegrationTest {

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @Autowired
    protected TestRestTemplate restTemplate;

    private String currentToken = null;

    @BeforeEach
    void resetToken() {
        currentToken = null;
    }

    protected void authenticateAsAdmin(Long userId) {
        when(authServiceClient.validate(any()))
                .thenReturn(new ValidateResponse("login", String.valueOf(userId), "ADMIN", "access"));
        currentToken = "Bearer test-token";
    }

    protected void authenticateAsUser(Long userId) {
        when(authServiceClient.validate(any()))
                .thenReturn(new ValidateResponse("login", String.valueOf(userId), "USER", "access"));
        currentToken = "Bearer test-token";
    }

    @PostConstruct
    void setup() {
        restTemplate.getRestTemplate().getInterceptors().add(
                (request, body, execution) -> {
                    request.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    request.getHeaders().set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    if (currentToken != null) {
                        request.getHeaders().set(HttpHeaders.AUTHORIZATION, currentToken);
                    }
                    return execution.execute(request, body);
                }
        );
    }
}