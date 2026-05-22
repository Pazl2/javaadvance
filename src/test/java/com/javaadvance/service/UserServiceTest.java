package com.javaadvance.service;

import com.javaadvance.dto.UserCreateRequest;
import com.javaadvance.dto.UserResponse;
import com.javaadvance.entity.User;
import com.javaadvance.exception.DublicateEmailException;
import com.javaadvance.mapper.UserMapper;
import com.javaadvance.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

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

        DublicateEmailException exception = assertThrows(
                DublicateEmailException.class,
                () -> userService.createUser(request)
        );


        assertTrue(exception.getMessage().contains("ivanov@gmail.com"));

        verify(userRepository, never()).save(any());
    }

}
