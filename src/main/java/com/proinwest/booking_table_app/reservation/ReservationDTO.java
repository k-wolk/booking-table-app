package com.proinwest.booking_table_app.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.user.UserDTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationDTO(
        Long id,
        LocalDate reservationDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime reservationTime,
        Integer duration,
        UserDTO user,
        DiningTable diningTable
) {
}
