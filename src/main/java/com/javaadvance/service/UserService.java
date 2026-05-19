package com.javaadvance.service;

import com.javaadvance.entity.User;
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

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void createUser(User user){
        userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                ()-> new NoSuchElementException("No such User with "+ userId + " id"));
    }

    public Page<User> getUsersWithPaginationAndFilter(
            String firstName,
            String surname,
            int page, int size){

        Specification<User> spec = Specification.where((Specification<User>) null);
        spec = spec.and(UserSpecification.hasFirstName(firstName));
        spec = spec.and(UserSpecification.hasSurname(surname));

        return userRepository.findAll(spec, PageRequest.of(page, size));
    }

    @Transactional
    public void updateUser(Long id, String name,
                           String surname, LocalDate birthDate,
                           String email, boolean active){
        userRepository.updateUser(id, name, surname, birthDate, email, active);
    }

    @Transactional
    public void updateUserActivity(Long id, boolean active){
        userRepository.updateActive(id, active);
    }

}
