package com.innowise.userservice.security;

public record ValidateResponse(String login, String userId, String role, String tokenType) {}