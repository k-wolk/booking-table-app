package com.proinwest.booking_table_app.diningTable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.proinwest.booking_table_app.reservation.Reservation;
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
import java.time.LocalTime;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.*;
import static com.proinwest.booking_table_app.reservation.ReservationService.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class DiningTableE2ETest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.4.0");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DiningTableRepository tableRepository;
    private DiningTable table;
    private User user;
    private User admin;

    @BeforeEach
    void setUp() throws Exception {
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
        user.setRole("USER");
        user.setActive(true);

        admin = new User();
        admin.setLogin("ann");
        admin.setPassword("secretpassword");
        admin.setFirstName("Ann");
        admin.setLastName("Doe");
        admin.setEmail("ann@mail.com");
        admin.setPhoneNumber("999888777");
        admin.setRole("ADMIN");
        admin.setActive(true);
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
    void getAllTables_whenUserIsAdmin_shouldFetchAllTables() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());

        final Integer tableId = createTable(table, token);
        table.setId(tableId);

        DiningTable table2 = new DiningTable();
        table2.setNumber(2);
        table2.setSeats(2);
        table2.setActive(false);
        final Integer table2Id = createTable(table2, token);
        table2.setId(table2Id);

        // when & then
        mockMvc.perform(get("/tables")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")     .value(2))
                .andExpect(jsonPath("$[0].id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(table.getSeats()))
                .andExpect(jsonPath("$[0].active")  .value(table.isActive()))
                .andExpect(jsonPath("$[1].id")      .value(table2.getId()))
                .andExpect(jsonPath("$[1].number")  .value(table2.getNumber()))
                .andExpect(jsonPath("$[1].seats")   .value(table2.getSeats()))
                .andExpect(jsonPath("$[1].active")  .value(table2.isActive()));
    }

    @Test
    void getAllTables_whenNoTableExists_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());

        // when & then
        mockMvc.perform(get("/tables")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_TABLES_FOUND));
    }

    @Test
    void getAllActiveTables_whenTableExists_shouldFetchAllActiveTables() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));

        // when & then
        mockMvc.perform(get("/tables/getactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")     .value(1))
                .andExpect(jsonPath("$[0].id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(table.getSeats()))
                .andExpect(jsonPath("$[0].active")  .value(table.isActive()));
    }

    @Test
    void getAllActiveTables_whenNoTableExists_shouldReturnNotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/tables/getactive"))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_TABLES_FOUND));
    }

    @Test
    void getTable_createTable_whenUserIsAdmin_shouldCreateAndFetchTableById() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());

        // when & then
        final Integer tableId = createTable(table, tokenAdmin);
        table.setId(tableId);

        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()))
                .andExpect(jsonPath("$.active") .value(table.isActive()));
    }

    @Test
    void getTable_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        final Integer tableId = 111;

        // when & then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    void getTable_whenUserIsNotAdminAndTableIsInactive_shouldReturnForbidden() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));
        deactivateTable(table.getId(), tokenAdmin);

        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        // when & then
        mockMvc.perform(get("/tables/{tableId}", table.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED_TABLE_INACTIVE));
    }


    @Test
    void createTable_whenTableParamsAreInvalid_shouldReturnBadRequest() throws Exception {
        // given
        table.setNumber(null);
        table.setSeats(0);

        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());

        // when & then
        mockMvc.perform(post("/tables")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.number") .value(FIELD_REQUIRED + NUMBER_MESSAGE))
                .andExpect(jsonPath("$.seats")  .value(SEATS_MESSAGE));
    }

    @Test
    void deactivateTable_whenUserIsAdmin_shouldDeactivateTable() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Integer tableId = createTable(table, token);
        assertTrue(table.isActive());

        // when
        mockMvc.perform(post("/tables/deactivate/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deactivateTable_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Integer tableId = 111;

        // when
        mockMvc.perform(post("/tables/deactivate/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    void deactivateTable_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(post("/tables/deactivate/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    @Test
    void activateTable_whenUserIsAdmin_shouldActivateTable() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, token));
        final Integer tableId = table.getId();
        deactivateTable(tableId, token);

        mockMvc.perform(get("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // when
        mockMvc.perform(post("/tables/activate/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void activateTable_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Integer tableId = 111;

        // when
        mockMvc.perform(post("/tables/activate/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    void activateTable_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(post("/tables/activate/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    @Test
    void updateTable_whenAllFieldsProvided_shouldUpdateTable() throws Exception {
        // given
        final DiningTable updatedTable = new DiningTable();
        updatedTable.setNumber(table.getNumber() + 1);
        updatedTable.setSeats(table.getSeats() + 1);

        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, token));
        final Integer tableId = table.getId();

        // when
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedTable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));

        // then
        mockMvc.perform(get("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));
    }

    @Test
    void updateTable_whenSomeFieldsProvided_shouldPartiallyUpdateTable() throws Exception {
        // given
        final DiningTable updatedTable = new DiningTable();
        updatedTable.setNumber(table.getNumber() + 2);

        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Integer tableId = createTable(table, token);

        // when
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedTable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));

        // then
        mockMvc.perform(get("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));
    }

    @Test
    void updateTable_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Integer tableId = 111;

        // when & then
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    void updateTable_whenTableParamsAreInvalid_shouldReturnBadRequest() throws Exception {
        // given
        final DiningTable invalidTable = new DiningTable();
        invalidTable.setNumber(MAX_NUMBER + 1);
        invalidTable.setSeats(MIN_SEATS - 1);

        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Integer tableId = createTable(table, token);

        // when & then
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidTable)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.number") .value(NUMBER_MESSAGE))
                .andExpect(jsonPath("$.seats")  .value(SEATS_MESSAGE));
    }

    @Test
    void updateTable_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    @Test
    void deleteTable_whenTableExists_shouldDeleteTable() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());

        final Integer tableId = createTable(table, token);

        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));

        // when
        mockMvc.perform(delete("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTable_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());

        final Integer tableId = 111;

        // when & then
        mockMvc.perform(delete("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    void deleteTable_whenTableHasAssignedReservation_shouldReturnConflict() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());

        table.setId(createTable(table, tokenAdmin));
        final Integer tableId = table.getId();

                user.setId(createUser(user));
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        final Long userId = user.getId();

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME);
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(user);
        reservation.setDiningTable(table);
        createReservation(reservation, token);

        // when & then
        mockMvc.perform(delete("/tables/{tableId}", tableId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Table with " + tableId
                                + " can not be deleted because it has at least one reservation assigned."));
    }

    @Test
    void availableHours_whenNoReservations_shouldReturnFullTime() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());

        table.setId(createTable(table, token));
        final Integer tableId = table.getId();

        final LocalDate date = LocalDate.now().plusDays(1);

        // when & then
        mockMvc.perform(get("/tables/{tableId}/date/{date}", tableId, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(OPENING_TIME + " - " + CLOSING_TIME));
    }

    @Test
    void availableHours_whenAreReservations_shouldReturnAvailableTimes() throws Exception {
        // given
        createUser(admin);
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());
        table.setId(createTable(table, tokenAdmin));
        final Integer tableId = table.getId();
        final LocalDate date = LocalDate.now().plusDays(1);

        user.setId(createUser(user));

        final Reservation reservation1 = new Reservation();
        reservation1.setReservationDate(date);
        reservation1.setReservationTime(OPENING_TIME.plusHours(1));
        reservation1.setDuration(1);
        reservation1.setUser(user);
        reservation1.setDiningTable(table);
        createReservation(reservation1, tokenAdmin);
        final LocalTime reservation1Starts = reservation1.getReservationTime();
        final LocalTime reservation1Ends = reservation1Starts.plusHours(reservation1.getDuration());

        final Reservation reservation2 = new Reservation();
        reservation2.setReservationDate(date);
        reservation2.setReservationTime(OPENING_TIME.plusHours(5));
        reservation2.setDuration(2);
        reservation2.setUser(user);
        reservation2.setDiningTable(table);
        createReservation(reservation2, tokenAdmin);
        final LocalTime reservation2Starts = reservation2.getReservationTime();
        final LocalTime reservation2Ends = reservation2Starts.plusHours(reservation2.getDuration());

        // when
        mockMvc.perform(get("/tables/{tableId}/date/{date}", tableId, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(OPENING_TIME + " - " + reservation1Starts))
                .andExpect(jsonPath("$[1]").value(reservation1Ends + " - " + reservation2Starts))
                .andExpect(jsonPath("$[2]").value(reservation2Ends + " - " + CLOSING_TIME));
    }

    @Test
    void availableHours_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        final Integer tableId = 111;
        final LocalDate date = LocalDate.now().plusDays(1);

        // when & then
        mockMvc.perform(get("/tables/{tableId}/date/{date}", tableId, date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    void availableTables_whenIsFreeTable_shouldReturnOk() throws Exception {
        // given
        admin.setId(createUser(admin));
        final String tokenAdmin = obtainJwtToken(admin.getLogin(), admin.getPassword());

        table.setId(createTable(table, tokenAdmin));

        final DiningTable freeTable = new DiningTable();
        freeTable.setNumber(table.getNumber() + 1);
        freeTable.setSeats(table.getSeats());
        final Integer freeTableId = createTable(freeTable, tokenAdmin);
        freeTable.setId(freeTableId);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME);
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(admin);
        reservation.setDiningTable(table);
        createReservation(reservation, tokenAdmin);

        final DiningTable requestedTable = new DiningTable();
        requestedTable.setSeats(freeTable.getSeats() - 1);

        final Reservation requestedReservation = new Reservation();
        requestedReservation.setReservationDate(reservation.getReservationDate());
        requestedReservation.setReservationTime(reservation.getReservationTime());
        requestedReservation.setDuration(reservation.getDuration());
        requestedReservation.setDiningTable(requestedTable);

        // when & then
        mockMvc.perform(get("/tables/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestedReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")     .value(1))
                .andExpect(jsonPath("$[0].id")      .value(freeTableId))
                .andExpect(jsonPath("$[0].number")  .value(freeTable.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(freeTable.getSeats()));
    }

    @Test
    void availableTables_whenReservationParamsAreInvalid_shouldReturnBadRequest() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());

        table.setId(createTable(table, token));
        table.setNumber(null);
        table.setSeats(MAX_SEATS + 1);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().minusDays(1));
        reservation.setReservationTime(OPENING_TIME.minusHours(1));
        reservation.setDuration(MAX_DURATION + 1);
        reservation.setDiningTable(table);

        // when & then
        mockMvc.perform(get("/tables/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reservationDate")    .value(DATE_MESSAGE))
                .andExpect(jsonPath("$.reservationTime")    .value(OPENING_HOURS_MESSAGE
                        + " Try change reservation time and/or duration."))
                .andExpect(jsonPath("$.duration")           .value(DURATION_MESSAGE))
                .andExpect(jsonPath("$.seats")              .value(SEATS_MESSAGE));
    }

    @Test
    void availableTables_whenThereIsNoTablesWithRequiredSeats_shouldReturnNotFound() throws Exception {
        // given
        final DiningTable requestedTable = new DiningTable();
        requestedTable.setSeats(table.getSeats() + 1);

        final Reservation requestedReservation = new Reservation();
        requestedReservation.setReservationDate(LocalDate.now().plusDays(1));
        requestedReservation.setReservationTime(OPENING_TIME);
        requestedReservation.setDuration(MIN_DURATION);
        requestedReservation.setDiningTable(requestedTable);

        // when & then
        mockMvc.perform(get("/tables/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestedReservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no tables with the required number of seats ("
                                + requestedTable.getSeats() + ")."));
    }

    @Test
    void availableTables_whenAllTablesAreBooked_shouldReturnNotFound() throws Exception {
        // given
        admin.setId(createUser(admin));
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());

        table.setId(createTable(table, token));

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME.plusHours(1));
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(admin);
        reservation.setDiningTable(table);
        createReservation(reservation, token);

        final DiningTable requestedTable = new DiningTable();
        requestedTable.setSeats(table.getSeats());

        final Reservation requestedReservation = new Reservation();
        requestedReservation.setReservationDate(reservation.getReservationDate());
        requestedReservation.setReservationTime(reservation.getReservationTime());
        requestedReservation.setDuration(reservation.getDuration());
        requestedReservation.setDiningTable(requestedTable);

        // when & then
        mockMvc.perform(get("/tables/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestedReservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_FREE_TABLES_WAS_FOUND));
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

    private void createReservation(Reservation reservation, String token) throws Exception {
        mockMvc.perform(post("/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());
    }

    private void deactivateTable(Integer id, String tokenAdmin) throws Exception {
        mockMvc.perform(post("/tables/deactivate/{tableId}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute("TRUNCATE TABLE dining_table");
        jdbcTemplate.execute("TRUNCATE TABLE user");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }

    private String obtainJwtToken(String login, String password) throws Exception {
        final LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin(login);
        loginRequest.setPassword(password);

        final MvcResult result = mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        final String contentAsString = result.getResponse().getContentAsString();
        return JsonPath.read(contentAsString, "$.jwtToken");
    }
}