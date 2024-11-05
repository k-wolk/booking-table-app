package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.reservation.Reservation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("diningtables")
public class DiningTableController {

    private final DiningTableService diningTableService;

    public DiningTableController(DiningTableService diningTableService) {
        this.diningTableService = diningTableService;
    }

    @GetMapping()
    public ResponseEntity<Iterable<DiningTable>> getAllDiningTables() {
        Iterable<DiningTable> allDiningTables = diningTableService.getAllDiningTables();
        return ResponseEntity.ok(allDiningTables);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiningTable> getDiningTable(@PathVariable Integer id) {
        return diningTableService.getDiningTable(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<DiningTable> addDiningTable(@Valid @RequestBody DiningTable diningTable) {
        DiningTable savedDiningTable = diningTableService.addDiningTable(diningTable);
        return ResponseEntity.created(diningTableService.location(savedDiningTable))
                .body(savedDiningTable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiningTable> updateDiningTable(@Valid @PathVariable Integer id, @RequestBody DiningTable diningTable) {
        return diningTableService.updateDiningTable(id, diningTable)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DiningTable> partiallyUpdateDiningTable(@Valid @PathVariable Integer id, @RequestBody DiningTable diningTable) {
        return diningTableService.partiallyUpdateDiningTable(id, diningTable)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiningTable(@PathVariable Integer id) {
        if (diningTableService.deleteDiningTable(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/availabletables")
    public ResponseEntity<List<DiningTable>> availableDiningTables(@RequestBody Reservation reservation) {
        List<DiningTable> availableDiningTables =  diningTableService.availableDiningTables(reservation);
        return ResponseEntity.ok(availableDiningTables);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        var errors = new HashMap<String, String>();
        exception.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var fieldName = ((FieldError) error).getField();
                    var errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}


