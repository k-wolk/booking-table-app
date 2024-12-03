package com.proinwest.booking_table_app.diningTable;

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
public class DiningTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private Integer number;

    private Integer seats;

    @OneToMany(mappedBy = "diningTable")
    @JsonIgnore
    List<Reservation> reservations;
}
