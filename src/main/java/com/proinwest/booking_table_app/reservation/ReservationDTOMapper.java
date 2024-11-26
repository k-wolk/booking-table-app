package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.user.UserDTOMapper;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ReservationDTOMapper implements Function<Reservation, ReservationDTO> {
    @Override
    public ReservationDTO apply(Reservation reservation) {
        UserDTOMapper mapper = new UserDTOMapper();
        return new ReservationDTO(
                reservation.getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration(),
                mapper.apply(reservation.getUser()),
                reservation.getDiningTable()
        );
    }



}
