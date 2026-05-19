package com.javaadvance.repository;

import com.javaadvance.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>,
        JpaSpecificationExecutor<PaymentCard> {

    public List<PaymentCard> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE PaymentCard p SET p.number = :number, p.holder = :holder, " +
            "p.expirationDate = :expirationDate, " +
            " p.active = :active, p.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE p.id = :id")
    public void updatePaymentCard(@Param("id") Long id,
                           @Param("number") String number,
                           @Param("holder") String holder,
                           @Param("expirationDate") LocalDate expirationDate,
                           @Param("active") boolean active);





}
