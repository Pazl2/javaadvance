package com.javaadvance.service;

import com.javaadvance.dto.PaymentCardCreateRequest;
import com.javaadvance.dto.PaymentCardResponse;
import com.javaadvance.dto.PaymentCardUpdateRequest;
import com.javaadvance.entity.PaymentCard;
import com.javaadvance.entity.User;
import com.javaadvance.exception.ResourceNotFoundException;
import com.javaadvance.exception.TooManyCardsException;
import com.javaadvance.mapper.PaymentCardMapper;
import com.javaadvance.repository.PaymentCardRepository;
import com.javaadvance.repository.UserRepository;
import com.javaadvance.specification.PaymentCardSpecification;
import com.javaadvance.specification.UserSpecification;
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
        userRepository.save(currentUser);
        return paymentCardMapper.toDto(card);
    }

    public Page<PaymentCardResponse> getPaymentCardsWithPaginationAndFilter(String holder,
                                                             int page, int size){
        Specification<PaymentCard> spec = null;

        if (holder != null && !holder.isBlank()) {
            spec = PaymentCardSpecification.hasHolder(holder);
        }

        return paymentCardRepository.findAll(spec, PageRequest.of(page, size)).map(paymentCardMapper::toDto);
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

}
