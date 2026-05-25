package com.innowise.userservice.specification;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

    private PaymentCardSpecification(){}

    public static Specification<PaymentCard> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) -> {
            if (firstName == null || firstName.isBlank()) return null;
            Join<PaymentCard, User> join = root.join("user");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(join.get("name")),
                    firstName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<PaymentCard> hasSurname(String surname) {
        return (root, query, criteriaBuilder) -> {
            if (surname == null || surname.isBlank()) return null;
            Join<PaymentCard, User> join = root.join("user");
            return criteriaBuilder.like(
                    criteriaBuilder.lower(join.get("surname")),
                    surname.toLowerCase() + "%"
            );
        };
    }

}
