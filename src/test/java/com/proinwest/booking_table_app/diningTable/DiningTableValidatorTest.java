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
        final Integer id = 1;

        final DiningTable table = new DiningTable();
        table.setId(id);
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
        final Integer id = 1;

        final DiningTable table = new DiningTable();
        table.setId(id);
        table.setNumber(number);
        table.setSeats(4);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        final Map<String, String> expected = new HashMap<>();
        expected.put("number", FIELD_REQUIRED + NUMBER_MESSAGE);

        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenNumberAlreadyExists_shouldReturnError() {
        // given
        final Integer number = 1;
        final Integer id = 1;

        final DiningTable table = new DiningTable();
        table.setId(id);
        table.setNumber(number);
        table.setSeats(4);

        when(tableService.findNumberByTableId(id)).thenReturn(number + 1);
        when(tableService.existsByNumber(number)).thenReturn(true);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        final Map<String, String> expected = new HashMap<>();
        expected.put("number", "Table number " + number + " already exists. It should be unique.");

        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenNumberIsLowerThanMin_shouldReturnError() {
        // given
        final Integer number = MIN_NUMBER - 1;
        final Integer id = 1;

        final DiningTable table = new DiningTable();
        table.setId(id);
        table.setNumber(number);
        table.setSeats(4);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        final Map<String, String> expected = new HashMap<>();
        expected.put("number", NUMBER_MESSAGE);

        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenNumberIsHigherThanMax_shouldReturnError() {
        // given
        final Integer number = MAX_NUMBER + 1;
        final Integer id = 1;

        final DiningTable table = new DiningTable();
        table.setId(id);
        table.setNumber(number);
        table.setSeats(4);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        final Map<String, String> expected = new HashMap<>();
        expected.put("number", NUMBER_MESSAGE);

        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenSeatsIsNull_shouldReturnError() {
        // given
        final Integer seats = null;
        final Integer id = 1;

        final DiningTable table = new DiningTable();
        table.setId(id);
        table.setSeats(seats);
        table.setNumber(1);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        final Map<String, String> expected = new HashMap<>();
        expected.put("seats", FIELD_REQUIRED + SEATS_MESSAGE);

        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenSeatsIsLowerThanMin_shouldReturnError() {
        // given
        final Integer seats = MIN_SEATS - 1;
        final Integer id = 1;

        final DiningTable table = new DiningTable();
        table.setId(id);
        table.setSeats(seats);
        table.setNumber(1);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        final Map<String, String> expected = new HashMap<>();
        expected.put("seats", SEATS_MESSAGE);

        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }

    @Test
    void validateTable_whenSeatsIsHigherThanMax_shouldReturnError() {
        // given
        final Integer seats = MAX_SEATS + 1;
        final Integer id = 1;

        final DiningTable table = new DiningTable();
        table.setId(id);
        table.setSeats(seats);
        table.setNumber(1);

        // when
        final Map<String, String> errors = tableValidator.validateTable(table);

        // then
        final Map<String, String> expected = new HashMap<>();
        expected.put("seats", SEATS_MESSAGE);

        assertEquals(1, errors.size());
        assertEquals(expected, errors);
    }
}