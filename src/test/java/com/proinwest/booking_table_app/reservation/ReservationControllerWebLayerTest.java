package com.proinwest.booking_table_app.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserDTO;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@ExtendWith(MockitoExtension.class)
class ReservationControllerWebLayerTest {
    @MockBean
    private ReservationService reservationService;
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
                user.getPhoneNumber()
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
    void shouldGetAllReservations() throws Exception {
        // given
        final List<ReservationDTO> allReservations = new ArrayList<>();
        allReservations.add(reservationDTO);

        when(reservationService.getAllReservations()).thenReturn(allReservations);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations"))
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
    void shouldGetReservationById() throws Exception {
        // given
        final Long id = reservation.getId();

        when(reservationService.getReservation(id)).thenReturn(reservationDTO);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/{id}", id))
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
    void shouldAddReservation() throws Exception {
        // given
        final Long id = reservation.getId();

        reservation.setId(null);

        when(reservationService.addReservation(any(Reservation.class))).thenReturn(reservationDTO);
        when(reservationService.location(any(Reservation.class))).thenReturn(URI.create("/reservations/" + id));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/reservations")
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

        verify(reservationService, times(1)).addReservation(any(Reservation.class));
        verify(reservationService, times(1)).location(any(Reservation.class));
    }

    @Test
    void shouldUpdateReservation() throws Exception {
        // given
        final Long id = reservation.getId();

        when(reservationService.updateReservation(eq(id), any(Reservation.class))).thenReturn(reservationDTO);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .put("/reservations/{id}", id)
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
    void shouldPartiallyUpdateReservation() throws Exception {
        // given
        final Long id = reservation.getId();

        when(reservationService.partiallyUpdateReservation(eq(id), any(Reservation.class))).thenReturn(reservationDTO);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/reservations/{id}", id)
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

        verify(reservationService, times(1)).partiallyUpdateReservation(eq(id), any(Reservation.class));
    }

    @Test
    void shouldDeleteReservation() throws Exception {
        // given
        final Long id = reservation.getId();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/reservations/{id}", id))
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).deleteReservation(id);
    }

    @Test
    void shouldFindAllReservationsByDate() throws Exception {
        // given
        final LocalDate date = reservation.getReservationDate();

        when(reservationService.findAllByDate(date)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}", date))
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
    void shouldFindAllReservationsByUserId() throws Exception {
        // given
        final Long userId = reservation.getUser().getId();

        when(reservationService.findAllByUserId(userId)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/userid/{id}", userId))
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

        verify(reservationService, times(1)).findAllByUserId(userId);
    }

    @Test
    void shouldFindAllReservationsByUserLogin() throws Exception {
        // given
        final String login = reservation.getUser().getLogin();

        when(reservationService.findAllByUserLogin(login)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/login/{login}", login))
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

        verify(reservationService, times(1)).findAllByUserLogin(login);
    }

    @Test
    void shouldFindAllReservationsByUserFirstName() throws Exception {
        // given
        final String firstName = reservation.getUser().getFirstName();

        when(reservationService.findAllByUserFirstName(firstName)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/firstname/{firstName}", firstName))
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

        verify(reservationService, times(1)).findAllByUserFirstName(firstName);
    }

    @Test
    void shouldFindAllReservationsByUserLastName() throws Exception {
        // given
        final String lastName = reservation.getUser().getLastName();

        when(reservationService.findAllByUserLastName(lastName)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/lastname/{lastName}", lastName))
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

        verify(reservationService, times(1)).findAllByUserLastName(lastName);
    }

    @Test
    void shouldFindAllReservationsByUserEmail() throws Exception {
        // given
        final String email = reservation.getUser().getEmail();

        when(reservationService.findAllByUserEmail(email)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/email/{email}", email))
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

        verify(reservationService, times(1)).findAllByUserEmail(email);
    }

    @Test
    void shouldFindAllReservationsByUserPhoneNumber() throws Exception {
        // given
        final String phoneNumber = reservation.getUser().getPhoneNumber();

        when(reservationService.findAllByUserPhoneNumber(phoneNumber)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/phonenumber/{phoneNumber}", phoneNumber))
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

        verify(reservationService, times(1)).findAllByUserPhoneNumber(phoneNumber);
    }

    @Test
    void shouldFindAllReservationsByTableId() throws Exception {
        // given
        final Integer tableId = reservation.getDiningTable().getId();

        when(reservationService.findAllByTableId(tableId)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/table/{tableId}", tableId))
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

        verify(reservationService, times(1)).findAllByTableId(tableId);
    }

    @Test
    void shouldFindAllReservationsByDateAndTableId() throws Exception {
        // given
        final LocalDate date = reservation.getReservationDate();
        final Integer tableId = reservation.getDiningTable().getId();

        when(reservationService.findAllByDateAndTableId(date, tableId)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}/table/{tableId}", date, tableId))
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

        verify(reservationService, times(1)).findAllByDateAndTableId(date, tableId);
    }

    @Test
    void shouldFindAllReservationsByDateAndTime() throws Exception {
        // given
        final LocalDate date = reservation.getReservationDate();
        final LocalTime time = reservation.getReservationTime();

        when(reservationService.findAllByDateAndTime(date, time)).thenReturn(List.of(reservationDTO));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}/time/{time}", date, time))
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

        verify(reservationService, times(1)).findAllByDateAndTime(date, time);
    }
}