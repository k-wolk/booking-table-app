package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.reservation.Reservation;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiningTableService {

    private final DiningTableRepository diningTableRepository;

    public DiningTableService(DiningTableRepository diningTableRepository) {
        this.diningTableRepository = diningTableRepository;
    }

    public Iterable<DiningTable> getAllDiningTables() {
        return diningTableRepository.findAll();
    }

    public Optional<DiningTable> getDiningTable(Integer id) {
        return diningTableRepository.findById(id);
    }

    public DiningTable addDiningTable(DiningTable diningTable) {
        return diningTableRepository.save(diningTable);
    }

    public URI location (DiningTable diningTable) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(diningTable.getId())
                .toUri();
    }

    public Optional<DiningTable> updateDiningTable(Integer id, DiningTable diningTable) {
        return diningTableRepository.findById(id)
                .map(existingDiningTable -> {
                    existingDiningTable.setNumber(diningTable.getNumber());
                    existingDiningTable.setSeats(diningTable.getSeats());

                    return diningTableRepository.save(existingDiningTable);
                });
    }

    public Optional<DiningTable> partiallyUpdateDiningTable(Integer id, DiningTable diningTable) {
        return diningTableRepository.findById(id)
                .map(existingDiningTable -> {
                    if (diningTable.getNumber() != null) existingDiningTable.setNumber(diningTable.getNumber());
                    if (diningTable.getSeats() != null) existingDiningTable.setSeats(diningTable.getSeats());

                    return diningTableRepository.save(existingDiningTable);
                });
    }

    public boolean deleteDiningTable(Integer id) {
        if (!diningTableRepository.existsById(id)) {
            return false;
        }

        diningTableRepository.deleteById(id);
        return true;
    }

    public List<DiningTable> availableDiningTables(Reservation reservation) {
        Iterable<DiningTable> allDiningTables = getAllDiningTables();

        List<DiningTable> bookedDiningTables = bookedDiningTables(
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration()
        );

        Set<DiningTable> availableDiningTables = new HashSet<>((Collection) allDiningTables);
        bookedDiningTables.forEach(availableDiningTables::remove);

        return availableDiningTables
                .stream()
                .sorted(Comparator.comparingInt(DiningTable::getId))
                .collect(Collectors.toList());
    }

    public List<DiningTable> bookedDiningTables(LocalDate date, LocalTime time, int duration) {
        return diningTableRepository.BookedTablesByDateTimeAndDuration(date, time, duration);
    }
}
