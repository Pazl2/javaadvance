package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.PaymentCardCreateRequest;
import com.innowise.userservice.dto.PaymentCardResponse;
import com.innowise.userservice.dto.PaymentCardUpdateRequest;
import com.innowise.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    PaymentCard toEntity(PaymentCardCreateRequest dto);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardResponse toDto(PaymentCard entity);

    void updateFromDto(PaymentCardUpdateRequest dto, @MappingTarget PaymentCard entity);
}