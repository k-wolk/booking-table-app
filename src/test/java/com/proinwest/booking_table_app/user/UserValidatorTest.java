package com.proinwest.booking_table_app.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static com.proinwest.booking_table_app.user.UserService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserValidator userValidator;
    private User user;
    private Long userId;

    @BeforeEach
    void setup() {
        user = new User();
        user.setLogin("john");
        user.setPassword("secretpassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPhoneNumber("123-456-789");

        userId = 1L;
    }

    @Test
    void whenUserAndIdAreValid_shouldNotReturnError() {
        // given
        when(userService.findLoginByUserId(userId)).thenReturn(user.getLogin());
        when(userService.findEmailByUserId(userId)).thenReturn(user.getEmail());

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertTrue(result.isEmpty());
        verify(userService, times(1)).findLoginByUserId(userId);
        verify(userService, times(1)).findEmailByUserId(userId);
    }

    @Test
    void validateUserToUpdate_whenLoginIsBlank_shouldReturnError() {
        // given
        user.setLogin(" ");

        when(userService.findEmailByUserId(userId)).thenReturn(user.getEmail());

        final Map<String, String> expected = new HashMap<>();
        expected.put("login", FIELD_REQUIRED + LOGIN_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).findEmailByUserId(userId);
    }

    @Test
    void validateUserToUpdate_whenLoginExists_shouldReturnError() {
        // given
        final String login = user.getLogin();

        when(userService.findLoginByUserId(userId)).thenReturn("login");
        when(userService.existsByLogin(login)).thenReturn(true);
        when(userService.findEmailByUserId(userId)).thenReturn(user.getEmail());

        final Map<String, String> expected = new HashMap<>();
        expected.put("login", "Login " + login + " already exists. It should be unique.");

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).findLoginByUserId(userId);
        verify(userService, times(1)).existsByLogin(login);
        verify(userService, times(1)).findEmailByUserId(userId);
    }

    @Test
    void validateUserToUpdate_whenLoginIsTooShort_shouldReturnError() {
        // given
        user.setLogin("aa");

        when(userService.findLoginByUserId(userId)).thenReturn(user.getLogin());
        when(userService.findEmailByUserId(userId)).thenReturn(user.getEmail());

        final Map<String, String> expected = new HashMap<>();
        expected.put("login", LOGIN_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).findLoginByUserId(userId);
        verify(userService, times(1)).findEmailByUserId(userId);
    }

    @Test
    void validateUserToUpdate_whenEmailIsBlank_shouldReturnError() {
        // given
        user.setEmail(" ");

        when(userService.findLoginByUserId(userId)).thenReturn(user.getLogin());

        final Map<String, String> expected = new HashMap<>();
        expected.put("email", FIELD_REQUIRED + EMAIL_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).findLoginByUserId(userId);
    }

    @Test
    void validateUserToUpdate_whenEmailExists_shouldReturnError() {
        // given
        final String email = user.getEmail();

        when(userService.findLoginByUserId(userId)).thenReturn(user.getLogin());
        when(userService.existsByEmail(email)).thenReturn(true);
        when(userService.findEmailByUserId(userId)).thenReturn("ann@gmail.com");

        final Map<String, String> expected = new HashMap<>();
        expected.put("email", "Email address " + email + " already exists. It should be unique.");

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).findLoginByUserId(userId);
        verify(userService, times(1)).existsByEmail(email);
        verify(userService, times(1)).findEmailByUserId(userId);
    }

    @Test
    void validateUserToUpdate_whenEmailIsTooLong_shouldReturnError() {
        // given
        user.setEmail("1234567890.1234567890.1234567890.1234567890.1234567890.1234567890@x.com");

        when(userService.findLoginByUserId(userId)).thenReturn(user.getLogin());
        when(userService.findEmailByUserId(userId)).thenReturn(user.getEmail());

        final Map<String, String> expected = new HashMap<>();
        expected.put("email", EMAIL_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).findLoginByUserId(userId);
        verify(userService, times(1)).findEmailByUserId(userId);
    }

    @Test
    void validateUserToUpdate_whenEmailNotMatchPattern_shouldReturnError() {
        // given
        user.setEmail("mail@mail");

        when(userService.findLoginByUserId(userId)).thenReturn(user.getLogin());
        when(userService.findEmailByUserId(userId)).thenReturn(user.getEmail());

        final Map<String, String> expected = new HashMap<>();
        expected.put("email", WRONG_EMAIL);

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).findLoginByUserId(userId);
        verify(userService, times(1)).findEmailByUserId(userId);
    }

    @Test
    void whenUserIsValid_shouldReturnEmptyErrorsMap() {
        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void whenLoginIsNull_shouldReturnError() {
        // given
        user.setLogin(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("login", FIELD_REQUIRED + LOGIN_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenLoginIsBlank_shouldReturnError() {
        // given
        user.setLogin(" ");

        final Map<String, String> expected = new HashMap<>();
        expected.put("login", FIELD_REQUIRED + LOGIN_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenLoginExists_shouldReturnError() {
        // given
        final String login = user.getLogin();

        when(userService.existsByLogin(login)).thenReturn(true);

        final Map<String, String> expected = new HashMap<>();
        expected.put("login", "Login " + login + " already exists. It should be unique.");

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsByLogin(login);
    }

    @Test
    void whenLoginIsTooShort_shouldReturnError() {
        // given
        user.setLogin("aa");

        final Map<String, String> expected = new HashMap<>();
        expected.put("login", LOGIN_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenEmailIsNull_shouldReturnError() {
        // given
        user.setEmail(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("email", FIELD_REQUIRED + EMAIL_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenEmailIsBlank_shouldReturnError() {
        // given
        user.setEmail(" ");

        final Map<String, String> expected = new HashMap<>();
        expected.put("email", FIELD_REQUIRED + EMAIL_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenEmailExists_shouldReturnError() {
        // given
        final String email = user.getEmail();

        when(userService.existsByEmail(email)).thenReturn(true);

        final Map<String, String> expected = new HashMap<>();
        expected.put("email", "Email address " + email + " already exists. It should be unique.");

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsByEmail(email);
    }

    @Test
    void whenEmailIsTooLong_shouldReturnError() {
        // given
        user.setEmail("1234567890.1234567890.1234567890.1234567890.1234567890.1234567890@x.com");

        final Map<String, String> expected = new HashMap<>();
        expected.put("email", EMAIL_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenEmailNotMatchPattern_shouldReturnError() {
        // given
        user.setEmail("mail@mail");

        final Map<String, String> expected = new HashMap<>();
        expected.put("email", WRONG_EMAIL);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenPasswordIsNull_shouldReturnError() {
        // given
        user.setPassword(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("password", FIELD_REQUIRED + PASSWORD_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenPasswordIsBlank_shouldReturnError() {
        // given
        user.setPassword(" ");

        final Map<String, String> expected = new HashMap<>();
        expected.put("password", FIELD_REQUIRED + PASSWORD_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenPasswordIsTooShort_shouldReturnError() {
        // given
        user.setPassword("secret12345");

        final Map<String, String> expected = new HashMap<>();
        expected.put("password", PASSWORD_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenFirstNameIsNull_shouldReturnError() {
        // given
        user.setFirstName(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("firstName", FIELD_REQUIRED);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenFirstNameIsBlank_shouldReturnError() {
        // given
        user.setFirstName(" ");

        final Map<String, String> expected = new HashMap<>();
        expected.put("firstName", FIELD_REQUIRED);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenLastNameIsNull_shouldReturnError() {
        // given
        user.setLastName(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("lastName", FIELD_REQUIRED);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenLastNameIsBlank_shouldReturnError() {
        // given
        user.setLastName(" ");

        final Map<String, String> expected = new HashMap<>();
        expected.put("lastName", FIELD_REQUIRED);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenPhoneNumberIsNull_shouldReturnError() {
        // given
        user.setPhoneNumber(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("phoneNumber", FIELD_REQUIRED + PHONE_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenPhoneNumberIsBlank_shouldReturnError() {
        // given
        user.setPhoneNumber(" ");

        final Map<String, String> expected = new HashMap<>();
        expected.put("phoneNumber", FIELD_REQUIRED + PHONE_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenPhoneNumberIsTooShort_shouldReturnError() {
        // given
        user.setPhoneNumber("123456");

        final Map<String, String> expected = new HashMap<>();
        expected.put("phoneNumber", PHONE_MESSAGE);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void whenPhoneNumberNotMatchPattern_shouldReturnError() {
        // given
        user.setPhoneNumber("123456/8");

        final Map<String, String> expected = new HashMap<>();
        expected.put("phoneNumber", PHONE_MESSAGE + VALID_PHONE_NUMBER);

        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void validatePartialUser_whenUserAndUserIdAreValid_shouldNotReturnError() {
        // given
        when(userService.findLoginByUserId(userId)).thenReturn(user.getLogin());
        when(userService.findEmailByUserId(userId)).thenReturn(user.getEmail());

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertTrue(result.isEmpty());
        verify(userService, times(1)).findLoginByUserId(userId);
        verify(userService, times(1)).findEmailByUserId(userId);
    }

    @Test
    void validatePartialUser_whenUsersFieldsAreNull_shouldReturnEmptyErrorsMap() {
        // given
        user.setId(null);
        user.setLogin(null);
        user.setPassword(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setEmail(null);
        user.setPhoneNumber(null);

        // when
        final Map<String, String> result = userValidator.validateUserToUpdate(user, userId);

        // then
        assertTrue(result.isEmpty());
    }
}