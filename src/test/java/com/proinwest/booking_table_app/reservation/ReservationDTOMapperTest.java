package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.user.User;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationDTOMapperTest {

    @Test
    public void should_map_reservation_to_reservationDTO() {
        // given
        User user = Instancio.create(User.class);
        DiningTable diningTable = Instancio.create(DiningTable.class);

        Reservation reservation = new Reservation();
        reservation.setId(1111L);
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.parse("19:30"));
        reservation.setDuration(3);
        reservation.setUser(user);
        reservation.setDiningTable(diningTable);

        ReservationDTOMapper reservationDTOMapper = new ReservationDTOMapper();

        // when
        ReservationDTO reservationDTO = reservationDTOMapper.apply(reservation);

        // then
        assertEquals(reservation.getId(), reservationDTO.id());
        assertEquals(reservation.getReservationDate(), reservationDTO.reservationDate());
        assertEquals(reservation.getReservationTime(), reservationDTO.reservationTime());
        assertEquals(reservation.getDuration(), reservationDTO.duration());

        assertEquals(reservation.getUser().getId(), reservationDTO.user().id());
        assertEquals(reservation.getUser().getLogin(), reservationDTO.user().login());
        assertEquals(reservation.getUser().getEmail(), reservationDTO.user().email());
        assertEquals(reservation.getUser().getFirstName(), reservationDTO.user().firstName());
        assertEquals(reservation.getUser().getLastName(), reservationDTO.user().lastName());
        assertEquals(reservation.getUser().getPhoneNumber(), reservationDTO.user().phoneNumber());

        assertEquals(reservation.getDiningTable(), reservationDTO.diningTable());
    }
}