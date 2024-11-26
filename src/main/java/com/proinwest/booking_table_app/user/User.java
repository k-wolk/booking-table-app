package com.proinwest.booking_table_app.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proinwest.booking_table_app.reservation.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    private static final String FIELD_REQUIRED = UserService.FIELD_REQUIRED;
    private static final int LOGIN_MIN_LENGTH = UserService.LOGIN_MIN_LENGTH;
    private static final String LOGIN_MESSAGE = UserService.LOGIN_MESSAGE;
    private static final int PASSWORD_MIN_LENGTH = UserService.PASSWORD_MIN_LENGTH;
    private static final String PASSWORD_MESSAGE = UserService.PASSWORD_MESSAGE;
    private static final int EMAIL_MAX_LENGTH = UserService.EMAIL_MAX_LENGTH;
    private static final String EMAIL_REGEX = UserService.EMAIL_REGEX;
    private static final String EMAIL_MESSAGE = UserService.EMAIL_MESSAGE;
    private static final String WRONG_EMAIL = UserService.WRONG_EMAIL;
    private static final int PHONE_NUMBER_MIN_LENGTH = UserService.PHONE_NUMBER_MIN_LENGTH;
    private static final String PHONE_NUMBER_REGEX = UserService.PHONE_NUMBER_REGEX;
    private static final String PHONE_MESSAGE = UserService.PHONE_MESSAGE;
    private static final String VALID_PHONE_NUMBER =UserService.VALID_PHONE_NUMBER;

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (unique = true)
    @NotEmpty (message = FIELD_REQUIRED + LOGIN_MESSAGE)
    @Size (min = LOGIN_MIN_LENGTH, message = LOGIN_MESSAGE)
    private String login;
    @NotEmpty (message = FIELD_REQUIRED + PASSWORD_MESSAGE)
    @Size (min = PASSWORD_MIN_LENGTH, message = PASSWORD_MESSAGE)
    private String password;
    @NotEmpty (message = FIELD_REQUIRED)
    private String firstName;
    @NotEmpty (message = FIELD_REQUIRED)
    private String lastName;
    @NotEmpty (message = FIELD_REQUIRED + EMAIL_MESSAGE)
    @Size (max = EMAIL_MAX_LENGTH, message = EMAIL_MESSAGE)
    @Email (regexp = EMAIL_REGEX,
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = WRONG_EMAIL)
    @Column (unique = true)
    private String email;
    @NotEmpty (message = FIELD_REQUIRED + PHONE_MESSAGE)
    @Size (min = PHONE_NUMBER_MIN_LENGTH, message = PHONE_MESSAGE + VALID_PHONE_NUMBER)
    @Pattern (regexp = PHONE_NUMBER_REGEX, message = PHONE_MESSAGE + VALID_PHONE_NUMBER)
    private String phoneNumber;

    @OneToMany (mappedBy = "user")
    @JsonIgnore
    List<Reservation> reservations;
}


