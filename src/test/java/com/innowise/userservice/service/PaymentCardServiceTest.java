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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    PaymentCardRepository paymentCardRepository;

    @Mock
    PaymentCardMapper paymentCardMapper;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    PaymentCardService paymentCardService;

    @Test
    void createCard_ShouldReturnPaymentCardResponse_WhenSuccessful() {
        Long userId = 1L;
        PaymentCardCreateRequest request = new PaymentCardCreateRequest();
        request.setNumber("1234-5678");
        request.setHolder("Ivan Petrov");
        request.setExpirationDate(LocalDate.of(2028, 12, 31));
        request.setActive(true);

        User currentUser = new User();
        currentUser.setId(userId);
        currentUser.setPaymentCards(new ArrayList<>());

        PaymentCard cardBeforeSave = new PaymentCard();
        cardBeforeSave.setNumber("1234-5678");
        cardBeforeSave.setHolder("Ivan Petrov");

        PaymentCard savedCard = new PaymentCard();
        savedCard.setId(10L);
        savedCard.setNumber("1234-5678");
        savedCard.setHolder("Ivan Petrov");
        savedCard.setUser(currentUser);

        PaymentCardResponse responseDto = new PaymentCardResponse();
        responseDto.setId(10L);
        responseDto.setUserId(userId);
        responseDto.setNumber("1234-5678");
        responseDto.setHolder("Ivan Petrov");

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        doReturn(2).when(paymentCardRepository).countByUserId(userId);
        when(paymentCardMapper.toEntity(request)).thenReturn(cardBeforeSave);
        when(paymentCardMapper.toDto(cardBeforeSave)).thenReturn(responseDto);
        when(userRepository.save(currentUser)).thenReturn(currentUser);

        PaymentCardResponse result = paymentCardService.createCard(request, userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("1234-5678", result.getNumber());

        verify(paymentCardRepository).countByUserId(userId);
        verify(paymentCardMapper).toEntity(request);
        verify(userRepository).save(currentUser);
        verify(paymentCardMapper).toDto(cardBeforeSave);
    }

    @Test
    void createCard_ShouldThrowTooManyCardsException_WhenLimitExceeded() {
        Long userId = 1L;
        PaymentCardCreateRequest request = new PaymentCardCreateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        doReturn(5).when(paymentCardRepository).countByUserId(userId);

        TooManyCardsException exception = assertThrows(
                TooManyCardsException.class,
                () -> paymentCardService.createCard(request, userId)
        );

        assertTrue(exception.getMessage().contains("5"));
        verify(userRepository, never()).save(any());
        verify(paymentCardMapper, never()).toDto(any());
    }

    @Test
    void createCard_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        Long userId = 999L;
        PaymentCardCreateRequest request = new PaymentCardCreateRequest();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentCardService.createCard(request, userId)
        );

        verify(paymentCardRepository, never()).countByUserId(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getCardById_ShouldReturnPaymentCardResponse_WhenCardExists() {
        Long cardId = 10L;
        PaymentCard card = new PaymentCard();
        card.setId(cardId);
        card.setNumber("1111");

        PaymentCardResponse responseDto = new PaymentCardResponse();
        responseDto.setId(cardId);
        responseDto.setNumber("1111");

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardMapper.toDto(card)).thenReturn(responseDto);

        PaymentCardResponse result = paymentCardService.getCardById(cardId);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
        assertEquals("1111", result.getNumber());
        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardMapper).toDto(card);
    }

    @Test
    void getCardById_ShouldThrowResourceNotFoundException_WhenCardNotFound() {
        Long cardId = 999L;
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentCardService.getCardById(cardId)
        );
        verify(paymentCardMapper, never()).toDto(any());
    }

    @Test
    void getCardsByUserId_ShouldReturnListOfResponse_WhenUserExists() {
        Long userId = 1L;
        PaymentCard card1 = new PaymentCard();
        card1.setId(1L);
        PaymentCard card2 = new PaymentCard();
        card2.setId(2L);

        PaymentCardResponse dto1 = new PaymentCardResponse();
        dto1.setId(1L);
        PaymentCardResponse dto2 = new PaymentCardResponse();
        dto2.setId(2L);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentCardRepository.findByUserId(userId)).thenReturn(List.of(card1, card2));
        when(paymentCardMapper.toDto(card1)).thenReturn(dto1);
        when(paymentCardMapper.toDto(card2)).thenReturn(dto2);

        List<PaymentCardResponse> result = paymentCardService.getCardsByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(paymentCardRepository).findByUserId(userId);
        verify(paymentCardMapper, times(2)).toDto(any());
    }

    @Test
    void getCardsByUserId_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentCardService.getCardsByUserId(userId)
        );
        verify(paymentCardRepository, never()).findByUserId(anyLong());
    }


    @Test
    void updatePaymentCard_ShouldReturnUpdatedResponse_WhenCardExists() {
        Long cardId = 10L;
        PaymentCardUpdateRequest request = new PaymentCardUpdateRequest();
        request.setNumber("new-number");
        request.setHolder("New Holder");
        request.setExpirationDate(LocalDate.of(2029, 1, 1));
        request.setActive(false);

        PaymentCard existingCard = new PaymentCard();
        existingCard.setId(cardId);
        existingCard.setNumber("old-number");
        existingCard.setHolder("Old Holder");
        existingCard.setExpirationDate(LocalDate.of(2025, 1, 1));
        existingCard.setActive(true);

        PaymentCardResponse responseDto = new PaymentCardResponse();
        responseDto.setId(cardId);
        responseDto.setNumber("new-number");
        responseDto.setHolder("New Holder");
        responseDto.setExpirationDate(LocalDate.of(2029, 1, 1));
        responseDto.setActive(false);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(paymentCardMapper.toDto(existingCard)).thenReturn(responseDto);

        PaymentCardResponse result = paymentCardService.updatePaymentCard(cardId, request);

        assertNotNull(result);
        assertEquals("new-number", result.getNumber());
        assertEquals(false, result.getActive());

        verify(paymentCardMapper).updateFromDto(request, existingCard);
        verify(paymentCardRepository).updatePaymentCard(eq(cardId), any(), any(), any(), anyBoolean());
        verify(paymentCardMapper).toDto(existingCard);
    }

    @Test
    void updatePaymentCard_ShouldThrowResourceNotFoundException_WhenCardNotFound() {
        Long cardId = 999L;
        PaymentCardUpdateRequest request = new PaymentCardUpdateRequest();
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentCardService.updatePaymentCard(cardId, request)
        );
        verify(paymentCardRepository, never()).updatePaymentCard(anyLong(), any(), any(), any(), anyBoolean());
    }


    @Test
    void updateActivity_ShouldReturnUpdatedCardResponse_WhenSuccessful() {
        Long cardId = 10L;
        boolean active = false;

        PaymentCard cardAfterUpdate = new PaymentCard();
        cardAfterUpdate.setId(cardId);
        cardAfterUpdate.setActive(false);

        PaymentCardResponse responseDto = new PaymentCardResponse();
        responseDto.setId(cardId);
        responseDto.setActive(false);

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(cardAfterUpdate));
        when(paymentCardMapper.toDto(cardAfterUpdate)).thenReturn(responseDto);

        PaymentCardResponse result = paymentCardService.updateActivity(cardId, active);

        assertNotNull(result);
        assertEquals(false, result.getActive());

        verify(paymentCardRepository).updateActive(cardId, active);
        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardMapper).toDto(cardAfterUpdate);
    }


    @Test
    void getCardsWithPaginationAndFilter_ShouldReturnPageOfResponses_WhenFilterProvided() {
        String name = "Ivan";
        String surname = "Ivanov";
        int page = 0;
        int size = 10;

        PaymentCard card = new PaymentCard();
        card.setId(1L);
        card.setHolder("Ivan");

        PaymentCardResponse dto = new PaymentCardResponse();
        dto.setId(1L);
        dto.setHolder("Ivan");

        Page<PaymentCard> cardPage = new PageImpl<>(List.of(card), PageRequest.of(page, size), 1);

        when(paymentCardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(cardPage);
        when(paymentCardMapper.toDto(card)).thenReturn(dto);

        Page<PaymentCardResponse> result = paymentCardService.getPaymentCardsWithPaginationAndFilter(name, surname, page, size);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(paymentCardRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(paymentCardMapper).toDto(card);
    }


}
