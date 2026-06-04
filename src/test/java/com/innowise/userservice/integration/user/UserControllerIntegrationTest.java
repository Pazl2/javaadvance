package com.innowise.userservice.integration.user;

import com.innowise.userservice.dto.*;
import com.innowise.userservice.integration.config.BaseIntegrationTest;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
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

    private UserResponse createUser(String email) {
        authenticateAsAdmin(1L);
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Test");
        request.setSurname("User");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setEmail(email);
        request.setActive(true);
        return restTemplate.exchange(
                "/users", HttpMethod.POST,
                new HttpEntity<>(request), UserResponse.class
        ).getBody();
    }

    private PaymentCardResponse createCard(Long userId, String number) {
        authenticateAsUser(userId);
        PaymentCardCreateRequest request = new PaymentCardCreateRequest();
        request.setNumber(number);
        request.setHolder("TEST USER");
        request.setExpirationDate(LocalDate.of(2029, 12, 31));
        request.setActive(true);
        return restTemplate.exchange(
                "/users/" + userId + "/cards", HttpMethod.POST,
                new HttpEntity<>(request), PaymentCardResponse.class
        ).getBody();
    }

    @Test
    void shouldCreateUserSuccessfully() {
        authenticateAsAdmin(1L);
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setBirthDate(LocalDate.of(2000, 1, 1));
        request.setEmail("ivan.controller@example.com");
        request.setActive(true);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/users", HttpMethod.POST,
                new HttpEntity<>(request), UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("ivan.controller@example.com");
        assertThat(userRepository.existsByEmail("ivan.controller@example.com")).isTrue();
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() {
        authenticateAsAdmin(1L);
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setBirthDate(LocalDate.of(2000, 1, 1));
        request.setEmail("duplicate@example.com");
        request.setActive(true);

        restTemplate.exchange("/users", HttpMethod.POST, new HttpEntity<>(request), UserResponse.class);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users", HttpMethod.POST,
                new HttpEntity<>(request), ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    void shouldReturnBadRequestWhenEmailInvalid() {
        authenticateAsAdmin(1L);
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setBirthDate(LocalDate.of(2000, 1, 1));
        request.setEmail("invalid-email");
        request.setActive(true);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users", HttpMethod.POST,
                new HttpEntity<>(request), ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        UserResponse created = createUser("john.get@example.com");
        assertThat(created).isNotNull();
        authenticateAsUser(created.getId());

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/users/" + created.getId(), HttpMethod.GET,
                HttpEntity.EMPTY, UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getEmail()).isEqualTo("john.get@example.com");
    }

    @Test
    void shouldReturnUserWithCardsInResponse() {
        UserResponse user = createUser("user.with.cards@example.com");
        assertThat(user).isNotNull();
        createCard(user.getId(), "4111111111111111");
        createCard(user.getId(), "5500005555555559");
        authenticateAsUser(user.getId());

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/users/" + user.getId(), HttpMethod.GET,
                HttpEntity.EMPTY, UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentCards()).hasSize(2);
        assertThat(response.getBody().getPaymentCards())
                .extracting("number")
                .containsExactlyInAnyOrder("4111111111111111", "5500005555555559");
    }

    @Test
    void shouldReturnEmptyCardsListWhenUserHasNoCards() {
        UserResponse user = createUser("user.no.cards@example.com");
        assertThat(user).isNotNull();
        authenticateAsUser(user.getId());

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/users/" + user.getId(), HttpMethod.GET,
                HttpEntity.EMPTY, UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentCards()).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        authenticateAsAdmin(1L);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users/999999", HttpMethod.GET,
                HttpEntity.EMPTY, ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        UserResponse created = createUser("old@example.com");
        assertThat(created).isNotNull();
        authenticateAsUser(created.getId());

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("New");
        updateRequest.setSurname("Updated");
        updateRequest.setBirthDate(LocalDate.of(1991, 2, 2));
        updateRequest.setEmail("new@example.com");
        updateRequest.setActive(true);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/users/" + created.getId(), HttpMethod.PUT,
                new HttpEntity<>(updateRequest), UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("New");
        assertThat(response.getBody().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldReturnConflictWhenUpdatingToExistingEmail() {
        createUser("first@example.com");
        UserResponse second = createUser("second@example.com");
        assertThat(second).isNotNull();
        authenticateAsUser(second.getId());

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Second");
        updateRequest.setSurname("User");
        updateRequest.setBirthDate(LocalDate.of(1991, 1, 1));
        updateRequest.setEmail("first@example.com");
        updateRequest.setActive(true);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users/" + second.getId(), HttpMethod.PUT,
                new HttpEntity<>(updateRequest), ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    void shouldUpdateUserActivitySuccessfully() {
        UserResponse created = createUser("active@example.com");
        assertThat(created).isNotNull();
        authenticateAsAdmin(1L);

        ActiveStatusRequest statusRequest = new ActiveStatusRequest();
        statusRequest.setActive(false);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/users/" + created.getId() + "/active", HttpMethod.PATCH,
                new HttpEntity<>(statusRequest), UserResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getActive()).isFalse();
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        UserResponse created = createUser("delete@example.com");
        assertThat(created).isNotNull();
        authenticateAsAdmin(1L);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/users/" + created.getId(), HttpMethod.DELETE,
                HttpEntity.EMPTY, Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentUser() {
        authenticateAsAdmin(1L);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users/999999", HttpMethod.DELETE,
                HttpEntity.EMPTY, ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void shouldDeleteUserWithCardsSuccessfully() {
        UserResponse user = createUser("delete.with.cards@example.com");
        assertThat(user).isNotNull();
        createCard(user.getId(), "4111111111111111");
        createCard(user.getId(), "5500005555555559");
        authenticateAsAdmin(1L);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/users/" + user.getId(), HttpMethod.DELETE,
                HttpEntity.EMPTY, Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(userRepository.existsById(user.getId())).isFalse();
        assertThat(paymentCardRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    void shouldReturnFilteredUsersByFirstName() {
        authenticateAsAdmin(1L);
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

        restTemplate.exchange("/users", HttpMethod.POST, new HttpEntity<>(request1), UserResponse.class);
        restTemplate.exchange("/users", HttpMethod.POST, new HttpEntity<>(request2), UserResponse.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/users?firstName=Alex&page=0&size=10", HttpMethod.GET,
                HttpEntity.EMPTY, String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("alex@example.com");
        assertThat(response.getBody()).doesNotContain("maria@example.com");
    }

    @Test
    void shouldReturnFilteredUsersBySurname() {
        authenticateAsAdmin(1L);
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

        restTemplate.exchange("/users", HttpMethod.POST, new HttpEntity<>(request1), UserResponse.class);
        restTemplate.exchange("/users", HttpMethod.POST, new HttpEntity<>(request2), UserResponse.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/users?surname=Smith&page=0&size=10", HttpMethod.GET,
                HttpEntity.EMPTY, String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("alex.smith@example.com");
        assertThat(response.getBody()).doesNotContain("bob.johnson@example.com");
    }

    @Test
    void shouldReturnAllUsersWhenNoFilterApplied() {
        authenticateAsAdmin(1L);
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

        restTemplate.exchange("/users", HttpMethod.POST, new HttpEntity<>(request1), UserResponse.class);
        restTemplate.exchange("/users", HttpMethod.POST, new HttpEntity<>(request2), UserResponse.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/users?page=0&size=10", HttpMethod.GET,
                HttpEntity.EMPTY, String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("user1@example.com");
        assertThat(response.getBody()).contains("user2@example.com");
    }

    @Test
    void shouldReturn401WhenNoToken() {
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users", HttpMethod.GET,
                HttpEntity.EMPTY, ErrorResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn403WhenUserAccessesAdminEndpoint() {
        authenticateAsUser(1L);
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setBirthDate(LocalDate.of(2000, 1, 1));
        request.setEmail("ivan@example.com");
        request.setActive(true);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users", HttpMethod.POST,
                new HttpEntity<>(request), ErrorResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getUser_withoutToken_returns401() {
        // currentToken остаётся null (не задан)
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/users/1", ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getUser_asOtherUser_returns403() {
        authenticateAsUser(99L); // аутентифицированы как пользователь 99
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/users/1", ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createUser_asNonAdmin_returns403() {
        authenticateAsUser(1L);
        UserCreateRequest body = new UserCreateRequest();
        body.setName("Ivan");
        body.setSurname("Ivanov");
        body.setBirthDate(LocalDate.of(2000, 1, 1));
        body.setEmail("ivan@example.com");
        body.setActive(true);
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/users", body, ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}