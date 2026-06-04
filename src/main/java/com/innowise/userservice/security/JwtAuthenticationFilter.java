package com.innowise.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(AuthServiceClient authServiceClient, ObjectMapper objectMapper) {
        this.authServiceClient = authServiceClient;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        ValidateResponse validateResponse;
        try {
            validateResponse = authServiceClient.validate(token);
        } catch (RestClientException e) {
            writeUnauthorized(response, "Invalid or expired token");
            return;
        }

        if (!"access".equals(validateResponse.tokenType())) {
            writeUnauthorized(response, "Token is not an access token");
            return;
        }

        if (validateResponse.userId() == null || validateResponse.role() == null) {
            writeUnauthorized(response, "Token is missing required claims");
            return;
        }

        UserPrincipal principal = new UserPrincipal(Long.parseLong(validateResponse.userId()));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + validateResponse.role()))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                new ErrorResponse(401, "Unauthorized", message));
    }
}