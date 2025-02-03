package com.proinwest.booking_table_app.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void connectionEstablished() {
        assertTrue(mySQLContainer.isCreated());
        assertTrue(mySQLContainer.isRunning());
    }

    @Test
    void shouldFindLoginById_whenExists() {
        // given
        final User user = new User();
        final String login = "newLogin";
        user.setLogin(login);
        user.setEmail("john@mail.com");
        user.setPassword("secret");
        user.setPhoneNumber("123-456-789");

        final User savedUser = userRepository.save(user);

        // when
        final String result = userRepository.findLoginByUserId(savedUser.getId());

        // then
        assertNotNull(result);
        assertEquals(login, result);
    }

    @Test
    void shouldNotFindLoginById_whenNotExists() {
        // given
        final Long id = 1L;

        // when
        final String result = userRepository.findLoginByUserId(id);

        // then
        assertNull(result);
    }

    @Test
    void shouldFindEmailById_whenExists() {
        // given
        final User user = new User();
        final String email = "john.doe@gmail.com";
        user.setLogin("johnny");
        user.setEmail(email);
        user.setPassword("secret");
        user.setPhoneNumber("123-456-789");

        final User savedUser = userRepository.save(user);
        final Long id = savedUser.getId();

        // when
        final String result = userRepository.findEmailByUserId(id);

        // then
        assertNotNull(result);
        assertEquals(email, result);
    }

    @Test
    void shouldNotFindEmailById_whenNotExists() {
        // given
        final Long id = 1L;

        // when
        final String result = userRepository.findEmailByUserId(id);

        // then
        assertNull(result);
    }
}