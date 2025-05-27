package com.proinwest.booking_table_app.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.diningTable.DiningTableRepository;
import com.proinwest.booking_table_app.security.jwt.JwtUtils;
import com.proinwest.booking_table_app.security.userDetails.CustomUserDetails;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.TABLE_ID_IS_REQUIRED;
import static com.proinwest.booking_table_app.reservation.ReservationService.*;
import static com.proinwest.booking_table_app.security.jwt.SecurityUtils.ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER;
import static com.proinwest.booking_table_app.user.UserService.ACCESS_DENIED;
import static com.proinwest.booking_table_app.user.UserService.USER_ID_IS_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class ReservationControllerIntegrationTest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.4.0");
    @MockBean
    private JwtUtils jwtUtils;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiningTableRepository tableRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    private Reservation reservation;
    private User user;
    private DiningTable table;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        tableRepository.deleteAll();

        user = new User();
        user.setLogin("john");
        user.setFirstName("Sam");
        user.setLastName("Doe");
        user.setEmail("ann@mail.com");
        user.setPhoneNumber("123-456-789");
        user.setPassword("secretpassword");
        userRepository.save(user);

        table = new DiningTable();
        table.setNumber(33);
        table.setSeats(6);

        reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(17,0));
        reservation.setDuration(2);
        reservation.setUser(user);
        reservation.setDiningTable(table);
    }

    @AfterAll
    static void stopContainer() {
        mySQLContainer.stop();
    }

    @Test
    void connectionEstablished() {
        mySQLContainer.isCreated();
        mySQLContainer.isRunning();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReservations_whenUserIsAdmin_shouldReturnAllReservations() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")                 .value(1))
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")     .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")            .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")             .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")          .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")      .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")       .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")          .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")    .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReservations_whenReservationsNotExists_shouldReturnNotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_RESERVATIONS_IN_DATABASE));
    }

    @Test
    @WithMockUser
    void getAllReservations_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReservation_whenUserIsOwner_shouldFetchReservationById() throws Exception {
        // given
        authenticateAs(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations/{id}", reservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")                 .value(reservation.getId()))
                .andExpect(jsonPath("$.reservationDate")    .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime")    .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$.duration")           .value(reservation.getDuration()))
                .andExpect(jsonPath("$.user.id")            .value(user.getId()))
                .andExpect(jsonPath("$.user.login")         .value(user.getLogin()))
                .andExpect(jsonPath("$.user.firstName")     .value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")      .value(user.getLastName()))
                .andExpect(jsonPath("$.user.email")         .value(user.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber")   .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")     .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")  .value(table.getSeats()));
    }

    @Test
    @WithMockUser
    void getReservation_whenReservationNotExists_shouldReturnNotFound() throws Exception {
        // given
        final Long id = 111L;

        // when & then
        mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void getReservation_whenUserIsNotOwner_shouldReturnForbidden() throws Exception {
        // given
        authenticateAs(user);

        final User user1 = new User();
        user1.setLogin("sam");
        user1.setEmail("sam@mail.com");
        user1.setPassword("secretpassword");
        user1.setPhoneNumber("111222444");
        userRepository.save(user1);
        tableRepository.save(table);
        reservation.setUser(user1);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations/{id}", reservation.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER));
    }

    @Test
    @WithMockUser
    void createReservation_whenUserIsAuthenticated_shouldAddReservation() throws Exception {
        // given
        tableRepository.save(table);
        authenticateAs(user);

        // when
        final Long id = createReservation(reservation);

        // then
        mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")                 .value(id))
                .andExpect(jsonPath("$.reservationDate")    .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime")    .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$.duration")           .value(reservation.getDuration()))
                .andExpect(jsonPath("$.user.id")            .value(user.getId()))
                .andExpect(jsonPath("$.user.login")         .value(user.getLogin()))
                .andExpect(jsonPath("$.user.firstName")     .value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")      .value(user.getLastName()))
                .andExpect(jsonPath("$.user.email")         .value(user.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber")   .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")     .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")  .value(table.getSeats()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReservation_whenUserIsAdmin_shouldAddReservation() throws Exception {
        // given
        tableRepository.save(table);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationDate")    .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime")    .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$.duration")           .value(reservation.getDuration()))
                .andExpect(jsonPath("$.user.id")            .value(user.getId()))
                .andExpect(jsonPath("$.user.login")         .value(user.getLogin()))
                .andExpect(jsonPath("$.user.firstName")     .value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")      .value(user.getLastName()))
                .andExpect(jsonPath("$.user.email")         .value(user.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber")   .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")     .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")  .value(table.getSeats()));
    }

    @Test
    @WithMockUser
    void createReservation_whenReservationParamsAreNotValid_shouldReturnErrorsMessage() throws Exception {
        // given
        user.setId(null);
        table.setId(null);

        final Reservation notValidReservation = new Reservation();
        notValidReservation.setReservationDate(null);
        notValidReservation.setReservationTime(null);
        notValidReservation.setDuration(null);
        notValidReservation.setUser(user);
        notValidReservation.setDiningTable(table);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(notValidReservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.user")           .value(USER_ID_IS_REQUIRED))
                .andExpect(jsonPath("$.diningTable")    .value(TABLE_ID_IS_REQUIRED))
                .andExpect(jsonPath("$.duration")       .value(FIELD_REQUIRED + DURATION_MESSAGE))
                .andExpect(jsonPath("$.reservationDate").value(FIELD_REQUIRED + DATE_MESSAGE))
                .andExpect(jsonPath("$.reservationTime").value(FIELD_REQUIRED + TIME_MESSAGE));
    }

    @Test
    @WithMockUser
    void createReservation_whenReservationCollide_shouldReturnConflict() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Dining table with id " + reservation.getDiningTable().getId()
                                + " is not available at the time."));
    }

    @Test
    void updateReservation_whenAllParamsProvided_shouldUpdateReservation() throws Exception {
        // given
        authenticateAs(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        final User newUser = new User();
        newUser.setId(user.getId());
        newUser.setLogin("ann");
        newUser.setFirstName("Ann");
        newUser.setLastName("Smith");
        newUser.setEmail("john@mail.pl");
        newUser.setPhoneNumber("987 654-321");
        newUser.setPassword("qwerty1234567890");

        final DiningTable newTable = new DiningTable();
        newTable.setId(table.getId());
        newTable.setNumber(table.getNumber() + 1);
        newTable.setSeats(table.getSeats() + 1);

        final Reservation newReservation = new Reservation();
        newReservation.setId(reservation.getId());
        newReservation.setReservationDate(reservation.getReservationDate().plusDays(1));
        newReservation.setReservationTime(reservation.getReservationTime().plusHours(1));
        newReservation.setDuration(reservation.getDuration() + 1);
        newReservation.setDiningTable(newTable);
        newReservation.setUser(newUser);

        // when &  then
        mockMvc.perform(patch("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")                 .value(reservation.getId()))
                .andExpect(jsonPath("$.reservationDate")    .value(newReservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime")    .value(newReservation.getReservationTime().toString()))
                .andExpect(jsonPath("$.duration")           .value(newReservation.getDuration()))
                .andExpect(jsonPath("$.user.id")            .value(user.getId()))
                .andExpect(jsonPath("$.user.login")         .value(newUser.getLogin()))
                .andExpect(jsonPath("$.user.firstName")     .value(newUser.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")      .value(newUser.getLastName()))
                .andExpect(jsonPath("$.user.email")         .value(newUser.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber")   .value(newUser.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")     .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number") .value(newTable.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")  .value(newTable.getSeats()));
    }

    @Test
    void updateReservation_whenReservationNotExists_shouldReturnNotFound() throws Exception {
        // given
        authenticateAs(user);

        final Long id = 111L;

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void updateReservation_whenUserIsNotOwner_shouldReturnForbidden() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);

        final User user1 = new User();
        user1.setLogin("ann");
        user1.setFirstName("Ann");
        user1.setLastName("Smith");
        user1.setEmail("john@mail.pl");
        user1.setPhoneNumber("987 654-321");
        user1.setPassword("qwerty1234567890");
        userRepository.save(user1);
        authenticateAs(user1);

        // when & then
        mockMvc.perform(patch("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER));
    }

    @Test
    void updateReservation_whenReservationParamsAreNotValid_shouldReturnErrorsMessage() throws Exception {
        // given
        authenticateAs(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        final User notValidUser = new User();
        notValidUser.setId(user.getId());

        final DiningTable notValidTable = new DiningTable();
        notValidTable.setId(222);

        final Reservation notValidReservation = new Reservation();
        notValidReservation.setReservationDate(LocalDate.now().minusDays(2));
        notValidReservation.setReservationTime(CLOSING_TIME.plusHours(1));
        notValidReservation.setDuration(MAX_DURATION + 1);
        notValidReservation.setUser(notValidUser);
        notValidReservation.setDiningTable(notValidTable);

        // when & then
        mockMvc.perform(patch("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(notValidReservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.duration")       .value(DURATION_MESSAGE))
                .andExpect(jsonPath("$.reservationDate").value(DATE_MESSAGE))
                .andExpect(jsonPath("$.reservationTime")
                        .value(OPENING_HOURS_MESSAGE + " Try change reservation time and/or duration."))
                .andExpect(jsonPath("$.diningTable")
                        .value("Dining table with id " + notValidTable.getId() + " was not found."));
    }

    @Test
    void updateReservation_whenReservationCollide_shouldReturnConflict() throws Exception {
        // given
        authenticateAs(user);

        final Reservation reservation2 = new Reservation();
        reservation2.setReservationDate(reservation.getReservationDate());
        reservation2.setReservationTime(reservation.getReservationTime().plusHours(reservation.getDuration()));
        reservation2.setDuration(reservation.getDuration());
        reservation2.setUser(user);
        reservation2.setDiningTable(table);

        final Reservation newReservation = new Reservation();
        newReservation.setId(null);
        newReservation.setReservationDate(reservation.getReservationDate());
        newReservation.setReservationTime(reservation.getReservationTime().plusHours(reservation.getDuration()/2));
        newReservation.setDuration(reservation.getDuration());
        newReservation.setUser(user);
        newReservation.setDiningTable(table);

        tableRepository.save(table);
        reservationRepository.save(reservation);
        reservationRepository.save(reservation2);

        // when & then
        mockMvc.perform(patch("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Dining table with id " + reservation.getDiningTable().getId()
                                + " is not available at the time."));
    }

    @Test
    void updateReservation_whenSomeParamsProvided_shouldPartiallyUpdateReservation() throws Exception {
        // given
        authenticateAs(user);

        final User newUser = new User();
        newUser.setLogin("ann");
        newUser.setFirstName("Ann");
        newUser.setEmail("smith@mail.pl");
        newUser.setPhoneNumber("987 654-321");
        newUser.setPassword("qwerty123456");
        userRepository.save(newUser);

        final DiningTable newTable = new DiningTable();
        newTable.setNumber(table.getNumber() + 2);
        newTable.setSeats(table.getSeats() + 2);
        tableRepository.save(newTable);

        tableRepository.save(table);
        reservationRepository.save(reservation);

        final Reservation newReservation = new Reservation();
        newReservation.setReservationDate(reservation.getReservationDate().plusDays(1));
        newReservation.setReservationTime(reservation.getReservationTime().plusHours(1));
        newReservation.setDiningTable(newTable);
        newReservation.setUser(newUser);

        // when &  then
        mockMvc.perform(patch("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")                 .value(reservation.getId()))
                .andExpect(jsonPath("$.reservationDate")    .value(newReservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime")    .value(newReservation.getReservationTime().toString()))
                .andExpect(jsonPath("$.duration")           .value(reservation.getDuration()))
                .andExpect(jsonPath("$.user.id")            .value(newUser.getId()))
                .andExpect(jsonPath("$.user.login")         .value(newUser.getLogin()))
                .andExpect(jsonPath("$.user.firstName")     .value(newUser.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")      .value(newUser.getLastName()))
                .andExpect(jsonPath("$.user.email")         .value(newUser.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber")   .value(newUser.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")     .value(newTable.getId()))
                .andExpect(jsonPath("$.diningTable.number") .value(newTable.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")  .value(newTable.getSeats()));
    }

    @Test
    void cancelReservation_whenReservationExists_shouldDeleteReservation() throws Exception {
        // given
        authenticateAs(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);
        final Long id = reservation.getId();

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNoContent());

        final Optional<Reservation> deletedById = reservationRepository.findById(id);
        assertTrue(deletedById.isEmpty());
    }

    @Test
    @WithMockUser
    void cancelReservation_whenReservationNotExists_shouldReturnNotFound() throws Exception {
        // given
        final Long id = 111L;

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reservation with id " + id + " was not found."));
    }

    @Test
    void cancelReservation_whenUserIsNotOwner_shouldReturnForbidden() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);
        final Long id = reservation.getId();

        final User user1 = new User();
        user1.setLogin("ann");
        user1.setFirstName("Ann");
        user1.setLastName("Smith");
        user1.setEmail("john@mail.pl");
        user1.setPhoneNumber("987 654-321");
        user1.setPassword("qwerty1234567890");
        userRepository.save(user1);
        authenticateAs(user1);

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER));
    }

    @Test
    void getUserReservations_whenUserIsAuthenticated_shouldReturnUserReservation() throws Exception {
        // given
        authenticateAs(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations/user/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")     .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")            .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")             .value(reservation.getUser().getId()))
                .andExpect(jsonPath("$[0].user.login")          .value(reservation.getUser().getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")      .value(reservation.getUser().getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")       .value(reservation.getUser().getLastName()))
                .andExpect(jsonPath("$[0].user.email")          .value(reservation.getUser().getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")    .value(reservation.getUser().getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(reservation.getDiningTable().getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(reservation.getDiningTable().getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(reservation.getDiningTable().getSeats()));
    }

    @Test
    void getUserReservations_whenUserIsNotOwner_shouldReturnForbidden() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);

        final User user1 = new User();
        user1.setLogin("ann");
        user1.setFirstName("Ann");
        user1.setLastName("Smith");
        user1.setEmail("john@mail.pl");
        user1.setPhoneNumber("987 654-321");
        user1.setPassword("qwerty1234567890");
        userRepository.save(user1);
        authenticateAs(user1);

        // when & then
        mockMvc.perform(get("/reservations/user/{userId}", user.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER));
    }

    @Test
    void getUserReservations_whenUserHasNoReservation_shouldReturnNotFound() throws Exception {
        // given
        authenticateAs(user);
        final Long userId = user.getId();

        // when & then
        mockMvc.perform(get("/reservations/user/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("No reservation is assigned to the user with ID: " + userId + "."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllByDateAndTableId_whenReservationExists_shouldReturnReservation() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}",
                                reservation.getReservationDate(), table.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")     .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")            .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")             .value(reservation.getUser().getId()))
                .andExpect(jsonPath("$[0].user.login")          .value(reservation.getUser().getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")      .value(reservation.getUser().getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")       .value(reservation.getUser().getLastName()))
                .andExpect(jsonPath("$[0].user.email")          .value(reservation.getUser().getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")    .value(reservation.getUser().getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(reservation.getDiningTable().getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(reservation.getDiningTable().getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(reservation.getDiningTable().getSeats()));
    }

    @Test
    @WithMockUser
    void findAllByDateAndTableId_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}",
                                reservation.getReservationDate(), table.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllByDateAndTableId_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        final LocalDate date = LocalDate.now();
        final Integer tableId = 111;

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}", date, tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllByDateAndTableId_whenReservationNotExists_shouldReturnNotFound() throws Exception {
        // given
        tableRepository.save(table);
        final Integer tableId = table.getId();
        final LocalDate date = LocalDate.now();

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}", date, tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on " + date + " for the table with ID " + tableId +
                                "."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllByDate_whenReservationExists_shouldReturnReservation() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations/date/{date}", reservation.getReservationDate()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")                 .value(1))
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")     .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")            .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")             .value(reservation.getUser().getId()))
                .andExpect(jsonPath("$[0].user.login")          .value(reservation.getUser().getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")      .value(reservation.getUser().getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")       .value(reservation.getUser().getLastName()))
                .andExpect(jsonPath("$[0].user.email")          .value(reservation.getUser().getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")    .value(reservation.getUser().getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(reservation.getDiningTable().getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(reservation.getDiningTable().getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(reservation.getDiningTable().getSeats()));
    }

    @Test
    @WithMockUser
    void findAllByDate_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations/date/{date}", reservation.getReservationDate()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAllByDate_whenReservationNotExists_shouldReturnNotFound() throws Exception {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);

        // when & then
        mockMvc.perform(get("/reservations/date/{date}", date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on date " + date + "."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_whenReservationExists_shouldFindReservation() throws Exception {
        // given
        final String searchQuery = "John";
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations/search")
                        .param("query", searchQuery)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()") .value(1))
                .andExpect(jsonPath("$[0].id")  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate")    .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime")    .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")           .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")            .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")         .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")     .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")      .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")         .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber")   .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")     .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number") .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")  .value(table.getSeats()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_whenQueryIsBlank_shouldReturnBadRequest() throws Exception {
        // given
        final String query = " ";

        // when & then
        mockMvc.perform(get("/reservations/search")
                        .param("query", query)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(INPUT_IS_MISSING));
    }

    @Test
    @WithMockUser
    void searchReservations_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        final String query = "John";

        mockMvc.perform(get("/reservations/search")
                        .param("query", query)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservation_whenReservationNotFound_shouldThrowException() throws Exception {
        // given
        tableRepository.save(table);
        reservationRepository.save(reservation);
        final String query = "non-existing-query";

        // when & then
        mockMvc.perform(get("/reservations/search")
                .param("query", query)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("No reservation was found for the query: " + query + "."));
    }

    private void authenticateAs(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Integer createTable(DiningTable table) throws Exception {
        final ResultActions postResult = mockMvc.perform(post("/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isCreated());

        final String responseContent = postResult.andReturn().getResponse().getContentAsString();
        return JsonPath.read(responseContent, "$.id");
    }

    private Long createUser(User user) throws Exception {
        final ResultActions postResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        final String responseContent = postResult.andReturn().getResponse().getContentAsString();
        final Integer userId = JsonPath.read(responseContent, "$.id");
        return Long.valueOf(userId);
    }

    private Long createReservation(Reservation reservation) throws Exception {
        final ResultActions postResult = mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());

        final String responseContent = postResult.andReturn().getResponse().getContentAsString();
        final Integer id = JsonPath.read(responseContent, "$.id");
        return Long.valueOf(id);
    }

}