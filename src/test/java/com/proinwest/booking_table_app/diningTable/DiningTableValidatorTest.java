package com.proinwest.booking_table_app.diningTable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiningTableValidatorTest {
    @Mock
    DiningTableService tableService;
    @InjectMocks
    DiningTableValidator tableValidator;

    @Test
    void validateTable_whenNumberAndSeatsAreValid_shouldReturnEmptyErrorMap() {
        // given
        final Integer number = MIN_NUMBER + 1;
        final Integer seats = MIN_SEATS + 1;
        final Integer tableId = 1;

        final DiningTable table = new DiningTable();
        table.setId(tableId);
        table.setNumber(number);
        table.setSeats(seats);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        assertTrue(errors.isEmpty());
    }

    @Test
    void validateTable_whenNumberIsNull_shouldReturnError() {
        // given
        final Integer number = null;
        final Integer tableId = 1;

        final DiningTable table = new DiningTable();
        table.setId(tableId);
        table.setNumber(number);
        table.setSeats(4);

        final Map<String, String> expected = new HashMap<>();
        expected.put("number", FIELD_REQUIRED + NUMBER_MESSAGE);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenNumberAlreadyExists_shouldReturnError() {
        // given
        final Integer number = 1;
        final Integer tableId = 1;

        final DiningTable table = new DiningTable();
        table.setId(tableId);
        table.setNumber(number);
        table.setSeats(4);

        final Map<String, String> expected = new HashMap<>();
        expected.put("number", "Table number " + number + " already exists. It should be unique.");

        when(tableService.findNumberByTableId(tableId)).thenReturn(number + 1);
        when(tableService.existsByNumber(number)).thenReturn(true);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenNumberIsLowerThanMin_shouldReturnError() {
        // given
        final Integer number = MIN_NUMBER - 1;
        final Integer tableId = 1;

        final DiningTable table = new DiningTable();
        table.setId(tableId);
        table.setNumber(number);
        table.setSeats(4);

        final Map<String, String> expected = new HashMap<>();
        expected.put("number", NUMBER_MESSAGE);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenNumberIsHigherThanMax_shouldReturnError() {
        // given
        final Integer number = MAX_NUMBER + 1;
        final Integer tableId = 1;

        final DiningTable table = new DiningTable();
        table.setId(tableId);
        table.setNumber(number);
        table.setSeats(4);

        final Map<String, String> expected = new HashMap<>();
        expected.put("number", NUMBER_MESSAGE);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenSeatsIsNull_shouldReturnError() {
        // given
        final Integer seats = null;
        final Integer tableId = 1;

        final DiningTable table = new DiningTable();
        table.setId(tableId);
        table.setSeats(seats);
        table.setNumber(1);

        final Map<String, String> expected = new HashMap<>();
        expected.put("seats", FIELD_REQUIRED + SEATS_MESSAGE);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenSeatsIsLowerThanMin_shouldReturnError() {
        // given
        final Integer seats = MIN_SEATS - 1;
        final Integer tableId = 1;

        final DiningTable table = new DiningTable();
        table.setId(tableId);
        table.setSeats(seats);
        table.setNumber(1);

        final Map<String, String> expected = new HashMap<>();
        expected.put("seats", SEATS_MESSAGE);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenSeatsIsHigherThanMax_shouldReturnError() {
        // given
        final Integer seats = MAX_SEATS + 1;
        final Integer tableId = 1;

        final DiningTable table = new DiningTable();
        table.setId(tableId);
        table.setSeats(seats);
        table.setNumber(1);

        final Map<String, String> expected = new HashMap<>();
        expected.put("seats", SEATS_MESSAGE);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTableId_whenTableIdIsNull_shouldReturnError() {
        // given
        final Integer tableId = null;
        final Map<String, String> errors = new HashMap<>();

        final Map<String, String> expected = new HashMap<>();
        expected.put("diningTable", TABLE_ID_IS_REQUIRED);

        // when
        final Map<String, String> result = tableValidator.validateTableId(tableId, errors);

        // then
        assertEquals(1, result.size());
        assertEquals(expected, result);
    }

    @Test
    void validateTableId_whenTableNotExist_shouldReturnError() {
        // given
        final Integer tableId = 1;
        final Map<String, String> errors = new HashMap<>();

        final Map<String, String> expected = new HashMap<>();
        expected.put("diningTable", "Dining table with id " + tableId + " was not found.");

        when(tableService.existsById(tableId)).thenReturn(false);

        // when
        final Map<String, String> result = tableValidator.validateTableId(tableId, errors);

        // then
        assertEquals(1, result.size());
        assertEquals(expected, result);
    }

    @Test
    void validateTableId_whenTableIsNotActive_shouldReturnError() {
        // given
        final Integer tableId = 1;
        final Map<String, String> errors = new HashMap<>();

        final Map<String, String> expected = new HashMap<>();
        expected.put("diningTable", "Dining table with id " + tableId + " is not active.");

        when(tableService.existsById(tableId)).thenReturn(true);
        when(tableService.isActive(tableId)).thenReturn(false);

        // when
        final Map<String, String> result = tableValidator.validateTableId(tableId, errors);

        // then
        assertEquals(1, result.size());
        assertEquals(expected, result);
    }
}