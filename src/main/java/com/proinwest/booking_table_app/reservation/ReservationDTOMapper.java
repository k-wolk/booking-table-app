package com.proinwest.booking_table_app.reservation;

import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ReservationDTOMapper implements Function<Reservation, ReservationDTO> {
    @Override
    public ReservationDTO apply(Reservation reservation) {
        return new ReservationDTO(
                reservation.getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration(),
                reservation.getUser().getId(),
                reservation.getUser().getLogin(),
                reservation.getUser().getFirstName(),
                reservation.getUser().getLastName(),
                reservation.getUser().getEmail(),
                reservation.getUser().getPhoneNumber(),
                reservation.getDiningTable()
        );
    }



}
