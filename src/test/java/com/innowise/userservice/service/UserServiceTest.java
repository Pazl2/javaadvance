package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserCreateRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.dto.UserUpdateRequest;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.DuplicateEmailException;
import com.innowise.userservice.exception.ResourceNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;


    @Test
    void createUser_ShouldReturnUserResponse_WhenEmailIsUnique(){

        UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setName("Ivan");
        userCreateRequest.setSurname("Ivanov");
        userCreateRequest.setEmail("ivanov@gmail.com");
        userCreateRequest.setBirthDate(LocalDate.of(2006, 3, 20));
        userCreateRequest.setActive(true);

        User userBeforeSafe = new User();
        userBeforeSafe.setName("Ivan");
        userBeforeSafe.setEmail("ivanov@gmail.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Ivan");
        savedUser.setEmail("ivanov@gmail.com");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("Ivan");
        userResponse.setEmail("ivanov@gmail.com");

        when(userMapper.toEntity(userCreateRequest)).thenReturn(userBeforeSafe);
        when(userRepository.existsByEmail(userCreateRequest.getEmail())).thenReturn(false);
        when(userRepository.save(userBeforeSafe)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(userResponse);

        UserResponse result = userService.createUser(userCreateRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ivanov@gmail.com", result.getEmail());

        verify(userMapper, times(1)).toEntity(userCreateRequest);
        verify(userRepository, times(1)).existsByEmail(userCreateRequest.getEmail());
        verify(userRepository, times(1)).save(userBeforeSafe);
        verify(userMapper, times(1)).toDto(savedUser);
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailExists() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("ivanov@gmail.com");

        when(userRepository.existsByEmail("ivanov@gmail.com")).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.createUser(request)
        );


        assertTrue(exception.getMessage().contains("ivanov@gmail.com"));

        verify(userRepository, never()).save(any());
    }



    @Test
    void getUserById_ShouldReturnUserResponse_WhenUserExists() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Ivan");
        user.setEmail("ivan@example.com");
        user.setActive(true);
        user.setPaymentCards(new ArrayList<>());

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(userId);
        expectedResponse.setName("Ivan");
        expectedResponse.setEmail("ivan@example.com");
        expectedResponse.setActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedResponse);

        UserResponse result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("ivan@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(userMapper, never()).toDto(any());
    }



    @Test
    void updateUser_ShouldReturnUpdatedUserResponse_WhenEmailNotChanged() {

        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Petr");
        request.setSurname("Sidorov");
        request.setEmail("petr@example.com");
        request.setBirthDate(LocalDate.of(1992, 2, 2));
        request.setActive(true);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("OldName");
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Petr");
        updatedUser.setEmail("petr@example.com");

        UserResponse responseDto = new UserResponse();
        responseDto.setId(userId);
        responseDto.setName("Petr");
        responseDto.setEmail("petr@example.com");

        when(userRepository.existsByEmailAndIdNot("petr@example.com", userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userMapper.toDto(existingUser)).thenReturn(responseDto);

        UserResponse result = userService.updateUser(userId, request);

        assertNotNull(result);
        assertEquals("petr@example.com", result.getEmail());

        verify(userMapper, times(1)).updateFromDto(request, existingUser);
        verify(userRepository, times(1)).updateUser(eq(userId), any(), any(), any(), any(), anyBoolean());
        verify(userMapper, times(1)).toDto(existingUser);
    }

    @Test
    void updateUser_ShouldThrowException_WhenEmailAlreadyUsed() {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("duplicate@example.com");

        when(userRepository.existsByEmailAndIdNot("duplicate@example.com", userId)).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.updateUser(userId, request)
        );

        assertTrue(exception.getMessage().contains("duplicate@example.com"));
        verify(userRepository, never()).updateUser(anyLong(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        Long userId = 999L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("some@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(userId, request)
        );

        assertTrue(exception.getMessage().contains("999"));

        verify(userRepository, never()).updateUser(anyLong(), any(), any(), any(), any(), anyBoolean());
        verify(userMapper, never()).toDto(any());
    }



    @Test
    void getUsersWithPaginationAndFilter_ShouldReturnPageOfUserResponse_WhenFiltersProvided() {

        String firstName = "Ivan";
        String surname = "Petrov";
        int page = 0;
        int size = 10;

        User user = new User();
        user.setId(1L);
        user.setName("Ivan");
        user.setSurname("Petrov");
        user.setEmail("ivan@example.com");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("Ivan");
        userResponse.setSurname("Petrov");
        userResponse.setEmail("ivan@example.com");

        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(page, size), 1);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        Page<UserResponse> result = userService.getUsersWithPaginationAndFilter(firstName, surname, page, size);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("ivan@example.com", result.getContent().get(0).getEmail());

        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(userMapper, times(1)).toDto(user);
    }



    @Test
    void updateUserActivity_ShouldCallRepositoryUpdateActive() {
        Long userId = 1L;
        boolean active = false;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserActivity(userId, active);

        verify(userRepository, times(1)).updateActive(userId, active);
        verify(userRepository, times(1)).findById(userId);
    }
}