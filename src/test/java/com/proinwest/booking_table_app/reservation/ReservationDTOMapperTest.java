package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationDTOMapperTest {

    private ReservationDTOMapper reservationDTOMapper;
    private Reservation reservation;
    private User user;
    private DiningTable diningTable;

    @BeforeEach
    void setUp() {
        reservationDTOMapper = new ReservationDTOMapper();

        user = new User();
        user.setId(Long.valueOf(13));
        user.setLogin("johndoe");
        user.setPassword("password123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPhoneNumber("+48-600-700-800");

        diningTable = new DiningTable();
        diningTable.setId(115);
        diningTable.setNumber(15);
        diningTable.setSeats(8);

        reservation = new Reservation();
        reservation.setId(1111L);
        reservation.setReservationDate(LocalDate.ofEpochDay(2025-01-13));
        reservation.setReservationTime(LocalTime.parse("19:30"));
        reservation.setDuration(3);
        reservation.setUser(user);
        reservation.setDiningTable(diningTable);
    }

    @Test
    public void MapReservationToReservationDTO() {
        ReservationDTO reservationDTO = reservationDTOMapper.apply(reservation);

        assertEquals(reservation.getId(), reservationDTO.id());
        assertEquals(reservation.getReservationDate(), reservationDTO.reservationDate());
        assertEquals(reservation.getReservationTime(), reservationDTO.reservationTime());
        assertEquals(reservation.getDuration(), reservationDTO.duration());
        assertEquals(reservation.getUser().getId(), reservationDTO.userId());
        assertEquals(reservation.getUser().getLogin(), reservationDTO.login());
        assertEquals(reservation.getUser().getFirstName(), reservationDTO.firstName());
        assertEquals(reservation.getUser().getLastName(), reservationDTO.lastName());
        assertEquals(reservation.getUser().getEmail(), reservationDTO.email());
        assertEquals(reservation.getUser().getPhoneNumber(), reservationDTO.phoneNumber());
        assertEquals(reservation.getDiningTable(), reservationDTO.diningTable());
    }
}