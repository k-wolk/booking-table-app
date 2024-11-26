package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.exceptions.AlreadyExistsException;
import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.reservation.Reservation;
import com.proinwest.booking_table_app.reservation.ReservationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class DiningTableService {
    public static final String FIELD_REQUIRED = "This field is required. ";
    public static final int MIN_NUMBER = 1;
    public static final int MAX_NUMBER = 100;
    public static final String NUMBER_MESSAGE = "Table number should be between " + MIN_NUMBER + " and " + MAX_NUMBER + ".";
    public static final int MIN_SEATS = 1;
    public static final int MAX_SEATS = 50;
    public static final String SEATS_MESSAGE = "Number of seats should be between " + MIN_SEATS + " and " + MAX_SEATS + ".";
    private final DiningTableRepository diningTableRepository;
    private final ReservationService reservationService;

    public DiningTableService(DiningTableRepository diningTableRepository,
                              @Lazy ReservationService reservationService)
    {
        this.diningTableRepository = diningTableRepository;
        this.reservationService = reservationService;
    }

    public List<DiningTable> getAllDiningTables() {
        final List<DiningTable> allDiningTables = diningTableRepository.findAll();
        if (allDiningTables.isEmpty()) throw new NotFoundException("There are no dining tables in database.");
        return allDiningTables;
    }

    public DiningTable getDiningTable(Integer id) {
        return diningTableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Table with id " + id + " was not found!"));
    }

    public DiningTable addDiningTable(DiningTable diningTable) {
        existsByNumber(diningTable.getNumber(), diningTable.getId());
        return diningTableRepository.save(diningTable);
    }

    public URI location (DiningTable diningTable) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(diningTable.getId())
                .toUri();
    }

    public DiningTable updateDiningTable(Integer id, DiningTable diningTable) {
        existsByNumber(diningTable.getNumber(), id);

        DiningTable diningTableToUpdate = diningTableRepository.findById(id)
                .map(updatingDiningTable -> updateDiningTable(diningTable, updatingDiningTable))
                .orElseThrow(() -> new NotFoundException("Dining table with id " + id + " was not found."));

        return diningTableRepository.save(diningTableToUpdate);
    }

    public DiningTable partiallyUpdateDiningTable(Integer id, DiningTable diningTable) {
        isNumberValid(diningTable.getNumber(), id);
        isSeatsValid(diningTable.getSeats());

        final DiningTable diningTableToUpdate = diningTableRepository.findById(id)
                .map(updatingTable -> partiallyUpdateDiningTable(diningTable, updatingTable))
                .orElseThrow(() -> new NotFoundException("Dining table with id " + id + " was not found."));

        return diningTableRepository.save(diningTableToUpdate);
    }

    public void deleteDiningTable(Integer id) {
        existsById(id);
        if (!reservationService.findAllByDiningTableId(id).isEmpty())
            throw new InvalidInputException("Dining table with " + id + " can not be deleted because it has reservations assigned.");
        diningTableRepository.deleteById(id);
    }

    // todo: add checking table size
    public List<DiningTable> freeTables(Reservation reservation) {
        reservationService.isDateValid(reservation.getReservationDate());
        reservationService.isTimeValid(reservation.getReservationTime(), reservation.getReservationDate(), reservation.getDuration());
        reservationService.isDurationValid(reservation.getDuration());

        final Iterable<DiningTable> allDiningTables = getAllDiningTables();
        final List<DiningTable> bookedDiningTables = bookedDiningTables(
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration()
        );

        final Set<DiningTable> freeTables = new HashSet<>((Collection) allDiningTables);
        bookedDiningTables.forEach(freeTables::remove);

        final List<DiningTable> availableTables = freeTables
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
                throw new InvalidInputException(NUMBER_MESSAGE);
            }
            if (diningTableRepository.findNumberById(id) != number) existsByNumber(number, id);
        }
    }

    public void isSeatsValid(Integer seats){
        if (seats != null) {
            if (seats < MIN_SEATS || seats > MAX_SEATS)
                throw new InvalidInputException(SEATS_MESSAGE);
        }
    }

    public void existsById(int id) {
        if (!diningTableRepository.existsById(id))
            throw new NotFoundException("Dining table with id " + id + " was not found.");
    }

    public void existsByNumber(int tableNumber, int id) {
        if (diningTableRepository.findById(id).get().getNumber() != tableNumber) {
            if (diningTableRepository.existsByNumber(tableNumber))
                throw new AlreadyExistsException("Table number " + tableNumber + " already exists. It should be unique.");
        }
    }

    private static DiningTable updateDiningTable(DiningTable diningTable, DiningTable updatingDiningTable) {
        updatingDiningTable.setNumber(diningTable.getNumber());
        updatingDiningTable.setSeats(diningTable.getSeats());

        return updatingDiningTable;
    }

    private static DiningTable partiallyUpdateDiningTable(DiningTable diningTable, DiningTable updatingTable) {
        if (diningTable.getNumber() != null) updatingTable.setNumber(diningTable.getNumber());
        if (diningTable.getSeats() != null) updatingTable.setSeats(diningTable.getSeats());

        return updatingTable;
    }
}
