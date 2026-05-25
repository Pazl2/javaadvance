package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.UserCreateRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.dto.UserUpdateRequest;
import com.innowise.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {PaymentCardMapper.class})
public interface UserMapper {

    @Mapping(target = "paymentCards", ignore = true)
    User toEntity(UserCreateRequest dto);

    UserResponse toDto(User entity);

    @Mapping(target = "paymentCards", ignore = true)
    void updateFromDto(UserUpdateRequest dto, @MappingTarget User entity);
}