package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTableService;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.exceptions.ValidationException;
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

import static com.proinwest.booking_table_app.reservation.ReservationService.*;
import static com.proinwest.booking_table_app.user.UserService.FIELD_REQUIRED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationDTOMapper reservationDTOMapper;
    @Mock
    private ReservationValidator reservationValidator;
    @Mock
    private DiningTableService diningTableService;
    @InjectMocks
    private ReservationService reservationService;

    @Test
    void shouldGetAllReservation() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        final List<Reservation> allReservations = new ArrayList<>();
        allReservations.add(reservation);

        when(reservationRepository.findAll()).thenReturn(allReservations);
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.getAllReservations();

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1)).findAll();
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenNoReservationWasFound_shouldThrowException() {
        // given
        when(reservationRepository.findAll()).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.getAllReservations());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void shouldGetReservationById() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        final Long id = 111L;
        when(reservationRepository.findById(id)).thenReturn(Optional.ofNullable(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final ReservationDTO result = reservationService.getReservation(id);

        // then
        assertNotNull(result);
        assertEquals(reservationDTO, result);
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenReservationNotFoundById_shouldThrowException() {
        // given
        final Long id = 111L;
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->reservationService.getReservation(id));
        verify(reservationRepository, times(1)).findById(id);
    }

    @Test
    void shouldAddReservation() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final ReservationDTO result = reservationService.addReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(reservationDTO, result);
        verify(reservationRepository, times(1)).save(reservation);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void shouldGenerateCorrectLocationUri() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);
        reservation.setId(7L);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setRequestURI("/reservations");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        final URI expected = URI.create("http://localhost/reservations/7");

        // when
        final URI result = reservationService.location(reservation);

        // then
        assertEquals(expected, result);
    }

    @Test
    void shouldUpdateReservation() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);
        final Reservation reservationToUpdate = Instancio.create(Reservation.class);
        final Reservation savedReservation = reservation;
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservationToUpdate));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationDTOMapper.apply(savedReservation)).thenReturn(reservationDTO);

        // when
        final ReservationDTO result = reservationService.updateReservation(id, reservation);

        // then
        assertNotNull(result);
        assertEquals(reservationDTO, result);
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void updateReservation_whenReservationNotFound_shouldThrowException() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.updateReservation(id, reservation));
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    void shouldPartiallyUpdateReservation() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);
        final Reservation reservationToUpdate = Instancio.create(Reservation.class);
        final Reservation savedReservation = reservation;
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.ofNullable(reservationToUpdate));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final ReservationDTO result = reservationService.partiallyUpdateReservation(id, reservation);

        // then
        assertNotNull(result);
        assertEquals(reservationDTO, result);
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void partiallyUpdateReservation_whenReservationNotFound_shouldThrowException() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.partiallyUpdateReservation(id, reservation));
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    void shouldDeleteReservation() {
        // given
        final Long id = 111L;

        when(reservationRepository.existsById(id)).thenReturn(true);

        // when & then
        assertDoesNotThrow(() -> reservationService.deleteReservation(id));
        verify(reservationRepository, times(1)).existsById(id);
        verify(reservationRepository, times(1)).deleteById(id);
    }

    @Test
    void whenReservationNotExists_shouldThrowException() {
        // given
        final Long id = 111L;

        when(reservationRepository.existsById(id)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.deleteReservation(id));
        verify(reservationRepository, times(1)).existsById(id);
    }

    @Test
    void shouldFindAllReservationsByDate() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);
        when(reservationRepository.findAllByDate(tomorrow))
                .thenReturn(List.of(reservation));

        // when
        final List<ReservationDTO> result = reservationService.findAllByDate(tomorrow);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationDTOMapper, times(1)).apply(reservation);
        verify(reservationRepository, times(1)).findAllByDate(tomorrow);
    }

    @Test
    void whenNoReservationFoundByDate_shouldThrowException() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(reservationRepository.findAllByDate(tomorrow)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByDate(tomorrow));
        verify(reservationRepository, times(1)).findAllByDate(tomorrow);
    }

    @Test
    void shouldFindAllReservationsByUserId() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);
        final Long userId = reservation.getUser().getId();

        when(reservationRepository.findAllByUserId(userId)).thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByUserId(userId);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1)).findAllByUserId(userId);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenNoReservationFoundByUserId_shouldReturnEmptyList() {
        // given
        final Long userId = 111L;

        when(reservationRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

        // when
        final List<ReservationDTO> allByUserId = reservationService.findAllByUserId(userId);
        // then
        assertTrue(allByUserId.isEmpty());
        verify(reservationRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    void shouldFindAllReservationByUserLogin() {
        // given
        final String loginFragment = "any";
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(reservationRepository.findAllByUserLogin(loginFragment)).thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByUserLogin(loginFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1)).findAllByUserLogin(loginFragment);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenNoReservationFoundByLogin_shouldThrowException() {
        // given
        final String loginFragment = "any";

        when(reservationRepository.findAllByUserLogin(loginFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByUserLogin(loginFragment));
        verify(reservationRepository, times(1)).findAllByUserLogin(loginFragment);
    }

    @Test
    void shouldFindAllReservationByUserFirstName() {
        // given
        final String firstNameFragment = "any";
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(reservationRepository.findAllByUserFirstName(firstNameFragment)).thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByUserFirstName(firstNameFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1)).findAllByUserFirstName(firstNameFragment);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenNoReservationFoundByFirstName_shouldThrowException() {
        // given
        final String firstNameFragment = "any";

        when(reservationRepository.findAllByUserFirstName(firstNameFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByUserFirstName(firstNameFragment));
        verify(reservationRepository, times(1)).findAllByUserFirstName(firstNameFragment);
    }

    @Test
    void shouldFindAllReservationByUserLastName() {
        // given
        final String lastNameFragment = "any";
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(reservationRepository.findAllByUserLastName(lastNameFragment)).thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByUserLastName(lastNameFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1)).findAllByUserLastName(lastNameFragment);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenNoReservationFoundByLastName_shouldThrowException() {
        // given
        final String lastNameFragment = "any";

        when(reservationRepository.findAllByUserLastName(lastNameFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByUserLastName(lastNameFragment));
        verify(reservationRepository, times(1)).findAllByUserLastName(lastNameFragment);
    }

    @Test
    void shouldFindAllReservationByUserEmail() {
        // given
        final String emailFragment = "any";
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(reservationRepository.findAllByUserEmail(emailFragment))
                .thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByUserEmail(emailFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1)).findAllByUserEmail(emailFragment);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenNoReservationFoundByEmail_shouldThrowException() {
        // given
        final String emailFragment = "any";

        when(reservationRepository.findAllByUserEmail(emailFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByUserEmail(emailFragment));
        verify(reservationRepository, times(1)).findAllByUserEmail(emailFragment);
    }
    
    @Test
    void shouldFindAllReservationByUserPhoneNumber() {
        // given
        final String phoneNumberFragment = "any";
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(reservationRepository.findAllByUserPhoneNumber(phoneNumberFragment))
                .thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByUserPhoneNumber(phoneNumberFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1))
                .findAllByUserPhoneNumber(phoneNumberFragment);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenNoReservationFoundByPhoneNumber_shouldThrowException() {
        // given
        final String phoneNumberFragment = "any";

        when(reservationRepository.findAllByUserPhoneNumber(phoneNumberFragment))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByUserPhoneNumber(phoneNumberFragment));
        verify(reservationRepository, times(1))
                .findAllByUserPhoneNumber(phoneNumberFragment);
    }

    @Test
    void shouldFindAllReservationByTableId() {
        // given
        final Integer tableId = 111;
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(diningTableService.existsById(tableId)).thenReturn(true);
        when(reservationRepository.findAllByTableId(tableId)).thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByTableId(tableId);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1)).findAllByTableId(tableId);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenTableNotExists_shouldThrowException() {
        // given
        final Integer tableId = 111;

        when(diningTableService.existsById(tableId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByTableId(tableId));
    }

    @Test
    void whenNoReservationFoundByTableId_shouldReturnEmptyList() {
        // given
        final Integer tableId = 111;

        when(diningTableService.existsById(tableId)).thenReturn(true);
        when(reservationRepository.findAllByTableId(tableId)).thenReturn(Collections.emptyList());

        // when
        final List<ReservationDTO> allByTableId = reservationService.findAllByTableId(tableId);

        // then
        assertTrue(allByTableId.isEmpty());
        verify(diningTableService, times(1)).existsById(tableId);
        verify(reservationRepository, times(1)).findAllByTableId(tableId);
    }

    @Test
    void shouldFindAllReservationByDateAndTableId() {
        // given
        final Integer tableId = 111;
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(diningTableService.existsById(tableId)).thenReturn(true);
        when(reservationRepository.findAllByDateAndTableId(tomorrow, tableId)).thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByDateAndTableId(tomorrow, tableId);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(diningTableService, times(1)).existsById(tableId);
        verify(reservationRepository, times(1)).findAllByDateAndTableId(tomorrow, tableId);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void findAllByDateAndTableId_whenTableNotExists_shouldThrowException() {
        // given
        final Integer tableId = 111;
        final LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(diningTableService.existsById(tableId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByDateAndTableId(tomorrow, tableId));
        verify(diningTableService, times(1)).existsById(tableId);
    }

    @Test
    void whenNoReservationFoundByDateAndTableId_shouldThrowException() {
        // given
        final Integer tableId = 111;
        final LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(diningTableService.existsById(tableId)).thenReturn(true);
        when(reservationRepository.findAllByDateAndTableId(tomorrow, tableId)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByDateAndTableId(tomorrow, tableId));
        verify(diningTableService, times(1)).existsById(tableId);
        verify(reservationRepository, times(1)).findAllByDateAndTableId(tomorrow, tableId);
    }

    @Test
    void shouldFindAllReservationByDateAndTime() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.of(17,0);
        final Reservation reservation = Instancio.create(Reservation.class);
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(reservationRepository.findAllByDateAndTime(tomorrow, time))
                .thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByDateAndTime(tomorrow, time);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1)).findAllByDateAndTime(tomorrow, time);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void whenNoReservationFoundByDateAndTime_shouldThrowException() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.of(17,0);

        when(reservationRepository.findAllByDateAndTime(tomorrow, time)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByDateAndTime(tomorrow, time));
        verify(reservationRepository, times(1)).findAllByDateAndTime(tomorrow, time);
    }

    @Test
    void whenReservationIsValid_shouldNotThrowException() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);

        when(reservationValidator.validateReservation(reservation)).thenReturn(Collections.emptyMap());

        // when & then
        assertDoesNotThrow(() -> reservationService.validateReservation(reservation));
        verify(reservationValidator, times(1)).validateReservation(reservation);
    }

    @Test
    void whenReservationIsNotValid_shouldThrowException() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);

        final Map<String, String> validationMessages = new HashMap<>();
        validationMessages.put("duration", FIELD_REQUIRED + DURATION_MESSAGE);

        when(reservationValidator.validateReservation(reservation)).thenReturn(validationMessages);

        // when & then
        assertThrows(ValidationException.class, () -> reservationService.validateReservation(reservation));
        verify(reservationValidator, times(1)).validateReservation(reservation);
    }

    @Test
    void whenDateTimeDurationAndSeatsAreValid_shouldNotThrowException() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);

        when(reservationValidator.validateDateTimeDurationAndSeats(reservation)).thenReturn(Collections.emptyMap());

        // when & then
        assertDoesNotThrow(() -> reservationService.validateDateTimeDurationAndSeats(reservation));
        verify(reservationValidator, times(1)).validateDateTimeDurationAndSeats(reservation);
    }

    @Test
    void whenDateTimeDurationOrSeatsAreNotValid_shouldThrowException() {
        // given
        final Reservation reservation = Instancio.create(Reservation.class);

        final Map<String, String> validationMessages = new HashMap<>();
        validationMessages.put("reservationDate", DATE_MESSAGE);

        when(reservationValidator.validateDateTimeDurationAndSeats(reservation)).thenReturn(validationMessages);

        // when & then
        assertThrows(ValidationException.class, () -> reservationService.validateDateTimeDurationAndSeats(reservation));
        verify(reservationValidator, times(1)).validateDateTimeDurationAndSeats(reservation);
    }
}