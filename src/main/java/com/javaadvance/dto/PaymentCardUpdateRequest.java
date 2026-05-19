package com.javaadvance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentCardUpdateRequest {
    @NotNull
    private Long id;
    @NotBlank
    private String number;
    @NotBlank
    private String holder;
    @NotNull
    private LocalDate expirationDate;
    @NotNull
    private Boolean active;
}
