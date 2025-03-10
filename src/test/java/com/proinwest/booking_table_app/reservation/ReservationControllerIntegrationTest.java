package com.proinwest.booking_table_app.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.diningTable.DiningTableRepository;
import com.proinwest.booking_table_app.user.User;
import com.proinwest.booking_table_app.user.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static com.proinwest.booking_table_app.diningTable.DiningTableService.TABLE_ID_IS_REQUIRED;
import static com.proinwest.booking_table_app.reservation.ReservationService.*;

import static com.proinwest.booking_table_app.user.UserService.USER_ID_IS_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class ReservationControllerIntegrationTest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.4.0");
    @Autowired
    private ReservationService reservationService;
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
    void shouldGetAllReservations() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")                 .value(1))
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void whenReservationsNotFound_shouldThrowException() throws Exception {
        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_RESERVATIONS_IN_DATABASE));
    }

    @Test
    void shouldGetReservationById() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/{id}", reservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")                 .value(reservation.getId()))
                .andExpect(jsonPath("$.date")    .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.time")    .value(reservation.getReservationTime().toString()))
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
    void whenReservationNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long id = 111L;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/reservations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void shouldAddReservation() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date")    .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.time")    .value(reservation.getReservationTime().toString()))
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
    void addReservation_whenReservationParamsAreNotValid_shouldReturnErrorsMessage() throws Exception {
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
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(notValidReservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.user")           .value(USER_ID_IS_REQUIRED))
                .andExpect(jsonPath("$.diningTable")    .value(TABLE_ID_IS_REQUIRED))
                .andExpect(jsonPath("$.duration")       .value(FIELD_REQUIRED + DURATION_MESSAGE))
                .andExpect(jsonPath("$.date").value(FIELD_REQUIRED + DATE_MESSAGE))
                .andExpect(jsonPath("$.time").value(FIELD_REQUIRED + TIME_MESSAGE));
    }

    @Test
    void addReservation_whenReservationCollide_shouldThrowException() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Dining table with id " + reservation.getDiningTable().getId()
                                + " is not available at the time."));
    }

    @Test
    void shouldUpdateReservation() throws Exception {
        // given
        userRepository.save(user);
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
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")                 .value(reservation.getId()))
                .andExpect(jsonPath("$.date")    .value(newReservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.time")    .value(newReservation.getReservationTime().toString()))
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
    void updateReservation_whenReservationNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long id = 111L;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reservation with id " + id + " was not found."));
    }

    @Test
    void updateReservation_whenReservationParamsAreNotValid_shouldReturnErrorsMessage() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        final User notValidUser = new User();
        notValidUser.setId(111L);

        final DiningTable notValidTable = new DiningTable();
        notValidTable.setId(222);

        final Reservation notValidReservation = new Reservation();
        notValidReservation.setReservationDate(LocalDate.now().minusDays(1));
        notValidReservation.setReservationTime(CLOSING_TIME.plusHours(1));
        notValidReservation.setDuration(MAX_DURATION + 1);
        notValidReservation.setUser(notValidUser);
        notValidReservation.setDiningTable(notValidTable);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(notValidReservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.duration")       .value(DURATION_MESSAGE))
                .andExpect(jsonPath("$.date").value(DATE_MESSAGE))
                .andExpect(jsonPath("$.time")
                        .value(OPENING_HOURS_MESSAGE + " Try change reservation time and/or duration."))
                .andExpect(jsonPath("$.user")
                        .value("User with id " + notValidUser.getId() + " was not found."))
                .andExpect(jsonPath("$.diningTable")
                        .value("Dining table with id " + notValidTable.getId() + " was not found."));
    }

    @Test
    void updateReservation_whenReservationCollide_shouldThrowException() throws Exception {
        // given
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

        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);
        reservationRepository.save(reservation2);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Dining table with id " + reservation.getDiningTable().getId()
                                + " is not available at the time."));
    }

    @Test
    void shouldPartiallyUpdateReservation() throws Exception {
        // given
        final User newUser = new User();
        newUser.setLogin("ann");
        newUser.setFirstName("Ann");
        newUser.setLastName("Smith");
        newUser.setEmail("smith@mail.pl");
        newUser.setPhoneNumber("987 654-321");
        newUser.setPassword("qwerty123456");

        final DiningTable newTable = new DiningTable();
        newTable.setNumber(table.getNumber() + 1);
        newTable.setSeats(table.getSeats() + 2);

        final Reservation newReservation = new Reservation();
        newReservation.setReservationDate(reservation.getReservationDate().plusDays(1));
        newReservation.setReservationTime(reservation.getReservationTime().plusHours(1));
        newReservation.setDiningTable(newTable);
        newReservation.setUser(newUser);

        userRepository.save(user);
        userRepository.save(newUser);
        tableRepository.save(table);
        tableRepository.save(newTable);
        reservationRepository.save(reservation);

        // when &  then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")                 .value(reservation.getId()))
                .andExpect(jsonPath("$.date")    .value(newReservation.getReservationDate().toString()))
                .andExpect(jsonPath("$.time")    .value(newReservation.getReservationTime().toString()))
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
    void partiallyUpdateReservation_whenReservationNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long id = 111L;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reservation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reservation with id " + id + " was not found."));
    }

    @Test
    void partiallyUpdateReservation_whenReservationParamsAreNotValid_shouldReturnErrorsMessage() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        final User notValidUser = new User();
        notValidUser.setId(111L);

        final DiningTable notValidTable = new DiningTable();
        notValidTable.setId(222);

        final Reservation notValidReservation = new Reservation();
        notValidReservation.setReservationDate(LocalDate.now().minusDays(1));
        notValidReservation.setReservationTime(CLOSING_TIME.plusHours(1));
        notValidReservation.setDuration(MAX_DURATION + 1);
        notValidReservation.setUser(notValidUser);
        notValidReservation.setDiningTable(notValidTable);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(notValidReservation)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.duration")       .value(DURATION_MESSAGE))
                .andExpect(jsonPath("$.date").value(DATE_MESSAGE))
                .andExpect(jsonPath("$.time")
                        .value(OPENING_HOURS_MESSAGE + " Try change reservation time and/or duration."))
                .andExpect(jsonPath("$.user")
                        .value("User with id " + notValidUser.getId() + " was not found."))
                .andExpect(jsonPath("$.diningTable")
                        .value("Dining table with id " + notValidTable.getId() + " was not found."));
    }

    @Test
    void partiallyUpdateReservation_whenReservationCollide_shouldThrowException() throws Exception {
        // given
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

        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);
        reservationRepository.save(reservation2);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newReservation)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Dining table with id " + reservation.getDiningTable().getId()
                                + " is not available at the time."));
    }

    @Test
    void shouldDeleteReservation() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);
        final Long id = reservation.getId();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/reservations/{id}", id))
                .andExpect(status().isNoContent());

        final Optional<Reservation> deletedById = reservationRepository.findById(id);
        assertTrue(deletedById.isEmpty());
    }

    @Test
    void deleteReservation_whenReservationNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long id = 111L;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/reservations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reservation with id " + id + " was not found."));
    }

    @Test
    void shouldFindReservationByDate() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}", reservation.getReservationDate()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void whenReservationNotFoundByDate_shouldThrowException() throws Exception {
        // given
        final LocalDate date = reservation.getReservationDate();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}", date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on date " + date + "."));
    }

    @Test
    void shouldFindReservationByUserId() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/userid/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void whenReservationNotFoundByUserId_shouldThrowException() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/userid/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation booked by user with id: " + userId + "."));
    }

    @Test
    void shouldFindReservationByUserLogin() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/login/{login}", user.getLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void whenReservationNotFoundByUserLogin_shouldThrowException() throws Exception {
        // given
        userRepository.save(user);
        final String login = user.getLogin();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/login/{login}", login))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's login containing: " + login));
    }

    @Test
    void shouldFindReservationByUserFirstName() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/firstname/{firstName}", user.getFirstName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void whenReservationNotFoundByUserFirstName_shouldThrowException() throws Exception {
        // given
        userRepository.save(user);
        final String firstName = user.getFirstName();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/firstname/{firstName}", firstName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's first name containing: " + firstName));
    }

    @Test
    void shouldFindReservationByUserLastName() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/lastname/{lastName}", user.getLastName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void whenReservationNotFoundByUserLastName_shouldThrowException() throws Exception {
        // given
        userRepository.save(user);
        final String lastName = user.getLastName();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/lastname/{lastName}", lastName))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's last name containing: " + lastName));
    }

    @Test
    void shouldFindReservationByUserEmail() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/email/{email}", user.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void whenReservationNotFoundByUserEmail_shouldThrowException() throws Exception {
        // given
        userRepository.save(user);
        final String email = user.getEmail();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/email/{email}", email))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's email containing: " + email));
    }

    @Test
    void shouldFindReservationByUserPhoneNumber() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/phonenumber/{phoneNumber}", user.getPhoneNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void whenReservationNotFoundByUserPhoneNumber_shouldThrowException() throws Exception {
        // given
        userRepository.save(user);
        final String phoneNumber = user.getPhoneNumber();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/phonenumber/{phoneNumber}", phoneNumber))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with user's phone number containing: " + phoneNumber));
    }

    @Test
    void shouldFindReservationByTableId() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/table/{tableId}", table.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void findAllByTableId_whenTableNotExists_shouldThrowException() throws Exception {
        // given
        final Integer tableId = 111;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/table/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Dining table with id " + tableId + " was not found."));
    }

    @Test
    void whenReservationNotFoundByTableId_shouldThrowException() throws Exception {
        // given
        tableRepository.save(table);
        final Integer tableId = table.getId();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/table/{tableId}", tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation with table id: " + tableId));
    }

    @Test
    void shouldFindReservationByDateAndTableId() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}/table/{tableId}",
                                reservation.getReservationDate(), table.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void findAllByDateAndTableId_whenTableNotExists_shouldThrowException() throws Exception {
        // given
        final LocalDate date = LocalDate.now();
        final Integer tableId = 111;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}/table/{tableId}", date, tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Dining table with id " + tableId + " was not found."));
    }

    @Test
    void whenReservationNotFoundByDateAndTableId_shouldThrowException() throws Exception {
        // given
        tableRepository.save(table);
        final Integer tableId = table.getId();
        final LocalDate date = LocalDate.now();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}/table/{tableId}", date, tableId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on date " + date + " at table with id " + tableId + "."));
    }

    @Test
    void shouldFindReservationByDateAndTime() throws Exception {
        // given
        userRepository.save(user);
        tableRepository.save(table);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}/time/{time}",
                                reservation.getReservationDate(), reservation.getReservationTime()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")                  .value(reservation.getId()))
                .andExpect(jsonPath("$[0].date")     .value(reservation.getReservationDate().toString()))
                .andExpect(jsonPath("$[0].time")     .value(reservation.getReservationTime().toString()))
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
    void whenReservationNotFoundByDateAndTime_shouldThrowException() throws Exception {
        // given
        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.of(17,0);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/reservations/search/date/{date}/time/{time}", date, time))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There is no reservation on date " + date + " and time " + time + "."));
    }

}