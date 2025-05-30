package com.proinwest.booking_table_app.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.security.auth.LoginRequest;
import com.proinwest.booking_table_app.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.TABLE_ID_IS_REQUIRED;
import static com.proinwest.booking_table_app.reservation.ReservationService.*;
import static com.proinwest.booking_table_app.user.UserService.ACCESS_DENIED;
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
    private User admin;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        table = new DiningTable();
        table.setNumber(5);
        table.setSeats(7);
        table.setActive(true);

        user = new User();
        user.setLogin("john");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPhoneNumber("123-456-789");
        user.setPassword("secretpassword");
        user.setRole("USER");
        user.setActive(true);

        admin = new User();
        admin.setLogin("ann");
        admin.setFirstName("Ann");
        admin.setLastName("Doe");
        admin.setEmail("ann@mail.com");
        admin.setPhoneNumber("999888777");
        admin.setPassword("secretpassword");
        admin.setRole("ADMIN");
        admin.setActive(true);

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
    void getAllReservations_whenUserIsAdmin_shouldFetchAllReservations() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());

        table.setId(createTable(table, tokenAdmin));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation, tokenAdmin));

        // when & then
        mockMvc.perform(get("/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
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
    void getAllReservations_whenThereIsNoReservationInDatabase_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());

        // when & then
        mockMvc.perform(get("/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_RESERVATIONS_IN_DATABASE));
    }

    @Test
    void getReservation_createReservation_whenUserIsAuthenticated_shouldCreateAndGetOwnReservationById() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        user.setId(createUser(user));
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        // when & then
        final Long id = createReservation(reservation, token);

        mockMvc.perform(get("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
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
                .andExpect(jsonPath("$.user.role")       .value(user.getRole()))
                .andExpect(jsonPath("$.diningTable.id")      .value(table.getId()))
                .andExpect(jsonPath("$.diningTable.number")  .value(table.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")   .value(table.getSeats()));
    }

    @Test
    void getReservation_whenThereIsNoReservationInDatabase_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long id = 1L;

        // when & then
        mockMvc.perform(get("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void addReservation_whenReservationParamsAreInvalid_shouldReturnBadRequest() throws Exception {
        // given
        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        reservation.setReservationDate(null);
        reservation.setReservationTime(null);
        reservation.setDuration(null);
        reservation.setUser(user);
        reservation.setDiningTable(table);

        // when & then
        mockMvc.perform(post("/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
    void updateReservation_whenUserIsOwner_shouldUpdateReservation() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        user.setId(createUser(user));
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        final Long id = createReservation(reservation, tokenAdmin);

        final DiningTable newTable = new DiningTable();
        newTable.setNumber(table.getNumber() + 1);
        newTable.setSeats(table.getSeats() + 1);
        newTable.setId(createTable(newTable, tokenAdmin));

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

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
                .andExpect(jsonPath("$.user.role")       .value(newUser.getRole()))
                .andExpect(jsonPath("$.diningTable.id")      .value(newTable.getId()))
                .andExpect(jsonPath("$.diningTable.number")  .value(newTable.getNumber()))
                .andExpect(jsonPath("$.diningTable.seats")   .value(newTable.getSeats()));
    }

    @Test
    void updateReservation_whenReservationNotFoundById_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long id = 1L;

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void updateReservation_whenReservationParamsAreInvalid_shouldReturnBadRequest() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        user.setId(createUser(user));
        final Long id = createReservation(reservation, tokenAdmin);

        final Long invalidUserId = user.getId() + 111;
        final Integer invalidTableId = table.getId() + 111;

        reservation.setReservationDate(LocalDate.now().minusDays(1));
        reservation.setReservationTime(OPENING_TIME.minusHours(1));
        reservation.setDuration(MAX_DURATION + 1);
        user.setId(invalidUserId);
        reservation.setUser(user);
        table.setId(invalidTableId);
        reservation.setDiningTable(table);

        // when & then
        mockMvc.perform(patch("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reservationDate").value(DATE_MESSAGE))
                .andExpect(jsonPath("$.reservationTime")
                        .value(OPENING_HOURS_MESSAGE + " Try change reservation time and/or duration."))
                .andExpect(jsonPath("$.duration").value(DURATION_MESSAGE))
                .andExpect(jsonPath("$.user")
                        .value("User with id " + invalidUserId + " was not found."))
                .andExpect(jsonPath("$.diningTable")
                        .value("Dining table with id " + invalidTableId + " was not found."));
    }

    @Test
    void cancelReservation_whenUserIsAdmin_shouldDeleteReservation() throws Exception {
        // given
        admin.setId(createUser(admin));
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));
        reservation.setUser(admin);
        final Long id = createReservation(reservation, tokenAdmin);

        mockMvc.perform(get("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        // when
        mockMvc.perform(delete("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void cancelReservation_whenUserIsOwner_shouldDeleteReservation() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        user.setId(createUser(user));
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        final Long id = createReservation(reservation, token);

        mockMvc.perform(get("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        // when
        mockMvc.perform(delete("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void cancelReservation_whenReservationNotExists_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long id = 1L;

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void getUserReservations_whenUserIsOwner_shouldFetchAllOwnReservations() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        user.setId(createUser(user));
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        reservation.setId(createReservation(reservation, token));

        // when & then
        mockMvc.perform(get("/reservations/user/{userId}", user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
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
    void getUserReservations_whenUserIsAdmin_shouldFetchAllUserReservations() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        user.setId(createUser(user));
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        reservation.setId(createReservation(reservation, token));

        // when & then
        mockMvc.perform(get("/reservations/user/{userId}", user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
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
    void getUserReservations_whenUserNotExists_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long userId = 222L;

        // when & then
        mockMvc.perform(get("/reservations/user/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User not found for ID: " + userId + "."));
    }

    @Test
    void getUserReservations_whenThereIsNoReservationAssignedToUser_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));
        user.setId(createUser(user));

        // when & then
        mockMvc.perform(get("/reservations/user/{userId}", user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("No reservation is assigned to the user with ID: " + user.getId() + "."));
    }

    @Test
    void findAllByDateAndTableId_whenUserIsAdmin_shouldFetchAllReservations() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        user.setId(createUser(user));
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        reservation.setId(createReservation(reservation, token));

        final LocalDate date = reservation.getReservationDate();
        final Integer tableId = table.getId();

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}", date, tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
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
    void findAllByDateAndTableId_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        final LocalDate date = reservation.getReservationDate();
        final Integer tableId = 111;

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}", date, tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    void findAllByDateAndTableId_whenReservationNotFoundByDateAndTableId_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        final LocalDate date = LocalDate.now().plusDays(1);
        final Integer tableId = table.getId();

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}", date, tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on " + date + " for the table with ID " + tableId + "."));
    }

    @Test
    void findAllByDateAndTableId_whenTableNotFoundById_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation, tokenAdmin));

        final LocalDate date = reservation.getReservationDate();
        final Integer invalidTableId = table.getId() + 1;

        // when & then
        mockMvc.perform(get("/reservations/date/{date}/table/{tableId}", date, invalidTableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + invalidTableId + "."));
    }

    @Test
    void findAllByDate_whenUserIsAdmin_shouldFetchAllReservations() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        user.setId(createUser(user));
        reservation.setId(createReservation(reservation, tokenAdmin));

        final LocalDate date = reservation.getReservationDate();

        // when & then
        mockMvc.perform(get("/reservations/date/{date}", date)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
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
    void findAllByDate_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        final LocalDate date = reservation.getReservationDate();

        // when & then
        mockMvc.perform(get("/reservations/date/{date}", date)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    void findAllByDate_whenReservationNotFoundByDate_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation, tokenAdmin));

        final LocalDate date = reservation.getReservationDate().plusDays(1);

        // when & then
        mockMvc.perform(get("/reservations/date/{date}", date)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on date " + date + "."));
    }

    @Test
    void searchReservations_whenUserIsAdmin_shouldFindAllReservations() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation, tokenAdmin));

        final String query = reservation.getUser().getLogin();

        // when & then
        mockMvc.perform(get("/reservations/search?query={query}", query)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
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
    void searchReservations_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        final String query = reservation.getUser().getLogin();

        // when & then
        mockMvc.perform(get("/reservations/search?query={query}", query)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    void searchReservations_whenReservationNotFoundByQuery_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation, tokenAdmin));

        final String query = "non-existing-query";

        // when & then
        mockMvc.perform(get("/reservations/search?query={query}", query)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("No reservation was found for the query: " + query + "."));
    }

    @Test
    void searchReservations_whenQueryIsBlank_shouldReturnBadRequest() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));
        user.setId(createUser(user));
        reservation.setId(createReservation(reservation, tokenAdmin));

        final String query = "";

        // when & then
        mockMvc.perform(get("/reservations/search?query={query}", query)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(INPUT_IS_MISSING));
    }


    private Integer createTable(DiningTable table, String token) throws Exception {
        final ResultActions postResult = mockMvc.perform(post("/tables")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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

    private Long createReservation(Reservation reservation, String token) throws Exception {
        final ResultActions postResult = mockMvc.perform(post("/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());

        final String responseContent = postResult.andReturn().getResponse().getContentAsString();
        final Integer id = JsonPath.read(responseContent, "$.id");
        return Long.valueOf(id);
    }

    private String obtainJwtToken(String login, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin(login);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        final String contentAsString = result.getResponse().getContentAsString();
        return JsonPath.read(contentAsString, "$.jwtToken");
    }

    void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute("TRUNCATE TABLE dining_table");
        jdbcTemplate.execute("TRUNCATE TABLE user");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }
}