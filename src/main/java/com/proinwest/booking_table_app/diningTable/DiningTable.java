package com.proinwest.booking_table_app.diningTable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proinwest.booking_table_app.reservation.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DiningTable {
    private static final String FIELD_REQUIRED = DiningTableService.FIELD_REQUIRED;
    private static final int MIN_NUMBER = DiningTableService.MIN_NUMBER;
    private static final int MAX_NUMBER = DiningTableService.MAX_NUMBER;
    private static final String NUMBER_MESSAGE = DiningTableService.NUMBER_MESSAGE;
    private static final int MIN_SEATS = DiningTableService.MIN_SEATS;
    private static final int MAX_SEATS = DiningTableService.MAX_SEATS;
    private static final String SEATS_MESSAGE = DiningTableService.SEATS_MESSAGE;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull(message = FIELD_REQUIRED + NUMBER_MESSAGE)
    @Min(value = MIN_NUMBER, message = NUMBER_MESSAGE)
    @Max(value = MAX_NUMBER, message = NUMBER_MESSAGE)
    @Column(unique = true)
    private Integer number;
    @NotNull(message = FIELD_REQUIRED + SEATS_MESSAGE)
    @Min(value = MIN_SEATS, message = SEATS_MESSAGE)
    @Max(value = MAX_SEATS, message = SEATS_MESSAGE)
    private Integer seats;

    @OneToMany(mappedBy = "diningTable")
    @JsonIgnore
    List<Reservation> reservations;
}
