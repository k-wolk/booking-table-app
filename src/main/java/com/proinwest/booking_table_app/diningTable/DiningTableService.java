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
    private final DiningTableRepository diningTableRepository;
    private final ReservationService reservationService;
    private final DiningTableValidator diningTableValidator;

    public DiningTableService(DiningTableRepository diningTableRepository,
                              @Lazy ReservationService reservationService, DiningTableValidator diningTableValidator)
    {
        this.diningTableRepository = diningTableRepository;
        this.reservationService = reservationService;
        this.diningTableValidator = diningTableValidator;
    }

    List<DiningTable> getAllDiningTables() {
        final List<DiningTable> allDiningTables = diningTableRepository.findAll();
        if (allDiningTables.isEmpty()) throw new NotFoundException("There are no dining tables in database.");

        return allDiningTables;
    }

    DiningTable getDiningTable(Integer id) {
        return diningTableRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Table with id " + id + " was not found!"));
    }

    List<DiningTable> getAllDiningTablesWithMinSeats(Integer seats) {
        final List<DiningTable> allDiningTablesWithMinSeats = diningTableRepository.allDiningTablesWithMinSeats(seats);
        if (allDiningTablesWithMinSeats.isEmpty()) throw new NotFoundException("There are no tables with the required number of seats = " + seats + ".");

        return allDiningTablesWithMinSeats;
    }

    DiningTable addDiningTable(DiningTable diningTable) {
        validateDiningTable(diningTable.getId(), diningTable);

        return diningTableRepository.save(diningTable);
    }

    URI location (DiningTable diningTable) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(diningTable.getId())
                .toUri();
    }

    DiningTable updateDiningTable(Integer id, DiningTable diningTable) {
        DiningTable diningTableToUpdate = diningTableRepository.findById(id)
                .map(updatingDiningTable -> updateDiningTable(diningTable, updatingDiningTable))
                .orElseThrow(() -> new NotFoundException("Dining table with id " + id + " was not found."));

        validateDiningTable(id, diningTableToUpdate);

        return diningTableRepository.save(diningTableToUpdate);
    }

    DiningTable partiallyUpdateDiningTable(Integer id, DiningTable diningTable) {

        final DiningTable diningTableToUpdate = diningTableRepository.findById(id)
                .map(updatingTable -> partiallyUpdateDiningTable(diningTable, updatingTable))
                .orElseThrow(() -> new NotFoundException("Dining table with id " + id + " was not found."));

        validateDiningTable(id, diningTableToUpdate);

        return diningTableRepository.save(diningTableToUpdate);
    }

    void deleteDiningTable(Integer id) {
        existsById(id);

        if (!reservationService.findAllByDiningTableId(id).isEmpty())
            throw new InvalidInputException("Dining table with " + id + " can not be deleted because it has reservations assigned.");

        diningTableRepository.deleteById(id);
    }

    List<DiningTable> freeTables(Reservation reservation) {
        reservationService.validateDateTimeDurationAndSeats(reservation);

        final Iterable<DiningTable> allDiningTables = getAllDiningTablesWithMinSeats(reservation.getDiningTable().getSeats());
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

    private List<DiningTable> bookedDiningTables(LocalDate date, LocalTime time, int duration) {
        return diningTableRepository.BookedTablesByDateTimeAndDuration(date, time, duration);
    }

    Integer findNumberById(Integer id) {
        if (id == null) return 0;
        return diningTableRepository.findNumberById(id);
    }

    public boolean existsById(int id) {
        return diningTableRepository.existsById(id);
    }

    boolean existsByNumber(int tableNumber) {
        return diningTableRepository.existsByNumber(tableNumber);
    }

    private void validateDiningTable(Integer id, DiningTable diningTable) {
        Map<String, String> validationMessages = diningTableValidator.validateDiningTable(diningTable, id);
        if (!validationMessages.isEmpty()) throw new ValidationException(validationMessages);
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
