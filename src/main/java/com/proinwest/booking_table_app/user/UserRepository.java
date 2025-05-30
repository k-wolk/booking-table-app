package com.proinwest.booking_table_app.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    boolean existsByEmail(String email);
    boolean existsByLogin(String login);

    @Query(value = "SELECT u.login FROM user u WHERE u.id = :id", nativeQuery = true)
    String findLoginByUserId(Long id);
    @Query(value = "SELECT u.email FROM user u WHERE u.id = :id", nativeQuery = true)
    String findEmailByUserId(Long id);

    @Query(value = "SELECT * FROM user u WHERE " +
            "CAST(u.id AS CHAR) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "LOWER(u.login) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.first_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.last_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "u.phone_number LIKE CONCAT('%', :searchTerm, '%')",
            nativeQuery = true)
    List<User> searchUsers(String searchTerm);
}