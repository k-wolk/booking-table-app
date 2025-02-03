package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.diningTable.DiningTableService;
import com.proinwest.booking_table_app.diningTable.DiningTableValidator;
import com.proinwest.booking_table_app.exceptions.TableNotAvailableException;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserService;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.TABLE_ID_IS_REQUIRED;
import static com.proinwest.booking_table_app.reservation.ReservationService.*;
import static com.proinwest.booking_table_app.user.UserService.USER_ID_IS_REQUIRED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationValidatorTest {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserService userService;
    @Mock
    private DiningTableService tableService;
    @Mock
    private DiningTableValidator tableValidator;
    @InjectMocks
    private ReservationValidator reservationValidator;
    private User user;
    private DiningTable table;
    private Reservation reservation;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        table = Instancio.create(DiningTable.class);

        reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(15,0));
        reservation.setDuration(2);
        reservation.setUser(user);
        reservation.setDiningTable(table);
    }

    @Test
    void validateReservation_whenReservationParamsAreValid_shouldReturnEmptyErrorsMap() {
        // given
        when(userService.existsById(user.getId())).thenReturn(true);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertTrue(result.isEmpty());
        verify(userService, times(1)).existsById(user.getId());
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenUserIdIsNull_shouldReturnError() {
        // given
        user.setId(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("user", USER_ID_IS_REQUIRED);

        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenUserNotExistsById_shouldReturnError() {
        // given
        final Long userId = user.getId();

        final Map<String, String> expected = new HashMap<>();
        expected.put("user", "User with id " + userId + " was not found.");

        when(userService.existsById(userId)).thenReturn(false);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(userId);
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenTableIdIsNull_shouldReturnError() {
        // given
        table.setId(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("diningTable", TABLE_ID_IS_REQUIRED);

        when(userService.existsById(user.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
    }

    @Test
    void validateReservation_whenTableNotExists_shouldReturnError() {
        // given
        final Integer tableId = table.getId();

        final Map<String, String> expected = new HashMap<>();
        expected.put("diningTable", "Dining table with id " + tableId + " was not found.");

        when(userService.existsById(user.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
    }

    @Test
    void validateReservation_whenDateIsNull_shouldReturnError() {
        // given
        reservation.setReservationDate(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationDate", FIELD_REQUIRED + DATE_MESSAGE);
        expected.put("reservationTime", "Make sure reservation date and duration are not null.");

        when(userService.existsById(user.getId())).thenReturn(true);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenDateIsInPast_shouldReturnError() {
        // given
        reservation.setReservationDate(LocalDate.now().minusDays(1));

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationDate", DATE_MESSAGE);

        when(userService.existsById(user.getId())).thenReturn(true);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenTimeIsNull_shouldReturnError() {
        // given
        reservation.setReservationTime(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", FIELD_REQUIRED + TIME_MESSAGE);

        when(userService.existsById(user.getId())).thenReturn(true);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenTimeIsInPast_shouldReturnError() {
        // given
        reservation.setReservationDate(LocalDate.now());
        reservation.setReservationTime(LocalTime.now().minusMinutes(1));

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", TIME_MESSAGE + OPENING_HOURS_MESSAGE);

        when(userService.existsById(user.getId())).thenReturn(true);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenTimeIsBeforeOpeningHours_shouldReturnError() {
        // given
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME.minusMinutes(1));

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", OPENING_HOURS_MESSAGE + " Try change reservation time and/or duration.");

        when(userService.existsById(user.getId())).thenReturn(true);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenTimePlusDurationIsAfterClosingHours_shouldReturnError() {
        // given
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(CLOSING_TIME.minusHours(2));
        reservation.setDuration(3);

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", OPENING_HOURS_MESSAGE + " Try change reservation time and/or duration.");

        when(userService.existsById(user.getId())).thenReturn(true);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenDurationIsNull_shouldReturnError() {
        // given
        reservation.setDuration(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", "Make sure reservation date and duration are not null.");
        expected.put("duration", FIELD_REQUIRED + DURATION_MESSAGE);

        when(userService.existsById(user.getId())).thenReturn(true);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void validateReservation_whenDurationLargerThanMaxDuration_shouldReturnError() {
        // given
        reservation.setDuration(MAX_DURATION + 1);

        final Map<String, String> expected = new HashMap<>();
        expected.put("duration", DURATION_MESSAGE);

        when(userService.existsById(user.getId())).thenReturn(true);
        when(tableService.existsById(table.getId())).thenReturn(true);

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userService, times(1)).existsById(user.getId());
        verify(tableService, times(1)).existsById(table.getId());
    }

    @Test
    void whenDateTimeDurationAndSeatsAreValid_shouldReturnEmptyErrorsMap() {
        // when
        final Map<String, String> result = reservationValidator.validateDateTimeDurationAndSeats(reservation);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void whenReservationIsNotColliding_shouldNotThrowException() {
        // given
        when(reservationRepository.findAllByDateAndTableId(reservation.getReservationDate(), reservation.getDiningTable().getId()))
                .thenReturn(Collections.emptyList());

        // when & then
        assertDoesNotThrow(() -> reservationValidator.isTableAvailable(reservation));
    }

    @Test
    void whenReservationsCollide_shouldThrowException() {
        // given
        final int tableId = 123;
        table.setId(tableId);

        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.of(17, 0);

        final Reservation requestedReservation = new Reservation();
        requestedReservation.setReservationDate(tomorrow);
        requestedReservation.setReservationTime(time.plusHours(1));
        requestedReservation.setDiningTable(table);
        requestedReservation.setDuration(3);
        requestedReservation.setUser(user);

        final Reservation existingReservation = new Reservation();
        existingReservation.setReservationDate(tomorrow);
        existingReservation.setReservationTime(time);
        existingReservation.setDiningTable(table);
        existingReservation.setDuration(2);
        existingReservation.setUser(user);

        final List<Reservation> reservations = new ArrayList<>();
        reservations.add(existingReservation);

        when(reservationRepository.findAllByDateAndTableId(tomorrow, tableId)).thenReturn(reservations);

        // when & then
        assertThrows(TableNotAvailableException.class, () -> reservationValidator.isTableAvailable(requestedReservation));
        verify(reservationRepository, times(1)).findAllByDateAndTableId(tomorrow, tableId);
    }
}