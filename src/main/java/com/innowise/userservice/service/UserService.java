package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserCreateRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.dto.UserUpdateRequest;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.DuplicateEmailException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.specification.UserSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest dto){
        User user = userMapper.toEntity(dto);
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("User with email " + dto.getEmail() + " exists");
        }
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Cacheable(value = "users", key = "#userId")
    public UserResponse getUserById(Long userId) {
        User user = getUserEntityById(userId);
        return userMapper.toDto(user);
    }

    public UserResponse getUserByIdNoCache(Long userId) {
        User user = getUserEntityById(userId);
        return userMapper.toDto(user);
    }

    private User getUserEntityById(Long userId){
        return userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("No such User with " + userId + " id"));
    }

    public Page<UserResponse> getUsersWithPaginationAndFilter(
            String firstName,
            String surname,
            int page, int size){

        Specification<User> spec = null;

        if (firstName != null && !firstName.isBlank()) {
            spec = UserSpecification.hasFirstName(firstName);
        }
        if (surname != null && !surname.isBlank()) {
            if (spec == null) {
                spec = UserSpecification.hasSurname(surname);
            } else {
                spec = spec.and(UserSpecification.hasSurname(surname));
            }
        }

        return userRepository.findAll(spec, PageRequest.of(page, size))
                .map(userMapper::toDto);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest dto){

        if(userRepository.existsByEmailAndIdNot(dto.getEmail(), id)){
            throw new DuplicateEmailException("User with email " + dto.getEmail() + " exists");
        }

        User user = getUserEntityById(id);

        userMapper.updateFromDto(dto, user);
        userRepository.updateUser(user.getId(), user.getName(),
                user.getSurname(), user.getBirthDate(),
                user.getEmail(), user.isActive());
        return userMapper.toDto(user);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void updateUserActivity(Long id, boolean active){
        userRepository.updateActive(id, active);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "userCards", key = "#id")
    })
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("No such User with " + id + " id");
        }
        userRepository.deleteById(id);
    }

}