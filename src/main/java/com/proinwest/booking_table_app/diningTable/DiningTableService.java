package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.exceptions.AlreadyExistsException;
import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.reservation.Reservation;
import com.proinwest.booking_table_app.reservation.ReservationRepository;
import com.proinwest.booking_table_app.reservation.ReservationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiningTableService {
    public static final int MIN_NUMBER = 1;
    public static final int MAX_NUMBER = 100;
    public static final int MIN_SEATS = 1;
    public static final int MAX_SEATS = 50;
    private final DiningTableRepository diningTableRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public DiningTableService(DiningTableRepository diningTableRepository,
                              ReservationRepository reservationRepository,
                              @Lazy ReservationService reservationService)
    {
        this.diningTableRepository = diningTableRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    public List<DiningTable> getAllDiningTables() {
        List<DiningTable> allDiningTables = diningTableRepository.findAll();
        if (allDiningTables.isEmpty()) throw new NotFoundException("There are no dining tables in database.");
        return allDiningTables;
    }

    public DiningTable getDiningTable(Integer id) {
        return diningTableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Table with id " + id + " was not found!"));
    }

    public DiningTable addDiningTable(DiningTable diningTable) {
        existsByNumber(diningTable.getNumber());
        return diningTableRepository.save(diningTable);
    }

    public URI location (DiningTable diningTable) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(diningTable.getId())
                .toUri();
    }

    public Optional<DiningTable> updateDiningTable(Integer id, DiningTable diningTable) {
        existsById(id);
        existsByNumber(diningTable.getNumber());

        return diningTableRepository.findById(id)
                .map(existingDiningTable -> {
                    existingDiningTable.setNumber(diningTable.getNumber());
                    existingDiningTable.setSeats(diningTable.getSeats());

                    return diningTableRepository.save(existingDiningTable);
                });
    }

    public DiningTable partiallyUpdateDiningTable(Integer id, DiningTable diningTable) {
        DiningTable table = diningTableRepository.findById(id)
                .map(updatingTable -> {
                    if (diningTable.getNumber() != null) updatingTable.setNumber(diningTable.getNumber());
                    if (diningTable.getSeats() != null) updatingTable.setSeats(diningTable.getSeats());

                    return updatingTable;
                }).orElseThrow(() -> new NotFoundException("Dining table with id " + id + " was not found."));

        isNumberValid(diningTable.getNumber(), id);
        isSeatsValid(diningTable.getSeats());

        return diningTableRepository.save(table);
    }

    public void deleteDiningTable(Integer id) {
        existsById(id);
        if (!reservationRepository.findAllByDiningTableId(id).isEmpty())
            throw new InvalidInputException("Dining table with " + id + " can not be deleted because it has reservations assigned.");
        diningTableRepository.deleteById(id);
    }

    // dodać sprawdzenie wielkości stolików
    public List<DiningTable> freeTables(Reservation reservation) {
        reservationService.isDateValid(reservation.getReservationDate());
        reservationService.isTimeValid(reservation.getReservationTime(), reservation.getReservationDate(), reservation.getDuration());
        reservationService.isDurationValid(reservation.getDuration());

        Iterable<DiningTable> allDiningTables = getAllDiningTables();
        List<DiningTable> bookedDiningTables = bookedDiningTables(
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration()
        );

        Set<DiningTable> freeTables = new HashSet<>((Collection) allDiningTables);
        bookedDiningTables.forEach(freeTables::remove);

        List<DiningTable> availableTables = freeTables
                .stream()
                .sorted(Comparator.comparingInt(DiningTable::getId))
                .toList();

        if (availableTables.isEmpty()) throw new NotFoundException("No free tables was found according to your requirements.");

        return availableTables;
    }
    public List<DiningTable> bookedDiningTables(LocalDate date, LocalTime time, int duration) {
        return diningTableRepository.BookedTablesByDateTimeAndDuration(date, time, duration);
    }

    public void isNumberValid(Integer number, Integer id) {
        if (number != null) {
            if (number < MIN_NUMBER || number > MAX_NUMBER) {
                throw new InvalidInputException("Table number should be between " + MIN_NUMBER + " and " + MAX_NUMBER + ".");
            }
            if (diningTableRepository.findNumberById(id) != number) existsByNumber(number);
        }
    }

    public void isSeatsValid(Integer seats){
        if (seats != null) {
            if (seats < MIN_SEATS || seats > MAX_SEATS)
                throw new InvalidInputException("Number of seats should be between " + MIN_SEATS + " and " + MAX_SEATS + ".");
        }
    }

    public void existsById(int id) {
        if (!diningTableRepository.existsById(id))
            throw new NotFoundException("Dining table with id " + id + " was not found.");
    }

    public void existsByNumber(int tableNumber) {
        if (diningTableRepository.existsByNumber(tableNumber))
            throw new AlreadyExistsException("Table number " + tableNumber + " already exists. It should be unique.");
    }
}
