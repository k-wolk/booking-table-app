package com.proinwest.booking_table_app.diningTable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.jwt.CustomUserDetailsService;
import com.proinwest.booking_table_app.jwt.JwtUtils;
import com.proinwest.booking_table_app.reservation.Reservation;
import com.proinwest.booking_table_app.reservation.ReservationRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.*;
import static org.junit.jupiter.api.Assertions.*;
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
    private User admin = new User();

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        tableRepository.deleteAll();
        userRepository.deleteAll();

        admin.setLogin("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode("secretpassword"));
        admin.setFirstName("John");
        admin.setLastName("Doe");
        admin.setEmail("john@mail.com");
        admin.setPhoneNumber("111222333");
        admin.setRole("ADMIN");
        userRepository.save(admin);
        userRepository.flush();

        System.out.println("Users in DB: " + userRepository.findByLogin("admin"));
    }

    @AfterAll
    static void stopContainer() {
        mySQLContainer.stop();
    }

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldLoadUserByUsername() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");
        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
    }

    @Test
    void sh() {
        assertTrue(userRepository.findByLogin("admin").isPresent());
    }

    @Test
    void connectionEstablished() {
        assertTrue(mySQLContainer.isCreated());
        assertTrue(mySQLContainer.isRunning());
    }

    @Test
//    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "customUserDetailsService")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldGetAllDiningTables() throws Exception {
        // given
        final DiningTable table1 = new DiningTable();
        table1.setNumber(1);
        table1.setSeats(2);
        table1.setActive(true);

        final DiningTable table2 = new DiningTable();
        table2.setNumber(2);
        table2.setSeats(4);
        table2.setActive(true);

//        User admin = new User();
//        admin.setLogin("admin");
//        admin.setPassword(new BCryptPasswordEncoder().encode("password")); // jeśli hasła są hashowane
//        admin.setRole("ROLE_ADMIN");
//        admin.setActive(true);
//        userRepository.save(admin);

        tableRepository.saveAll(List.of(table1, table2));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/tables"))
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
    void whenTablesNotFound_shouldThrowException() throws Exception {
        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/tables"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_TABLES_IN_DATABASE));
    }

    @Test
    @WithMockUser
    void shouldGetTableById() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        tableRepository.save(table);
        final Integer tableId = table.getId();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/tables/{id}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(tableId))
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));
    }

    @Test
    void whenTableNotFoundById_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 7;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/tables/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table with id " + tableId + " was not found."));
    }

    @Test
    void shouldAddTable() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number") .value(table.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(table.getSeats()));
    }

    @Test
    void shouldUpdateTable() throws Exception {
        // given
        final DiningTable tableToUpdate = new DiningTable();
        tableToUpdate.setNumber(1);
        tableToUpdate.setSeats(2);

        tableRepository.save(tableToUpdate);

        final DiningTable newTable = new DiningTable();
        newTable.setNumber(2);
        newTable.setSeats(4);

        final DiningTable updatedTable = new DiningTable();
        updatedTable.setId(tableToUpdate.getId());
        updatedTable.setNumber(newTable.getNumber());
        updatedTable.setSeats(newTable.getSeats());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tables/{tableId}", tableToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newTable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(updatedTable.getId()))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));
    }

    @Test
    void updateTable_whenTableNotFoundById_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 7;

        final DiningTable newTable = new DiningTable();
        newTable.setNumber(2);
        newTable.setSeats(4);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newTable)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table with id " + tableId + " was not found."));
    }

    @Test
    void shouldPartiallyUpdateTable() throws Exception {
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
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/tables/{tableId}", tableToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newTable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")     .value(updatedTable.getId()))
                .andExpect(jsonPath("$.number") .value(updatedTable.getNumber()))
                .andExpect(jsonPath("$.seats")  .value(updatedTable.getSeats()));
    }

    @Test
    void partiallyUpdateTable_whenTableNotFoundById_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 7;

        final DiningTable newTable = new DiningTable();
        newTable.setNumber(2);
        newTable.setSeats(4);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newTable)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table with id " + tableId + " was not found."));
    }

    @Test
    void shouldDeleteTable() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        tableRepository.save(table);
        final Integer tableId = table.getId();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/tables/{tableId}", tableId))
                .andExpect(status().isNoContent());

        Optional<DiningTable> deletedTable = tableRepository.findById(tableId);
        assertTrue(deletedTable.isEmpty());
    }

    @Test
    void whenTableNotExistsById_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 7;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/tables/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Table with " + tableId + " was not found."));
    }

    @Test
    void whenTableHasAssignedReservation_shouldThrowException() throws Exception {
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
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/tables/{tableId}", tableId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Table with " + tableId
                                + " can not be deleted because it has at least one reservation assigned."));
    }

    @Test
    void shouldGetListOfFreeTables() throws Exception {
        // given
        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        final DiningTable freeTable = new DiningTable();
        freeTable.setNumber(2);
        freeTable.setSeats(4);

        tableRepository.save(table);
        tableRepository.save(freeTable);

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
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/tables/freetables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(jsonPath("$.size()")     .value(1))
                .andExpect(jsonPath("$[0].id")      .value(freeTable.getId()))
                .andExpect(jsonPath("$[0].number")  .value(freeTable.getNumber()))
                .andExpect(jsonPath("$[0].seats")   .value(freeTable.getSeats()));
    }

    @Test
    void whenThereAreNoFreeTablesWithMinSeats_shouldThrowException() throws Exception {
        // given
        final DiningTable table1 = new DiningTable();
        table1.setNumber(1);
        table1.setSeats(2);

        final DiningTable table2 = new DiningTable();
        table2.setNumber(2);
        table2.setSeats(6);

        tableRepository.save(table1);
        tableRepository.save(table2);

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
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/tables/freetables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(NO_FREE_TABLES_WAS_FOUND));
    }
}
