package com.javaadvance.service;

import com.javaadvance.entity.PaymentCard;
import com.javaadvance.entity.User;
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

    public PaymentCardService(PaymentCardRepository paymentCardRepository,
                               UserRepository userRepository){
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createCard(PaymentCard card, Long userId){
        User currentUser = userRepository.findById(userId).orElseThrow(()->
                new NoSuchElementException("No such user with " + userId + " id"));
        if(paymentCardRepository.countByUserId(userId) >= 5){
            throw new IllegalStateException("User can have only 5 cards");
        }
        currentUser.addCard(card);
        userRepository.save(currentUser);
    }

    public Page<PaymentCard> getPaymentCardsWithPaginationAndFilter(String holder,
                                                             int page, int size){
        Specification<PaymentCard> spec = Specification.where((Specification<PaymentCard>) null);
        spec = spec.and(PaymentCardSpecification.hasHolder(holder));
        return paymentCardRepository.findAll(spec, PageRequest.of(page, size));
    }

    public PaymentCard getCardById(Long cardId){
        return paymentCardRepository.findById(cardId).orElseThrow(() ->
                new NoSuchElementException("No such card with " + cardId + " id"));
    }

    public List<PaymentCard> getCardsByUserId(Long userId){
        return paymentCardRepository.findByUserId(userId);
    }

    @Transactional
    public void updatePaymentCard(Long id, String number,
                                  String holder, LocalDate expirationDate,
                                  boolean active){
        paymentCardRepository.updatePaymentCard(id, number, holder, expirationDate, active);
    }

    @Transactional
    public void updateActivity(Long id, boolean active){
        paymentCardRepository.updateActive(id,active);
    }

}
