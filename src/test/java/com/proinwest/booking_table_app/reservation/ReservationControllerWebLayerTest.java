package com.proinwest.booking_table_app.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.security.config.AuthEntryPointJwt;
import com.proinwest.booking_table_app.security.config.SecurityConfig;
import com.proinwest.booking_table_app.security.jwt.JwtUtils;
import com.proinwest.booking_table_app.security.userDetails.CustomUserDetailsService;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserDTO;
import com.proinwest.booking_table_app.user.UserRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class ReservationControllerWebLayerTest {
    @MockBean
    private ReservationService reservationService;
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    private Reservation reservation;
    private ReservationDTO reservationDTO;
    private User user;
    private UserDTO userDTO;
    private DiningTable table;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(111L);
        user.setLogin("john");
        user.setFirstName("Sam");
        user.setLastName("Doe");
        user.setEmail("ann@mail.com");
        user.setPhoneNumber("123-456-789");
        user.setPassword("secretpassword");

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

        table = Instancio.create(DiningTable.class);

        reservation = new Reservation();
        reservation.setId(222L);
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(17,0));
        reservation.setDuration(2);
        reservation.setUser(user);
        reservation.setDiningTable(table);

        reservationDTO = new ReservationDTO(
                reservation.getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getDuration(),
                userDTO,
                reservation.getDiningTable()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReservations_whenUserIsAdmin_shouldFetchAllReservations() throws Exception {
        // given
        final List<ReservationDTO> allReservations = new ArrayList<>();
        allReservations.add(reservationDTO);

        when(reservationService.getAllReservations()).thenReturn(allReservations);

        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")                 .value(1))
                .andExpect(jsonPath("$[0].id")                  .value(reservationDTO.id()))
                .andExpect(jsonPath("$[0].reservationDate")     .value(reservationDTO.reservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")     .value(reservationDTO.reservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")            .value(reservationDTO.duration()))
                .andExpect(jsonPath("$[0].user.id")             .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")          .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")      .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")       .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")          .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")    .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));

        verify(reservationService, times(1)).getAllReservations();
    }

    @Test
    @WithMockUser
    void getAllReservations_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isForbidden());

        verify(reservationService, never()).getAllReservations();
    }

    @Test
    @WithMockUser
    void getReservation_whenUserIsAuthenticated_shouldFetchReservationById() throws Exception {
        // given
        final Long id = reservation.getId();

        when(reservationService.getReservation(id)).thenReturn(reservationDTO);

        // when & then
        mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")                 .value(id))
                .andExpect(jsonPath("$.reservationDate")    .value(reservationDTO.reservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime")    .value(reservationDTO.reservationTime().toString()))
                .andExpect(jsonPath("$.duration")           .value(reservationDTO.duration()))
                .andExpect(jsonPath("$.user.id")            .value(user.getId()))
                .andExpect(jsonPath("$.user.login")         .value(user.getLogin()))
                .andExpect(jsonPath("$.user.firstName")     .value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")      .value(user.getLastName()))
                .andExpect(jsonPath("$.user.email")         .value(user.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber")   .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")     .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")  .value(table.getSeats()));

        verify(reservationService, times(1)).getReservation(id);
    }

    @Test
    @WithMockUser
    void createReservation_whenUserIsAuthenticated_shouldCreateReservation() throws Exception {
        // given
        final Long id = reservation.getId();
        reservation.setId(null);

        when(reservationService.createReservation(any(Reservation.class))).thenReturn(reservationDTO);
        when(reservationService.location(any(Reservation.class))).thenReturn(URI.create("/reservations/" + id));

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/" + id))
                .andExpect(jsonPath("$.id")                 .value(reservationDTO.id()))
                .andExpect(jsonPath("$.reservationDate")    .value(reservationDTO.reservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime")    .value(reservationDTO.reservationTime().toString()))
                .andExpect(jsonPath("$.duration")           .value(reservationDTO.duration()))
                .andExpect(jsonPath("$.user.id")            .value(user.getId()))
                .andExpect(jsonPath("$.user.login")         .value(user.getLogin()))
                .andExpect(jsonPath("$.user.firstName")     .value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")      .value(user.getLastName()))
                .andExpect(jsonPath("$.user.email")         .value(user.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber")   .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")     .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")  .value(table.getSeats()));

        verify(reservationService, times(1)).createReservation(any(Reservation.class));
        verify(reservationService, times(1)).location(any(Reservation.class));
    }

    @Test
    @WithMockUser
    void updateReservation_whenUserIsAuthenticated_shouldUpdateReservation() throws Exception {
        // given
        final Long id = reservation.getId();

        when(reservationService.updateReservation(eq(id), any(Reservation.class))).thenReturn(reservationDTO);

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")                 .value(reservationDTO.id()))
                .andExpect(jsonPath("$.reservationDate")    .value(reservationDTO.reservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime")    .value(reservationDTO.reservationTime().toString()))
                .andExpect(jsonPath("$.duration")           .value(reservationDTO.duration()))
                .andExpect(jsonPath("$.user.id")            .value(user.getId()))
                .andExpect(jsonPath("$.user.login")         .value(user.getLogin()))
                .andExpect(jsonPath("$.user.firstName")     .value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")      .value(user.getLastName()))
                .andExpect(jsonPath("$.user.email")         .value(user.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber")   .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")     .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")  .value(table.getSeats()));

        verify(reservationService, times(1)).updateReservation(eq(id), any(Reservation.class));
    }

    @Test
    @WithMockUser
    void cancelReservation_whenUserIsAuthenticated_shouldDeleteReservation() throws Exception {
        // given
        final Long id = reservation.getId();

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).cancelReservation(id);
    }

    @Test
    @WithMockUser
    void getUserReservations_whenUserIsAuthenticated_shouldFetchAllReservationsByUserId() throws Exception {
        // given
        final Long userId = reservation.getUser().getId();

        when(reservationService.getUserReservations(userId)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(get("/reservations/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")                 .value(1))
                .andExpect(jsonPath("$[0].id")                  .value(reservationDTO.id()))
                .andExpect(jsonPath("$[0].reservationDate")     .value(reservationDTO.reservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")     .value(reservationDTO.reservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")            .value(reservationDTO.duration()))
                .andExpect(jsonPath("$[0].user.id")             .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")          .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")      .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")       .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")          .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")    .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));

        verify(reservationService, times(1)).getUserReservations(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllByDateAndTableId_whenUserIsAdmin_shouldFindAllReservationsByDateAndTableId() throws Exception {
        // given
        final LocalDate date = reservation.getReservationDate();
        final Integer tableId = reservation.getDiningTable().getId();

        when(reservationService.requireAllByDateAndTableId(date, tableId)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}", date, tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")                 .value(1))
                .andExpect(jsonPath("$[0].id")                  .value(reservationDTO.id()))
                .andExpect(jsonPath("$[0].reservationDate")     .value(reservationDTO.reservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")     .value(reservationDTO.reservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")            .value(reservationDTO.duration()))
                .andExpect(jsonPath("$[0].user.id")             .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")          .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")      .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")       .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")          .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")    .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));

        verify(reservationService, times(1)).requireAllByDateAndTableId(date, tableId);
    }

    @Test
    @WithMockUser
    void findAllByDateAndTableId_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        final LocalDate date = reservation.getReservationDate();
        final Integer tableId = reservation.getDiningTable().getId();

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}", date, tableId))
                .andExpect(status().isForbidden());

        verify(reservationService, never()).getAllByDateAndTableId(date, tableId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllByDate_whenUserIsAdmin_shouldFindAllReservationsByDate() throws Exception {
        // given
        final LocalDate date = reservation.getReservationDate();

        when(reservationService.findAllByDate(date)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(get("/reservations/date/{date}", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")                 .value(1))
                .andExpect(jsonPath("$[0].id")                  .value(reservationDTO.id()))
                .andExpect(jsonPath("$[0].reservationDate")     .value(reservationDTO.reservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")     .value(reservationDTO.reservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")            .value(reservationDTO.duration()))
                .andExpect(jsonPath("$[0].user.id")             .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")          .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")      .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")       .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")          .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")    .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));

        verify(reservationService, times(1)).findAllByDate(date);
    }

    @Test
    @WithMockUser
    void findAllByDate_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        final LocalDate date = reservation.getReservationDate();

        // when & then
        mockMvc.perform(get("/reservations/date/{date}", date))
                .andExpect(status().isForbidden());

        verify(reservationService, never()).findAllByDate(date);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservation_whenUserIsAdmin_shouldFindReservations() throws Exception {
        // given
        final String query = "Doe";
        final List<ReservationDTO> searchResults = List.of(reservationDTO);

        when(reservationService.searchReservations(query)).thenReturn(searchResults);

        // when & then
        mockMvc.perform(get("/reservations/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")                 .value(1))
                .andExpect(jsonPath("$[0].id")                  .value(reservationDTO.id()))
                .andExpect(jsonPath("$[0].reservationDate")     .value(reservationDTO.reservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")     .value(reservationDTO.reservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")            .value(reservationDTO.duration()))
                .andExpect(jsonPath("$[0].user.id")             .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")          .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")      .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")       .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")          .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")    .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));

        verify(reservationService, times(1)).searchReservations(query);
    }

    @Test
    @WithMockUser
    void searchReservation_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        final String query = "Doe";

        // when & then
        mockMvc.perform(get("/reservations/search")
                        .param("query", query))
                .andExpect(status().isForbidden());

        verify(reservationService, never()).searchReservations(query);
    }
}