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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

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
        Specification<PaymentCard> spec = Specification.where((Specification<PaymentCard>) null);
        spec = spec.and(PaymentCardSpecification.hasHolder(holder));
        return paymentCardRepository.findAll(spec, PageRequest.of(page, size)).map(paymentCardMapper::toDto);
    }


    public PaymentCardResponse getCardById(Long cardId){
        return paymentCardMapper.toDto(getCardEntityById(cardId));
    }

    private PaymentCard getCardEntityById(Long cardId){
        return paymentCardRepository.findById(cardId).orElseThrow(() ->
                new ResourceNotFoundException("No such card with " + cardId + " id"));
    }

    public List<PaymentCardResponse> getCardsByUserId(Long userId){
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return paymentCardRepository.findByUserId(userId)
                .stream()
                .map(paymentCardMapper::toDto)
                .toList();
    }

    @Transactional
    public void updatePaymentCard(PaymentCardUpdateRequest dto){
        PaymentCard card = getCardEntityById((dto.getId()));

        paymentCardMapper.updateFromDto(dto, card);

        paymentCardRepository.updatePaymentCard(card.getId(), card.getNumber(),
                card.getHolder(), card.getExpirationDate(), card.isActive());
    }

    @Transactional
    public void updateActivity(Long id, boolean active){
        paymentCardRepository.updateActive(id,active);
    }

}
