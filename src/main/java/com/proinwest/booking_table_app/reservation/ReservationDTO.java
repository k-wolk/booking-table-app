package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationDTO(
        Long id,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer duration,
        Long userId,
        String login,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        DiningTable diningTable
) {
}
