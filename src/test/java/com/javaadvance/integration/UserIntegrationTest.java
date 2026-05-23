package com.javaadvance.integration;

import com.javaadvance.exception.DublicateEmailException;
import com.javaadvance.integration.config.BaseIntegrationTest;
import com.javaadvance.dto.UserCreateRequest;
import com.javaadvance.dto.UserResponse;
import com.javaadvance.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setBirthDate(LocalDate.of(2005, 4, 1));
        request.setEmail("ivan.integration@example.com");

        System.out.println("Email: " + request.getEmail());
        System.out.println("Name: " + request.getName());

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("ivan.integration@example.com");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        UserCreateRequest request1 = createTestUserRequest("duplicate@example.com");
        userService.createUser(request1);

        UserCreateRequest request2 = createTestUserRequest("duplicate@example.com");

        org.junit.jupiter.api.Assertions.assertThrows(
                DublicateEmailException.class,
                () -> userService.createUser(request2)
        );
    }

    private UserCreateRequest createTestUserRequest(String email) {
        UserCreateRequest req = new UserCreateRequest();
        req.setName("Test");
        req.setSurname("User");
        req.setBirthDate(LocalDate.of(2005, 4, 1));
        req.setEmail(email);
        return req;
    }
}