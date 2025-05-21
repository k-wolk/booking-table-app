package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.exceptions.types.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.types.NotFoundException;
import com.proinwest.booking_table_app.exceptions.types.CustomSecurityException;
import com.proinwest.booking_table_app.exceptions.types.ValidationException;
import com.proinwest.booking_table_app.security.jwt.SecurityUtils;
import com.proinwest.booking_table_app.reservation.Reservation;
import com.proinwest.booking_table_app.reservation.ReservationDTO;
import com.proinwest.booking_table_app.reservation.ReservationService;
import com.proinwest.booking_table_app.user.UserDTO;
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

import static com.proinwest.booking_table_app.diningTable.DiningTableService.FIELD_REQUIRED;
import static com.proinwest.booking_table_app.diningTable.DiningTableService.NUMBER_MESSAGE;
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
    @Mock
    private SecurityUtils securityUtils;
    @InjectMocks
    private DiningTableService tableService;

    @Test
    void shouldReturnAllTables() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        table.setActive(true);

        final List<DiningTable> allTables = new ArrayList<>();
        allTables.add(table);

        when(tableRepository.allTablesOrderByActive())
                .thenReturn(allTables);

        // when
        final List<DiningTable> result = tableService.getAllTables();

        // then
        assertNotNull(result);
        assertEquals(allTables, result);
        verify(tableRepository, times(1)).allTablesOrderByActive();
    }

    @Test
    void getAllTables_whenTablesListIsEmpty_shouldThrowException() {
        // given
        when(tableRepository.allTablesOrderByActive())
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, tableService::getAllTables);
        verify(tableRepository, times(1)).allTablesOrderByActive();
    }

    @Test
    void shouldReturnAllActiveTables() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        table.setActive(true);

        final List<DiningTable> allActiveTables = new ArrayList<>();
        allActiveTables.add(table);

        when(tableRepository.allActiveTables())
                .thenReturn(allActiveTables);

        // when
        final List<DiningTable> result = tableService.getAllActiveTables();

        // then
        assertNotNull(result);
        assertEquals(allActiveTables, result);
        verify(tableRepository, times(1)).allActiveTables();
    }

    @Test
    void getAllActiveTables_whenTablesListIsEmpty_shouldThrowException() {
        // given
        when(tableRepository.allActiveTables())
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, tableService::getAllActiveTables);
        verify(tableRepository, times(1)).allActiveTables();
    }

    @Test
    void shouldReturnTableById() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final int tableId = table.getId();
        table.setActive(true);

        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(securityUtils.isAdmin()).thenReturn(true);

        // when
        final DiningTable result = tableService.getTable(tableId);

        // then
        assertNotNull(result);
        assertEquals(table, result);
        verify(tableRepository, times(1)).findById(tableId);
        verify(securityUtils, times(1)).isAdmin();
    }

    @Test
    void getTable_whenTableNotFoundById_shouldThrowException() {
        // given
        final int tableId = 1;

        when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.getTable(tableId));
        verify(tableRepository, times(1)).findById(tableId);
    }

    @Test
    void getTable_whenTableIsInactive_shouldThrowException() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final Integer tableId = table.getId();
        table.setActive(false);

        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(securityUtils.isAdmin()).thenReturn(false);

        // when & then
        assertThrows(SecurityException.class, () -> tableService.getTable(tableId));
        verify(tableRepository, times(1)).findById(tableId);
        verify(securityUtils, times(1)).isAdmin();
    }

    @Test
    void shouldCreateTable() {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(2);
        table.setSeats(4);

        final DiningTable savedTable = table;
        savedTable.setId(1);

        when(tableRepository.save(table))
                .thenReturn(savedTable);

        // when
        final DiningTable result = tableService.createTable(table);

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
    void shouldDeactivateTable() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final Integer tableId = table.getId();

        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));

        // when
        tableService.deactivateTable(tableId);

        // then
        assertFalse(table.isActive());
        verify(tableRepository, times(1)).save(table);
    }

    @Test
    void deactivateTable_whenTableNotFound_shouldThrowException() {
        // given
        final int tableId = 1;

        when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.deactivateTable(tableId));
        verify(tableRepository, never()).save(any());
    }

    @Test
    void shouldActivateTable() {
        // given
        DiningTable table = Instancio.create(DiningTable.class);
        Integer tableId = table.getId();
        table.setActive(false);

        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));

        // when
        tableService.activateTable(tableId);

        // then
        assertTrue(table.isActive());
        verify(tableRepository, times(1)).findById(tableId);
        verify(tableRepository, times(1)).save(table);
    }

    @Test
    void activateTable_whenTableNotFound_shouldThrowException() {
        // given
        final int tableId = 1;

        when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.activateTable(tableId));
        verify(tableRepository, never()).save(any());
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
    void updateTable_whenTableNotFoundById_shouldThrowException() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final int tableId = table.getId();

        when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.updateTable(tableId, table));
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
    void deleteTable_whenTableNotExists_shouldThrowException() {
        // given
        final int tableId = 111;

        when(tableService.existsById(tableId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.deleteTable(tableId));
        verify(tableRepository, times(1)).existsById(tableId);
    }

    @Test
    void deleteTable_whenTableHasAssignedReservation_shouldThrowException() {
        // given
        final int tableId = 111;
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(tableService.existsById(tableId)).thenReturn(true);
        when(reservationService.findAllByTableId(tableId)).thenReturn(List.of(reservationDTO));

        // when & then
        assertThrows(InvalidInputException.class, () -> tableService.deleteTable(tableId));
    }

    @Test
    void shouldGetAvailableTables() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);

        final Reservation reservation = Instancio.create(Reservation.class);
        reservation.setDiningTable(table);

        final List<DiningTable> allTablesWithMinSeats = new ArrayList<>();
        allTablesWithMinSeats.add(table);

        final List<DiningTable> bookedDiningTables = new ArrayList<>();

        when(tableRepository.allActiveTablesWithMinSeats(table.getSeats()))
                .thenReturn(allTablesWithMinSeats);
        when(tableService.getBookedTables(
                        reservation.getReservationDate(),
                        reservation.getReservationTime(),
                        reservation.getDuration()))
                .thenReturn(bookedDiningTables);

        // when
        final List<DiningTable> result = tableService.getAvailableTables(reservation);

        // then
        assertNotNull(result);
        assertEquals(allTablesWithMinSeats, result);
        verify(tableRepository, times(1)).allActiveTablesWithMinSeats(table.getSeats());
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

        when(tableRepository.allActiveTablesWithMinSeats(table.getSeats()))
                .thenReturn(allTablesWithMinSeats);
        when(tableService.getBookedTables(
                        reservation.getReservationDate(),
                        reservation.getReservationTime(),
                        reservation.getDuration()))
                .thenReturn(bookedDiningTables);

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.getAvailableTables(reservation));
        verify(tableRepository, times(1)).allActiveTablesWithMinSeats(table.getSeats());
    }

    @Test
    void shouldReturnAllTableWithMinSeats() {
        // given
        final DiningTable table = Instancio.create(DiningTable.class);
        final int minSeats = table.getSeats();

        when(tableRepository.allActiveTablesWithMinSeats(minSeats)).thenReturn(List.of(table));

        // when
        final List<DiningTable> result = tableService.getAllActiveTablesWithMinSeats(minSeats);

        // then
        assertNotNull(result);
        assertEquals(List.of(table), result);
        verify(tableRepository, times(1)).allActiveTablesWithMinSeats(minSeats);
    }

    @Test
    void whenThereIsNoTableWithMinSeats_shouldThrowException() {
        // given
        final int minSeats = 6;

        when(tableRepository.allActiveTablesWithMinSeats(minSeats)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> tableService.getAllActiveTablesWithMinSeats(minSeats));
        verify(tableRepository, times(1)).allActiveTablesWithMinSeats(minSeats);
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
    void shouldReturnAvailableTimes_whenReservationExists() {
        // given
        DiningTable table = Instancio.create(DiningTable.class);
        Integer tableId = table.getId();

        UserDTO user = Instancio.create(UserDTO.class);

        LocalDate date = LocalDate.now().plusDays(1);

        List<ReservationDTO> reservations = List.of(
                new ReservationDTO(1L, date, LocalTime.of(12, 0), 1, user, table),
                new ReservationDTO(2L, date, LocalTime.of(14, 0), 2, user, table)
        );

        when(reservationService.getAllByDateAndTableId(date, tableId)).thenReturn(reservations);

        // when
        List<String> availableTimes = tableService.whenTableIsAvailable(tableId, date);

        // then
        assertNotNull(availableTimes);
        assertFalse(availableTimes.isEmpty());
        assertTrue(availableTimes.contains(ReservationService.OPENING_TIME + " - 12:00"));
        assertTrue(availableTimes.contains("13:00 - 14:00"));
        assertTrue(availableTimes.contains("16:00 - " + ReservationService.CLOSING_TIME));
        verify(reservationService, times(1)).getAllByDateAndTableId(date, tableId);
    }

    @Test
    void shouldReturnFullAvailability_whenNoReservationsExist() {
        // given
        int tableId = 1;
        LocalDate date = LocalDate.now().plusDays(1);

        when(reservationService.getAllByDateAndTableId(date, tableId)).thenReturn(Collections.emptyList());

        // when
        List<String> availableTimes = tableService.whenTableIsAvailable(tableId, date);

        // then
        assertNotNull(availableTimes);
        assertEquals(1, availableTimes.size());
        assertEquals(ReservationService.OPENING_TIME + " - " + ReservationService.CLOSING_TIME, availableTimes.get(0));
        verify(reservationService, times(1)).getAllByDateAndTableId(date, tableId);
    }

    @Test
    void whenTableIdIsValid_shouldReturnTableNumber() {
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