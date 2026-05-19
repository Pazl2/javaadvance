package com.javaadvance.mapper;

import com.javaadvance.dto.UserCreateRequest;
import com.javaadvance.dto.UserResponse;
import com.javaadvance.dto.UserUpdateRequest;
import com.javaadvance.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    public User toEntity(UserCreateRequest dto);
    public UserResponse toDto(User entity);
    public void updateFromDto(UserUpdateRequest dto, @MappingTarget User entity);

}
