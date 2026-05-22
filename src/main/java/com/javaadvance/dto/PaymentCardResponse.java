package com.javaadvance.dto;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentCardResponse implements Serializable {
    private Long id;
    private Long userId;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Boolean active;
}
