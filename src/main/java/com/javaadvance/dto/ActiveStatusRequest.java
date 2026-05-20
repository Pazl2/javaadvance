package com.javaadvance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActiveStatusRequest {
    @NotNull
    private boolean active;
}
