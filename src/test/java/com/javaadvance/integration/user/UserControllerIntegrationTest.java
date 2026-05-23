package com.javaadvance.integration.user;

import com.javaadvance.dto.ErrorResponse;
import com.javaadvance.dto.UserCreateRequest;
import com.javaadvance.dto.UserResponse;
import com.javaadvance.dto.UserUpdateRequest;
import com.javaadvance.integration.config.BaseIntegrationTest;
import com.javaadvance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserSuccessfully() {

        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setBirthDate(LocalDate.of(2000, 1, 1));
        request.setEmail("ivan.controller@example.com");
        request.setActive(true);

        ResponseEntity<UserResponse> response =
                restTemplate.postForEntity(
                        "/users",
                        request,
                        UserResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getEmail())
                .isEqualTo("ivan.controller@example.com");

        assertThat(userRepository.existsByEmail("ivan.controller@example.com"))
                .isTrue();
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() {

        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setBirthDate(LocalDate.of(2000, 1, 1));
        request.setEmail("duplicate@example.com");
        request.setActive(true);

        restTemplate.postForEntity(
                "/users",
                request,
                UserResponse.class
        );

        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity(
                        "/users",
                        request,
                        ErrorResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    void shouldReturnBadRequestWhenEmailInvalid() {

        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setBirthDate(LocalDate.of(2000, 1, 1));
        request.setEmail("invalid-email");
        request.setActive(true);

        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity(
                        "/users",
                        request,
                        ErrorResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void shouldGetUserByIdSuccessfully() {

        UserCreateRequest request = new UserCreateRequest();
        request.setName("John");
        request.setSurname("Doe");
        request.setBirthDate(LocalDate.of(1995, 5, 5));
        request.setEmail("john.get@example.com");
        request.setActive(true);

        ResponseEntity<UserResponse> createResponse =
                restTemplate.postForEntity(
                        "/users",
                        request,
                        UserResponse.class
                );

        // Добавь эти проверки чтобы понять что вернул POST
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();

        UserResponse created = createResponse.getBody();

        ResponseEntity<UserResponse> response =
                restTemplate.getForEntity(
                        "/users/" + created.getId(),
                        UserResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getEmail()).isEqualTo("john.get@example.com");
    }

    @Test
    void shouldUpdateUserSuccessfully() {

        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setName("Old");
        createRequest.setSurname("Name");
        createRequest.setBirthDate(LocalDate.of(1990, 1, 1));
        createRequest.setEmail("old@example.com");
        createRequest.setActive(true);

        UserResponse created =
                restTemplate.postForEntity(
                        "/users",
                        createRequest,
                        UserResponse.class
                ).getBody();

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("New");
        updateRequest.setSurname("Updated");
        updateRequest.setBirthDate(LocalDate.of(1991, 2, 2));
        updateRequest.setEmail("new@example.com");
        updateRequest.setActive(true);

        HttpEntity<UserUpdateRequest> entity =
                new HttpEntity<>(updateRequest);

        ResponseEntity<UserResponse> response =
                restTemplate.exchange(
                        "/users/" + created.getId(),
                        HttpMethod.PUT,
                        entity,
                        UserResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("New");
        assertThat(response.getBody().getEmail())
                .isEqualTo("new@example.com");
    }

    @Test
    void shouldReturnFilteredUsers() {

        UserCreateRequest request1 = new UserCreateRequest();
        request1.setName("Alex");
        request1.setSurname("Smith");
        request1.setBirthDate(LocalDate.of(1990, 1, 1));
        request1.setEmail("alex@example.com");
        request1.setActive(true);


        UserCreateRequest request2 = new UserCreateRequest();
        request2.setName("Maria");
        request2.setSurname("Johnson");
        request2.setBirthDate(LocalDate.of(1992, 2, 2));
        request2.setEmail("maria@example.com");
        request2.setActive(true);


        restTemplate.postForEntity("/users", request1, UserResponse.class);
        restTemplate.postForEntity("/users", request2, UserResponse.class);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/users?firstName=Alex&page=0&size=10",
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).contains("alex@example.com");
        assertThat(response.getBody()).doesNotContain("maria@example.com");
    }
}