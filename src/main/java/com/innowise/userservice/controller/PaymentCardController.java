package com.innowise.userservice.controller;

import com.innowise.userservice.dto.ActiveStatusRequest;
import com.innowise.userservice.dto.PaymentCardResponse;
import com.innowise.userservice.dto.PaymentCardUpdateRequest;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    public PaymentCardController(PaymentCardService paymentCardService){
        this.paymentCardService = paymentCardService;
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardResponse>> getCards(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok().body(
                paymentCardService.getPaymentCardsWithPaginationAndFilter(
                        firstName, surname, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponse> getCardById(@PathVariable Long id){
        return ResponseEntity.ok().body(paymentCardService.getCardById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardResponse> updateCard(
            @PathVariable Long id,
            @RequestBody @Valid PaymentCardUpdateRequest request){
        PaymentCardResponse response = paymentCardService.updatePaymentCard(id, request);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<PaymentCardResponse> updateActive(
            @PathVariable Long id,
            @RequestBody @Valid ActiveStatusRequest active){
        return ResponseEntity.ok().body(
                paymentCardService.updateActivity(id, active.isActive()));
    }

}
