package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Reservation {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @FutureOrPresent (message = "Reservation date should be present or future.")
    private LocalDate reservationDate;

    private LocalTime reservationTime;
    @NotNull (message = "Duration time should be between 1 and 6 hours.")
    @DecimalMin(value = "1", message = "Duration time should be between 1 and 6 hours.")
    @DecimalMax(value = "6", message = "Duration time should be between 1 and 6 hours.")
    private Integer duration;

    @ManyToOne
    @JoinColumn (name = "user_id")
    @NotNull(message = "User is required.")
    private User user;

    @ManyToOne
    @JoinColumn (name = "table_id")
    @NotNull(message = "Table is required.")
    private DiningTable diningTable;

    public Reservation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalTime getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(LocalTime reservationTime) {
        this.reservationTime = reservationTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DiningTable getDiningTable() {
        return diningTable;
    }

    public void setDiningTable(DiningTable diningTable) {
        this.diningTable = diningTable;
    }

}
