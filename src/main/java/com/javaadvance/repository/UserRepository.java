package com.javaadvance.repository;

import com.javaadvance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    public boolean existsByEmail(String email);

    public boolean existsByEmailAndIdNot(String email, Long id);

    @Modifying
    @Query("UPDATE User u SET u.name = :name, u.surname = :surname, " +
            "u.birthDate = :birthDate, u.email = :email, " +
            "u.active = :active, u.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE u.id = :id")
    public void updateUser(@Param("id") Long id,
                           @Param("name") String name,
                           @Param("surname") String surname,
                           @Param("birthDate") LocalDate birthDate,
                           @Param("email") String email,
                           @Param("active") boolean active);

    @Modifying
    @Query(value = "UPDATE users SET active = :active, updated_at = NOW() " +
            "WHERE id = :id", nativeQuery = true)
    public void updateActive(@Param("id") Long id, @Param("active") boolean active);


}
