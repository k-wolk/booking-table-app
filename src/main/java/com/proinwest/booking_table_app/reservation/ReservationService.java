package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTableService;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.exceptions.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
public class ReservationService {
    static final String FIELD_REQUIRED = "This field is required. ";
    static final LocalTime OPENING_TIME = LocalTime.of(11,0);
    static final LocalTime CLOSING_TIME = LocalTime.of(23,0);
    static final String OPENING_HOURS_MESSAGE = "Our place is open from " + OPENING_TIME + " to " + CLOSING_TIME + ".";
    static final String DATE_MESSAGE = "Reservation date should be present or future.";
    static final String TIME_MESSAGE = "Reservation time should be present of future.";
    static final int MIN_DURATION = 1;
    static final int MAX_DURATION = 6;
    static final String DURATION_MESSAGE = "Duration should be between " + MIN_DURATION + " and " + MAX_DURATION + " hours.";

    private final ReservationRepository reservationRepository;
    private final ReservationDTOMapper reservationDTOMapper;
    private final ReservationValidator reservationValidator;
    private final DiningTableService diningTableService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationDTOMapper reservationDTOMapper,
                              ReservationValidator reservationValidator,
                              DiningTableService diningTableService)
    {
        this.reservationRepository = reservationRepository;
        this.reservationDTOMapper = reservationDTOMapper;
        this.reservationValidator = reservationValidator;
        this.diningTableService = diningTableService;
    }

    List<ReservationDTO> getAllReservation() {
        final Iterable<Reservation> allReservations = reservationRepository.findAll();

        final List<ReservationDTO> allReservationsList = StreamSupport.stream(allReservations.spliterator(), false)
                .map(reservationDTOMapper)
                .toList();

        if (allReservationsList.isEmpty()) throw new NotFoundException("There are no reservations in database.");

        return allReservationsList;
    }

    ReservationDTO getReservation(Long id) {
        return reservationRepository.findById(id)
                .map(reservationDTOMapper)
                .orElseThrow(() -> new NotFoundException("Reservation with id " + id + " was not found."));
    }

    ReservationDTO addReservation(Reservation reservation) {
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

    ReservationDTO updateReservation(Long id, Reservation reservation) {
        final Reservation reservationToUpdate = reservationRepository.findById(id)
                        .map(updatingReservation -> updateReservation(reservation, updatingReservation))
                        .orElseThrow(() -> new NotFoundException("Reservation with id " + id + " was not found."));

        validateReservation(reservationToUpdate);

        final Reservation savedReservation = reservationRepository.save(reservationToUpdate);
        return reservationDTOMapper.apply(savedReservation);
    }

    ReservationDTO partiallyUpdateReservation(final Long id, final Reservation reservation) {
        final Reservation reservationToUpdate = reservationRepository.findById(id)
                .map(updatingReservation -> partiallyUpdateReservation(reservation, updatingReservation))
                .orElseThrow(() -> new NotFoundException("Reservation with id " + id + " was not found."));

        validateReservation(reservationToUpdate);

        final Reservation savedReservation = reservationRepository.save(reservationToUpdate);
        return reservationDTOMapper.apply(savedReservation);
    }

    void deleteReservation(Long id) {
        if (!existsById(id)) throw new NotFoundException("Reservation with id " + id + " was not found.");
        reservationRepository.deleteById(id);
    }

    private boolean existsById(Long id) {
        return reservationRepository.existsById(id);
    }

    List<ReservationDTO> findAllByReservationDate(LocalDate date) {
        final List<ReservationDTO> allByDate = reservationRepository.findAllByReservationDate(date)
                .stream()
                .sorted(Comparator.comparing(Reservation::getReservationTime))
                .map(reservationDTOMapper)
                .toList();

        if (allByDate.isEmpty()) throw new NotFoundException("There is no reservation on date " + date + ".");

        return allByDate;
    }

    public List<ReservationDTO> findAllByUserId(Long id) {
        return reservationRepository.findAllByUserId(id)
                .stream()
                .sorted(Comparator.comparing(Reservation::getId))
                .map(reservationDTOMapper)
                .toList();
    }

    List<ReservationDTO> findAllByUserLogin(String loginFragment) {
        final List<ReservationDTO> allByUserLogin = reservationRepository.findAllByUserLoginContainingIgnoreCase(loginFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserLogin.isEmpty()) throw new NotFoundException("There is no reservation with user's login containing: " + loginFragment);

        return allByUserLogin;
    }

    List<ReservationDTO> findAllByUserFirstName(String firstNameFragment) {
        final List<ReservationDTO> allByUserName = reservationRepository.findAllByUserFirstNameContainingIgnoreCase(firstNameFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserName.isEmpty()) throw new NotFoundException("There is no reservation with user's first name containing: " + firstNameFragment);

        return allByUserName;
    }

    List<ReservationDTO> findAllByUserLastName(String lastNameFragment) {
        final List<ReservationDTO> allByUserLastName = reservationRepository.findAllByUserLastNameContainingIgnoreCase(lastNameFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserLastName.isEmpty()) throw new NotFoundException("There is no reservation with user's last name containing: " + lastNameFragment);

        return allByUserLastName;
    }

    List<ReservationDTO> findAllByUserEmail(String emailFragment) {
        final List<ReservationDTO> allByUserEmail = reservationRepository.findAllByUserEmailContainingIgnoreCase(emailFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserEmail.isEmpty()) throw new NotFoundException("There is no reservation with user's email containing: " + emailFragment);

        return allByUserEmail;
    }

    List<ReservationDTO> findAllByUserPhoneNumber(String phoneNumberFragment) {
        final List<ReservationDTO> allByUserPhoneNumber = reservationRepository.findAllByUserPhoneNumberContaining(phoneNumberFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserPhoneNumber.isEmpty()) throw new NotFoundException("There is no reservation with user's phone number containing: " + phoneNumberFragment);

        return allByUserPhoneNumber;
    }

    List<ReservationDTO> findAllByTableId(Integer id) {
        if (!diningTableService.existsById(id))
            throw new NotFoundException("Dining table with id " + id + " was not found.");

        final List<ReservationDTO> allByTableId = reservationRepository.findAllByDiningTableId(id)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByTableId.isEmpty()) throw new NotFoundException("There is no reservation with table id: " + id);

        return allByTableId;
    }

    List<ReservationDTO> findAllByDateAndTableId(LocalDate date, Integer id) {
        if (!diningTableService.existsById(id))
            throw new NotFoundException("Dining table with id " + id + " was not found.");

        final List<ReservationDTO> allByDateAndTableId = reservationRepository.findAllByReservationDateAndDiningTableId(date, id)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByDateAndTableId.isEmpty()) throw new NotFoundException("There is no reservation on date " + date + " at table with id " + id + ".");

        return allByDateAndTableId;
    }

    List<ReservationDTO> findAllByDateAndTime(LocalDate date, LocalTime time) {
        final List<ReservationDTO> allByDateAndTime = reservationRepository.findAllByReservationDateAndReservationTime(date, time)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByDateAndTime.isEmpty()) throw new NotFoundException("There is no reservation on date " + date + " and time " + time + ".");

        return allByDateAndTime;
    }

    public List<ReservationDTO> findAllByDiningTableId(Integer id) {
        return reservationRepository.findAllByDiningTableId(id)
                .stream()
                .map(reservationDTOMapper)
                .toList();
    }

    private void validateReservation(Reservation reservation) {
        Map<String, String> validationMessages = reservationValidator.validateReservation(reservation);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    public void validateDateTimeDurationAndSeats(Reservation reservation) {
        Map<String, String> validationMessages = reservationValidator.validateDateTimeDurationAndSeats(reservation);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    private static Reservation updateReservation(Reservation reservation, Reservation updatingReservation) {
        updatingReservation.setReservationDate(reservation.getReservationDate());
        updatingReservation.setReservationTime(reservation.getReservationTime());
        updatingReservation.setDuration(reservation.getDuration());
        updatingReservation.setUser(reservation.getUser());
        updatingReservation.setDiningTable(reservation.getDiningTable());

        return updatingReservation;
    }

    private static Reservation partiallyUpdateReservation(Reservation reservation, Reservation updatingReservation) {
        if (reservation.getReservationDate() != null) updatingReservation.setReservationDate(reservation.getReservationDate());
        if (reservation.getReservationTime() != null) updatingReservation.setReservationTime(reservation.getReservationTime());
        if (reservation.getDuration() != null) updatingReservation.setDuration(reservation.getDuration());
        if (reservation.getUser().getId() != null) updatingReservation.setUser(reservation.getUser());
        if (reservation.getDiningTable().getId() != null) updatingReservation.setDiningTable(reservation.getDiningTable());

        return updatingReservation;
    }
}