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
        user.setRole("USER");
        user.setActive(true);
        userId = 1L;
    }

    @Test
    void validateNewUser_whenUserIsValid_shouldReturnEmptyErrorsMap() {
        // when
        final Map<String, String> result = userValidator.validateNewUser(user);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void validateNewUser_whenLoginIsNull_shouldReturnError() {
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
    void validateNewUser_whenLoginIsBlank_shouldReturnError() {
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
    void validateNewUser_whenLoginAlreadyExists_shouldReturnError() {
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
    void validateNewUser_whenLoginIsTooShort_shouldReturnError() {
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
    void validateNewUser_whenPasswordIsNull_shouldReturnError() {
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
    void validateNewUser_whenPasswordIsBlank_shouldReturnError() {
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
    void validateNewUser_whenPasswordIsTooShort_shouldReturnError() {
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
    void validateNewUser_whenFirstNameIsNull_shouldReturnError() {
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
    void validateNewUser_whenFirstNameIsBlank_shouldReturnError() {
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
    void validateNewUser_whenLastNameIsNull_shouldReturnError() {
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
    void validateNewUser_whenLastNameIsBlank_shouldReturnError() {
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
    void validateNewUser_whenEmailIsNull_shouldReturnError() {
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
    void validateNewUser_whenEmailIsBlank_shouldReturnError() {
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
    void validateNewUser_whenEmailAlreadyExists_shouldReturnError() {
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
    void validateNewUser_whenEmailIsTooLong_shouldReturnError() {
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
    void validateNewUser_whenEmailNotMatchPattern_shouldReturnError() {
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
    void validateNewUser_whenPhoneNumberIsNull_shouldReturnError() {
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
    void validateNewUser_whenPhoneNumberIsBlank_shouldReturnError() {
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
    void validateNewUser_whenPhoneNumberIsTooShort_shouldReturnError() {
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
    void validateNewUser_whenPhoneNumberNotMatchPattern_shouldReturnError() {
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
    void validateUserToUpdate_whenParamsAreValid_shouldNotReturnError() {
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
    void validateUserToUpdate_whenLoginAlreadyExists_shouldReturnError() {
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
    void validateUserToUpdate_whenEmailAlreadyExists_shouldReturnError() {
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
    void validateUserId_whenUserIdIsNull_shouldReturnError() {
        // given
        userId = null;

        final Map<String, String> errors = new HashMap<>();
        final Map<String, String> expected = new HashMap<>();
        expected.put("user", USER_ID_IS_REQUIRED);

        // when
        final Map<String, String> result = userValidator.validateUserId(userId, errors);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void validateUserId_whenUserNotExists_shouldReturnError() {
        // given
        userId = 111L;

        final Map<String, String> errors = new HashMap<>();
        final Map<String, String> expected = new HashMap<>();
        expected.put("user", "User with id " + userId + " was not found.");

        when(userService.existsById(userId)).thenReturn(false);

        // when
        final Map<String, String> result = userValidator.validateUserId(userId, errors);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(userId);
    }
}