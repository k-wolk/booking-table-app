package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.reservation.Reservation;
import org.springframework.http.ResponseEntity;
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
        List<DiningTable> allTables = tableService.getAllTables();
        return ResponseEntity.ok(allTables);
    }

    @GetMapping("/{tableId}")
    public ResponseEntity<DiningTable> getTable(@PathVariable Integer tableId) {
        return ResponseEntity.ok(tableService.getTable(tableId));
    }

    @PostMapping()
    public ResponseEntity<DiningTable> addTable(@RequestBody DiningTable table) {
        DiningTable savedTable = tableService.addTable(table);
        return ResponseEntity.created(tableService.location(savedTable))
                .body(savedTable);
    }

    @PutMapping("/{tableId}")
    public ResponseEntity<DiningTable> updateTable(@PathVariable Integer tableId, @RequestBody DiningTable table) {
        DiningTable updatedTable = tableService.updateTable(tableId, table);
        return ResponseEntity.ok(updatedTable);
    }

    @PatchMapping("/{tableId}")
    public ResponseEntity<DiningTable> partiallyUpdateTable(@PathVariable Integer tableId, @RequestBody DiningTable table) {
        DiningTable updateTable = tableService.partiallyUpdateTable(tableId, table);
        return ResponseEntity.ok(updateTable);
    }

    @DeleteMapping("/{tableId}")
    public ResponseEntity<Void> deleteTable(@PathVariable Integer tableId) {
        tableService.deleteTable(tableId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/freetables")
    public ResponseEntity<List<DiningTable>> freeTables(@RequestBody Reservation reservation) {
        List<DiningTable> freeTables =  tableService.getFreeTables(reservation);
        return ResponseEntity.ok(freeTables);
    }
}


