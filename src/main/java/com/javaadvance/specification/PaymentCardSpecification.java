package com.javaadvance.specification;

import com.javaadvance.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

    private PaymentCardSpecification(){}

    public static Specification<PaymentCard> hasHolder(String holder){
        return (root, query, criteriaBuilder) -> {
            if(holder == null || holder.isBlank()){
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("holder")),
                    "%" + holder.toLowerCase() + "%"
            );

        };
    }

}
