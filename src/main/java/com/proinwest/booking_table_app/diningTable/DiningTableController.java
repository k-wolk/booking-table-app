package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.reservation.Reservation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("tables")
public class DiningTableController {
    private final DiningTableService tableService;

    public DiningTableController(DiningTableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping()
    public ResponseEntity<List<DiningTable>> getAllTables() {
        // todo: tutaj dodać sprawdzanie roli. Utworzyc nową klasę sprawdzającą w pakiecie security
        List<DiningTable> allTables = tableService.getAllTables();
        return ResponseEntity.ok(allTables);
    }

    @GetMapping("/{tableId}")
    public ResponseEntity<DiningTable> getTable(@PathVariable Integer tableId) {
        DiningTable table = tableService.getTable(tableId);
        return ResponseEntity.ok(table);
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiningTable> createTable(@RequestBody DiningTable table) {
        DiningTable savedTable = tableService.createTable(table);
        return ResponseEntity.created(tableService.location(savedTable))
                .body(savedTable);
    }

    @PostMapping("/deactivate/{tableId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateTable(@PathVariable Integer tableId) {
        tableService.deactivateTable(tableId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/activate/{tableId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateTable(@PathVariable Integer tableId) {
        tableService.activateTable(tableId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{tableId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiningTable> updateTable(@PathVariable Integer tableId, @RequestBody DiningTable table) {
        DiningTable updatedTable = tableService.updateTable(tableId, table);
        return ResponseEntity.ok(updatedTable);
    }

    @DeleteMapping("/{tableId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTable(@PathVariable Integer tableId) {
        tableService.deleteTable(tableId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tableId}/available")
    public ResponseEntity<List<String>> availableTimes(@PathVariable Integer tableId,
                                                       @RequestBody Reservation reservation) {
        List<String> availableTimes = tableService.whenTableIsAvailable(tableId, reservation);
        return ResponseEntity.ok(availableTimes);
    }

    @GetMapping("/available")
    public ResponseEntity<List<DiningTable>> freeTables(@RequestBody Reservation reservation) {
        List<DiningTable> freeTables =  tableService.getAvailableTables(reservation);
        return ResponseEntity.ok(freeTables);
    }
}