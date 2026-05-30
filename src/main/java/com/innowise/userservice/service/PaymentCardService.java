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
import com.innowise.userservice.specification.PaymentCardSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    public PaymentCardService(PaymentCardRepository paymentCardRepository,
                               UserRepository userRepository,
                              PaymentCardMapper paymentCardMapper){
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
        this.paymentCardMapper = paymentCardMapper;
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "userCards", key = "#userId")
    })

    @Transactional
    public PaymentCardResponse createCard(PaymentCardCreateRequest dto, Long userId){
        User currentUser = userRepository.findById(userId).orElseThrow(()->
                new ResourceNotFoundException("No such user with " + userId + " id"));

        if(paymentCardRepository.countByUserId(userId) >= 5){
            throw new TooManyCardsException("User can have only 5 cards");
        }

        PaymentCard card = paymentCardMapper.toEntity(dto);
        currentUser.addCard(card);

        User savedUser = userRepository.save(currentUser);
        PaymentCard savedCard = savedUser.getPaymentCards().get(savedUser.getPaymentCards().size() - 1);

        return paymentCardMapper.toDto(savedCard);
    }

    public Page<PaymentCardResponse> getPaymentCardsWithPaginationAndFilter(
            String firstName, String surname, int page, int size) {
        Specification<PaymentCard> spec = null;
        if (firstName != null && !firstName.isBlank()) {
            spec = PaymentCardSpecification.hasFirstName(firstName);
        }
        if (surname != null && !surname.isBlank()) {
            if (spec == null) {
                spec = PaymentCardSpecification.hasSurname(surname);
            } else {
                spec = spec.and(PaymentCardSpecification.hasSurname(surname));
            }
        }
        return paymentCardRepository.findAll(spec, PageRequest.of(page, size))
                .map(paymentCardMapper::toDto);
    }


    @Cacheable(value = "cards", key = "#cardId")
    public PaymentCardResponse getCardById(Long cardId){
        return paymentCardMapper.toDto(getCardEntityById(cardId));
    }

    private PaymentCard getCardEntityById(Long cardId){
        return paymentCardRepository.findById(cardId).orElseThrow(() ->
                new ResourceNotFoundException("No such card with " + cardId + " id"));
    }

    @Cacheable(value = "userCards", key = "#userId")
    public List<PaymentCardResponse> getCardsByUserId(Long userId){
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return paymentCardRepository.findByUserId(userId)
                .stream()
                .map(paymentCardMapper::toDto)
                .toList();
    }

    @Caching( evict = {
            @CacheEvict(value = "cards", key = "#id"),
            @CacheEvict(value = "users", key = "#result.userId"),
            @CacheEvict(value = "userCards", key = "#result.userId")
    })
    @Transactional
    public PaymentCardResponse updatePaymentCard(Long id, PaymentCardUpdateRequest dto){
        PaymentCard card = getCardEntityById(id);

        paymentCardMapper.updateFromDto(dto, card);

        paymentCardRepository.updatePaymentCard(card.getId(), card.getNumber(),
                card.getHolder(), card.getExpirationDate(), card.isActive());

        return paymentCardMapper.toDto(card);
    }


    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#id"),
            @CacheEvict(value = "users", key = "#result.userId"),
            @CacheEvict(value = "userCards", key = "#result.userId")
    })

    @Transactional
    public PaymentCardResponse updateActivity(Long id, boolean active){
        paymentCardRepository.updateActive(id,active);
        PaymentCard card = getCardEntityById(id);
        return paymentCardMapper.toDto(card);
    }

    @Transactional
    public void deleteCard(Long id) {
        PaymentCard card = getCardEntityById(id);
        Long userId = card.getUser().getId();
        paymentCardRepository.deleteById(id);
        evictCardCaches(id, userId);
    }

    @Caching(evict = {
            @CacheEvict(value = "cards", key = "#cardId"),
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "userCards", key = "#userId")
    })
    public void evictCardCaches(Long cardId, Long userId) {
        //evictCardCaches
    }

}
