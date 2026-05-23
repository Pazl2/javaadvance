package com.javaadvance.mapper;

import com.javaadvance.dto.UserCreateRequest;
import com.javaadvance.dto.UserResponse;
import com.javaadvance.dto.UserUpdateRequest;
import com.javaadvance.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "active", constant = "true")
    User toEntity(UserCreateRequest dto);

    UserResponse toDto(User entity);

    void updateFromDto(UserUpdateRequest dto, @MappingTarget User entity);

}