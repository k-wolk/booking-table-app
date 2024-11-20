package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.reservation.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

@Entity
public class DiningTable {
    private static final Integer MIN_NUMBER = DiningTableService.MIN_NUMBER;
    private static final int MAX_NUMBER = DiningTableService.MAX_NUMBER;
    private static final int MIN_SEATS = DiningTableService.MIN_SEATS;
    private static final int MAX_SEATS = DiningTableService.MAX_SEATS;


//    String numberMessage = "Table number should be between " + MIN_NUMBER + " and " + MAX_NUMBER + ".";
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull(message = "Table number is required.")
    @Min(value = 1, message = "Table number should be between 1 and 100.")
    @Max(value = 100, message = "Table number should be between 1 and 100.")
    @Column(unique = true)
    private Integer number;
    @NotNull (message = "Number of seats is required.")
    @DecimalMin(value = "1", message = "Number of seats should be between 1 and 50.")
    @DecimalMax(value = "50", message = "Number of seats should be between 1 and 50.")
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
