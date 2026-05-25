package com.innowise.userservice.integration.paymentcard;

import com.innowise.userservice.dto.ActiveStatusRequest;
import com.innowise.userservice.dto.ErrorResponse;
import com.innowise.userservice.dto.PaymentCardCreateRequest;
import com.innowise.userservice.dto.PaymentCardResponse;
import com.innowise.userservice.dto.PaymentCardUpdateRequest;
import com.innowise.userservice.dto.UserCreateRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.integration.config.BaseIntegrationTest;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCardControllerIntegrationTest extends BaseIntegrationTest {

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
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Test");
        request.setSurname("User");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setEmail(email);
        request.setActive(true);
        return restTemplate.postForEntity("/users", request, UserResponse.class).getBody();
    }

    private PaymentCardCreateRequest buildCard(String number) {
        PaymentCardCreateRequest card = new PaymentCardCreateRequest();
        card.setNumber(number);
        card.setHolder("TEST USER");
        card.setExpirationDate(LocalDate.of(2029, 12, 31));
        card.setActive(true);
        return card;
    }

    @Test
    void shouldCreateCardSuccessfully() {

        UserResponse user = createUser("cardowner@example.com");
        assertThat(user).isNotNull();

        ResponseEntity<PaymentCardResponse> response =
                restTemplate.postForEntity(
                        "/users/" + user.getId() + "/cards",
                        buildCard("4111111111111111"),
                        PaymentCardResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getNumber()).isEqualTo("4111111111111111");
        assertThat(response.getBody().getUserId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnNotFoundWhenCreatingCardForNonExistentUser() {

        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity(
                        "/users/999999/cards",
                        buildCard("4111111111111111"),
                        ErrorResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void shouldReturnErrorWhenCardLimitExceeded() {

        UserResponse user = createUser("limitcards@example.com");
        assertThat(user).isNotNull();

        for (int i = 1; i <= 5; i++) {
            restTemplate.postForEntity(
                    "/users/" + user.getId() + "/cards",
                    buildCard("411111111111111" + i),
                    PaymentCardResponse.class
            );
        }

        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity(
                        "/users/" + user.getId() + "/cards",
                        buildCard("5555555555555555"),
                        ErrorResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
    }

    @Test
    void shouldReturnCardsByUserId() {

        UserResponse user = createUser("cardsuser@example.com");
        assertThat(user).isNotNull();

        restTemplate.postForEntity(
                "/users/" + user.getId() + "/cards",
                buildCard("4444444444444444"),
                PaymentCardResponse.class
        );

        ResponseEntity<String> response =
                restTemplate.getForEntity("/users/" + user.getId() + "/cards", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("4444444444444444");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoCards() {

        UserResponse user = createUser("nocards@example.com");
        assertThat(user).isNotNull();

        ResponseEntity<String> response =
                restTemplate.getForEntity("/users/" + user.getId() + "/cards", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("[]");
    }

    @Test
    void shouldGetCardByIdSuccessfully() {

        UserResponse user = createUser("getcard@example.com");
        assertThat(user).isNotNull();

        PaymentCardResponse created =
                restTemplate.postForEntity(
                        "/users/" + user.getId() + "/cards",
                        buildCard("4111111111111111"),
                        PaymentCardResponse.class
                ).getBody();

        assertThat(created).isNotNull();

        ResponseEntity<PaymentCardResponse> response =
                restTemplate.getForEntity("/cards/" + created.getId(), PaymentCardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getNumber()).isEqualTo("4111111111111111");
    }

    @Test
    void shouldReturnNotFoundWhenCardDoesNotExist() {

        ResponseEntity<ErrorResponse> response =
                restTemplate.getForEntity("/cards/999999", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void shouldUpdateCardSuccessfully() {

        UserResponse user = createUser("updatecard@example.com");
        assertThat(user).isNotNull();

        PaymentCardResponse created =
                restTemplate.postForEntity(
                        "/users/" + user.getId() + "/cards",
                        buildCard("4111111111111111"),
                        PaymentCardResponse.class
                ).getBody();

        assertThat(created).isNotNull();

        PaymentCardUpdateRequest updateRequest = new PaymentCardUpdateRequest();
        updateRequest.setNumber("5500005555555559");
        updateRequest.setHolder("UPDATED HOLDER");
        updateRequest.setExpirationDate(LocalDate.of(2030, 6, 30));
        updateRequest.setActive(true);

        ResponseEntity<PaymentCardResponse> response =
                restTemplate.exchange(
                        "/cards/" + created.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(updateRequest),
                        PaymentCardResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNumber()).isEqualTo("5500005555555559");
        assertThat(response.getBody().getHolder()).isEqualTo("UPDATED HOLDER");
    }

    @Test
    void shouldUpdateCardActivitySuccessfully() {

        UserResponse user = createUser("activecard@example.com");
        assertThat(user).isNotNull();

        PaymentCardResponse created =
                restTemplate.postForEntity(
                        "/users/" + user.getId() + "/cards",
                        buildCard("4111111111111111"),
                        PaymentCardResponse.class
                ).getBody();

        assertThat(created).isNotNull();

        ActiveStatusRequest statusRequest = new ActiveStatusRequest();
        statusRequest.setActive(false);

        ResponseEntity<PaymentCardResponse> response =
                restTemplate.exchange(
                        "/cards/" + created.getId() + "/active",
                        HttpMethod.PATCH,
                        new HttpEntity<>(statusRequest),
                        PaymentCardResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getActive()).isFalse();
    }

    @Test
    void shouldDeleteCardSuccessfully() {

        UserResponse user = createUser("deletecard@example.com");
        assertThat(user).isNotNull();

        PaymentCardResponse created =
                restTemplate.postForEntity(
                        "/users/" + user.getId() + "/cards",
                        buildCard("4111111111111111"),
                        PaymentCardResponse.class
                ).getBody();

        assertThat(created).isNotNull();

        ResponseEntity<Void> response =
                restTemplate.exchange(
                        "/cards/" + created.getId(),
                        HttpMethod.DELETE,
                        null,
                        Void.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(paymentCardRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentCard() {

        ResponseEntity<ErrorResponse> response =
                restTemplate.exchange(
                        "/cards/999999",
                        HttpMethod.DELETE,
                        null,
                        ErrorResponse.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void shouldReturnFilteredCardsByUserFirstName() {

        UserCreateRequest userRequest1 = new UserCreateRequest();
        userRequest1.setName("Ivan");
        userRequest1.setSurname("Ivanov");
        userRequest1.setBirthDate(LocalDate.of(1990, 1, 1));
        userRequest1.setEmail("ivan.filter@example.com");
        userRequest1.setActive(true);

        UserCreateRequest userRequest2 = new UserCreateRequest();
        userRequest2.setName("Maria");
        userRequest2.setSurname("Petrova");
        userRequest2.setBirthDate(LocalDate.of(1992, 2, 2));
        userRequest2.setEmail("maria.filter@example.com");
        userRequest2.setActive(true);

        UserResponse user1 = restTemplate.postForEntity("/users", userRequest1, UserResponse.class).getBody();
        UserResponse user2 = restTemplate.postForEntity("/users", userRequest2, UserResponse.class).getBody();

        assertThat(user1).isNotNull();
        assertThat(user2).isNotNull();

        restTemplate.postForEntity("/users/" + user1.getId() + "/cards", buildCard("4111111111111111"), PaymentCardResponse.class);
        restTemplate.postForEntity("/users/" + user2.getId() + "/cards", buildCard("5500005555555559"), PaymentCardResponse.class);

        ResponseEntity<String> response =
                restTemplate.getForEntity("/cards?firstName=Ivan&page=0&size=10", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("4111111111111111");
        assertThat(response.getBody()).doesNotContain("5500005555555559");
    }

    @Test
    void shouldReturnFilteredCardsByUserSurname() {

        UserCreateRequest userRequest1 = new UserCreateRequest();
        userRequest1.setName("Ivan");
        userRequest1.setSurname("Ivanov");
        userRequest1.setBirthDate(LocalDate.of(1990, 1, 1));
        userRequest1.setEmail("ivan.surname@example.com");
        userRequest1.setActive(true);

        UserCreateRequest userRequest2 = new UserCreateRequest();
        userRequest2.setName("Maria");
        userRequest2.setSurname("Petrova");
        userRequest2.setBirthDate(LocalDate.of(1992, 2, 2));
        userRequest2.setEmail("maria.surname@example.com");
        userRequest2.setActive(true);

        UserResponse user1 = restTemplate.postForEntity("/users", userRequest1, UserResponse.class).getBody();
        UserResponse user2 = restTemplate.postForEntity("/users", userRequest2, UserResponse.class).getBody();

        assertThat(user1).isNotNull();
        assertThat(user2).isNotNull();

        restTemplate.postForEntity("/users/" + user1.getId() + "/cards", buildCard("4111111111111111"), PaymentCardResponse.class);
        restTemplate.postForEntity("/users/" + user2.getId() + "/cards", buildCard("5500005555555559"), PaymentCardResponse.class);

        ResponseEntity<String> response =
                restTemplate.getForEntity("/cards?surname=Ivanov&page=0&size=10", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("4111111111111111");
        assertThat(response.getBody()).doesNotContain("5500005555555559");
    }
}