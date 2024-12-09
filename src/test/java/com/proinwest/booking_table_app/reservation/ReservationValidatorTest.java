package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.exceptions.TableNotAvailableException;
import com.proinwest.booking_table_app.user.User;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationValidatorTest {

    @Test
    void whenReservationsCollide_shouldThrowException() {
        // given
        User user = Instancio.create(User.class);
        DiningTable table = new DiningTable();
        int tableId = 123;
        table.setId(tableId);

        LocalDate reservationDate = LocalDate.now();
        Reservation incomingReservation = new Reservation();
        incomingReservation.setReservationDate(reservationDate);
        incomingReservation.setReservationTime(LocalTime.now());
        incomingReservation.setDiningTable(table);
        incomingReservation.setDuration(1);
        incomingReservation.setUser(user);

        Reservation existingReservation = new Reservation();
        existingReservation.setReservationDate(reservationDate);
        existingReservation.setReservationTime(LocalTime.now());
        existingReservation.setDiningTable(table);
        existingReservation.setDuration(1);
        existingReservation.setUser(user);

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(existingReservation);

        ReservationRepository reservationRepository = Mockito.mock(ReservationRepository.class);
        Mockito.when(
                        reservationRepository.findAllByReservationDateAndDiningTableId(reservationDate, tableId)
                )
                .thenReturn(reservations);
        ReservationValidator reservationValidator = new ReservationValidator(null, null, null, reservationRepository);

        // when then
        assertThrows(TableNotAvailableException.class, () -> reservationValidator.isTableAvailable(incomingReservation));
    }
}