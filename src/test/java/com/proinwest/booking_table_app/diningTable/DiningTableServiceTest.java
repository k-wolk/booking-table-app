package com.proinwest.booking_table_app.diningTable;

import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.exceptions.ValidationException;
import com.proinwest.booking_table_app.reservation.*;
import com.proinwest.booking_table_app.user.User;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiningTableServiceTest {

    @Test
    void shouldReturnAllDiningTables() {
        // given
        DiningTable diningTable = Instancio.create(DiningTable.class);
        List<DiningTable> diningTables = new ArrayList<>();
        diningTables.add(diningTable);

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, null);

        when(diningTableRepository.findAll())
                .thenReturn(diningTables);

        // when
        List<DiningTable> result = diningTableService.getAllDiningTables();

        // then
        verify(diningTableRepository, times(1)).findAll();
        assertNotNull(result);
        assertEquals(diningTables, result);
    }

    @Test
    void whenDiningTableListIsEmpty_shouldThrowException(){
        // given
        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, null);

        when(diningTableRepository.findAll())
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, diningTableService::getAllDiningTables);
    }

    @Test
    void shouldReturnDiningTableById() {
        // given
        DiningTable diningTable = Instancio.create(DiningTable.class);
        int tableId = diningTable.getId();

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, null);

        when(diningTableRepository.findById(tableId))
                .thenReturn(Optional.of(diningTable));

        // when
        DiningTable result = diningTableService.getDiningTable(tableId);

        // then
        verify(diningTableRepository, times(1)).findById(tableId);
        assertNotNull(result);
        assertEquals(diningTable, result);
    }

    @Test
    void whenDiningTableNotFoundById_shouldThrowException() {
        // given
        int tableId = 1;

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, null);

        when(diningTableRepository.findById(tableId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> diningTableService.getDiningTable(tableId));
    }

    @Test
    void shouldAddDiningTable() {
        // given
        DiningTable diningTable = new DiningTable();
        diningTable.setNumber(2);
        diningTable.setSeats(4);

        new DiningTable();
        DiningTable savedDiningTable;
        savedDiningTable = diningTable;
        savedDiningTable.setId(1);

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableValidator diningTableValidator = mock(DiningTableValidator.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, diningTableValidator);

        when(diningTableRepository.save(diningTable))
                .thenReturn(savedDiningTable);

        // when
        DiningTable result = diningTableService.addDiningTable(diningTable);

        // then
        verify(diningTableRepository, times(1)).save(diningTable);
        assertNotNull(result);
        assertEquals(savedDiningTable, result);
    }

    @Test
    void shouldUpdateDiningTable() {
        // given
        DiningTable diningTable = Instancio.create(DiningTable.class);
        int tableId = diningTable.getId();

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableValidator diningTableValidator = mock(DiningTableValidator.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, diningTableValidator);

        when(diningTableRepository.findById(tableId)
                        .map(updatingDiningTable -> updateDiningTable(diningTable, updatingDiningTable)))
                .thenReturn(Optional.of(diningTable));
        when(diningTableRepository.save(diningTable))
                .thenReturn(diningTable);

                                                    // todo: ask Arek anything (AAA): should I mock void method validateDiningTable by doNothing? I've tried:
//        Mockito.doNothing().when(diningTableValidator.validateDiningTable(tableId, diningTable));
                                                    // todo: but it doesn't work. Without it test pass.

        // when
        DiningTable result = diningTableService.updateDiningTable(tableId, diningTable);

        // then
        verify(diningTableRepository, times(1)).findById(tableId);
        verify(diningTableRepository, times(1)).save(diningTable);

        assertNotNull(result);
        assertEquals(diningTable, result);
    }

    @Test
    void whenTableNotFoundById_shouldThrowException() {
        // given
        DiningTable diningTable = Instancio.create(DiningTable.class);
        int tableId = diningTable.getId();

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, null);

        when(diningTableRepository.findById(tableId)
                .map(updatingDiningTable -> updateDiningTable(diningTable, updatingDiningTable)))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> diningTableService.updateDiningTable(tableId, diningTable));
    }

    @Test
    void shouldPartiallyUpdateDiningTable() {
        // given
        DiningTable diningTable = Instancio.create(DiningTable.class);
        int tableId = diningTable.getId();

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableValidator diningTableValidator = mock(DiningTableValidator.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, diningTableValidator);

        when(diningTableRepository.findById(tableId)
                        .map(updatingTable -> partiallyUpdateDiningTable(diningTable, updatingTable)))
                .thenReturn(Optional.of(diningTable));
        when(diningTableRepository.save(diningTable))
                .thenReturn(diningTable);

        // when
        DiningTable result = diningTableService.partiallyUpdateDiningTable(tableId, diningTable);

        // then
        verify(diningTableRepository, times(1)).findById(tableId);
        verify(diningTableRepository, times(1)).save(diningTable);

        assertNotNull(result);
        assertEquals(diningTable, result);
    }

    @Test
    void whenDiningTableDoesNotExists_shouldThrowException() {
        // given
        int tableId = 123;

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, null);

        when(diningTableRepository.existsById(tableId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> diningTableService.deleteDiningTable(tableId));
    }

    @Test
    void whenDiningTableHasAssignedReservation_shouldThrowException() {
        // given
        DiningTable diningTable = Instancio.create(DiningTable.class);
        int tableId = diningTable.getId();
        User user = Instancio.create(User.class);

        Reservation reservation = new Reservation();
        reservation.setId(12L);
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(17,0));
        reservation.setDuration(2);
        reservation.setUser(user);
        reservation.setDiningTable(diningTable);

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ReservationDTOMapper reservationDTOMapper = mock(ReservationDTOMapper.class);
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
        DiningTable diningTable = Instancio.create(DiningTable.class);
        User user = Instancio.create(User.class);

        Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(19, 0));
        reservation.setDuration(3);
        reservation.setUser(user);
        reservation.setDiningTable(diningTable);

        List<DiningTable> allDiningTablesWithMinSeats = new ArrayList<>();
        allDiningTablesWithMinSeats.add(diningTable);

        List<DiningTable> bookedDiningTables = new ArrayList<>();

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableValidator diningTableValidator = mock(DiningTableValidator.class);
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
        List<DiningTable> result = diningTableService.getFreeTables(reservation);

        // then
        assertNotNull(result);
        assertEquals(allDiningTablesWithMinSeats, result);
    }

    @Test
    void whenAllTablesReserved_shouldThrowException() {
        // given
        DiningTable diningTable = Instancio.create(DiningTable.class);
        User user = Instancio.create(User.class);

        Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(19, 0));
        reservation.setDuration(3);
        reservation.setUser(user);
        reservation.setDiningTable(diningTable);

        List<DiningTable> allDiningTablesWithMinSeats = new ArrayList<>();
        allDiningTablesWithMinSeats.add(diningTable);

        List<DiningTable> bookedDiningTables = new ArrayList<>();
        bookedDiningTables.add(diningTable);

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableValidator diningTableValidator = mock(DiningTableValidator.class);
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
        DiningTable diningTable = new DiningTable();
        diningTable.setId(2);
        diningTable.setNumber(2);
        diningTable.setSeats(6);
        int minSeats = 6;

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, null);

        when(diningTableRepository.allDiningTablesWithMinSeats(minSeats))
                .thenReturn(List.of(diningTable));

        // when
        List<DiningTable> result = diningTableService.getAllDiningTablesWithMinSeats(minSeats);

        // then
        verify(diningTableRepository, times(1)).allDiningTablesWithMinSeats(minSeats);
        assertNotNull(result);
        assertEquals(List.of(diningTable), result);
    }

    @Test
    void whenThereIsNoTableWithMinSeats_shouldThrowException() {
        // given
        int minSeats = 6;

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, null);

        when(diningTableRepository.allDiningTablesWithMinSeats(minSeats))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> diningTableService.getAllDiningTablesWithMinSeats(minSeats));
    }

    @Test
    void shouldReturnBookedDiningTables() {
        // given
        DiningTable diningTable = Instancio.create(DiningTable.class);

        List<DiningTable> diningTables = new ArrayList<>();
        diningTables.add(diningTable);

        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(19, 0);
        int duration = 2;

        DiningTableRepository diningTableRepository = mock(DiningTableRepository.class);
        DiningTableService diningTableService = new DiningTableService(diningTableRepository, null, null);

        when(diningTableRepository.BookedTablesByDateTimeAndDuration(date, time, duration))
                .thenReturn(diningTables);

        // when
        List<DiningTable> result = diningTableService.getBookedDiningTables(date, time, duration);

        // then
        assertNotNull(result);
        assertEquals(diningTables, result);
    }

    @Test
    void whenTableNumberIsNull_shouldThrowException() {
        //given
        Integer id = 1;
        DiningTable diningTable = new DiningTable();
        diningTable.setSeats(4);

        Map<String, String> validationMessages = Map.of("number", FIELD_REQUIRED + NUMBER_MESSAGE);

        DiningTableValidator diningTableValidator = mock(DiningTableValidator.class);
        DiningTableService diningTableService = new DiningTableService(null, null, diningTableValidator);

        when(diningTableValidator.validateDiningTable(id, diningTable))
                .thenReturn(validationMessages);

        // when
        assertThrows(ValidationException.class, () -> diningTableService.validateDiningTable(id, diningTable));
    }

    @Test
    void whenTableIsValid_shouldNotThrowException() {
        // given
        Integer id = 1;
        DiningTable diningTable = new DiningTable();
        diningTable.setNumber(1);
        diningTable.setSeats(2);

        DiningTableValidator diningTableValidator = mock(DiningTableValidator.class);
        DiningTableService diningTableService = new DiningTableService(null, null, diningTableValidator);

        when(diningTableValidator.validateDiningTable(id, diningTable))
                .thenReturn(Collections.emptyMap());

        // when & then
        assertDoesNotThrow(() -> diningTableService.validateDiningTable(id, diningTable));
    }
}