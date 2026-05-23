package com.javaadvance.mapper;

import com.javaadvance.dto.PaymentCardCreateRequest;
import com.javaadvance.dto.PaymentCardResponse;
import com.javaadvance.dto.PaymentCardUpdateRequest;
import com.javaadvance.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(target = "active", constant = "true")
    PaymentCard toEntity(PaymentCardCreateRequest dto);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardResponse toDto(PaymentCard entity);

    void updateFromDto(PaymentCardUpdateRequest dto, @MappingTarget PaymentCard entity);

}