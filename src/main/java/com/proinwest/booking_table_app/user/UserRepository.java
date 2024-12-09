package com.proinwest.booking_table_app.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByLoginContainingIgnoreCase(String login);
    List<User> findAllByFirstNameContainingIgnoreCase(String firstName);
    List<User> findAllByLastNameContainingIgnoreCase(String lastName);
    List<User> findAllByEmailContainingIgnoreCase(String email);
    List<User> findAllByPhoneNumberContaining(String phoneNumber);
    List<User> findAllByLoginContainingOrFirstNameContainingOrLastNameContainingOrEmailContaining(String login, String firstName, String lastName, String email);
    boolean existsByEmail(String email);
    boolean existsByLogin(String login);

    @Query(value = "SELECT u.login FROM user u WHERE u.id = :id", nativeQuery = true)
    String findLoginById(Long id);

    @Query(value = "SELECT u.email FROM user u WHERE u.id = :id", nativeQuery = true)
    String findEmailById(Long id);
}
