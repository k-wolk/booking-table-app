package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.reservation.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
public class DiningTable {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull(message = "Table number is required!")
    @DecimalMin(value = "1", message = "Table number should be between 1 and 100!")
    @DecimalMax(value = "100", message = "Table number should be between 1 and 100!")
    @Column (unique = true)
    private Integer number;
    @NotNull (message = "Number of seats can not be empty!")
    @DecimalMin(value = "1", message = "Number of seats should be between 1 and 50!")
    @DecimalMax(value = "50", message = "Number of seats should be between 1 and 50!")
    private Integer seats;

    @OneToMany(mappedBy = "diningTable")
    List<Reservation> reservations;

    public DiningTable() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }
}
