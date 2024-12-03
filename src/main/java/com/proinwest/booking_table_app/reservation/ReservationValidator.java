package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTableService;
import com.proinwest.booking_table_app.diningTable.DiningTableValidator;
import com.proinwest.booking_table_app.exceptions.TableNotAvailableException;
import com.proinwest.booking_table_app.user.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.proinwest.booking_table_app.reservation.ReservationService.*;

@Component
public class ReservationValidator {

    private final UserService userService;
    private final DiningTableService diningTableService;
    private final DiningTableValidator diningTableValidator;
    private final ReservationRepository reservationRepository;

    public ReservationValidator(UserService userService, DiningTableService diningTableService, DiningTableValidator diningTableValidator, ReservationRepository reservationRepository) {
        this.userService = userService;
        this.diningTableService = diningTableService;
        this.diningTableValidator = diningTableValidator;
        this.reservationRepository = reservationRepository;
    }

    Map<String, String> validateReservation(Reservation reservation) {
        final Map<String, String> errors = new HashMap<>();

        validateUserId(reservation.getUser().getId(), errors);
        validateDiningTableId(reservation.getDiningTable().getId(), errors);
        validateDate(reservation.getReservationDate(), errors);
        validateTime(reservation.getReservationTime(),
                reservation.getReservationDate(),
                reservation.getDuration(),
                errors);
        validateDuration(reservation.getDuration(), errors);
        isTableAvailable(reservation);

        return errors;
    }

    Map<String, String> validateDateTimeDurationAndSeats(Reservation reservation) {
        final Map<String, String> errors = new HashMap<>();

        validateDate(reservation.getReservationDate(), errors);
        validateTime(reservation.getReservationTime(),
                reservation.getReservationDate(),
                reservation.getDuration(),
                errors);
        validateDuration(reservation.getDuration(), errors);
        diningTableValidator.validateSeats(reservation.getDiningTable().getSeats(), errors);

        return errors;
    }

    private void validateUserId(Long id, Map<String, String> errors) {
        if (id == null) {
            errors.put("user", "User ID is required.");
        } else if (!userService.existsById(id)) {
            errors.put("user", "User with id " + id + " was not found.");
        }
    }

    private void validateDiningTableId(Integer id, Map<String, String> errors) {
        if (id == null) {
            errors.put("diningTable", "Dining table ID is required.");
        } else if (!diningTableService.existsById(id)) {
            errors.put("diningTable", "Dining table with id " + id + " was not found.");
        }

    }

    private void validateDuration(Integer duration, Map<String, String> errors) {
        if (duration == null) {
            errors.put("duration", FIELD_REQUIRED + DURATION_MESSAGE);
        } else if (duration < MIN_DURATION || duration > MAX_DURATION) {
            errors.put("duration", DURATION_MESSAGE);
        }
    }

    private void validateDate(LocalDate date, Map<String, String> errors) {
        if (date == null) {
            errors.put("reservationDate", FIELD_REQUIRED + DATE_MESSAGE);
        } else if (date.isBefore(LocalDate.now())) {
            errors.put("reservationDate", DATE_MESSAGE);
        }
    }

    private void validateTime(LocalTime time, LocalDate date, Integer duration, Map<String, String> errors) {
        if (time == null) {
            errors.put("reservationTime", FIELD_REQUIRED + TIME_MESSAGE);
        } else if (date == null || duration == null) {
            errors.put("reservationTime", "Make sure reservation date and duration are not null.");
        } else if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            errors.put("reservationTime", TIME_MESSAGE + OPENING_HOURS_MESSAGE);
        } else if (time.isBefore(OPENING_TIME) || time.plusHours(duration).isAfter(CLOSING_TIME) || time.plusHours(duration).isBefore(OPENING_TIME)) {
            errors.put("reservationTime", OPENING_HOURS_MESSAGE + " Try change reservation time and/or duration.");
        }
    }

    void isTableAvailable(Reservation reservation) {
        final List<Reservation> allReservationsByDateAndTableId = reservationRepository
                .findAllByReservationDateAndDiningTableId(reservation.getReservationDate(), reservation.getDiningTable().getId());

        allReservationsByDateAndTableId.remove(reservation);

        for (Reservation savedReservation : allReservationsByDateAndTableId) {
            if (isReservationColliding(reservation, savedReservation))
            {
                throw new TableNotAvailableException("Dining table with id " + reservation.getDiningTable().getId() + " is not available at the time.");
            }
        }
    }

    private static boolean isReservationColliding(Reservation requestedReservation, Reservation currentReservation) {

        final LocalDateTime currentReservationBegin = LocalDateTime.of(currentReservation.getReservationDate(), currentReservation.getReservationTime());
        final LocalDateTime currentReservationEnd = currentReservationBegin.plusHours(currentReservation.getDuration());

        final LocalDateTime requestedReservationBegin = LocalDateTime.of(requestedReservation.getReservationDate(), requestedReservation.getReservationTime());
        final LocalDateTime requestedReservationEnd = requestedReservationBegin.plusHours(requestedReservation.getDuration());

        return (requestedReservationBegin.isEqual(currentReservationBegin)) ||
                (requestedReservationBegin.isBefore(currentReservationBegin) && requestedReservationEnd.isAfter(currentReservationBegin)) ||
                (requestedReservationBegin.isAfter(currentReservationBegin) && requestedReservationBegin.isBefore(currentReservationEnd));
    }
}
