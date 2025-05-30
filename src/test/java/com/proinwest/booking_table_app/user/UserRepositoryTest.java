package com.proinwest.booking_table_app.user;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4.0");
    @Autowired
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLogin("testUser");
        user.setEmail("john@mail.com");
        user.setFirstName("Johnny");
        user.setLastName("Doe");
        user.setPhoneNumber("999 999 999");
        user.setPassword("secretpassword");
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @AfterAll
    static void stopContainer() {
        mySQLContainer.stop();
    }

    @Test
    void connectionEstablished() {
        assertTrue(mySQLContainer.isCreated());
        assertTrue(mySQLContainer.isRunning());
    }

    @Test
    void findLoginByUserId_whenExists_shouldReturnUserLogin() {
        // when
        final String result = userRepository.findLoginByUserId(user.getId());

        // then
        assertNotNull(result);
        assertEquals(user.getLogin(), result);
    }

    @Test
    void findLoginByUserId_whenNotExists_shouldReturnNull() {
        // given
        final Long id = user.getId() + 1;

        // when
        final String result = userRepository.findLoginByUserId(id);

        // then
        assertNull(result);
    }

    @Test
    void findEmailByUserId_whenExists_shouldReturnUserEmail() {
        // when
        final String result = userRepository.findEmailByUserId(user.getId());

        // then
        assertNotNull(result);
        assertEquals(user.getEmail(), result);
    }

    @Test
    void findEmailByUserId_whenNotExists_shouldReturnNull() {
        // given
        final Long id = user.getId() + 1;

        // when
        final String result = userRepository.findEmailByUserId(id);

        // then
        assertNull(result);
    }

    @Test
    void searchUsers_whenExists_shouldFindUserById() {
        // given
        String searchTerm = user.getId().toString();

        // when
        List<User> result = userRepository.searchUsers(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(user), result);
    }

    @Test
    void searchUsers_whenExists_shouldFindUserByLogin() {
        // given
        String searchTerm = "stu";

        // when
        List<User> result = userRepository.searchUsers(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(user), result);
    }

    @Test
    void searchUsers_whenExists_shouldFindUserByEmail() {
        // given
        String searchTerm = "l.c";

        // when
        List<User> result = userRepository.searchUsers(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(user), result);
    }

    @Test
    void searchUsers_whenExists_shouldFindUserByFirstName() {
        // given
        String searchTerm = "nny";

        // when
        List<User> result = userRepository.searchUsers(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(user), result);
    }

    @Test
    void searchUsers_whenExists_shouldFindUserByLastName() {
        // given
        String searchTerm = "doe";

        // when
        List<User> result = userRepository.searchUsers(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(user), result);
    }

    @Test
    void searchUsers_whenExists_shouldFindUserByPhoneNumber() {
        // given
        String searchTerm = "999";

        // when
        List<User> result = userRepository.searchUsers(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(user), result);
    }

    @Test
    void searchUsers_whenUserNotFound_shouldReturnEmptyList() {
        // given
        String searchTerm = "nonexistent";

        // when
        List<User> result = userRepository.searchUsers(searchTerm);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}