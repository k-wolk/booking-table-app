package com.proinwest.booking_table_app.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.proinwest.booking_table_app.user.UserService.*;

@Component
public class UserValidator {

    private final UserService userService;

    public UserValidator(@Lazy UserService userService) {
        this.userService = userService;
    }

    final Map<String, String> validateNewUser(User user) {
        final Map<String, String> errors = new HashMap<>();

        validateLogin(user.getLogin(), errors);
        validatePassword(user.getPassword(), errors);
        validateFirstName(user.getFirstName(), errors);
        validateLastName(user.getLastName(), errors);
        validateEmail(user.getEmail(), errors);
        validatePhoneNumber(user.getPhoneNumber(), errors);

        return errors;
    }

    Map<String, String> validateUserToUpdate(User user, Long userId) {
        final Map<String, String> errors = new HashMap<>();

        if (user.getLogin() != null) validateLogin(user.getLogin(), errors, userId);
        if (user.getPassword() != null) validatePassword(user.getPassword(), errors);
        if (user.getFirstName() != null) validateFirstName(user.getFirstName(), errors);
        if (user.getLastName() != null) validateLastName(user.getLastName(), errors);
        if (user.getEmail() != null) validateEmail(user.getEmail(), errors, userId);
        if (user.getPhoneNumber() != null) validatePhoneNumber(user.getPhoneNumber(), errors);

        return errors;
    }

    private void validateLogin(String login, Map<String, String> errors, Long userId) {
        if (login == null || login.isBlank()) {
            errors.put("login", FIELD_REQUIRED + LOGIN_MESSAGE);
        } else if (!userService.findLoginByUserId(userId).equals(login) && userService.existsByLogin(login)) {
            errors.put("login", "Login " + login + " already exists. It should be unique.");
        } else if (login.length() < LOGIN_MIN_LENGTH) {
            errors.put("login", LOGIN_MESSAGE);
        }
    }

    private void validateLogin(String login, Map<String, String> errors) {
        if (login == null || login.isBlank()) {
            errors.put("login", FIELD_REQUIRED + LOGIN_MESSAGE);
        } else if (userService.existsByLogin(login)) {
            errors.put("login", "Login " + login + " already exists. It should be unique.");
        } else if (login.length() < LOGIN_MIN_LENGTH) {
            errors.put("login", LOGIN_MESSAGE);
        }
    }

    private void validatePassword(String password, Map<String, String> errors) {
        if (password == null || password.isBlank()) {
            errors.put("password", FIELD_REQUIRED + PASSWORD_MESSAGE);
        } else if (password.length() < PASSWORD_MIN_LENGTH) {
            errors.put("password", PASSWORD_MESSAGE);
        }
    }

    private void validateFirstName(String firstName, Map<String, String> errors) {
        if (firstName == null || firstName.isBlank()) errors.put("firstName", FIELD_REQUIRED);
    }

    private void validateLastName(String lastName, Map<String, String> errors) {
        if (lastName == null || lastName.isBlank()) errors.put("lastName", FIELD_REQUIRED);
    }

    private void validateEmail(String email, Map<String, String> errors, Long userId) {
        if (email == null || email.isBlank()) {
            errors.put("email", FIELD_REQUIRED + EMAIL_MESSAGE);
        } else if (!userService.findEmailByUserId(userId).equals(email) && userService.existsByEmail(email)) {
            errors.put("email", "Email address " + email + " already exists. It should be unique.");
        } else if (email.length() > EMAIL_MAX_LENGTH) {
            errors.put("email", EMAIL_MESSAGE);
        } else if (!Pattern.matches(EMAIL_REGEX, email)) {
            errors.put("email", WRONG_EMAIL);
        }
    }

    private void validateEmail(String email, Map<String, String> errors) {
        if (email == null || email.isBlank()) {
            errors.put("email", FIELD_REQUIRED + EMAIL_MESSAGE);
        } else if (userService.existsByEmail(email)) {
            errors.put("email", "Email address " + email + " already exists. It should be unique.");
        } else if (email.length() > EMAIL_MAX_LENGTH) {
            errors.put("email", EMAIL_MESSAGE);
        } else if (!Pattern.matches(EMAIL_REGEX, email)) {
            errors.put("email", WRONG_EMAIL);
        }
    }

    private void validatePhoneNumber(String phoneNumber, Map<String, String> errors) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            errors.put("phoneNumber", FIELD_REQUIRED + PHONE_MESSAGE);
        } else if (phoneNumber.length() < PHONE_NUMBER_MIN_LENGTH) {
            errors.put("phoneNumber", PHONE_MESSAGE);
        } else if (!Pattern.matches(PHONE_NUMBER_REGEX, phoneNumber)) {
            errors.put("phoneNumber", PHONE_MESSAGE + VALID_PHONE_NUMBER);
        }
    }
}