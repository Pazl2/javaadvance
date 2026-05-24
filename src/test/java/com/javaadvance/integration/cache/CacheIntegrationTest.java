package com.javaadvance.integration.cache;

import com.javaadvance.dto.ActiveStatusRequest;
import com.javaadvance.dto.PaymentCardCreateRequest;
import com.javaadvance.dto.PaymentCardResponse;
import com.javaadvance.dto.PaymentCardUpdateRequest;
import com.javaadvance.dto.UserCreateRequest;
import com.javaadvance.dto.UserResponse;
import com.javaadvance.dto.UserUpdateRequest;
import com.javaadvance.integration.config.BaseIntegrationTest;
import com.javaadvance.repository.PaymentCardRepository;
import com.javaadvance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CacheIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @BeforeEach
    void cleanUp() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
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

    private PaymentCardResponse createCard(Long userId, String number) {
        PaymentCardCreateRequest request = new PaymentCardCreateRequest();
        request.setNumber(number);
        request.setHolder("TEST USER");
        request.setExpirationDate(LocalDate.of(2029, 12, 31));
        request.setActive(true);
        return restTemplate.postForEntity(
                "/users/" + userId + "/cards", request, PaymentCardResponse.class
        ).getBody();
    }

    @Test
    void shouldCacheUserAfterFirstGet() {

        UserResponse user = createUser("cache.user@example.com");
        assertThat(user).isNotNull();

        Cache usersCache = cacheManager.getCache("users");
        assertThat(usersCache).isNotNull();
        assertThat(usersCache.get(user.getId())).isNull();

        restTemplate.getForEntity("/users/" + user.getId(), UserResponse.class);

        assertThat(usersCache.get(user.getId())).isNotNull();
    }

    @Test
    void shouldReturnSameDataFromCacheOnSecondGet() {

        UserResponse user = createUser("cache.double@example.com");
        assertThat(user).isNotNull();

        ResponseEntity<UserResponse> first =
                restTemplate.getForEntity("/users/" + user.getId(), UserResponse.class);

        ResponseEntity<UserResponse> second =
                restTemplate.getForEntity("/users/" + user.getId(), UserResponse.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody()).isNotNull();
        assertThat(second.getBody()).isNotNull();
        assertThat(first.getBody().getId()).isEqualTo(second.getBody().getId());
        assertThat(first.getBody().getEmail()).isEqualTo(second.getBody().getEmail());
    }

    @Test
    void shouldEvictUserCacheAfterUpdate() {

        UserResponse user = createUser("cache.evict@example.com");
        assertThat(user).isNotNull();

        restTemplate.getForEntity("/users/" + user.getId(), UserResponse.class);

        Cache usersCache = cacheManager.getCache("users");
        assertThat(usersCache).isNotNull();
        assertThat(usersCache.get(user.getId())).isNotNull();

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated");
        updateRequest.setSurname("User");
        updateRequest.setBirthDate(LocalDate.of(1990, 1, 1));
        updateRequest.setEmail("cache.evict.updated@example.com");
        updateRequest.setActive(true);

        restTemplate.exchange(
                "/users/" + user.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                UserResponse.class
        );

        assertThat(usersCache.get(user.getId())).isNull();
    }

    @Test
    void shouldEvictUserCacheAfterActivityUpdate() {

        UserResponse user = createUser("cache.activity@example.com");
        assertThat(user).isNotNull();

        restTemplate.getForEntity("/users/" + user.getId(), UserResponse.class);

        Cache usersCache = cacheManager.getCache("users");
        assertThat(usersCache).isNotNull();
        assertThat(usersCache.get(user.getId())).isNotNull();

        ActiveStatusRequest statusRequest = new ActiveStatusRequest();
        statusRequest.setActive(false);

        restTemplate.exchange(
                "/users/" + user.getId() + "/active",
                HttpMethod.PATCH,
                new HttpEntity<>(statusRequest),
                Void.class
        );

        assertThat(usersCache.get(user.getId())).isNull();
    }

    @Test
    void shouldCacheCardAfterFirstGet() {

        UserResponse user = createUser("cache.card@example.com");
        assertThat(user).isNotNull();

        PaymentCardResponse card = createCard(user.getId(), "4111111111111111");
        assertThat(card).isNotNull();

        Cache cardsCache = cacheManager.getCache("cards");
        assertThat(cardsCache).isNotNull();
        assertThat(cardsCache.get(card.getId())).isNull();

        restTemplate.getForEntity("/cards/" + card.getId(), PaymentCardResponse.class);

        assertThat(cardsCache.get(card.getId())).isNotNull();
    }

    @Test
    void shouldCacheUserCardsAfterFirstGet() {

        UserResponse user = createUser("cache.usercards@example.com");
        assertThat(user).isNotNull();

        createCard(user.getId(), "4111111111111111");

        Cache userCardsCache = cacheManager.getCache("userCards");
        assertThat(userCardsCache).isNotNull();
        assertThat(userCardsCache.get(user.getId())).isNull();

        restTemplate.getForEntity("/users/" + user.getId() + "/cards", String.class);

        assertThat(userCardsCache.get(user.getId())).isNotNull();
    }

    @Test
    void shouldEvictCardCachesAfterCardUpdate() {

        UserResponse user = createUser("cache.cardupdate@example.com");
        assertThat(user).isNotNull();

        PaymentCardResponse card = createCard(user.getId(), "4111111111111111");
        assertThat(card).isNotNull();

        restTemplate.getForEntity("/cards/" + card.getId(), PaymentCardResponse.class);
        restTemplate.getForEntity("/users/" + user.getId() + "/cards", String.class);

        Cache cardsCache = cacheManager.getCache("cards");
        Cache userCardsCache = cacheManager.getCache("userCards");
        assertThat(cardsCache).isNotNull();
        assertThat(userCardsCache).isNotNull();
        assertThat(cardsCache.get(card.getId())).isNotNull();
        assertThat(userCardsCache.get(user.getId())).isNotNull();

        PaymentCardUpdateRequest updateRequest = new PaymentCardUpdateRequest();
        updateRequest.setNumber("5500005555555559");
        updateRequest.setHolder("UPDATED");
        updateRequest.setExpirationDate(LocalDate.of(2030, 1, 1));
        updateRequest.setActive(true);

        restTemplate.exchange(
                "/cards/" + card.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                PaymentCardResponse.class
        );

        assertThat(cardsCache.get(card.getId())).isNull();
        assertThat(userCardsCache.get(user.getId())).isNull();
    }

    @Test
    void shouldEvictCardCachesAfterCardActivityUpdate() {

        UserResponse user = createUser("cache.cardactive@example.com");
        assertThat(user).isNotNull();

        PaymentCardResponse card = createCard(user.getId(), "4111111111111111");
        assertThat(card).isNotNull();

        restTemplate.getForEntity("/cards/" + card.getId(), PaymentCardResponse.class);
        restTemplate.getForEntity("/users/" + user.getId() + "/cards", String.class);

        Cache cardsCache = cacheManager.getCache("cards");
        Cache userCardsCache = cacheManager.getCache("userCards");
        assertThat(cardsCache).isNotNull();
        assertThat(userCardsCache).isNotNull();
        assertThat(cardsCache.get(card.getId())).isNotNull();
        assertThat(userCardsCache.get(user.getId())).isNotNull();

        ActiveStatusRequest statusRequest = new ActiveStatusRequest();
        statusRequest.setActive(false);

        restTemplate.exchange(
                "/cards/" + card.getId() + "/active",
                HttpMethod.PATCH,
                new HttpEntity<>(statusRequest),
                PaymentCardResponse.class
        );

        assertThat(cardsCache.get(card.getId())).isNull();
        assertThat(userCardsCache.get(user.getId())).isNull();
    }

    @Test
    void shouldEvictUserAndCardCachesAfterCardCreation() {

        UserResponse user = createUser("cache.cardcreate@example.com");
        assertThat(user).isNotNull();

        restTemplate.getForEntity("/users/" + user.getId(), UserResponse.class);
        restTemplate.getForEntity("/users/" + user.getId() + "/cards", String.class);

        Cache usersCache = cacheManager.getCache("users");
        Cache userCardsCache = cacheManager.getCache("userCards");
        assertThat(usersCache).isNotNull();
        assertThat(userCardsCache).isNotNull();
        assertThat(usersCache.get(user.getId())).isNotNull();
        assertThat(userCardsCache.get(user.getId())).isNotNull();

        createCard(user.getId(), "4111111111111111");

        assertThat(usersCache.get(user.getId())).isNull();
        assertThat(userCardsCache.get(user.getId())).isNull();
    }
}