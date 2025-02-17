package com.proinwest.booking_table_app.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    List<User> findAllByLoginContainingIgnoreCase(String login);
    List<User> findAllByFirstNameContainingIgnoreCase(String firstName);
    List<User> findAllByLastNameContainingIgnoreCase(String lastName);
    List<User> findAllByEmailContainingIgnoreCase(String email);
    List<User> findAllByPhoneNumberContaining(String phoneNumber);
    List<User> findAllByLoginContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumber(String login, String firstName, String lastName, String email, String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByLogin(String login);

    default List<User> findAllByLogin(String login) {
        return findAllByLoginContainingIgnoreCase(login);
    }
    default List<User> findAllByFirstName(String firstName) {
        return findAllByFirstNameContainingIgnoreCase(firstName);
    }
    default List<User> findAllByLastName(String lastName) {
        return findAllByLastNameContainingIgnoreCase(lastName);
    }
    default List<User> findAllByEmail(String email) {
        return findAllByEmailContainingIgnoreCase(email);
    }
    default List<User> findAllByPhoneNumber(String phoneNumber) {
        return findAllByPhoneNumberContaining(phoneNumber);
    }
    default List<User> findAllByAnyString(String any) {
        return findAllByLoginContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumber(any, any, any, any, any);
    }

    @Query(value = "SELECT u.login FROM user u WHERE u.id = :id", nativeQuery = true)
    String findLoginByUserId(Long id);
    @Query(value = "SELECT u.email FROM user u WHERE u.id = :id", nativeQuery = true)
    String findEmailByUserId(Long id);
}
