package com.javaadvance.controller;

import com.javaadvance.dto.ActiveStatusRequest;
import com.javaadvance.dto.UserCreateRequest;
import com.javaadvance.dto.UserResponse;
import com.javaadvance.dto.UserUpdateRequest;
import com.javaadvance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserCreateRequest dto){
        UserResponse response = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id){
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody @Valid UserUpdateRequest updateRequest){
        UserResponse response = userService.updateUser(id, updateRequest);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> updateActive(@PathVariable Long id,
                                             @RequestBody @Valid ActiveStatusRequest active){
        userService.updateUserActivity(id, active.isActive());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok().body(userService.getUsersWithPaginationAndFilter(firstName,surname,page,size));
    }

}
