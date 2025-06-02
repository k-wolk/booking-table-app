package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTableService;
import com.proinwest.booking_table_app.exceptions.types.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.types.NotFoundException;
import com.proinwest.booking_table_app.exceptions.types.ValidationException;
import com.proinwest.booking_table_app.security.jwt.SecurityUtils;
import com.proinwest.booking_table_app.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class ReservationService {
    static final String FIELD_REQUIRED = "This field is required. ";
    public static final String INPUT_IS_MISSING = "Input is missing.";
    public static final LocalTime OPENING_TIME = LocalTime.of(11,0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(23,0);
    public static final String OPENING_HOURS_MESSAGE = "Our place is open from " + OPENING_TIME + " to " + CLOSING_TIME + ".";
    public static final String DATE_MESSAGE = "Reservation date should be present or future.";
    public static final String TIME_MESSAGE = "Reservation time should be present of future.";
    public static final int MIN_DURATION = 1;
    public static final int MAX_DURATION = 6;
    public static final String DURATION_MESSAGE = "Duration should be between " + MIN_DURATION + " and " + MAX_DURATION + " hours.";
    public static final String NO_RESERVATIONS_IN_DATABASE = "There are no reservations in database.";

    private final ReservationRepository reservationRepository;
    private final ReservationDTOMapper reservationDTOMapper;
    private final ReservationValidator reservationValidator;
    private final DiningTableService diningTableService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationDTOMapper reservationDTOMapper,
                              ReservationValidator reservationValidator,
                              DiningTableService tableService,
                              UserService userService,
                              SecurityUtils securityUtils
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationDTOMapper = reservationDTOMapper;
        this.reservationValidator = reservationValidator;
        this.diningTableService = tableService;
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    List<ReservationDTO> getAllReservations() {
        final Iterable<Reservation> allReservations = reservationRepository.findAll();

        final List<ReservationDTO> allReservationsList = StreamSupport.stream(allReservations.spliterator(), false)
                .sorted(Comparator.comparing(Reservation::getReservationDate))
                .map(reservationDTOMapper)
                .toList();

        if (allReservationsList.isEmpty()) throw new NotFoundException(NO_RESERVATIONS_IN_DATABASE);

        return allReservationsList;
    }

    ReservationDTO getReservation(Long id) {
        final ReservationDTO reservationDTO = reservationRepository.findById(id)
                .map(reservationDTOMapper)
                .orElseThrow(() -> new NotFoundException("Reservation with id " + id + " was not found."));

        securityUtils.isAdminOrOwner(reservationDTO.user().id());

        return reservationDTO;
    }

    ReservationDTO createReservation(Reservation reservation) {
        validateReservation(reservation);

        final Reservation savedReservation = reservationRepository.save(reservation);
        return reservationDTOMapper.apply(savedReservation);
    }

    URI location(Reservation reservation) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();
    }

    ReservationDTO updateReservation(final Long id, final Reservation reservation) {
        final Reservation existingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation with id " + id + " was not found."));

        securityUtils.isAdminOrOwner(existingReservation.getUser().getId());

        final Reservation updatedReservation = updateReservation(reservation, existingReservation);
        validateReservation(updatedReservation);

        final Reservation savedReservation = reservationRepository.save(updatedReservation);
        return reservationDTOMapper.apply(savedReservation);
    }

    void cancelReservation(Long id) {
        final Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation with id " + id + " was not found."));

        securityUtils.isAdminOrOwner(reservation.getUser().getId());

        reservationRepository.deleteById(id);
    }

    List<ReservationDTO> getUserReservations(Long userId) {
        securityUtils.isAdminOrOwner(userId);

        if (!userService.existsById(userId)) throw new NotFoundException("User not found for ID: " + userId + ".");

        final List<ReservationDTO> allByUserId = reservationRepository.findAllByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(Reservation::getReservationDate))
                .map(reservationDTOMapper)
                .toList();

        if (allByUserId.isEmpty())
            throw new NotFoundException("No reservation is assigned to the user with ID: " + userId + ".");

        return allByUserId;
    }

    public List<ReservationDTO> getAllByDateAndTableId(LocalDate date, Integer tableId) {
        if (!diningTableService.existsById(tableId))
            throw new NotFoundException("Table not found for ID: " + tableId + ".");

        final List<ReservationDTO> allByDateAndTableId = reservationRepository.findAllByDateAndTableId(date, tableId)
                .stream()
                .sorted(Comparator.comparing(Reservation::getReservationTime))
                .map(reservationDTOMapper)
                .toList();

        return allByDateAndTableId;
    }

    List<ReservationDTO> requireAllByDateAndTableId(LocalDate date, Integer tableId) {
        final List<ReservationDTO> allByDateAndTableId = getAllByDateAndTableId(date, tableId);
        if (allByDateAndTableId.isEmpty())
            throw new NotFoundException("There is no reservation on " + date + " for the table with ID " +
                    tableId + ".");
        return allByDateAndTableId;
    }

    List<ReservationDTO> findAllByDate(LocalDate date) {
        final List<ReservationDTO> allByDate = reservationRepository.findAllByDate(date)
                .stream()
                .sorted(Comparator.comparing(Reservation::getReservationTime))
                .map(reservationDTOMapper)
                .toList();

        if (allByDate.isEmpty()) throw new NotFoundException("There is no reservation on date " + date + ".");

        return allByDate;
    }

    public List<ReservationDTO> findAllByTableId(Integer tableId) {
        if (!diningTableService.existsById(tableId))
            throw new NotFoundException("Table not found for ID: " + tableId + ".");

        final List<ReservationDTO> allByTableId = reservationRepository.findAllByTableId(tableId)
                .stream()
                .sorted(Comparator.comparing(Reservation::getReservationDate))
                .map(reservationDTOMapper)
                .toList();

        return allByTableId;
    }

    List<ReservationDTO> searchReservations(String query) {
        if (query.isBlank()) throw new InvalidInputException(INPUT_IS_MISSING);

        final String[] terms = query.split("\\s+");
        final Set<ReservationDTO> resultSet = new HashSet<>();

        for (String term : terms) {
            final List<ReservationDTO> reservations = reservationRepository.searchReservations(term)
                    .stream()
                    .map(reservationDTOMapper)
                    .toList();
            resultSet.addAll(reservations);
        }

        if (resultSet.isEmpty())
            throw new NotFoundException("No reservation was found for the query: " + query + ".");

        return new ArrayList<>(resultSet);
    }

    void validateReservation(Reservation reservation) {
        final Map<String, String> validationMessages = reservationValidator.validateReservation(reservation);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    public void validateDateTimeDurationAndSeats(Reservation reservation) {
        final Map<String, String> validationMessages = reservationValidator.validateDateTimeDurationAndSeats(reservation);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    private static Reservation updateReservation(Reservation reservation, Reservation updatingReservation) {
        if (reservation.getReservationDate() != null) updatingReservation.setReservationDate(reservation.getReservationDate());
        if (reservation.getReservationTime() != null) updatingReservation.setReservationTime(reservation.getReservationTime());
        if (reservation.getDuration() != null) updatingReservation.setDuration(reservation.getDuration());
        if (reservation.getUser() != null) updatingReservation.setUser(reservation.getUser());
        if (reservation.getDiningTable() != null) updatingReservation.setDiningTable(reservation.getDiningTable());

        return updatingReservation;
    }
}