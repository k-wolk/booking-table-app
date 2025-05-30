package com.proinwest.booking_table_app.diningTable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.reservation.Reservation;
import com.proinwest.booking_table_app.reservation.ReservationRepository;
import com.proinwest.booking_table_app.security.jwt.JwtUtils;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.*;
import static com.proinwest.booking_table_app.reservation.ReservationService.CLOSING_TIME;
import static com.proinwest.booking_table_app.reservation.ReservationService.OPENING_TIME;
import static com.proinwest.booking_table_app.user.UserService.ACCESS_DENIED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class DiningTableControllerIntegrationTest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.4.0");
    @MockBean
    private JwtUtils jwtUtils;
    @Autowired
    private DiningTableRepository tableRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        tableRepository.deleteAll();
        userRepository.deleteAll();
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
    @WithMockUser(roles = "ADMIN")
    void getAllTables_whenUserIsAdmin_shouldFetchAllTables() throws Exception {
        // given
        final DiningTable table1 = new DiningTable();
        table1.setNumber(1);
        table1.setSeats(2);
        table1.setActive(true);

        final DiningTable table2 = new DiningTable();
        table2.setNumber(2);
        table2.setSeats(4);
        table2.setActive(true);

        tableRepository.saveAll(List.of(table1, table2));

        // when & then
        mockMvc.perform(get("/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")   .value(2))
                .andExpect(jsonPath("$[0].id")      .value(table1.getId()))
                .andExpect(jsonPath("$[0].number")  .value(table1.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(table1.getSeats()))
                .andExpect(jsonPath("$[1].id")      .value(table2.getId()))
                .andExpect(jsonPath("$[1].number")  .value(table2.getNumber()))
                .andExpect(jsonPath("$[1].seats")   .value(table2.getSeats()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTables_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/tables"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_TABLES_FOUND));
    }

    @Test
    @WithMockUser
    void getAllTables_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/tables"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    @WithMockUser
    void getAllActiveTables_shouldReturnAllActiveTables() throws Exception {
        // given
        final DiningTable table1 = new DiningTable();
        table1.setNumber(1);
        table1.setSeats(2);
        table1.setActive(false);

        final DiningTable table2 = new DiningTable();
        table2.setNumber(2);
        table2.setSeats(4);
        table2.setActive(true);

        tableRepository.saveAll(List.of(table1, table2));

        // when & then
        mockMvc.perform(get("/tables/getactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")   .value(1))
                .andExpect(jsonPath("$[0].id")      .value(table2.getId()))
                .andExpect(jsonPath("$[0].number")  .value(table2.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(table2.getSeats()));
    }

    @Test
    void getAllActiveTables_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/tables/getactive"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_TABLES_FOUND));
    }

    @Test
    void getTable_whenTableIsActive_shouldFetchTable() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);
        table.setActive(true);

        tableRepository.save(table);
        final Integer tableId = table.getId();

        // when & then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));
    }

    @Test
    void getTable_whenTableIsInactive_shouldReturnForbidden() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);
        table.setActive(false);

        tableRepository.save(table);
        final Integer tableId = table.getId();

        // when & then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED_TABLE_INACTIVE));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTable_userIsAdmin_whenTableIsInactive_shouldFetchTable() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);
        table.setActive(false);

        tableRepository.save(table);
        final Integer tableId = table.getId();

        // when & then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));    }


    @Test
    void getTable_whenTableExists_shouldReturnNotFound() throws Exception {
        // given
        final Integer tableId = 7;

        // when & then
        mockMvc.perform(get("/tables/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTable_whenUserIsAdmin_shouldAddTable() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        // when & then
        mockMvc.perform(post("/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateTable_whenUserIsAdmin_shouldDeactivateTable() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);
        table.setActive(true);
        tableRepository.save(table);
        Integer tableId = table.getId();

        // when & then
        mockMvc.perform(post("/tables/deactivate/{tableId}", tableId))
                .andExpect(status().isNoContent());

        Optional<DiningTable> deactivatedTable = tableRepository.findById(tableId);
        assertTrue(deactivatedTable.isPresent());
        assertFalse(deactivatedTable.get().isActive());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateTable_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        Integer tableId = 7;

        // when & then
        mockMvc.perform(post("/tables/deactivate/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")

                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateTable_whenUserIsAdmin_shouldActivateTable() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);
        table.setActive(false);
        tableRepository.save(table);
        Integer tableId = table.getId();

        // when & then
        mockMvc.perform(post("/tables/activate/{tableId}", tableId))
                .andExpect(status().isNoContent());

        Optional<DiningTable> activatedTable = tableRepository.findById(tableId);
        assertTrue(activatedTable.isPresent());
        assertTrue(activatedTable.get().isActive());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateTable_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        Integer tableId = 7;

        // when & then
        mockMvc.perform(post("/tables/activate/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTable_whenUserIsAdmin_shouldUpdateTable() throws Exception {
        // given
        final DiningTable tableToUpdate = new DiningTable();
        tableToUpdate.setNumber(1);
        tableToUpdate.setSeats(2);

        tableRepository.save(tableToUpdate);

        final DiningTable newTable = new DiningTable();
        newTable.setNumber(2);

        final DiningTable updatedTable = tableToUpdate;
        updatedTable.setNumber(newTable.getNumber());

        // when & then
        mockMvc.perform(patch("/tables/{tableId}", tableToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newTable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(updatedTable.getId()))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTable_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        final Integer tableId = 7;

        final DiningTable newTable = new DiningTable();
        newTable.setNumber(2);
        newTable.setSeats(4);

        // when & then
        mockMvc.perform(patch("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newTable)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTable_whenUserIsAdmin_shouldDeleteTable() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        tableRepository.save(table);
        final Integer tableId = table.getId();

        // when & then
        mockMvc.perform(delete("/tables/{tableId}", tableId))
                .andExpect(status().isNoContent());

        Optional<DiningTable> deletedTable = tableRepository.findById(tableId);
        assertTrue(deletedTable.isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTable_whenTableNotExists_shouldReturnNotFound() throws Exception {
        // given
        final Integer tableId = 7;

        // when & then
        mockMvc.perform(delete("/tables/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table not found for ID: " + tableId + "."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTable_whenTableHasAssignedReservation_shouldReturnConflict() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        tableRepository.save(table);
        final Integer tableId = table.getId();

        final User user = new User();
        user.setLogin("john");
        user.setEmail("john@mail.com");
        user.setPassword("secret");
        user.setPhoneNumber("123456789");

        userRepository.save(user);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(17,0));
        reservation.setDuration(2);
        reservation.setDiningTable(table);
        reservation.setUser(user);

        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(delete("/tables/{tableId}", tableId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Table with " + tableId
                                + " can not be deleted because it has at least one reservation assigned."));
    }

    @Test
    @WithMockUser
    void availableTimes_whenAreReservations_shouldReturnAvailableTimes() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        tableRepository.save(table);
        final Integer tableId = table.getId();

        final User user = new User();
        user.setLogin("john");
        user.setEmail("john@mail.com");
        user.setPassword("secret");
        user.setPhoneNumber("123456789");

        userRepository.save(user);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(17,0));
        reservation.setDuration(2);
        reservation.setDiningTable(table);
        reservation.setUser(user);

        reservationRepository.save(reservation);

        // when & then
        LocalTime reservationStarts = reservation.getReservationTime();
        LocalTime reservationEnds = reservation.getReservationTime().plusHours(reservation.getDuration());
        mockMvc.perform(get("/tables/{tableId}/date/{date}", tableId,
                        reservation.getReservationDate()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(OPENING_TIME + " - " + reservationStarts))
                .andExpect(jsonPath("$[1]").value(reservationEnds + " - " + CLOSING_TIME));
    }

    @Test
    @WithMockUser
    void availableTimes_whenNoReservation_shouldReturnFullTime() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        tableRepository.save(table);
        final Integer tableId = table.getId();

        LocalDate date = LocalDate.now().plusDays(1);

        // when & then
        mockMvc.perform(get("/tables/{tableId}/date/{date}", tableId, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(OPENING_TIME + " - "
                        + CLOSING_TIME));
    }

    @Test
    @WithMockUser
    void availableTables_whenUserIsAuthenticated_shouldGetListOfAvailableTables() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        final DiningTable freeTable = new DiningTable();
        freeTable.setNumber(2);
        freeTable.setSeats(4);

        tableRepository.saveAll(List.of(table, freeTable));

        final User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setLogin("johnny");
        user.setEmail("john@mail.com");
        user.setPassword("secretpassword");
        user.setPhoneNumber("123456789");

        userRepository.save(user);

        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.of(17, 0);

        final Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setDiningTable(table);
        reservation.setReservationDate(date);
        reservation.setReservationTime(time);
        reservation.setDuration(1);

        final Reservation newReservation = new Reservation();
        newReservation.setReservationDate(date);
        newReservation.setReservationTime(time);
        newReservation.setDuration(2);
        newReservation.setDiningTable(table);

        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/tables/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")     .value(1))
                .andExpect(jsonPath("$[0].id")      .value(freeTable.getId()))
                .andExpect(jsonPath("$[0].number")  .value(freeTable.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(freeTable.getSeats()));
    }

    @Test
    @WithMockUser
    void availableTables_whenThereAreNoFreeTablesWithMinSeats_shouldReturnNotFound() throws Exception {
        // given
        final DiningTable table1 = new DiningTable();
        table1.setNumber(1);
        table1.setSeats(2);

        final DiningTable table2 = new DiningTable();
        table2.setNumber(2);
        table2.setSeats(6);

        tableRepository.saveAll(List.of(table1, table2));

        final User user = new User();
        user.setLogin("john");
        user.setEmail("john@mail.com");
        user.setPassword("secretpassword");
        user.setPhoneNumber("123-456-789");

        userRepository.save(user);

        final Reservation savedReservation = new Reservation();
        savedReservation.setReservationDate(LocalDate.now().plusDays(1));
        savedReservation.setReservationTime(LocalTime.of(17,0));
        savedReservation.setDuration(1);
        savedReservation.setDiningTable(table2);
        savedReservation.setUser(user);

        reservationRepository.save(savedReservation);

        final Reservation newReservation = new Reservation();
        newReservation.setReservationDate(LocalDate.now().plusDays(1));
        newReservation.setReservationTime(LocalTime.of(17,0));
        newReservation.setDuration(2);
        newReservation.setDiningTable(table2);
        newReservation.setUser(user);

        // when & then
        mockMvc.perform(get("/tables/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_FREE_TABLES_WAS_FOUND));
    }

    @Test
    @WithMockUser
    void availableTables_whenNoTableWithMinSeats_shouldReturnNotFound() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(2);
        table.setSeats(6);

        final User user = new User();
        user.setLogin("john");
        user.setEmail("john@mail.com");
        user.setPassword("secretpassword");
        user.setPhoneNumber("123-456-789");

        userRepository.save(user);

        final Reservation newReservation = new Reservation();
        newReservation.setReservationDate(LocalDate.now().plusDays(1));
        newReservation.setReservationTime(LocalTime.of(17,0));
        newReservation.setDuration(2);
        newReservation.setDiningTable(table);
        newReservation.setUser(user);

        // when & then
        mockMvc.perform(get("/tables/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no tables with the required number of seats ("
                                + table.getSeats() + ")."));
    }
}