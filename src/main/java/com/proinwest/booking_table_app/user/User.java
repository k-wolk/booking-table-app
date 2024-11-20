package com.proinwest.booking_table_app.user;

import com.proinwest.booking_table_app.reservation.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (unique = true)
    @NotEmpty (message = "Login is required and should contain at least 3 characters.")
    @Size (min = 3, message = "Login should contain at least 3 characters.")
    private String login;
    @NotEmpty (message = "Password is required and should contain at least 12 characters.")
    @Size (min = 12, message = "Password should contain at least 12 characters.")
    private String password;
    @NotEmpty (message = "First name is required.")
    private String firstName;
    @NotEmpty (message = "Last name is required.")
    private String lastName;
    @Column (unique = true)
    @NotEmpty (message = "Email address is required.")
    @Size (max = 70, message = "Email address should contain max 70 characters.")
    @Email (regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$",
            flags = Pattern.Flag.CASE_INSENSITIVE, message = "Wrong email address.")
    private String email;
    @NotEmpty (message = "Phone number is required and should contain at least 7 digits.")
    @Size (min = 7, message = "Phone number should contain at least 7 digits.")
    private String phoneNumber;

    @OneToMany (mappedBy = "user")
    List<Reservation> reservations;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}


