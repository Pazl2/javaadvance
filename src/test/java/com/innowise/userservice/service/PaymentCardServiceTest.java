package com.innowise.userservice.service;

import com.innowise.userservice.dto.PaymentCardCreateRequest;
import com.innowise.userservice.dto.PaymentCardResponse;
import com.innowise.userservice.dto.PaymentCardUpdateRequest;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.exception.TooManyCardsException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardService paymentCardService;

    @Test
    void createCard_ShouldReturnPaymentCardResponse_WhenUserExistsAndCardLimitNotReached() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        PaymentCardCreateRequest request = new PaymentCardCreateRequest();
        request.setNumber("1234567890123456");
        request.setHolder("Ivan Ivanov");
        request.setExpirationDate(LocalDate.of(2027, 1, 1));
        request.setActive(true);

        PaymentCard card = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();
        response.setId(1L);

        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.addCard(card);

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(0L).when(paymentCardRepository).countByUserId(userId);
        doReturn(card).when(paymentCardMapper).toEntity(request);
        doReturn(savedUser).when(userRepository).save(user);
        doReturn(response).when(paymentCardMapper).toDto(any(PaymentCard.class));

        PaymentCardResponse result = paymentCardService.createCard(request, userId);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(paymentCardRepository, times(1)).countByUserId(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createCard_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        Long userId = 99L;
        PaymentCardCreateRequest request = new PaymentCardCreateRequest();
        doReturn(Optional.empty()).when(userRepository).findById(userId);

        assertThrows(ResourceNotFoundException.class,
                () -> paymentCardService.createCard(request, userId));
    }

    @Test
    void createCard_ShouldThrowTooManyCardsException_WhenLimitReached() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        doReturn(Optional.of(user)).when(userRepository).findById(userId);
        doReturn(5L).when(paymentCardRepository).countByUserId(userId);

        PaymentCardCreateRequest request = new PaymentCardCreateRequest();
        assertThrows(TooManyCardsException.class,
                () -> paymentCardService.createCard(request, userId));
    }

    @Test
    void getCardById_ShouldReturnPaymentCardResponse_WhenCardExists() {
        Long cardId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(cardId);
        PaymentCardResponse response = new PaymentCardResponse();
        response.setId(cardId);

        doReturn(Optional.of(card)).when(paymentCardRepository).findById(cardId);
        doReturn(response).when(paymentCardMapper).toDto(card);

        PaymentCardResponse result = paymentCardService.getCardById(cardId);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
    }

    @Test
    void getCardById_ShouldThrowResourceNotFoundException_WhenCardNotFound() {
        Long cardId = 99L;
        doReturn(Optional.empty()).when(paymentCardRepository).findById(cardId);

        assertThrows(ResourceNotFoundException.class,
                () -> paymentCardService.getCardById(cardId));
    }

    @Test
    void getCardsByUserId_ShouldReturnList_WhenUserExists() {
        Long userId = 1L;
        PaymentCard card = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();

        doReturn(true).when(userRepository).existsById(userId);
        doReturn(List.of(card)).when(paymentCardRepository).findByUserId(userId);
        doReturn(response).when(paymentCardMapper).toDto(card);

        List<PaymentCardResponse> result = paymentCardService.getCardsByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getCardsByUserId_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        Long userId = 99L;
        doReturn(false).when(userRepository).existsById(userId);

        assertThrows(ResourceNotFoundException.class,
                () -> paymentCardService.getCardsByUserId(userId));
    }

    @Test
    void getPaymentCardsWithPaginationAndFilter_ShouldReturnPage_WhenNoFilters() {
        PaymentCard card = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();
        Page<PaymentCard> cardPage = new PageImpl<>(List.of(card));

        doReturn(cardPage).when(paymentCardRepository).findAll(any(Specification.class), any(Pageable.class));
        doReturn(response).when(paymentCardMapper).toDto(card);

        Page<PaymentCardResponse> result =
                paymentCardService.getPaymentCardsWithPaginationAndFilter(null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getPaymentCardsWithPaginationAndFilter_ShouldReturnPage_WhenFiltersProvided() {
        PaymentCard card = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();
        Page<PaymentCard> cardPage = new PageImpl<>(List.of(card));

        doReturn(cardPage).when(paymentCardRepository).findAll(any(Specification.class), any(Pageable.class));
        doReturn(response).when(paymentCardMapper).toDto(card);

        Page<PaymentCardResponse> result =
                paymentCardService.getPaymentCardsWithPaginationAndFilter("Ivan", "Ivanov", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updatePaymentCard_ShouldReturnUpdatedResponse_WhenCardExists() {
        Long cardId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(cardId);
        User user = new User();
        user.setId(2L);
        card.setUser(user);

        PaymentCardUpdateRequest request = new PaymentCardUpdateRequest();
        request.setNumber("9999999999999999");
        request.setHolder("Updated Holder");
        request.setExpirationDate(LocalDate.of(2028, 6, 1));
        request.setActive(false);

        PaymentCardResponse response = new PaymentCardResponse();
        response.setId(cardId);
        response.setUserId(2L);

        doReturn(Optional.of(card)).when(paymentCardRepository).findById(cardId);
        doReturn(response).when(paymentCardMapper).toDto(card);

        PaymentCardResponse result = paymentCardService.updatePaymentCard(cardId, request);

        assertNotNull(result);
        verify(paymentCardRepository, times(1)).updatePaymentCard(
                any(), any(), any(), any(), any());
    }

    @Test
    void updatePaymentCard_ShouldThrowResourceNotFoundException_WhenCardNotFound() {
        Long cardId = 99L;
        PaymentCardUpdateRequest request = new PaymentCardUpdateRequest();
        doReturn(Optional.empty()).when(paymentCardRepository).findById(cardId);

        assertThrows(ResourceNotFoundException.class,
                () -> paymentCardService.updatePaymentCard(cardId, request));
    }

    @Test
    void updateActivity_ShouldReturnUpdatedResponse_WhenCardExists() {
        Long cardId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(cardId);
        User user = new User();
        user.setId(2L);
        card.setUser(user);
        PaymentCardResponse response = new PaymentCardResponse();
        response.setId(cardId);
        response.setUserId(2L);

        doReturn(Optional.of(card)).when(paymentCardRepository).findById(cardId);
        doReturn(response).when(paymentCardMapper).toDto(card);

        PaymentCardResponse result = paymentCardService.updateActivity(cardId, false);

        assertNotNull(result);
        verify(paymentCardRepository, times(1)).updateActive(cardId, false);
    }

    @Test
    void updateActivity_ShouldThrowResourceNotFoundException_WhenCardNotFound() {
        Long cardId = 99L;
        doReturn(Optional.empty()).when(paymentCardRepository).findById(cardId);

        assertThrows(ResourceNotFoundException.class,
                () -> paymentCardService.updateActivity(cardId, false));
    }

    @Test
    void deleteCard_ShouldDeleteCard_WhenCardExists() {
        Long cardId = 1L;
        PaymentCard card = new PaymentCard();
        card.setId(cardId);
        User user = new User();
        user.setId(2L);
        card.setUser(user);

        doReturn(Optional.of(card)).when(paymentCardRepository).findById(cardId);

        paymentCardService.deleteCard(cardId);

        verify(paymentCardRepository, times(1)).deleteById(cardId);
    }

    @Test
    void deleteCard_ShouldThrowResourceNotFoundException_WhenCardNotFound() {
        Long cardId = 99L;
        doReturn(Optional.empty()).when(paymentCardRepository).findById(cardId);

        assertThrows(ResourceNotFoundException.class,
                () -> paymentCardService.deleteCard(cardId));
    }
}