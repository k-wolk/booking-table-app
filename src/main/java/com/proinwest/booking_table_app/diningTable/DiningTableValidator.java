package com.proinwest.booking_table_app.diningTable;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.*;

@Component
public class DiningTableValidator {
    private final DiningTableService diningTableService;

    public DiningTableValidator(@Lazy DiningTableService diningTableService) {
        this.diningTableService = diningTableService;
    }

    Map<String, String> validateDiningTable(Integer id, DiningTable diningTable) {
        final Map<String, String> errors = new HashMap<>();
        
        validateNumber(diningTable.getNumber(), errors, id);
        validateSeats(diningTable.getSeats(), errors);
        
        return errors;
    }

    private void validateNumber(Integer number, Map<String, String> errors, Integer id) {
        if (number == null) {
            errors.put("number", FIELD_REQUIRED + NUMBER_MESSAGE);
        } else if (!diningTableService.findNumberById(id).equals(number) && diningTableService.existsByNumber(number)) {
            errors.put("number", "Table number " + number + " already exists. It should be unique.");
        } else if (number < MIN_NUMBER || number > MAX_NUMBER) {
            errors.put("number", NUMBER_MESSAGE);
        }
    }

    public void validateSeats(Integer seats, Map<String, String> errors){
        if (seats == null) {
            errors.put("seats", FIELD_REQUIRED + SEATS_MESSAGE);
        } else if (seats < MIN_SEATS || seats > MAX_SEATS) {
            errors.put("seats", SEATS_MESSAGE);
        }
    }
}
