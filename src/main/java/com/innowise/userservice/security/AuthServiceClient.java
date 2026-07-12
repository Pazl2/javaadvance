package com.innowise.userservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthServiceClient {

    private final RestClient restClient;

    public AuthServiceClient(@Value("${auth.service.url}") String authServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }

    public ValidateResponse validate(String token) {
        return restClient.post()
                .uri("/api/auth/validate")
                .body(new ValidateRequest(token))
                .retrieve()
                .body(ValidateResponse.class);
    }
}