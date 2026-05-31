package com.innowise.userservice.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
@ConditionalOnProperty(name = "security.method.enabled", havingValue = "true", matchIfMissing = true)
public class MethodSecurityConfig {
}