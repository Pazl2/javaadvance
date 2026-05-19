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

    public PaymentCard toEntity(PaymentCardCreateRequest dto);
    @Mapping(source = "user.id", target = "userId")
    public PaymentCardResponse toDto(PaymentCard entity);
    public void updateFromDto(PaymentCardUpdateRequest dto, @MappingTarget PaymentCard entity);


}
