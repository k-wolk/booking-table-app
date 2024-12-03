package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.reservation.Reservation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("diningtables")
public class DiningTableController {

    private final DiningTableService diningTableService;

    public DiningTableController(DiningTableService diningTableService) {
        this.diningTableService = diningTableService;
    }

    @GetMapping()
    public ResponseEntity<List<DiningTable>> getAllDiningTables() {
        List<DiningTable> allDiningTables = diningTableService.getAllDiningTables();
        return ResponseEntity.ok(allDiningTables);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiningTable> getDiningTable(@PathVariable Integer id) {
        return ResponseEntity.ok(diningTableService.getDiningTable(id));
    }

    @PostMapping()
    public ResponseEntity<DiningTable> addDiningTable(@RequestBody DiningTable diningTable) {
        DiningTable savedDiningTable = diningTableService.addDiningTable(diningTable);
        return ResponseEntity.created(diningTableService.location(savedDiningTable))
                .body(savedDiningTable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiningTable> updateDiningTable(@PathVariable Integer id, @RequestBody DiningTable diningTable) {
        DiningTable updatedDiningTable = diningTableService.updateDiningTable(id, diningTable);
        return ResponseEntity.ok(updatedDiningTable);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DiningTable> partiallyUpdateDiningTable(@PathVariable Integer id, @RequestBody DiningTable diningTable) {
        DiningTable updatedDiningTable = diningTableService.partiallyUpdateDiningTable(id, diningTable);
        return ResponseEntity.ok(updatedDiningTable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiningTable(@PathVariable Integer id) {
        diningTableService.deleteDiningTable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/freetables")
    public ResponseEntity<List<DiningTable>> freeTables(@RequestBody Reservation reservation) {
        List<DiningTable> freeTables =  diningTableService.freeTables(reservation);
        return ResponseEntity.ok(freeTables);
    }

}


