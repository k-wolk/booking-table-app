package com.proinwest.booking_table_app.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.diningTable.DiningTableRepository;
import com.proinwest.booking_table_app.reservation.Reservation;
import com.proinwest.booking_table_app.reservation.ReservationRepository;
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

import static com.proinwest.booking_table_app.user.UserService.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.4.0");
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private DiningTableRepository tableRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    private User user;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        tableRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setLogin("john");
        user.setFirstName("Sam");
        user.setLastName("Doe");
        user.setEmail("ann@mail.com");
        user.setPhoneNumber("123-456-789");
        user.setPassword("secretpassword");
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
    void shouldReturnAllUsers() throws Exception {
        // given
        userRepository.save(user);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(1))
                .andExpect(jsonPath("$[0].id")          .value(user.getId()))
                .andExpect(jsonPath("$[0].login")       .value(user.getLogin()))
                .andExpect(jsonPath("$[0].firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$[0].email")       .value(user.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void whenUsersNotFound_shouldThrowException() throws Exception {
        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_USERS_IN_DATABASE));
    }

    @Test
    void shouldGetUserById() throws Exception {
        // given
        userRepository.save(user);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")          .value(user.getId()))
                .andExpect(jsonPath("$.login")       .value(user.getLogin()))
                .andExpect(jsonPath("$.firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$.email")       .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void whenUserNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long userId = 111L;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void shouldAddUser() throws Exception {
        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login")       .value(user.getLogin()))
                .andExpect(jsonPath("$.firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$.email")       .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();

        final User userToUpdate = new User();
        userToUpdate.setId(userId);
        userToUpdate.setLogin("johnny");
        userToUpdate.setFirstName("John");
        userToUpdate.setLastName("Smith");
        userToUpdate.setEmail("john@gmail.com");
        userToUpdate.setPhoneNumber("147-258-369");
        userToUpdate.setPassword("newPassword1");

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")          .value(userToUpdate.getId()))
                .andExpect(jsonPath("$.login")       .value(userToUpdate.getLogin()))
                .andExpect(jsonPath("$.firstName")   .value(userToUpdate.getFirstName()))
                .andExpect(jsonPath("$.lastName")    .value(userToUpdate.getLastName()))
                .andExpect(jsonPath("$.email")       .value(userToUpdate.getEmail()))
                .andExpect(jsonPath("$.phoneNumber") .value(userToUpdate.getPhoneNumber()));
    }

    @Test
    void updateUser_whenUserNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long userId = 111L;

        final User userToUpdate = new User();
        userToUpdate.setId(userId);
        userToUpdate.setLogin("johnny");
        userToUpdate.setFirstName("John");
        userToUpdate.setLastName("Smith");
        userToUpdate.setEmail("john@gmail.com");
        userToUpdate.setPhoneNumber("147-258-369");
        userToUpdate.setPassword("newPassword1");

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void shouldPartiallyUpdateUser() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();

        final User userToUpdate = new User();
        userToUpdate.setLogin("johnny");
        userToUpdate.setFirstName("John");
        userToUpdate.setLastName("Smith");

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")          .value(userId))
                .andExpect(jsonPath("$.login")       .value(userToUpdate.getLogin()))
                .andExpect(jsonPath("$.firstName")   .value(userToUpdate.getFirstName()))
                .andExpect(jsonPath("$.lastName")    .value(userToUpdate.getLastName()))
                .andExpect(jsonPath("$.email")       .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void partiallyUpdateUser_whenUserNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long userId = 111L;

        final User userToUpdate = new User();
        userToUpdate.setId(userId);
        userToUpdate.setLogin("johnny");
        userToUpdate.setFirstName("John");
        userToUpdate.setLastName("Smith");
        userToUpdate.setEmail("john@gmail.com");
        userToUpdate.setPhoneNumber("147-258-369");
        userToUpdate.setPassword("newPassword1");

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        final Optional<User> deletedUserById = userRepository.findById(userId);
        assertTrue(deletedUserById.isEmpty());
    }

    @Test
    void whenUserNotExistsById_shouldThrowException() throws Exception {
        // given
        final Long userId = 111L;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void whenUserHasAssignedReservation_shouldThrowException() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();

        final DiningTable table = new DiningTable();
        table.setId(1);
        table.setNumber(1);
        table.setSeats(2);

        tableRepository.save(table);

        final Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(LocalTime.of(17,0));
        reservation.setDuration(1);
        reservation.setDiningTable(table);

        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/users/{id}", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " can not be deleted because he has at least one reservation assigned."));

        reservationRepository.deleteAll();
    }

    @Test
    void shouldFindAllUsersByLoginFragment() throws Exception {
        // given
        userRepository.save(user);

        final String loginFragment = "oH";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/login/{login}", loginFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(1))
                .andExpect(jsonPath("$[0].id")          .value(user.getId()))
                .andExpect(jsonPath("$[0].login")       .value(user.getLogin()))
                .andExpect(jsonPath("$[0].firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$[0].email")       .value(user.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void whenLoginFragmentIsBlank_shouldThrowException() throws Exception {
        // given
        final String loginFragment = " ";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/login/{login}", loginFragment))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(INPUT_IS_MISSING));
    }

    @Test
    void whenLoginNotFound_shouldThrowException() throws Exception {
        // given
        final String loginFragment = "x";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/login/{login}", loginFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no users containing login: " + loginFragment));
    }

    @Test
    void shouldFindAllUsersByFirstNameFragment() throws Exception {
        // given
        userRepository.save(user);

        final String firstNameFragment = "AM";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/firstname/{firstName}", firstNameFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(1))
                .andExpect(jsonPath("$[0].id")          .value(user.getId()))
                .andExpect(jsonPath("$[0].login")       .value(user.getLogin()))
                .andExpect(jsonPath("$[0].firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$[0].email")       .value(user.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void whenFirstNameFragmentIsBlank_shouldThrowException() throws Exception {
        // given
        final String firstNameFragment = " ";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/firstname/{firstName}", firstNameFragment))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(INPUT_IS_MISSING));
    }

    @Test
    void whenFirstNameNotFound_shouldThrowException() throws Exception {
        // given
        final String firstNameFragment = "x";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/firstname/{firstName}", firstNameFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no users containing first name: " + firstNameFragment));
    }

    @Test
    void shouldFindAllUsersByLastNameFragment() throws Exception {
        // given
        userRepository.save(user);

        final String lastNameFragment = "OE";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/lastname/{lastName}", lastNameFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(1))
                .andExpect(jsonPath("$[0].id")          .value(user.getId()))
                .andExpect(jsonPath("$[0].login")       .value(user.getLogin()))
                .andExpect(jsonPath("$[0].firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$[0].email")       .value(user.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void whenLastNameFragmentIsBlank_shouldThrowException() throws Exception {
        // given
        final String lastNameFragment = " ";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/lastname/{lastName}", lastNameFragment))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(INPUT_IS_MISSING));
    }

    @Test
    void whenLastNameNotFound_shouldThrowException() throws Exception {
        // given
        final String lastNameFragment = "x";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/lastname/{lastName}", lastNameFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no users containing last name: " + lastNameFragment));
    }

    @Test
    void shouldFindAllUsersByEmailFragment() throws Exception {
        // given
        userRepository.save(user);

        final String emailFragment = ".com";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/email/{email}", emailFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(1))
                .andExpect(jsonPath("$[0].id")          .value(user.getId()))
                .andExpect(jsonPath("$[0].login")       .value(user.getLogin()))
                .andExpect(jsonPath("$[0].firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$[0].email")       .value(user.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void whenEmailFragmentIsBlank_shouldThrowException() throws Exception {
        // given
        final String emailFragment = " ";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/email/{email}", emailFragment))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(INPUT_IS_MISSING));
    }

    @Test
    void whenEmailNotFound_shouldThrowException() throws Exception {
        // given
        final String emailFragment = "x";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/email/{email}", emailFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no users containing email: " + emailFragment));
    }

    @Test
    void shouldFindAllUsersByPhoneNumberFragment() throws Exception {
        // given
        userRepository.save(user);

        final String phoneNumberFragment = "23-4";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/phone/{phoneNumber}", phoneNumberFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(1))
                .andExpect(jsonPath("$[0].id")          .value(user.getId()))
                .andExpect(jsonPath("$[0].login")       .value(user.getLogin()))
                .andExpect(jsonPath("$[0].firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$[0].email")       .value(user.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void whenPhoneNumberFragmentIsBlank_shouldThrowException() throws Exception {
        // given
        final String phoneNumberFragment = " ";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/phone/{phoneNumber}", phoneNumberFragment))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(INPUT_IS_MISSING));
    }

    @Test
    void whenPhoneNumberNotFound_shouldThrowException() throws Exception {
        // given
        final String phoneNumberFragment = "x";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/phone/{phoneNumber}", phoneNumberFragment))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no users containing phone number: " + phoneNumberFragment));
    }

    @Test
    void shouldFindAllUsersByAnyString() throws Exception {
        // given
        userRepository.save(user);

        final String anyString = "oH";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/{anyString}", anyString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(1))
                .andExpect(jsonPath("$[0].id")          .value(user.getId()))
                .andExpect(jsonPath("$[0].login")       .value(user.getLogin()))
                .andExpect(jsonPath("$[0].firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$[0].email")       .value(user.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void whenAnyStringIsBlank_shouldThrowException() throws Exception {
        // given
        final String anyString = " ";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/{anyString}", anyString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(INPUT_IS_MISSING));
    }

    @Test
    void whenUserNotFound_shouldThrowException() throws Exception {
        // given
        final String anyString = "x";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/search/{anyString}", anyString))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no users containing login, name, email or phone number: " + anyString));
    }
}