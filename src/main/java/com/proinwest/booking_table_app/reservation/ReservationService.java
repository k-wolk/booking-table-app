package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTableRepository;
import com.proinwest.booking_table_app.diningTable.DiningTableService;
import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.exceptions.TableNotAvailableException;
import com.proinwest.booking_table_app.user.UserRepository;
import com.proinwest.booking_table_app.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class ReservationService {

    private static final LocalTime OPENING_TIME = LocalTime.of(11,00);
    private static final LocalTime CLOSING_TIME = LocalTime.of(23,00);
    private static final int MIN_DURATION = 1;
    private static final int MAX_DURATION = 6;

    private final ReservationRepository reservationRepository;
    private final ReservationDTOMapper reservationDTOMapper;
    private final UserRepository userRepository;
    private final UserService userService;
    private final DiningTableRepository diningTableRepository;
    private final DiningTableService diningTableService;

    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              ReservationDTOMapper reservationDTOMapper,
                              UserService userService,
                              DiningTableRepository diningTableRepository,
                              DiningTableService diningTableService)
    {
        this.reservationRepository = reservationRepository;
        this.reservationDTOMapper = reservationDTOMapper;
        this.userRepository = userRepository;
        this.userService = userService;
        this.diningTableRepository = diningTableRepository;
        this.diningTableService = diningTableService;
    }

    public List<ReservationDTO> getAllReservation() {
        Iterable<Reservation> allReservations = reservationRepository.findAll();

        List<ReservationDTO> allReservationsList = StreamSupport.stream(allReservations.spliterator(), false)
                .map(reservationDTOMapper)
                .toList();

        if (allReservationsList.isEmpty()) throw new NotFoundException("There are no reservations in database.");

        return allReservationsList;
    }

    public ReservationDTO getReservation(Long id) {
        return reservationRepository.findById(id)
                .map(reservationDTOMapper)
                .orElseThrow(() -> new NotFoundException("Reservation with id " + id + " was not found."));
    }

    public ReservationDTO addReservation(Reservation reservation) {
        isReservationValid(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationDTOMapper.apply(savedReservation);
    }

    public URI location(Reservation reservation) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();
    }

    public ReservationDTO updateReservation(Long id, Reservation reservation) {
        Reservation reservationToUpdate = reservationRepository.findById(id)
                        .map(updatingReservation -> {
                            updatingReservation.setReservationDate(reservation.getReservationDate());
                            updatingReservation.setReservationTime(reservation.getReservationTime());
                            updatingReservation.setDuration(reservation.getDuration());
                            updatingReservation.setUser(reservation.getUser());
                            updatingReservation.setDiningTable(reservation.getDiningTable());

                            return updatingReservation;
                        }).orElseThrow(() -> new NotFoundException("Reservation with id " + id + " was not found."));

        isReservationValid(reservationToUpdate);

        Reservation savedReservation = reservationRepository.save(reservationToUpdate);
        return reservationDTOMapper.apply(savedReservation);
    }

    public ReservationDTO partiallyUpdateReservation(Long id, Reservation reservation) {
        Reservation reservationToUpdate = reservationRepository.findById(id)
                .map(updatingReservation -> {
                    if (reservation.getUser() != null) updatingReservation.setUser(reservation.getUser());
                    if (reservation.getReservationDate() != null) updatingReservation.setReservationDate(reservation.getReservationDate());
                    if (reservation.getReservationTime() != null) updatingReservation.setReservationTime(reservation.getReservationTime());
                    if (reservation.getDuration() != null) updatingReservation.setDuration(reservation.getDuration());
                    if (reservation.getDiningTable() != null) updatingReservation.setDiningTable(reservation.getDiningTable());

                    return updatingReservation;
                }).orElseThrow(() -> new NotFoundException("Reservation with id " + id + " was not found."));

        isDurationValid(reservationToUpdate.getDuration());
        isReservationValid(reservationToUpdate);

        Reservation savedReservation = reservationRepository.save(reservationToUpdate);
        return reservationDTOMapper.apply(savedReservation);
    }

    public boolean existsById(Long id) {
        return reservationRepository.existsById(id);
    }

    public void deleteReservation(Long id) {
        if (!existsById(id)) throw new NotFoundException("Reservation with id " + id + " was not found.");
        reservationRepository.deleteById(id);
    }

    public List<ReservationDTO> findAllByReservationDate(LocalDate date) {
        List<ReservationDTO> allByDate = reservationRepository.findAllByReservationDate(date)
                .stream()
                .sorted(Comparator.comparing(Reservation::getReservationTime))
                .map(reservationDTOMapper)
                .toList();

        if (allByDate.isEmpty()) throw new NotFoundException("There is no reservation on date " + date + ".");

        return allByDate;
    }

    public List<ReservationDTO> findAllByUserId(Long id) {
        List<ReservationDTO> allByUserId = reservationRepository.findAllByUserId(id)
                .stream()
                .sorted(Comparator.comparing(Reservation::getId))
                .map(reservationDTOMapper)
                .toList();

        if (allByUserId.isEmpty()) throw new NotFoundException("There is no reservation with user's id " + id + ".");

        return allByUserId;
    }

    public List<ReservationDTO> findAllByUserLogin(String loginFragment) {
        List<ReservationDTO> allByUserLogin = reservationRepository.findAllByUserLoginContainingIgnoreCase(loginFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserLogin.isEmpty()) throw new NotFoundException("There is no reservation with user's login containing: " + loginFragment);

        return allByUserLogin;
    }

    public List<ReservationDTO> findAllByUserFirstName(String firstNameFragment) {
        List<ReservationDTO> allByUserName = reservationRepository.findAllByUserFirstNameContainingIgnoreCase(firstNameFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserName.isEmpty()) throw new NotFoundException("There is no reservation with user's first name containing: " + firstNameFragment);

        return allByUserName;
    }

    public List<ReservationDTO> findAllByUserLastName(String lastNameFragment) {
        List<ReservationDTO> allByUserLastName = reservationRepository.findAllByUserLastNameContainingIgnoreCase(lastNameFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserLastName.isEmpty()) throw new NotFoundException("There is no reservation with user's last name containing: " + lastNameFragment);

        return allByUserLastName;
    }

    public List<ReservationDTO> findAllByUserEmail(String emailFragment) {
        List<ReservationDTO> allByUserEmail = reservationRepository.findAllByUserEmailContainingIgnoreCase(emailFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserEmail.isEmpty()) throw new NotFoundException("There is no reservation with user's email containing: " + emailFragment);

        return allByUserEmail;
    }

    public List<ReservationDTO> findAllByUserPhoneNumber(String phoneNumberFragment) {
        List<ReservationDTO> allByUserPhoneNumber = reservationRepository.findAllByUserPhoneNumberContaining(phoneNumberFragment)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByUserPhoneNumber.isEmpty()) throw new NotFoundException("There is no reservation with user's phone number containing: " + phoneNumberFragment);

        return allByUserPhoneNumber;
    }

    public List<ReservationDTO> findAllByTableId(Integer id) {
        diningTableService.existsById(id);
        List<ReservationDTO> allByTableId = reservationRepository.findAllByDiningTableId(id)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByTableId.isEmpty()) throw new NotFoundException("There is no reservation with table id: " + id);

        return allByTableId;
    }

    public List<ReservationDTO> findAllByDateAndTableId(LocalDate date, Integer id) {
        diningTableService.existsById(id);
        List<ReservationDTO> allByDateAndTableId = reservationRepository.findAllByReservationDateAndDiningTableId(date, id)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByDateAndTableId.isEmpty()) throw new NotFoundException("There is no reservation on date " + date + " at table with id " + id + ".");

        return allByDateAndTableId;
    }

    public List<ReservationDTO> findAllByDateAndTime(LocalDate date, LocalTime time) {
        List<ReservationDTO> allByDateAndTime = reservationRepository.findAllByReservationDateAndReservationTime(date, time)
                .stream()
                .map(reservationDTOMapper)
                .toList();

        if (allByDateAndTime.isEmpty()) throw new NotFoundException("There is no reservation on date " + date + " and time " + time + ".");

        return allByDateAndTime;
    }

    public void isReservationValid(Reservation reservation) {
        userService.existsById(reservation.getUser().getId());
        diningTableService.existsById(reservation.getDiningTable().getId());

        isDateValid(reservation.getReservationDate());
        isTimeValid(reservation.getReservationTime(),
                reservation.getReservationDate(),
                reservation.getDuration());
        isTableAvailable(reservation);
    }

    public void isDateValid(LocalDate date) {
        if (date == null) throw new InvalidInputException("Reservation date is required.");
        if (date.isBefore(LocalDate.now())) throw new InvalidInputException("Reservation date should be present or future.");
    }

    public void isTimeValid(LocalTime time, LocalDate date, Integer duration) {
        if (time == null)
            throw new InvalidInputException("Reservation time is required.");

        if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now()))
            throw new InvalidInputException("Reservation time should be future.");

        if (time.isBefore(OPENING_TIME) || time.plusHours(duration).isAfter(CLOSING_TIME) || time.plusHours(duration).isBefore(OPENING_TIME))
            throw new InvalidInputException("Our place is open from " + OPENING_TIME + " to " + CLOSING_TIME + ". Try change reservation time and/or duration.");
    }

    public void isDurationValid(Integer duration) {
        if (duration < MIN_DURATION || duration > MAX_DURATION)
            throw new InvalidInputException("Duration should be between " + MIN_DURATION + " and " + MAX_DURATION + " hours.");
    }

    public void isTableAvailable(Reservation reservation) {
        List<Reservation> allReservationsByDateAndTableId = reservationRepository
                .findAllByReservationDateAndDiningTableId(reservation.getReservationDate(), reservation.getDiningTable().getId());

        allReservationsByDateAndTableId.remove(reservation);

        LocalDateTime newReservationBegin = LocalDateTime.of(reservation.getReservationDate(), reservation.getReservationTime());
        LocalDateTime newReservationEnd = newReservationBegin.plusHours(reservation.getDuration());

        LocalDateTime reservationBegin;
        LocalDateTime reservationEnd;

        for (Reservation savedReservation : allReservationsByDateAndTableId) {

            reservationBegin = LocalDateTime.of(savedReservation.getReservationDate(), savedReservation.getReservationTime());
            reservationEnd = reservationBegin.plusHours(savedReservation.getDuration());

            if ((newReservationBegin.isEqual(reservationBegin)) ||
                    (newReservationBegin.isBefore(reservationBegin) && newReservationEnd.isAfter(reservationBegin)) ||
                    (newReservationBegin.isAfter(reservationBegin) && newReservationBegin.isBefore(reservationEnd)))
            {
                throw new TableNotAvailableException("Dining table with id " + reservation.getDiningTable().getId() + " is not available at the time.");
            }
        }
    }
}