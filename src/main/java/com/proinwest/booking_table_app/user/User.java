package com.proinwest.booking_table_app.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proinwest.booking_table_app.reservation.Reservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (unique = true)
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    @Column (unique = true)
    private String email;
    private String phoneNumber;

    @OneToMany (mappedBy = "user")
    @JsonIgnore
    List<Reservation> reservations;
}


