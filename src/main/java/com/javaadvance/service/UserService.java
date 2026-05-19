package com.javaadvance.service;

import com.javaadvance.dto.UserCreateRequest;
import com.javaadvance.dto.UserResponse;
import com.javaadvance.dto.UserUpdateRequest;
import com.javaadvance.entity.User;
import com.javaadvance.mapper.UserMapper;
import com.javaadvance.repository.UserRepository;
import com.javaadvance.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.NoSuchElementException;

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
            throw new IllegalStateException("User with email " + dto.getEmail() + " exists");
        }
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public UserResponse getUserById(Long userId) {
        return userMapper.toDto(getUserEntityById(userId));
    }

    private User getUserEntityById (Long userId){
        return userRepository.findById(userId).orElseThrow(
                ()-> new NoSuchElementException("No such User with "+ userId + " id"));
    }

    public Page<UserResponse> getUsersWithPaginationAndFilter(
            String firstName,
            String surname,
            int page, int size){

        Specification<User> spec = Specification.where((Specification<User>) null);
        spec = spec.and(UserSpecification.hasFirstName(firstName));
        spec = spec.and(UserSpecification.hasSurname(surname));

        return userRepository.findAll(spec, PageRequest.of(page, size))
                .map(userMapper::toDto);
    }

    @Transactional
    public void updateUser(UserUpdateRequest dto){

        if(userRepository.existsByEmailAndIdNot(dto.getEmail(), dto.getId())){
            throw new IllegalStateException("User with email " + dto.getEmail() + " exists");
        }

        User user = getUserEntityById(dto.getId());

        userMapper.updateFromDto(dto, user);
        userRepository.updateUser(user.getId(), user.getName(),
                user.getSurname(), user.getBirthDate(),
                user.getEmail(), user.isActive());
    }

    @Transactional
    public void updateUserActivity(Long id, boolean active){
        userRepository.updateActive(id, active);
    }

}
