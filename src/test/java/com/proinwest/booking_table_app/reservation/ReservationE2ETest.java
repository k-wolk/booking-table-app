package com.proinwest.booking_table_app.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.TABLE_ID_IS_REQUIRED;
import static com.proinwest.booking_table_app.reservation.ReservationService.*;
import static com.proinwest.booking_table_app.user.UserService.USER_ID_IS_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ReservationE2ETest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.4.0");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private DiningTable table;
    private User user;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        table = new DiningTable();
        table.setNumber(5);
        table.setSeats(7);

        user = new User();
        user.setLogin("john");
        user.setPassword("secretpassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPhoneNumber("123-456-789");

        reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME);
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(user);
        reservation.setDiningTable(table);
    }

    @AfterAll
    static void stopContainer() {
        mySQLContainer.stop();
    }

    @Test
    void connectionEstablished() {
        assertTrue(mySQLContainer.isCreated());
        assertTrue(mySQLContainer.isRunning());
    }

    @Test
    void shouldGetAllReservations() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")          .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void whenThereAreNoReservationsInDatabase_shouldThrowException() throws Exception {
        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_RESERVATIONS_IN_DATABASE));
    }

    @Test
    void shouldCreateAndGetReservationById() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));

        // when & then
        final Long id = createReservation(reservation);

        mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")              .value(id))
                .andExpect(jsonPath("$.reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$.duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$.user.id")         .value(user.getId()))
                .andExpect(jsonPath("$.user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$.user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$.user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void getReservation_whenReservationNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long id = 1L;

        // when & then
        mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void addReservation_whenReservationParamsAreInvalid_shouldThrowException() throws Exception {
        // given
        reservation.setReservationDate(null);
        reservation.setReservationTime(null);
        reservation.setDuration(null);
        reservation.setUser(user);
        reservation.setDiningTable(table);

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reservationDate").value(FIELD_REQUIRED + DATE_MESSAGE))
                .andExpect(jsonPath("$.reservationTime").value(FIELD_REQUIRED + TIME_MESSAGE))
                .andExpect(jsonPath("$.duration")       .value(FIELD_REQUIRED + DURATION_MESSAGE))
                .andExpect(jsonPath("$.user")           .value(USER_ID_IS_REQUIRED))
                .andExpect(jsonPath("$.diningTable")    .value(TABLE_ID_IS_REQUIRED));
    }

    @Test
    void shouldUpdateReservation() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        final Long id = createReservation(reservation);

        final DiningTable newTable = new DiningTable();
        newTable.setNumber(table.getNumber() + 1);
        newTable.setSeats(table.getSeats() + 1);
        newTable.setId(createTable(newTable));

        final User newUser = new User();
        newUser.setLogin("sam");
        newUser.setPassword("qwerty123456");
        newUser.setFirstName("Sam");
        newUser.setLastName("Smith");
        newUser.setEmail("smith@mail.com");
        newUser.setPhoneNumber("987-654-321");
        newUser.setId(createUser(newUser));

        final Reservation updatedReservation = new Reservation();
        updatedReservation.setReservationDate(reservation.getReservationDate().plusDays(1));
        updatedReservation.setReservationTime(reservation.getReservationTime().plusHours(1));
        updatedReservation.setDuration(reservation.getDuration() + 1);
        updatedReservation.setUser(newUser);
        updatedReservation.setDiningTable(newTable);

        // when
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")              .value(id))
                .andExpect(jsonPath("$.reservationDate") .value(updatedReservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime") .value(updatedReservation.getReservationTime().toString()))
                .andExpect(jsonPath("$.duration")        .value(updatedReservation.getDuration()))
                .andExpect(jsonPath("$.user.id")         .value(newUser.getId()))
                .andExpect(jsonPath("$.user.login")      .value(newUser.getLogin()))
                .andExpect(jsonPath("$.user.firstName")  .value(newUser.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")   .value(newUser.getLastName()))
                .andExpect(jsonPath("$.user.email")      .value(newUser.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber").value(newUser.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")      .value(newTable.getId()))
                .andExpect(jsonPath("$.diningTable.number")  .value(newTable.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")   .value(newTable.getSeats()));
    }

    @Test
    void updateReservation_whenReservationNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long id = 1L;

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void updateReservation_whenReservationParamsAreInvalid_shouldThrowException() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);
        final Long userId = createUser(user);
        user.setId(userId);
        final Long id = createReservation(reservation);

        final Long invalidUserId = userId + 1;
        final Integer invalidTableId = tableId + 1;

        reservation.setReservationDate(LocalDate.now().minusDays(1));
        reservation.setReservationTime(OPENING_TIME.minusHours(1));
        reservation.setDuration(MAX_DURATION + 1);
        user.setId(invalidUserId);
        reservation.setUser(user);
        table.setId(invalidTableId);
        reservation.setDiningTable(table);

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reservationDate").value(DATE_MESSAGE))
                .andExpect(jsonPath("$.reservationTime")
                        .value(OPENING_HOURS_MESSAGE + " Try change reservation reservationTime and/or duration."))
                .andExpect(jsonPath("$.duration").value(DURATION_MESSAGE))
                .andExpect(jsonPath("$.user")
                        .value("User with id " + invalidUserId + " was not found."))
                .andExpect(jsonPath("$.diningTable")
                        .value("Dining table with id " + invalidTableId + " was not found."));
    }

    @Test
    void shouldPartiallyUpdateReservation() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        final Long id = createReservation(reservation);

        final Reservation updatedReservation = new Reservation();
        updatedReservation.setReservationDate(reservation.getReservationDate().plusDays(1));
        updatedReservation.setDuration(reservation.getDuration() + 1);
        updatedReservation.setUser(user);
        updatedReservation.setDiningTable(table);

        // when
        mockMvc.perform(patch("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")              .value(id))
                .andExpect(jsonPath("$.reservationDate") .value(updatedReservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$.duration")        .value(updatedReservation.getDuration()))
                .andExpect(jsonPath("$.user.id")         .value(user.getId()))
                .andExpect(jsonPath("$.user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$.user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$.user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$.user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void partiallyUpdateReservation_whenReservationNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long id = 1L;

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void partiallyUpdateReservation_whenReservationParamsAreInvalid_shouldThrowException() throws Exception {
        // given
        Integer tableId = createTable(table);
        table.setId(tableId);
        Long userId = createUser(user);
        user.setId(userId);
        final Long id = createReservation(reservation);

        final Long invalidUserId = userId + 1;
        final Integer invalidTableId = tableId + 1;

        reservation.setReservationDate(LocalDate.now().minusDays(1));
        reservation.setReservationTime(CLOSING_TIME.plusHours(1));
        reservation.setDuration(MIN_DURATION - 1);
        user.setId(invalidUserId);
        reservation.setUser(user);
        table.setId(invalidTableId);
        reservation.setDiningTable(table);

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reservationDate").value(DATE_MESSAGE))
                .andExpect(jsonPath("$.reservationTime")
                        .value(OPENING_HOURS_MESSAGE + " Try change reservation reservationTime and/or duration."))
                .andExpect(jsonPath("$.duration").value(DURATION_MESSAGE))
                .andExpect(jsonPath("$.user")
                        .value("User with id " + invalidUserId + " was not found."))
                .andExpect(jsonPath("$.diningTable")
                        .value("Dining table with id " + invalidTableId + " was not found."));
    }

    @Test
    void shouldDeleteReservation() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        final Long id = createReservation(reservation);

        mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        // when
        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void deleteReservation_whenReservationNotExistsById_shouldThrowException() throws Exception {
        // given
        final Long id = 1L;

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void shouldFindReservationByDate() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        final LocalDate date = reservation.getReservationDate();

        // when & then
        mockMvc.perform(get("/reservations/search/reservationDate/{reservationDate}", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void whenReservationNotFoundByDate_shouldThrowException() throws Exception {
        // given
        final LocalDate date = LocalDate.now();

        // when & then
        mockMvc.perform(get("/reservations/search/reservationDate/{reservationDate}", date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on reservationDate " + date + "."));
    }

    @Test
    void shouldFindReservationByUserId() throws Exception {
        // given
        table.setId(createTable(table));
        final Long userId = createUser(user);
        user.setId(userId);
        reservation.setId(createReservation(reservation));

        // when & then
        mockMvc.perform(get("/reservations/search/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void whenReservationNotFoundByUserId_shouldThrowException() throws Exception {
        // given
        final Long userId = 1L;

        // when & then
        mockMvc.perform(get("/reservations/search/userid/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation booked by user with id: " + userId + "."));
    }

    @Test
    void shouldFindReservationByUserLogin() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        final String login = user.getLogin();

        // when & then
        mockMvc.perform(get("/reservations/search/login/{login}", login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void whenReservationNotFoundByUserLogin_shouldThrowException() throws Exception {
        // given
        final String loginFragment = "a";

        // when & then
        mockMvc.perform(get("/reservations/search/login/{loginFragment}", loginFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's login containing: " + loginFragment));
    }

    @Test
    void shouldFindReservationByUserFirstName() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        final String firstName = user.getFirstName();

        // when & then
        mockMvc.perform(get("/reservations/search/firstname/{firstName}", firstName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void whenReservationNotFoundByUserFirstName_shouldThrowException() throws Exception {
        // given
        final String firstNameFragment = "a";

        // when & then
        mockMvc.perform(get("/reservations/search/firstname/{firstNameFragment}", firstNameFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's first name containing: " + firstNameFragment));
    }

    @Test
    void shouldFindReservationByUserLastName() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        final String lastName = user.getLastName();

        // when & then
        mockMvc.perform(get("/reservations/search/lastname/{lastName}", lastName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void whenReservationNotFoundByUserLastName_shouldThrowException() throws Exception {
        // given
        final String lastNameFragment = "a";

        // when & then
        mockMvc.perform(get("/reservations/search/lastname/{lastNameFragment}", lastNameFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's last name containing: " + lastNameFragment));
    }

    @Test
    void shouldFindReservationByUserEmail() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        final String email = user.getEmail();

        // when & then
        mockMvc.perform(get("/reservations/search/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void whenReservationNotFoundByUserEmail_shouldThrowException() throws Exception {
        // given
        final String emailFragment = "@";

        // when & then
        mockMvc.perform(get("/reservations/search/email/{emailFragment}", emailFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's email containing: " + emailFragment));
    }

    @Test
    void shouldFindReservationByUserPhoneNumber() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        final String phoneNumber = user.getPhoneNumber();

        // when & then
        mockMvc.perform(get("/reservations/search/phonenumber/{phoneNumber}", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void whenReservationNotFoundByUserPhoneNumber_shouldThrowException() throws Exception {
        // given
        final String phoneNumberFragment = "1";

        // when & then
        mockMvc.perform(get("/reservations/search/phonenumber/{phoneNumberFragment}", phoneNumberFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's phone number containing: " + phoneNumberFragment));
    }

    @Test
    void shouldFindReservationByTableId() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        // when & then
        mockMvc.perform(get("/reservations/search/table/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void findReservationByTableId_whenTableNotExistsById_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(get("/reservations/search/table/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Dining table with id " + tableId + " was not found."));
    }

    @Test
    void whenReservationNotFoundByTableId_shouldThrowException() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);

        // when & then
        mockMvc.perform(get("/reservations/search/table/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with table id: " + tableId));
    }

    @Test
    void shouldFindReservationByDateAndTableId() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        final LocalDate date = reservation.getReservationDate();

        // when & then
        mockMvc.perform(get("/reservations/search/reservationDate/{reservationDate}/table/{tableId}", date, tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void findReservationByDateAndTableId_whenTableNotExistsById_shouldThrowException() throws Exception {
        // given
        final LocalDate date = LocalDate.now();
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(get("/reservations/search/reservationDate/{reservationDate}/table/{tableId}", date, tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Dining table with id " + tableId + " was not found."));
    }

    @Test
    void whenReservationNotFoundByDateAndTableId_shouldThrowException() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);

        final LocalDate date = LocalDate.now();

        // when & then
        mockMvc.perform(get("/reservations/search/reservationDate/{reservationDate}/table/{tableId}", date, tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on reservationDate " + date + " at table with id " + tableId + "."));
    }

    @Test
    void shouldFindReservationByDateAndTime() throws Exception {
        // given
        table.setId(createTable(table));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation));

        final LocalDate date = reservation.getReservationDate();
        final LocalTime time = reservation.getReservationTime();

        // when & then
        mockMvc.perform(get("/reservations/search/reservationDate/{reservationDate}/reservationTime/{reservationTime}", date, time))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")             .value(1))
                .andExpect(jsonPath("$[0].id")              .value(reservation.getId()))
                .andExpect(jsonPath("$[0].reservationDate") .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].reservationTime") .value(reservation.getReservationTime().toString()))
                .andExpect(jsonPath("$[0].duration")        .value(reservation.getDuration()))
                .andExpect(jsonPath("$[0].user.id")         .value(user.getId()))
                .andExpect(jsonPath("$[0].user.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$[0].user.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$[0].user.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void whenReservationNotFoundByDateAndTime_shouldThrowException() throws Exception {
        // given
        final LocalDate date = LocalDate.now();
        final LocalTime time = OPENING_TIME;

        // when & then
        mockMvc.perform(get("/reservations/search/reservationDate/{reservationDate}/reservationTime/{reservationTime}", date, time))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on reservationDate " + date + " and reservationTime " + time + "."));
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

    void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute("TRUNCATE TABLE dining_table");
        jdbcTemplate.execute("TRUNCATE TABLE user");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }
}
