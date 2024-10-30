package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTableRepository;
import com.proinwest.booking_table_app.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationDTOMapper reservationDTOMapper;
    private final UserRepository userRepository;
    private final DiningTableRepository diningTableRepository;

    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, ReservationDTOMapper reservationDTOMapper, DiningTableRepository diningTableRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationDTOMapper = reservationDTOMapper;
        this.userRepository = userRepository;
        this.diningTableRepository = diningTableRepository;
    }

    public List<ReservationDTO> getAllReservation() {
        Iterable<Reservation> allReservations = reservationRepository.findAll();
        return StreamSupport.stream(allReservations.spliterator(), false)
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public Optional<ReservationDTO> getReservation(Long id) {
        return reservationRepository.findById(id)
                .map(reservationDTOMapper);
    }

    public ReservationDTO addReservation(Reservation reservation) {

        // Check if user exists
        Long userId = reservation.getUser().getId();
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User (id: " + userId + ") was not found!");
        }

        // Check if dining table exists
        int diningTableId = reservation.getDiningTable().getId();
        if (!diningTableRepository.existsById(diningTableId)) {
            throw new IllegalArgumentException("Dining table (id: " + diningTableId + ") was not found!");
        }

        // Check if table is available
        List<Reservation> allReservationsByDateAndTableId = reservationRepository
                .findAllByReservationDateAndDiningTableId(reservation.getReservationDate(), reservation.getDiningTable().getId());

        LocalDateTime startOfNewReservation = LocalDateTime.of(reservation.getReservationDate(), reservation.getReservationTime());
        LocalDateTime endOfNewReservation = startOfNewReservation.plusHours(reservation.getDuration());

        LocalDateTime startOfReservation;
        LocalDateTime endOfReservation;

        for (Reservation savedReservation : allReservationsByDateAndTableId) {

            startOfReservation = LocalDateTime.of(savedReservation.getReservationDate(), savedReservation.getReservationTime());
            endOfReservation = startOfReservation.plusHours(savedReservation.getDuration());

            if ((startOfNewReservation.isEqual(startOfReservation)) ||
                    (startOfNewReservation.isBefore(startOfReservation) && endOfNewReservation.isAfter(startOfReservation)) ||
                    (startOfNewReservation.isAfter(startOfReservation) && startOfNewReservation.isBefore(endOfReservation)))
            {
                throw new IllegalArgumentException("Dining table (id: " + reservation.getDiningTable().getId() + ") is not available.");
            }
        }

        // Save new reservation
        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationDTOMapper.apply(savedReservation);
    }

    public URI location(Reservation reservation) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reservation.getId())
                .toUri();
    }

    public Optional<ReservationDTO> updateReservation(Long id, Reservation reservation) {
        return reservationRepository.findById(id)
                .map(updatingReservation -> {
                    updatingReservation.setReservationDate(reservation.getReservationDate());
                    updatingReservation.setReservationTime(reservation.getReservationTime());
                    updatingReservation.setDuration(reservation.getDuration());
                    updatingReservation.setUser(reservation.getUser());
                    updatingReservation.setDiningTable(reservation.getDiningTable());

                    reservationRepository.save(updatingReservation);
                    return reservationDTOMapper.apply(reservation);
                });
    }

    public Optional<ReservationDTO> partiallyUpdateReservation(Long id, Reservation reservation) {
        return reservationRepository.findById(id)
                .map(updatingReservation -> {
                    if (reservation.getUser() != null) updatingReservation.setUser(reservation.getUser());
                    if (reservation.getReservationDate() != null) updatingReservation.setReservationDate(reservation.getReservationDate());
                    if (reservation.getReservationTime() != null) updatingReservation.setReservationTime(reservation.getReservationTime());
                    if (reservation.getDuration() != null) updatingReservation.setDuration(reservation.getDuration());
                    if (reservation.getDiningTable() != null) updatingReservation.setDiningTable(reservation.getDiningTable());

                    reservationRepository.save(updatingReservation);
                    return reservationDTOMapper.apply(reservation);
                });
    }

    public boolean existsById(Long id) {
        return reservationRepository.existsById(id);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationDTO> findAllByReservationDate(LocalDate date) {
        return reservationRepository.findAllByReservationDate(date)
                .stream()
                .sorted(Comparator.comparing(Reservation::getReservationTime))
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> findAllByUserId(Long id) {
        return reservationRepository.findAllByUserId(id)
                .stream()
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> findAllByUserLogin(String loginFragment) {
        return reservationRepository.findAllByUserLoginContainingIgnoreCase(loginFragment)
                .stream()
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> findAllByUserName(String nameFragment) {
        return reservationRepository.findAllByUserFirstNameContainingIgnoreCase(nameFragment)
                .stream()
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> findAllByUserLastName(String lastNameFragment) {
        return reservationRepository.findAllByUserLastNameContainingIgnoreCase(lastNameFragment)
                .stream()
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> findAllByUserEmail(String emailFragment) {
        return reservationRepository.findAllByUserEmailContainingIgnoreCase(emailFragment)
                .stream()
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> findAllByUserPhoneNumber(String phoneNumberFragment) {
        return reservationRepository.findAllByUserPhoneNumberContaining(phoneNumberFragment)
                .stream()
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> findAllByTableId(Long id) {
        return reservationRepository.findAllByDiningTableId(id)
                .stream()
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> findAllByDateAndTableId(LocalDate date, Integer id) {
        return reservationRepository.findAllByReservationDateAndDiningTableId(date, id)
                .stream()
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> findAllByDateAndTime(LocalDate date, LocalTime time) {
        return reservationRepository.findAllByReservationDateAndReservationTime(date, time)
                .stream()
                .map(reservationDTOMapper)
                .collect(Collectors.toList());
    }
}