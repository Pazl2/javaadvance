package com.innowise.userservice.specification;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification(){}

    public static Specification<User> hasFirstName(String firstName){
        return (root, query, criteriaBuilder) -> {
            if(firstName == null || firstName.isBlank()){
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                            "%" + firstName.toLowerCase() + "%");

        };
    }

    public static Specification<User> hasSurname(String surname){
        return (root, query, criteriaBuilder) -> {
        if(surname == null || surname.isBlank()){
            return null;
        }
        return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("surname")),
                "%" + surname.toLowerCase() + "%"
            );
        };
    }


}
