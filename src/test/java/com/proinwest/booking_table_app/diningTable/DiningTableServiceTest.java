package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.exceptions.ValidationException;
import com.proinwest.booking_table_app.reservation.*;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiningTableServiceTest {
    @Mock
    private DiningTableRepository tableRepository;
    @Mock
    private DiningTableValidator tableValidator;
    @Mock
    private ReservationService reservationService;
    @InjectMocks
    private DiningTableService tableService;

    @Test
    void shouldReturnAllTables() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);

        final List<DiningTable> tables = new ArrayList<>();
        tables.add(table);

        when(tableRepository.findAll())
                .thenReturn(tables);

        // when
        final List<DiningTable> result = tableService.getAllTables();

        // then
        assertNotNull(result);
        assertEquals(tables, result);
        verify(tableRepository, times(1)).findAll();
    }

    @Test
    void whenTablesListIsEmpty_shouldThrowException() {
        // given
        when(tableRepository.findAll())
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, tableService::getAllTables);
        verify(tableRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnTableById() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final int tableId = table.getId();

        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));

        // when
        final DiningTable result = tableService.getTable(tableId);

        // then
        assertNotNull(result);
        assertEquals(table, result);
        verify(tableRepository, times(1)).findById(tableId);
    }

    @Test
    void whenTableNotFoundById_shouldThrowException() {
        // given
        final int tableId = 1;

        when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.getTable(tableId));
        verify(tableRepository, times(1)).findById(tableId);
    }

    @Test
    void shouldAddTable() {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(2);
        table.setSeats(4);

        final DiningTable savedTable = table;
        savedTable.setId(1);

        when(tableRepository.save(table))
                .thenReturn(savedTable);

        // when
        final DiningTable result = tableService.addTable(table);

        // then
        assertNotNull(result);
        assertEquals(savedTable, result);
        verify(tableRepository, times(1)).save(table);
    }

    @Test
    void shouldGenerateCorrectLocationUri() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        table.setId(7);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setRequestURI("/diningtables");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        final URI expected = URI.create("http://localhost/diningtables/7");

        // when
        final URI result = tableService.location(table);

        // then
        assertEquals(expected, result);
    }

    @Test
    void shouldUpdateTable() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final int tableId = table.getId();

        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(tableRepository.save(table)).thenReturn(table);

        // when
        final DiningTable result = tableService.updateTable(tableId, table);

        // then
        assertNotNull(result);
        assertEquals(table, result);
        verify(tableRepository, times(1)).findById(tableId);
        verify(tableRepository, times(1)).save(table);
    }

    @Test
    void update_whenTableNotFoundById_shouldThrowException() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final int tableId = table.getId();

        when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.updateTable(tableId, table));
        verify(tableRepository, times(1)).findById(tableId);
    }

    @Test
    void shouldPartiallyUpdateTable() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final int tableId = table.getId();

        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(tableRepository.save(table)).thenReturn(table);

        // when
        final DiningTable result = tableService.partiallyUpdateTable(tableId, table);

        // then
        assertNotNull(result);
        assertEquals(table, result);
        verify(tableRepository, times(1)).findById(tableId);
        verify(tableRepository, times(1)).save(table);
    }

    @Test
    void partiallyUpdate_whenTableNotFoundById_shouldThrowException() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final int tableId = table.getId();

        when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.partiallyUpdateTable(tableId, table));
        verify(tableRepository, times(1)).findById(tableId);
    }

    @Test
    void shouldDeleteTable() {
        // given
        final int tableId = 111;

        when(tableService.existsById(tableId)).thenReturn(true);
        when(reservationService.findAllByTableId(tableId)).thenReturn(Collections.emptyList());

        // when & then
        assertDoesNotThrow(() -> tableService.deleteTable(tableId));
        verify(reservationService, times(1)).findAllByTableId(tableId);
        verify(tableRepository, times(1)).deleteById(tableId);
    }

    @Test
    void whenTableNotExists_shouldThrowException() {
        // given
        final int tableId = 111;

        when(tableService.existsById(tableId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.deleteTable(tableId));
        verify(tableRepository, times(1)).existsById(tableId);
    }

    @Test
    void whenTableHasAssignedReservation_shouldThrowException() {
        // given
        final int tableId = 111;
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(tableService.existsById(tableId)).thenReturn(true);
        when(reservationService.findAllByTableId(tableId)).thenReturn(List.of(reservationDTO));

        // when & then
        assertThrows(InvalidInputException.class, () -> tableService.deleteTable(tableId));
    }

    @Test
    void shouldReturnFreeTablesWhenAvailable() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);

        final Reservation reservation = Instancio.create(Reservation.class);
        reservation.setDiningTable(table);

        final List<DiningTable> allTablesWithMinSeats = new ArrayList<>();
        allTablesWithMinSeats.add(table);

        final List<DiningTable> bookedDiningTables = new ArrayList<>();

        when(tableRepository.allTablesWithMinSeats(table.getSeats()))
                .thenReturn(allTablesWithMinSeats);
        when(tableService.getBookedTables(
                        reservation.getReservationDate(),
                        reservation.getReservationTime(),
                        reservation.getDuration()))
                .thenReturn(bookedDiningTables);

        // when
        final List<DiningTable> result = tableService.getFreeTables(reservation);

        // then
        assertNotNull(result);
        assertEquals(allTablesWithMinSeats, result);
        verify(tableRepository, times(1)).allTablesWithMinSeats(table.getSeats());
    }

    @Test
    void whenAllTablesAreReserved_shouldThrowException() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);

        final Reservation reservation = Instancio.create(Reservation.class);
        reservation.setDiningTable(table);

        final List<DiningTable> allTablesWithMinSeats = new ArrayList<>();
        allTablesWithMinSeats.add(table);

        final List<DiningTable> bookedDiningTables = new ArrayList<>();
        bookedDiningTables.add(table);

        when(tableRepository.allTablesWithMinSeats(table.getSeats()))
                .thenReturn(allTablesWithMinSeats);
        when(tableService.getBookedTables(
                        reservation.getReservationDate(),
                        reservation.getReservationTime(),
                        reservation.getDuration()))
                .thenReturn(bookedDiningTables);

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.getFreeTables(reservation));
        verify(tableRepository, times(1)).allTablesWithMinSeats(table.getSeats());
    }

    @Test
    void shouldReturnAllTableWithMinSeats() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final int minSeats = table.getSeats();

        when(tableRepository.allTablesWithMinSeats(minSeats)).thenReturn(List.of(table));

        // when
        final List<DiningTable> result = tableService.getAllTablesWithMinSeats(minSeats);

        // then
        assertNotNull(result);
        assertEquals(List.of(table), result);
        verify(tableRepository, times(1)).allTablesWithMinSeats(minSeats);
    }

    @Test
    void whenThereIsNoTableWithMinSeats_shouldThrowException() {
        // given
        final int minSeats = 6;

        when(tableRepository.allTablesWithMinSeats(minSeats)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.getAllTablesWithMinSeats(minSeats));
        verify(tableRepository, times(1)).allTablesWithMinSeats(minSeats);
    }

    @Test
    void shouldReturnBookedDiningTables() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);

        final List<DiningTable> tables = new ArrayList<>();
        tables.add(table);

        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.of(19, 0);
        final int duration = 2;

        when(tableRepository.bookedTablesByDateTimeAndDuration(date, time, duration))
                .thenReturn(tables);

        // when
        final List<DiningTable> result = tableService.getBookedTables(date, time, duration);

        // then
        assertNotNull(result);
        assertEquals(tables, result);
        verify(tableRepository, times(1)).bookedTablesByDateTimeAndDuration(date, time, duration);
    }

    @Test
    void whenIdIsValid_shouldReturnTableNumber() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final Integer tableId = table.getId();
        final Integer tableNumber = table.getNumber();

        when(tableRepository.findNumberByTableId(tableId)).thenReturn(tableNumber);

        // when
        final Integer result = tableService.findNumberByTableId(tableId);

        // then
        assertNotNull(result);
        assertEquals(tableNumber, result);
        verify(tableRepository, times(1)).findNumberByTableId(tableId);
    }

    @Test
    void whenIdIsNull_shouldReturnZero() {
        // given
        final Integer tableId = null;

        // when
        final Integer result = tableService.findNumberByTableId(tableId);

        // then
        assertEquals(0, result);
    }

    @Test
    void whenTableExistsById_shouldReturnTrue() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final Integer tableId = table.getId();

        when(tableRepository.existsById(tableId)).thenReturn(true);

        // when
        final boolean result = tableService.existsById(tableId);

        // then
        assertTrue(result);
        verify(tableRepository, times(1)).existsById(tableId);
    }

    @Test
    void whenTableDoesNotExistsById_shouldThrowException() {
        // given
        final int tableId = 123;

        when(tableRepository.existsById(tableId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.deleteTable(tableId));
        verify(tableRepository, times(1)).existsById(tableId);
    }

    @Test
    void whenTableExistByNumber_shouldReturnTrue() {
        // given
        final int tableNumber = 123;

        when(tableRepository.existsByNumber(tableNumber)).thenReturn(true);

        // when
        final boolean result = tableService.existsByNumber(tableNumber);

        // then
        assertTrue(result);
        verify(tableRepository, times(1)).existsByNumber(tableNumber);
    }

    @Test
    void whenTableDoesNotExistsByNumber_shouldReturnFalse() {
        // given
        final int tableNumber = 123;

        when(tableRepository.existsByNumber(tableNumber)).thenReturn(false);

        // when
        final boolean result = tableService.existsByNumber(tableNumber);

        // then
        assertFalse(result);
        verify(tableRepository, times(1)).existsByNumber(tableNumber);
    }

    @Test
    void whenTableNumberIsNull_shouldThrowException() {
        //given
        final Integer tableId = 1;
        final DiningTable table = new DiningTable();
        table.setSeats(4);
        table.setNumber(null);

        final Map<String, String> validationMessages = Map.of("number", FIELD_REQUIRED + NUMBER_MESSAGE);

        when(tableValidator.validateTable(table)).thenReturn(validationMessages);

        // when
        assertThrows(ValidationException.class, () -> tableService.validateTable(table));
        verify(tableValidator, times(1)).validateTable(table);
    }

    @Test
    void whenTableIsValid_shouldNotThrowException() {
        // given
        final Integer tableId = 1;
        final DiningTable table = Instancio.create(DiningTable.class);

        when(tableValidator.validateTable(table)).thenReturn(Collections.emptyMap());

        // when & then
        assertDoesNotThrow(() -> tableService.validateTable(table));
        verify(tableValidator, times(1)).validateTable(table);
    }
}