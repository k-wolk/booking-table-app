package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reservation {
    private static final String FIELD_REQUIRED = ReservationService.FIELD_REQUIRED;
    private static final String DATE_MESSAGE = ReservationService.DATE_MESSAGE;
    private static final int MIN_DURATION = ReservationService.MIN_DURATION;
    private static final int MAX_DURATION = ReservationService.MAX_DURATION;
    private static final String DURATION_MESSAGE = ReservationService.DURATION_MESSAGE;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = FIELD_REQUIRED + DATE_MESSAGE)
    @FutureOrPresent(message = DATE_MESSAGE)
    private LocalDate reservationDate;

    @NotNull(message = FIELD_REQUIRED)
    private LocalTime reservationTime;
    @NotNull(message = FIELD_REQUIRED + DURATION_MESSAGE)
    @Min(value = MIN_DURATION, message = DURATION_MESSAGE)
    @Max(value = MAX_DURATION, message = DURATION_MESSAGE)
    private Integer duration;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull(message = FIELD_REQUIRED)
    private User user;

    @ManyToOne
    @JoinColumn(name = "table_id")
    @NotNull(message = FIELD_REQUIRED)
    private DiningTable diningTable;
}
