package com.proinwest.booking_table_app.diningTable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.proinwest.booking_table_app.reservation.Reservation;
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
    private DiningTable table;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        table = new DiningTable();
        table.setNumber(5);
        table.setSeats(7);
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
    void shouldGetAllTables() throws Exception {
        // given
        table.setId(createTable(table));

        // when & then
        mockMvc.perform(get("/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")     .value(1))
                .andExpect(jsonPath("$[0].id")      .value(table.getId()))
                .andExpect(jsonPath("$[0].number")  .value(table.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(table.getSeats()));
    }

    @Test
    void whenThereAreNoTablesInDatabase_shouldThrowException() throws Exception {
        // when & then
        mockMvc.perform(get("/tables"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_TABLES_IN_DATABASE));
    }

    @Test
    void shouldCreateAndGetTableById() throws Exception {
        // when & then
        final Integer tableId = createTable(table);
        table.setId(tableId);

        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));
    }

    @Test
    void getTableById_whenTableNotFoundById_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table with id " + tableId + " was not found."));
    }

    @Test
    void addTable_whenTableParamsAreInvalid_shouldThrowException() throws Exception {
        // given
        table.setNumber(null);
        table.setSeats(0);

        // when & then
        mockMvc.perform(post("/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.number") .value(FIELD_REQUIRED + NUMBER_MESSAGE))
                .andExpect(jsonPath("$.seats")  .value(SEATS_MESSAGE));
    }

    @Test
    void shouldUpdateTable() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);

        final DiningTable updatedTable = new DiningTable();
        updatedTable.setNumber(table.getNumber() + 1);
        updatedTable.setSeats(table.getSeats() + 1);

        // when
        mockMvc.perform(put("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedTable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));

        // then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));
    }

    @Test
    void updateTable_whenTableNotFoundById_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(put("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table with id " + tableId + " was not found."));
    }

    @Test
    void updateTable_whenTableParamsAreInvalid_shouldThrowException() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);

        final DiningTable invalidTable = new DiningTable();
        invalidTable.setNumber(MAX_NUMBER + 1);
        invalidTable.setSeats(null);

        // when & then
        mockMvc.perform(put("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidTable)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.number") .value(NUMBER_MESSAGE))
                .andExpect(jsonPath("$.seats")  .value(FIELD_REQUIRED + SEATS_MESSAGE));
    }

    @Test
    void shouldPartiallyUpdateTable() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);

        final DiningTable updatedTable = new DiningTable();
        updatedTable.setNumber(table.getNumber() + 2);

        // when
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedTable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));

        // then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));
    }

    @Test
    void partiallyUpdateTable_whenTableNotFoundById_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table with id " + tableId + " was not found."));
    }

    @Test
    void partiallyUpdateTable_whenTableParamsAreInvalid_shouldThrowException() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);

        final DiningTable invalidTable = new DiningTable();
        invalidTable.setNumber(MIN_NUMBER - 1);
        invalidTable.setSeats(MAX_SEATS + 1);

        // when & then
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidTable)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.number") .value(NUMBER_MESSAGE))
                .andExpect(jsonPath("$.seats")  .value(SEATS_MESSAGE));
    }

    @Test
    void shouldDeleteTable() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);

        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));

        // when
        mockMvc.perform(delete("/tables/{tableId}", tableId))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenTableNotExists_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 1;

        // when & then
        mockMvc.perform(delete("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table with " + tableId + " was not found."));
    }

    @Test
    void whenTableHasAssignedReservation_shouldThrowException() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);

        final User user = new User();
        user.setLogin("john");
        user.setPassword("secretpassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPhoneNumber("123-456-789");

        final Long userId = createUser(user);
        user.setId(userId);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME);
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(user);
        reservation.setDiningTable(table);

        createReservation(reservation);

        // when & then
        mockMvc.perform(delete("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Table with " + tableId
                                + " can not be deleted because it has at least one reservation assigned."));
    }

    @Test
    void shouldGetFreeTables() throws Exception {
        // given
        table.setId(createTable(table));

        final DiningTable freeTable = new DiningTable();
        freeTable.setNumber(table.getNumber() + 1);
        freeTable.setSeats(table.getSeats());

        final Integer freeTableId = createTable(freeTable);
        freeTable.setId(freeTableId);

        final User user = new User();
        user.setLogin("john");
        user.setPassword("secretpassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPhoneNumber("123-456-789");

        final Long userId = createUser(user);
        user.setId(userId);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME);
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(user);
        reservation.setDiningTable(table);

        createReservation(reservation);

        final DiningTable requestedTable = new DiningTable();
        requestedTable.setSeats(freeTable.getSeats() - 1);

        final Reservation requestedReservation = new Reservation();
        requestedReservation.setReservationDate(reservation.getReservationDate());
        requestedReservation.setReservationTime(reservation.getReservationTime());
        requestedReservation.setDuration(reservation.getDuration());
        requestedReservation.setDiningTable(requestedTable);

        // when & then
        mockMvc.perform(get("/tables/freetables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestedReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")     .value(1))
                .andExpect(jsonPath("$[0].id")      .value(freeTableId))
                .andExpect(jsonPath("$[0].number")  .value(freeTable.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(freeTable.getSeats()));
    }

    @Test
    void getFreeTables_whenReservationParamsAreInvalid_shouldThrowException() throws Exception {
        // given
        table.setNumber(null);
        table.setSeats(MAX_SEATS + 1);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().minusDays(1));
        reservation.setReservationTime(OPENING_TIME.minusHours(1));
        reservation.setDuration(MAX_DURATION + 1);
        reservation.setDiningTable(table);

        // when
        mockMvc.perform(get("/tables/freetables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date").value(DATE_MESSAGE))
                .andExpect(jsonPath("$.time").value(OPENING_HOURS_MESSAGE
                        + " Try change reservation time and/or duration."))
                .andExpect(jsonPath("$.duration").value(DURATION_MESSAGE))
                .andExpect(jsonPath("$.seats").value(SEATS_MESSAGE));
    }

    @Test
    void whenThereIsNoTablesWithRequiredSeats_shouldThrowException() throws Exception {
        // given
        createTable(table);

        final DiningTable requestedTable = new DiningTable();
        requestedTable.setSeats(table.getSeats() + 1);

        final Reservation requestedReservation = new Reservation();
        requestedReservation.setReservationDate(LocalDate.now().plusDays(1));
        requestedReservation.setReservationTime(OPENING_TIME);
        requestedReservation.setDuration(MIN_DURATION);
        requestedReservation.setDiningTable(requestedTable);

        // when & then
        mockMvc.perform(get("/tables/freetables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestedReservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no tables with the required number of seats ("
                                + requestedReservation.getDiningTable().getSeats() + ")."));
    }

    @Test
    void whenAllTablesAreBooked_shouldThrowException() throws Exception {
        // given
        final Integer tableId = createTable(table);
        table.setId(tableId);

        final User user = new User();
        user.setLogin("john");
        user.setPassword("secretpassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPhoneNumber("123-456-789");

        final Long userId = createUser(user);
        user.setId(userId);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME.plusHours(1));
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(user);
        reservation.setDiningTable(table);

        createReservation(reservation);

        final DiningTable requestedTable = new DiningTable();
        requestedTable.setSeats(table.getSeats());

        final Reservation requestedReservation = new Reservation();
        requestedReservation.setReservationDate(reservation.getReservationDate());
        requestedReservation.setReservationTime(reservation.getReservationTime());
        requestedReservation.setDuration(reservation.getDuration());
        requestedReservation.setDiningTable(requestedTable);

        // when & then
        mockMvc.perform(get("/tables/freetables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestedReservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_FREE_TABLES_WAS_FOUND));
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

    private void createReservation(Reservation reservation) throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());
    }

    void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute("TRUNCATE TABLE dining_table");
        jdbcTemplate.execute("TRUNCATE TABLE user");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }
}