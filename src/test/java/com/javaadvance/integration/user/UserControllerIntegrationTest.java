package com.javaadvance.integration.user;

import com.javaadvance.dto.ActiveStatusRequest;
import com.javaadvance.dto.ErrorResponse;
import com.javaadvance.dto.UserCreateRequest;
import com.javaadvance.dto.UserResponse;
import com.javaadvance.dto.UserUpdateRequest;
import com.javaadvance.integration.config.BaseIntegrationTest;
import com.javaadvance.repository.PaymentCardRepository;
import com.javaadvance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @BeforeEach
    void cleanUp() {
        paymentCardRepository.deleteAll();
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
                restTemplate.postForEntity("/users", request, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("ivan.controller@example.com");
        assertThat(userRepository.existsByEmail("ivan.controller@example.com")).isTrue();
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() {

        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setBirthDate(LocalDate.of(2000, 1, 1));
        request.setEmail("duplicate@example.com");
        request.setActive(true);

        restTemplate.postForEntity("/users", request, UserResponse.class);

        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity("/users", request, ErrorResponse.class);

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
                restTemplate.postForEntity("/users", request, ErrorResponse.class);

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

        UserResponse created =
                restTemplate.postForEntity("/users", request, UserResponse.class).getBody();

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();

        ResponseEntity<UserResponse> response =
                restTemplate.getForEntity("/users/" + created.getId(), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getEmail()).isEqualTo("john.get@example.com");
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {

        ResponseEntity<ErrorResponse> response =
                restTemplate.getForEntity("/users/999999", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
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
                restTemplate.postForEntity("/users", createRequest, UserResponse.class).getBody();

        assertThat(created).isNotNull();

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("New");
        updateRequest.setSurname("Updated");
        updateRequest.setBirthDate(LocalDate.of(1991, 2, 2));
        updateRequest.setEmail("new@example.com");
        updateRequest.setActive(true);

        ResponseEntity<UserResponse> response =
                restTemplate.exchange(
                        "/users/" + created.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(updateRequest),
                        UserResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("New");
        assertThat(response.getBody().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldReturnConflictWhenUpdatingToExistingEmail() {

        UserCreateRequest request1 = new UserCreateRequest();
        request1.setName("First");
        request1.setSurname("User");
        request1.setBirthDate(LocalDate.of(1990, 1, 1));
        request1.setEmail("first@example.com");
        request1.setActive(true);

        UserCreateRequest request2 = new UserCreateRequest();
        request2.setName("Second");
        request2.setSurname("User");
        request2.setBirthDate(LocalDate.of(1991, 1, 1));
        request2.setEmail("second@example.com");
        request2.setActive(true);

        restTemplate.postForEntity("/users", request1, UserResponse.class);
        UserResponse second =
                restTemplate.postForEntity("/users", request2, UserResponse.class).getBody();

        assertThat(second).isNotNull();

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Second");
        updateRequest.setSurname("User");
        updateRequest.setBirthDate(LocalDate.of(1991, 1, 1));
        updateRequest.setEmail("first@example.com");
        updateRequest.setActive(true);

        ResponseEntity<ErrorResponse> response =
                restTemplate.exchange(
                        "/users/" + second.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(updateRequest),
                        ErrorResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    void shouldUpdateUserActivitySuccessfully() {

        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setName("Active");
        createRequest.setSurname("User");
        createRequest.setBirthDate(LocalDate.of(1990, 1, 1));
        createRequest.setEmail("active@example.com");
        createRequest.setActive(true);

        UserResponse created =
                restTemplate.postForEntity("/users", createRequest, UserResponse.class).getBody();

        assertThat(created).isNotNull();

        ActiveStatusRequest statusRequest = new ActiveStatusRequest();
        statusRequest.setActive(false);

        ResponseEntity<Void> response =
                restTemplate.exchange(
                        "/users/" + created.getId() + "/active",
                        HttpMethod.PATCH,
                        new HttpEntity<>(statusRequest),
                        Void.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.findById(created.getId())).isPresent();
        assertThat(userRepository.findById(created.getId()).get().isActive()).isFalse();
    }

    @Test
    void shouldReturnFilteredUsersByFirstName() {

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
                restTemplate.getForEntity("/users?firstName=Alex&page=0&size=10", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("alex@example.com");
        assertThat(response.getBody()).doesNotContain("maria@example.com");
    }

    @Test
    void shouldReturnFilteredUsersBySurname() {

        UserCreateRequest request1 = new UserCreateRequest();
        request1.setName("Alex");
        request1.setSurname("Smith");
        request1.setBirthDate(LocalDate.of(1990, 1, 1));
        request1.setEmail("alex.smith@example.com");
        request1.setActive(true);

        UserCreateRequest request2 = new UserCreateRequest();
        request2.setName("Bob");
        request2.setSurname("Johnson");
        request2.setBirthDate(LocalDate.of(1992, 2, 2));
        request2.setEmail("bob.johnson@example.com");
        request2.setActive(true);

        restTemplate.postForEntity("/users", request1, UserResponse.class);
        restTemplate.postForEntity("/users", request2, UserResponse.class);

        ResponseEntity<String> response =
                restTemplate.getForEntity("/users?surname=Smith&page=0&size=10", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("alex.smith@example.com");
        assertThat(response.getBody()).doesNotContain("bob.johnson@example.com");
    }

    @Test
    void shouldReturnAllUsersWhenNoFilterApplied() {

        UserCreateRequest request1 = new UserCreateRequest();
        request1.setName("User1");
        request1.setSurname("Surname1");
        request1.setBirthDate(LocalDate.of(1990, 1, 1));
        request1.setEmail("user1@example.com");
        request1.setActive(true);

        UserCreateRequest request2 = new UserCreateRequest();
        request2.setName("User2");
        request2.setSurname("Surname2");
        request2.setBirthDate(LocalDate.of(1991, 1, 1));
        request2.setEmail("user2@example.com");
        request2.setActive(true);

        restTemplate.postForEntity("/users", request1, UserResponse.class);
        restTemplate.postForEntity("/users", request2, UserResponse.class);

        ResponseEntity<String> response =
                restTemplate.getForEntity("/users?page=0&size=10", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("user1@example.com");
        assertThat(response.getBody()).contains("user2@example.com");
    }
}