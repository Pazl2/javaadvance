package com.innowise.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public abstract class BaseUserRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @NotNull
    private LocalDate birthDate;
    @NotBlank
    @Email
    private String email;
    @NotNull
    private Boolean active;
}
