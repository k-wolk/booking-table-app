package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTableService;
import com.proinwest.booking_table_app.exceptions.types.NotFoundException;
import com.proinwest.booking_table_app.exceptions.types.ValidationException;
import com.proinwest.booking_table_app.security.jwt.SecurityUtils;
import com.proinwest.booking_table_app.user.UserDTO;
import com.proinwest.booking_table_app.user.UserService;
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
import java.util.*;

import static com.proinwest.booking_table_app.reservation.ReservationService.DATE_MESSAGE;
import static com.proinwest.booking_table_app.reservation.ReservationService.DURATION_MESSAGE;
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
    @Mock
    private UserService userService;
    @Mock
    private SecurityUtils securityUtils;
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
        final Long id = 111L;

        UserDTO userDTO = new UserDTO(
                reservation.getUser().getId(),
                reservation.getUser().getLogin(),
                reservation.getUser().getFirstName(),
                reservation.getUser().getLastName(),
                reservation.getUser().getEmail(),
                reservation.getUser().getPhoneNumber(),
                reservation.getUser().getRole()
        );

        final ReservationDTO reservationDTO = new ReservationDTO(
                reservation.getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration(),
                userDTO,
                reservation.getDiningTable()
        );

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
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
        final ReservationDTO result = reservationService.createReservation(reservation);

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

        when(reservationRepository.findById(id)).thenReturn(Optional.ofNullable(reservationToUpdate));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

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

//        when(userService.isAdminOrOwner(reservation.getUser().getId())).thenReturn(true);
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.updateReservation(id, reservation));
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    void shouldDeleteReservation() {
        // given
        final Long id = 111L;
        Reservation reservation = Instancio.create(Reservation.class);

        when(reservationRepository.findById(id)).thenReturn(Optional.ofNullable(reservation));

        // when & then
        assertDoesNotThrow(() -> reservationService.cancelReservation(id));
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, times(1)).deleteById(id);
    }

    @Test
    void whenReservationNotExists_shouldThrowException() {
        // given
        final Long id = 111L;

        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.cancelReservation(id));
        verify(reservationRepository, times(1)).findById(id);
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
        final List<ReservationDTO> result = reservationService.getUserReservations(userId);

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

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.getUserReservations(userId));
        verify(reservationRepository, times(1)).findAllByUserId(userId);
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
        final List<ReservationDTO> result = reservationService.getAllByDateAndTableId(tomorrow, tableId);

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
        assertThrows(NotFoundException.class, () -> reservationService.getAllByDateAndTableId(tomorrow, tableId));
        verify(diningTableService, times(1)).existsById(tableId);
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