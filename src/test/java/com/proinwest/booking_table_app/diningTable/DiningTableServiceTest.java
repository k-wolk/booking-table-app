package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.exceptions.ValidationException;
import com.proinwest.booking_table_app.reservation.*;
import com.proinwest.booking_table_app.user.User;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
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
    private DiningTableRepository diningTableRepository;
    @Mock
    private DiningTableValidator diningTableValidator;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationDTOMapper reservationDTOMapper;
    @InjectMocks
    private DiningTableService diningTableService;

    @BeforeEach
    void setUp() {
        diningTableService = new DiningTableService(diningTableRepository, null, diningTableValidator);
    }

    @Test
    void shouldReturnAllDiningTables() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);

        final List<DiningTable> diningTables = new ArrayList<>();
        diningTables.add(diningTable);

        when(diningTableRepository.findAll())
                .thenReturn(diningTables);

        // when
        final List<DiningTable> result = diningTableService.getAllDiningTables();

        // then
        assertNotNull(result);
        assertEquals(diningTables, result);
        verify(diningTableRepository, times(1)).findAll();
    }

    @Test
    void whenDiningTableListIsEmpty_shouldThrowException() {
        // given
        when(diningTableRepository.findAll())
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, diningTableService::getAllDiningTables);
    }

    @Test
    void shouldReturnDiningTableById() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);
        final int tableId = diningTable.getId();

        when(diningTableRepository.findById(tableId))
                .thenReturn(Optional.of(diningTable));

        // when
        final DiningTable result = diningTableService.getDiningTable(tableId);

        // then
        assertNotNull(result);
        assertEquals(diningTable, result);
        verify(diningTableRepository, times(1)).findById(tableId);
    }

    @Test
    void whenDiningTableNotFoundById_shouldThrowException() {
        // given
        final int tableId = 1;

        when(diningTableRepository.findById(tableId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> diningTableService.getDiningTable(tableId));
    }

    @Test
    void shouldAddDiningTable() {
        // given
        final DiningTable diningTable = new DiningTable();
        diningTable.setNumber(2);
        diningTable.setSeats(4);

        final DiningTable savedDiningTable = diningTable;
        savedDiningTable.setId(1);

        when(diningTableRepository.save(diningTable))
                .thenReturn(savedDiningTable);

        // when
        final DiningTable result = diningTableService.addDiningTable(diningTable);

        // then
        assertNotNull(result);
        assertEquals(savedDiningTable, result);
        verify(diningTableRepository, times(1)).save(diningTable);
    }

    @Test
    void shouldGenerateCorrectLocationUri() {
        // given
        final DiningTable table = mock(DiningTable.class);
        table.setId(7);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setRequestURI("/diningtables");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(table.getId()).thenReturn(7);

        // when
        final URI result = diningTableService.location(table);

        // then
        final URI expected = URI.create("http://localhost/diningtables/7");
        assertEquals(expected, result);
    }

    @Test
    void shouldUpdateDiningTable() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);
        final int tableId = diningTable.getId();

        when(diningTableRepository.findById(tableId)
                .map(updatingDiningTable -> updateDiningTable(diningTable, updatingDiningTable)))
                .thenReturn(Optional.of(diningTable));
        when(diningTableRepository.save(diningTable)).thenReturn(diningTable);

        // when
        final DiningTable result = diningTableService.updateDiningTable(tableId, diningTable);

        // then
        assertNotNull(result);
        assertEquals(diningTable, result);
        verify(diningTableRepository, times(1)).findById(tableId);
        verify(diningTableRepository, times(1)).save(diningTable);
    }

    @Test
    void whenTableNotFoundById_shouldThrowException() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);
        final int tableId = diningTable.getId();

        when(diningTableRepository.findById(tableId)
                .map(updatingDiningTable -> updateDiningTable(diningTable, updatingDiningTable)))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> diningTableService.updateDiningTable(tableId, diningTable));
    }

    @Test
    void shouldPartiallyUpdateDiningTable() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);
        final int tableId = diningTable.getId();

        when(diningTableRepository.findById(tableId)
                .map(updatingTable -> partiallyUpdateDiningTable(diningTable, updatingTable)))
                .thenReturn(Optional.of(diningTable));
        when(diningTableRepository.save(diningTable))
                .thenReturn(diningTable);

        // when
        final DiningTable result = diningTableService.partiallyUpdateDiningTable(tableId, diningTable);

        // then
        assertNotNull(result);
        assertEquals(diningTable, result);
        verify(diningTableRepository, times(1)).findById(tableId);
        verify(diningTableRepository, times(1)).save(diningTable);
    }

    @Test
    void whenDiningTableHasAssignedReservation_shouldThrowException() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);
        final int tableId = diningTable.getId();

        final User user = Instancio.create(User.class);

        final Reservation reservation = new Reservation();
        reservation.setId(12L);
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(17,0));
        reservation.setDuration(2);
        reservation.setUser(user);
        reservation.setDiningTable(diningTable);

        ReservationService reservationService = new ReservationService(reservationRepository, reservationDTOMapper, null, null);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, reservationService, null);

        when(diningTableService.existsById(tableId)).thenReturn(true);
        when(reservationRepository.findAllByDiningTableId(tableId))
                .thenReturn(List.of(reservation));

        // when & then
        assertThrows(InvalidInputException.class, () -> diningTableService.deleteDiningTable(tableId));
    }

    @Test
    void shouldReturnFreeTablesWhenAvailable() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);

        final User user = Instancio.create(User.class);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(19, 0));
        reservation.setDuration(3);
        reservation.setUser(user);
        reservation.setDiningTable(diningTable);

        final List<DiningTable> allDiningTablesWithMinSeats = new ArrayList<>();
        allDiningTablesWithMinSeats.add(diningTable);

        final List<DiningTable> bookedDiningTables = new ArrayList<>();

        ReservationValidator reservationValidator = new ReservationValidator(null, null, diningTableValidator, null);
        ReservationService reservationService = new ReservationService(null, null, reservationValidator, null);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, reservationService, null);

        when(diningTableRepository.allDiningTablesWithMinSeats(diningTable.getSeats()))
                .thenReturn(allDiningTablesWithMinSeats);
        when(diningTableService.getBookedDiningTables(
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration())
        )
                .thenReturn(bookedDiningTables);

        // when
        final List<DiningTable> result = diningTableService.getFreeTables(reservation);

        // then
        assertNotNull(result);
        assertEquals(allDiningTablesWithMinSeats, result);
    }

    @Test
    void whenAllTablesAreReserved_shouldThrowException() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);
        final User user = Instancio.create(User.class);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(19, 0));
        reservation.setDuration(3);
        reservation.setUser(user);
        reservation.setDiningTable(diningTable);

        final List<DiningTable> allDiningTablesWithMinSeats = new ArrayList<>();
        allDiningTablesWithMinSeats.add(diningTable);

        final List<DiningTable> bookedDiningTables = new ArrayList<>();
        bookedDiningTables.add(diningTable);

        ReservationValidator reservationValidator = new ReservationValidator(null, null, diningTableValidator, null);
        ReservationService reservationService = new ReservationService(null, null, reservationValidator, null);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, reservationService, null);

        when(diningTableRepository.allDiningTablesWithMinSeats(diningTable.getSeats()))
                .thenReturn(allDiningTablesWithMinSeats);
        when(diningTableService.getBookedDiningTables(
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration())
        )
                .thenReturn(bookedDiningTables);

        // when & then
        assertThrows(NotFoundException.class, () -> diningTableService.getFreeTables(reservation));
    }

    @Test
    void shouldReturnAllDiningTableWithMinSeats() {
        // given
        final DiningTable diningTable = new DiningTable();
        diningTable.setId(2);
        diningTable.setNumber(2);
        diningTable.setSeats(6);
        final int minSeats = 6;

        when(diningTableRepository.allDiningTablesWithMinSeats(minSeats))
                .thenReturn(List.of(diningTable));

        // when
        final List<DiningTable> result = diningTableService.getAllDiningTablesWithMinSeats(minSeats);

        // then
        assertNotNull(result);
        assertEquals(List.of(diningTable), result);
        verify(diningTableRepository, times(1)).allDiningTablesWithMinSeats(minSeats);
    }

    @Test
    void whenThereIsNoTableWithMinSeats_shouldThrowException() {
        // given
        final int minSeats = 6;

        when(diningTableRepository.allDiningTablesWithMinSeats(minSeats))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> diningTableService.getAllDiningTablesWithMinSeats(minSeats));
    }

    @Test
    void shouldReturnBookedDiningTables() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);

        final List<DiningTable> diningTables = new ArrayList<>();
        diningTables.add(diningTable);

        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.of(19, 0);
        final int duration = 2;

        when(diningTableRepository.BookedTablesByDateTimeAndDuration(date, time, duration))
                .thenReturn(diningTables);

        // when
        final List<DiningTable> result = diningTableService.getBookedDiningTables(date, time, duration);

        // then
        assertNotNull(result);
        assertEquals(diningTables, result);
    }

    @Test
    void whenIdIsValid_shouldReturnDiningTableNumber() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);
        final Integer tableId = diningTable.getId();
        final Integer tableNumber = diningTable.getNumber();

        when(diningTableRepository.findNumberById(tableId)).thenReturn(tableNumber);

        // when
        final Integer result = diningTableService.findNumberById(tableId);

        // then
        assertNotNull(result);
        assertEquals(tableNumber, result);
    }

    @Test
    void whenIdIsNull_shouldReturnZero() {
        // given
        final Integer tableId = null;

        // when
        final Integer result = diningTableService.findNumberById(tableId);

        // then
        assertEquals(0, result);
    }

    @Test
    void whenDiningTableExistsById_shouldReturnTrue() {
        // given
        final DiningTable diningTable = Instancio.create(DiningTable.class);
        final Integer tableId = diningTable.getId();

        when(diningTableRepository.existsById(tableId)).thenReturn(true);

        // when
        final boolean result = diningTableService.existsById(tableId);

        // then
        assertTrue(result);
    }

    @Test
    void whenDiningTableDoesNotExistsById_shouldThrowException() {
        // given
        final int tableId = 123;

        when(diningTableRepository.existsById(tableId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> diningTableService.deleteDiningTable(tableId));
    }

    @Test
    void whenDiningTableExistByNumber_shouldReturnTrue() {
        // given
        final int tableNumber = 123;

        when(diningTableRepository.existsByNumber(tableNumber)).thenReturn(true);

        // when
        final boolean result = diningTableService.existsByNumber(tableNumber);

        // then
        assertTrue(result);
    }

    @Test
    void whenDiningTableDoesNotExistsByNumber_shouldReturnFalse() {
        // given
        final int tableNumber = 123;

        when(diningTableRepository.existsByNumber(tableNumber)).thenReturn(false);

        // when
        final boolean result = diningTableService.existsByNumber(tableNumber);

        // then
        assertFalse(result);
    }

    @Test
    void whenTableNumberIsNull_shouldThrowException() {
        //given
        final Integer tableId = 1;
        final DiningTable diningTable = new DiningTable();
        diningTable.setSeats(4);

        final Map<String, String> validationMessages = Map.of("number", FIELD_REQUIRED + NUMBER_MESSAGE);

        when(diningTableValidator.validateDiningTable(tableId, diningTable))
                .thenReturn(validationMessages);

        // when
        assertThrows(ValidationException.class, () -> diningTableService.validateDiningTable(tableId, diningTable));
    }

    @Test
    void whenTableIsValid_shouldNotThrowException() {
        // given
        final Integer tableId = 1;
        final DiningTable diningTable = new DiningTable();
        diningTable.setNumber(1);
        diningTable.setSeats(2);

        when(diningTableValidator.validateDiningTable(tableId, diningTable))
                .thenReturn(Collections.emptyMap());

        // when & then
        assertDoesNotThrow(() -> diningTableService.validateDiningTable(tableId, diningTable));
    }
}