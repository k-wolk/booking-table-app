package com.proinwest.booking_table_app.user;

import com.proinwest.booking_table_app.reservation.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty (message = "Login is required and should have at least 3 characters!")
    @Size (min = 3, message = "Login should have at least 3 characters!")
    private String login;
    @NotEmpty (message = "Password is required and should have at least 12 characters!")
    @Size (min = 12, message = "Password should have at least 12 characters!")
    private String password;
    @NotEmpty (message = "First firstName is required!")
    private String firstName;
    @NotEmpty (message = "Last firstName is required!")
    private String lastName;
    @NotEmpty (message = "Email address is required!")
    @Email (message = "Wrong email address!")
    @Size (max = 255, message = "Email address should have max 255 characters!")
    @Column (unique = true)
    private String email;
    @NotEmpty (message = "Phone number is required!")
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


