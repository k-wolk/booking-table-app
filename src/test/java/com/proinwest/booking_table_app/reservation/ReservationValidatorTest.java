package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.diningTable.DiningTableValidator;
import com.proinwest.booking_table_app.exceptions.types.TableNotAvailableException;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserValidator;
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

import static com.proinwest.booking_table_app.reservation.ReservationService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationValidatorTest {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserValidator userValidator;
    @Mock
    private DiningTableValidator tableValidator;
    @InjectMocks
    private ReservationValidator reservationValidator;
    private User user;
    private DiningTable table;
    private Reservation reservation;
    private Long userId;
    private Integer tableId;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        userId = user.getId();

        table = Instancio.create(DiningTable.class);
        tableId = table.getId();

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
        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertTrue(result.isEmpty());
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenDateIsNull_shouldReturnError() {
        // given
        reservation.setReservationDate(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationDate", FIELD_REQUIRED + DATE_MESSAGE);
        expected.put("reservationTime", "Make sure reservation date and duration are not null.");

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenDateIsInPast_shouldReturnError() {
        // given
        reservation.setReservationDate(LocalDate.now().minusDays(1));

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationDate", DATE_MESSAGE);

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenTimeIsNull_shouldReturnError() {
        // given
        reservation.setReservationTime(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", FIELD_REQUIRED + TIME_MESSAGE);

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenTimeIsInPast_shouldReturnError() {
        // given
        reservation.setReservationDate(LocalDate.now());
        reservation.setReservationTime(LocalTime.now().minusMinutes(1));

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", TIME_MESSAGE + OPENING_HOURS_MESSAGE);

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenTimeIsBeforeOpeningHours_shouldReturnError() {
        // given
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME.minusMinutes(1));

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", OPENING_HOURS_MESSAGE + " Try change reservation time and/or duration.");

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenTimePlusDurationIsAfterClosingHours_shouldReturnError() {
        // given
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(CLOSING_TIME.minusHours(2));
        reservation.setDuration(3);

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", OPENING_HOURS_MESSAGE + " Try change reservation time and/or duration.");

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenDurationIsNull_shouldReturnError() {
        // given
        reservation.setDuration(null);

        final Map<String, String> expected = new HashMap<>();
        expected.put("reservationTime", "Make sure reservation date and duration are not null.");
        expected.put("duration", FIELD_REQUIRED + DURATION_MESSAGE);

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenDurationIsLessThanMinDuration_shouldReturnError() {
        // given
        reservation.setDuration(MIN_DURATION - 1);

        final Map<String, String> expected = new HashMap<>();
        expected.put("duration", DURATION_MESSAGE);

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenDurationLargerThanMaxDuration_shouldReturnError() {
        // given
        reservation.setDuration(MAX_DURATION + 1);

        final Map<String, String> expected = new HashMap<>();
        expected.put("duration", DURATION_MESSAGE);

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());
        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenUserIdIsNotValid_shouldReturnError() {
        // given
        final Map<String, String> expected = new HashMap<>();
        expected.put("user", "User with id " + userId + " was not found.");

        doAnswer(invocation -> {
            Map<String, String> errors = invocation.getArgument(1);
            errors.put("user", "User with id " + userId + " was not found.");
            return null;
        }).when(userValidator).validateUserId(eq(userId), anyMap());

        when(tableValidator.validateTableId(eq(tableId), anyMap())).thenReturn(Collections.emptyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateReservation_whenTableIdIsNotValid_shouldReturnError() {
        // given
        final Map<String, String> expected = new HashMap<>();
        expected.put("diningTable", "Dining table with id " + tableId + " is not active.");

        when(userValidator.validateUserId(eq(userId), anyMap())).thenReturn(Collections.emptyMap());

        doAnswer(invocation -> {
            Map<String, String> errors = invocation.getArgument(1);
            errors.put("diningTable", "Dining table with id " + tableId + " is not active.");
            return null;
        }).when(tableValidator).validateTableId(eq(tableId), anyMap());

        // when
        final Map<String, String> result = reservationValidator.validateReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(expected, result);
        verify(userValidator, times(1)).validateUserId(eq(userId), anyMap());
        verify(tableValidator, times(1)).validateTableId(eq(tableId), anyMap());
    }

    @Test
    void validateDateTimeDurationAndSeats_whenParamsAreValid_shouldReturnEmptyErrorsMap() {
        // when
        final Map<String, String> result = reservationValidator.validateDateTimeDurationAndSeats(reservation);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void isTableAvailable_whenReservationIsNotColliding_shouldNotThrowException() {
        // given
        when(reservationRepository.findAllByDateAndTableId(reservation.getReservationDate(), tableId))
                .thenReturn(Collections.emptyList());

        // when & then
        assertDoesNotThrow(() -> reservationValidator.isTableAvailable(reservation));
    }

    @Test
    void isTableAvailable_whenReservationsCollide_shouldThrowException() {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.of(17, 0);

        final Reservation requestedReservation = new Reservation();
        requestedReservation.setReservationDate(date);
        requestedReservation.setReservationTime(time.plusHours(1));
        requestedReservation.setDiningTable(table);
        requestedReservation.setDuration(3);
        requestedReservation.setUser(user);

        final Reservation existingReservation = new Reservation();
        existingReservation.setReservationDate(date);
        existingReservation.setReservationTime(time);
        existingReservation.setDiningTable(table);
        existingReservation.setDuration(2);
        existingReservation.setUser(user);

        final List<Reservation> reservations = new ArrayList<>();
        reservations.add(existingReservation);

        when(reservationRepository.findAllByDateAndTableId(date, tableId)).thenReturn(reservations);

        // when & then
        assertThrows(TableNotAvailableException.class, () -> reservationValidator.isTableAvailable(requestedReservation));
        verify(reservationRepository, times(1)).findAllByDateAndTableId(date, tableId);
    }
}