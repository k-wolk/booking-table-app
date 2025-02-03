package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.exceptions.ValidationException;
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
    public static final String NO_TABLES_IN_DATABASE = "There are no dining tables in database.";
    public static final String NO_FREE_TABLES_WAS_FOUND = "No free tables was found according to your requirements.";
    public static final String TABLE_ID_IS_REQUIRED = "Dining table id is required.";
    private final DiningTableRepository tableRepository;
    private final ReservationService reservationService;
    private final DiningTableValidator tableValidator;

    public DiningTableService(DiningTableRepository tableRepository,
                              @Lazy ReservationService reservationService, DiningTableValidator tableValidator)
    {
        this.tableRepository = tableRepository;
        this.reservationService = reservationService;
        this.tableValidator = tableValidator;
    }

    public List<DiningTable> getAllTables() {
        final List<DiningTable> allTables = tableRepository.findAll();
        if (allTables.isEmpty()) throw new NotFoundException(NO_TABLES_IN_DATABASE);

        return allTables;
    }

    DiningTable getTable(Integer tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table with id " + tableId + " was not found."));
    }

    DiningTable addTable(DiningTable table) {
        validateTable(table);

        return tableRepository.save(table);
    }

    URI location (DiningTable table) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(table.getId())
                .toUri();
    }

    DiningTable updateTable(Integer tableId, DiningTable table) {
        final DiningTable tableToUpdate = tableRepository.findById(tableId)
                .map(updatingTable -> updateTable(table, updatingTable))
                .orElseThrow(() -> new NotFoundException("Table with id " + tableId + " was not found."));

        validateTable(tableToUpdate);

        return tableRepository.save(tableToUpdate);
    }

    DiningTable partiallyUpdateTable(Integer tableId, DiningTable table) {
        final DiningTable tableToUpdate = tableRepository.findById(tableId)
                .map(updatingTable -> partiallyUpdateTable(table, updatingTable))
                .orElseThrow(() -> new NotFoundException("Table with id " + tableId + " was not found."));

        validateTable(tableToUpdate);

        return tableRepository.save(tableToUpdate);
    }

    void deleteTable(Integer tableId) {
        if (!existsById(tableId))
            throw new NotFoundException("Table with " + tableId + " was not found.");

        if (!reservationService.findAllByTableId(tableId).isEmpty())
            throw new InvalidInputException("Table with " + tableId + " can not be deleted because it has at least one reservation assigned.");

        tableRepository.deleteById(tableId);
    }

    List<DiningTable> getFreeTables(Reservation reservation) {
        reservationService.validateDateTimeDurationAndSeats(reservation);

        final Iterable<DiningTable> allTablesWithMinSeats = getAllTablesWithMinSeats(reservation.getDiningTable().getSeats());
        final List<DiningTable> bookedTables = getBookedTables(
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration()
        );

        final Set<DiningTable> freeTables = new HashSet<>((Collection) allTablesWithMinSeats);
        bookedTables.forEach(freeTables::remove);

        final List<DiningTable> availableTables = freeTables
                .stream()
                .sorted(Comparator.comparingInt(DiningTable::getId))
                .toList();

        if (availableTables.isEmpty()) throw new NotFoundException(NO_FREE_TABLES_WAS_FOUND);

        return availableTables;
    }

    List<DiningTable> getAllTablesWithMinSeats(Integer seats) {
        final List<DiningTable> allTablesWithMinSeats = tableRepository.allTablesWithMinSeats(seats);
        if (allTablesWithMinSeats.isEmpty()) throw new NotFoundException("There are no tables with the required number of seats = " + seats + ".");

        return allTablesWithMinSeats;
    }

    List<DiningTable> getBookedTables(LocalDate date, LocalTime time, int duration) {
        return tableRepository.bookedTablesByDateTimeAndDuration(date, time, duration);
    }

    Integer findNumberByTableId(Integer tableId) {
        if (tableId == null) return 0;
        return tableRepository.findNumberByTableId(tableId);
    }

    public boolean existsById(int id) {
        return tableRepository.existsById(id);
    }

    boolean existsByNumber(int tableNumber) {
        return tableRepository.existsByNumber(tableNumber);
    }

    void validateTable(DiningTable table) {
        Map<String, String> validationMessages = tableValidator.validateTable(table);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
    }

    static DiningTable updateTable(DiningTable table, DiningTable updatingTable) {
        updatingTable.setNumber(table.getNumber());
        updatingTable.setSeats(table.getSeats());

        return updatingTable;
    }

    static DiningTable partiallyUpdateTable(DiningTable table, DiningTable updatingTable) {
        if (table.getNumber() != null) updatingTable.setNumber(table.getNumber());
        if (table.getSeats() != null) updatingTable.setSeats(table.getSeats());

        return updatingTable;
    }
}
