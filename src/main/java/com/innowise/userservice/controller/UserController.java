package com.innowise.userservice.controller;

import com.innowise.userservice.dto.ActiveStatusRequest;
import com.innowise.userservice.dto.PaymentCardCreateRequest;
import com.innowise.userservice.dto.PaymentCardResponse;
import com.innowise.userservice.dto.UserCreateRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.dto.UserUpdateRequest;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PaymentCardService paymentCardService;

    public UserController(UserService userService,
                          PaymentCardService paymentCardService){
        this.userService = userService;
        this.paymentCardService = paymentCardService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserCreateRequest dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody @Valid UserUpdateRequest updateRequest){
        return ResponseEntity.ok(userService.updateUser(id, updateRequest));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<UserResponse> updateActive(@PathVariable Long id,
                                                     @RequestBody @Valid ActiveStatusRequest active){
        return ResponseEntity.ok(userService.updateUserActivity(id, active.isActive()));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(userService.getUsersWithPaginationAndFilter(firstName, surname, page, size));
    }

    @PostMapping("/{userId}/cards")
    public ResponseEntity<PaymentCardResponse> createCard(@RequestBody @Valid PaymentCardCreateRequest dto,
                                                          @PathVariable Long userId){
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.createCard(dto, userId));
    }

    @GetMapping("/{userId}/cards")
    public ResponseEntity<List<PaymentCardResponse>> getCardsByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(paymentCardService.getCardsByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}