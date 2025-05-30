package com.proinwest.booking_table_app.reservation;

import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.diningTable.DiningTableService;
import com.proinwest.booking_table_app.exceptions.types.CustomSecurityException;
import com.proinwest.booking_table_app.exceptions.types.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.types.NotFoundException;
import com.proinwest.booking_table_app.exceptions.types.ValidationException;
import com.proinwest.booking_table_app.security.jwt.SecurityUtils;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserDTO;
import com.proinwest.booking_table_app.user.UserService;
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
import java.util.*;

import static com.proinwest.booking_table_app.reservation.ReservationService.*;
import static com.proinwest.booking_table_app.security.jwt.SecurityUtils.ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER;
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
    private Reservation reservation;
    private ReservationDTO reservationDTO;
    private User user;
    private UserDTO userDTO;
    private DiningTable table;

    @BeforeEach
    void setUp() {
        user = Instancio.create(User.class);
        table = Instancio.create(DiningTable.class);

        userDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isActive()
        );

        reservation = new Reservation();
        reservation.setId(111L);
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME);
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(user);
        reservation.setDiningTable(table);

        reservationDTO = new ReservationDTO(
                reservation.getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration(),
                userDTO,
                table
        );
    }

    @Test
    void getAllReservations_whenExists_shouldFetchAllReservations() {
        // given
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
    void getAllReservations_whenNotExists_shouldThrowException() {
        // given
        when(reservationRepository.findAll()).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.getAllReservations());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void getReservation_whenExists_shouldFetchReservationById() {
        // given
        final Long id = reservation.getId();

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
    void getReservation_whenNotExists_shouldThrowException() {
        // given
        final Long id = reservation.getId();
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->reservationService.getReservation(id));
        verify(reservationRepository, times(1)).findById(id);
    }

    @Test
    void getReservation_whenUserIsNotAdminOrOwner_shouldThrowException() {
        // given
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);
        doThrow(new CustomSecurityException(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER))
                .when(securityUtils).isAdminOrOwner(reservation.getUser().getId());

        // when & then
        assertThrows(CustomSecurityException.class, () -> reservationService.getReservation(id));
        verify(reservationRepository, times(1)).findById(id);
    }

    @Test
    void createReservation_whenParamsAreValid_shouldAddReservation() {
        // given
        when(reservationValidator.validateReservation(reservation)).thenReturn(Collections.emptyMap());
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final ReservationDTO result = reservationService.createReservation(reservation);

        // then
        assertNotNull(result);
        assertEquals(reservationDTO, result);
        verify(reservationValidator, times(1)).validateReservation(reservation);
        verify(reservationRepository, times(1)).save(reservation);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void location_shouldGenerateCorrectLocationUri() {
        // given
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
    void updateReservations_whenParamsAreValid_shouldUpdateReservation() {
        // given
        final Reservation reservationToUpdate = Instancio.create(Reservation.class);
        final Reservation savedReservation = reservation;
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.ofNullable(reservationToUpdate));
        when(reservationValidator.validateReservation(any(Reservation.class))).thenReturn(Collections.emptyMap());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final ReservationDTO result = reservationService.updateReservation(id, reservation);

        // then
        assertNotNull(result);
        assertEquals(reservationDTO, result);
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationValidator, times(1)).validateReservation(any(Reservation.class));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void updateReservation_whenReservationNotExists_shouldThrowException() {
        // given
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.updateReservation(id, reservation));
        verify(reservationRepository, never()).save(reservation);
        verify(securityUtils, never()).isAdminOrOwner(anyLong());
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(reservationDTOMapper, never()).apply(any(Reservation.class));
    }

    @Test
    void updateReservation_whenUserIsNotAdminOrOwner_shouldThrowException() {
        // given
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
        doThrow(new CustomSecurityException(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER))
                .when(securityUtils).isAdminOrOwner(reservation.getUser().getId());

        // when & then
        assertThrows(CustomSecurityException.class, () -> reservationService.updateReservation(id, reservation));
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(reservationDTOMapper, never()).apply(any(Reservation.class));
    }

    @Test
    void cancelReservation_whenExists_shouldDeleteReservation() {
        // given
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));

        // when & then
        assertDoesNotThrow(() -> reservationService.cancelReservation(id));
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, times(1)).deleteById(id);
    }

    @Test
    void cancelReservation_whenReservationNotExists_shouldThrowException() {
        // given
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.cancelReservation(id));
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, never()).deleteById(id);
    }

    @Test
    void cancelReservation_whenUserIsNotAdminOrOwner_shouldThrowException() {
        // given
        final Long id = reservation.getId();

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
        doThrow(new CustomSecurityException(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER))
                .when(securityUtils).isAdminOrOwner(reservation.getUser().getId());

        // when & then
        assertThrows(CustomSecurityException.class, () -> reservationService.cancelReservation(id));
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationRepository, never()).deleteById(id);
    }

    @Test
    void getUserReservations_whenUserExists_shouldReturnUserReservations() {
        // given
        final Long userId = user.getId();

        when(userService.existsById(userId)).thenReturn(true);
        when(reservationRepository.findAllByUserId(userId)).thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.getUserReservations(userId);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(securityUtils, times(1)).isAdminOrOwner(userId);
        verify(userService, times(1)).existsById(userId);
        verify(reservationRepository, times(1)).findAllByUserId(userId);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void getUserReservations_whenUserNotExists_shouldThrowException() {
        // given
        final Long userId = user.getId();

        when(userService.existsById(userId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.getUserReservations(userId));
        verify(securityUtils, times(1)).isAdminOrOwner(userId);
        verify(userService, times(1)).existsById(userId);
        verify(reservationRepository, never()).findAllByUserId(userId);
    }

    @Test
    void getUserReservations_whenUserIsNotAdminOrOwner_shouldThrowException() {
        // given
        final Long userId = user.getId();

        doThrow(new CustomSecurityException(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER))
                .when(securityUtils).isAdminOrOwner(userId);

        // when & then
        assertThrows(CustomSecurityException.class, () -> reservationService.getUserReservations(userId));
        verify(securityUtils, times(1)).isAdminOrOwner(userId);
        verify(userService, never()).existsById(userId);
        verify(reservationRepository, never()).findAllByUserId(userId);
    }

    @Test
    void getUserReservations_whenNoReservationsFound_shouldThrowException() {
        // given
        final Long userId = user.getId();

        when(userService.existsById(userId)).thenReturn(true);
        when(reservationRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        
        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.getUserReservations(userId));
        verify(securityUtils, times(1)).isAdminOrOwner(userId);
        verify(userService, times(1)).existsById(userId);
        verify(reservationRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    void getAllByDateAndTableId_whenExists_shouldReturnListOfReservations() {
        // given
        final Integer tableId = table.getId();
        final LocalDate date = reservation.getReservationDate();

        when(diningTableService.existsById(tableId)).thenReturn(true);
        when(reservationRepository.findAllByDateAndTableId(date, tableId)).thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.getAllByDateAndTableId(date, tableId);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(diningTableService, times(1)).existsById(tableId);
        verify(reservationRepository, times(1)).findAllByDateAndTableId(date, tableId);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void getAllByDateAndTableId_whenTableNotExists_shouldThrowException() {
        // given
        final Integer tableId = 111;
        final LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(diningTableService.existsById(tableId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.getAllByDateAndTableId(tomorrow, tableId));
        verify(diningTableService, times(1)).existsById(tableId);
        verify(reservationRepository, never()).findAllByDateAndTableId(any(LocalDate.class), eq(tableId));
    }

    @Test
    void getAllByDateAndTableId_whenNoReservationsFound_shouldReturnEmptyList() {
        // given
        final Integer tableId = 111;
        final LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(diningTableService.existsById(tableId)).thenReturn(true);
        when(reservationRepository.findAllByDateAndTableId(tomorrow, tableId)).thenReturn(Collections.emptyList());

        // when
        final List<ReservationDTO> result = reservationService.getAllByDateAndTableId(tomorrow, tableId);

        // then
        assertTrue(result.isEmpty());
        verify(diningTableService, times(1)).existsById(tableId);
        verify(reservationRepository, times(1)).findAllByDateAndTableId(tomorrow, tableId);
    }

    @Test
    void findAllByDate_whenExists_shouldFindAllReservationsByDate() {
        // given
        final LocalDate date = reservation.getReservationDate();

        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);
        when(reservationRepository.findAllByDate(date)).thenReturn(List.of(reservation));

        // when
        final List<ReservationDTO> result = reservationService.findAllByDate(date);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationDTOMapper, times(1)).apply(reservation);
        verify(reservationRepository, times(1)).findAllByDate(date);
    }
    @Test
    void allByDate_whenNoReservationFoundByDate_shouldThrowException() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);

        when(reservationRepository.findAllByDate(tomorrow)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByDate(tomorrow));
        verify(reservationRepository, times(1)).findAllByDate(tomorrow);
    }

    @Test
    void findAllByTableId_whenExists_shouldFindAllReservationByTableId() {
        // given
        final Integer tableId = table.getId();

        when(diningTableService.existsById(tableId)).thenReturn(true);
        when(reservationRepository.findAllByTableId(tableId)).thenReturn(List.of(reservation));
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.findAllByTableId(tableId);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(diningTableService, times(1)).existsById(tableId);
        verify(reservationRepository, times(1)).findAllByTableId(tableId);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void findAllByTableId_whenTableNotExists_shouldThrowException() {
        // given
        final Integer tableId = 111;

        when(diningTableService.existsById(tableId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.findAllByTableId(tableId));
        verify(diningTableService, times(1)).existsById(tableId);
        verify(reservationRepository, never()).findAllByTableId(tableId);
    }

    @Test
    void findAllByTableId_whenNoReservationFoundByTableId_shouldReturnEmptyList() {
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

    // todo: searchReservation tests...

    @Test
    void searchReservation_whenValidParams_shouldReturnListOfReservations() {
        // given
        final String searchTerm = "test";
        final List<Reservation> reservations = List.of(reservation);

        when(reservationRepository.searchReservations(searchTerm)).thenReturn(reservations);
        when(reservationDTOMapper.apply(reservation)).thenReturn(reservationDTO);

        // when
        final List<ReservationDTO> result = reservationService.searchReservations(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(List.of(reservationDTO), result);
        verify(reservationRepository, times(1)).searchReservations(searchTerm);
        verify(reservationDTOMapper, times(1)).apply(reservation);
    }

    @Test
    void searchReservation_whenSearchTermIsBlank_shouldThrowException() {
        // given
        final String searchTerm = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> reservationService.searchReservations(searchTerm));
        verify(reservationRepository, never()).searchReservations(anyString());
    }

    @Test
    void searchReservation_whenNoReservationsFound_shouldThrowException() {
        // given
        final String searchTerm = "nonexistent";

        when(reservationRepository.searchReservations(searchTerm)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> reservationService.searchReservations(searchTerm));
        verify(reservationRepository, times(1)).searchReservations(searchTerm);
        verify(reservationDTOMapper, never()).apply(any(Reservation.class));
    }

    @Test
    void validateReservation_whenReservationIsValid_shouldNotThrowException() {
        // given
        when(reservationValidator.validateReservation(reservation)).thenReturn(Collections.emptyMap());

        // when & then
        assertDoesNotThrow(() -> reservationService.validateReservation(reservation));
        verify(reservationValidator, times(1)).validateReservation(reservation);
    }

    @Test
    void validateReservation_whenReservationIsNotValid_shouldThrowException() {
        // given
        final Map<String, String> validationMessages = new HashMap<>();
        validationMessages.put("duration", FIELD_REQUIRED + DURATION_MESSAGE);

        when(reservationValidator.validateReservation(reservation)).thenReturn(validationMessages);

        // when & then
        assertThrows(ValidationException.class, () -> reservationService.validateReservation(reservation));
        verify(reservationValidator, times(1)).validateReservation(reservation);
    }

    @Test
    void validateDateTimeDurationAndSeats_whenParamsAreValid_shouldNotThrowException() {
        // given
        when(reservationValidator.validateDateTimeDurationAndSeats(reservation)).thenReturn(Collections.emptyMap());

        // when & then
        assertDoesNotThrow(() -> reservationService.validateDateTimeDurationAndSeats(reservation));
        verify(reservationValidator, times(1)).validateDateTimeDurationAndSeats(reservation);
    }

    @Test
    void validateDateTimeDurationAndSeats_whenParamsAreNotValid_shouldThrowException() {
        // given
        final Map<String, String> validationMessages = new HashMap<>();
        validationMessages.put("reservationDate", DATE_MESSAGE);

        when(reservationValidator.validateDateTimeDurationAndSeats(reservation)).thenReturn(validationMessages);

        // when & then
        assertThrows(ValidationException.class, () -> reservationService.validateDateTimeDurationAndSeats(reservation));
        verify(reservationValidator, times(1)).validateDateTimeDurationAndSeats(reservation);
    }
}