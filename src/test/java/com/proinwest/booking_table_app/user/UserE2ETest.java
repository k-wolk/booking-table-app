package com.proinwest.booking_table_app.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.proinwest.booking_table_app.diningTable.DiningTable;
import com.proinwest.booking_table_app.reservation.Reservation;
import com.proinwest.booking_table_app.security.jwt.JwtUtils;
import com.proinwest.booking_table_app.security.userDetails.CustomUserDetails;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static com.proinwest.booking_table_app.reservation.ReservationService.MIN_DURATION;
import static com.proinwest.booking_table_app.reservation.ReservationService.OPENING_TIME;
import static com.proinwest.booking_table_app.security.jwt.SecurityUtils.ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER;
import static com.proinwest.booking_table_app.user.UserService.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserE2ETest {
    @MockBean
    private JwtUtils jwtUtils;
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.4.0");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private User user;
    private User admin;
    
    @BeforeEach
    void setUp() {
        cleanDatabase();
        
        user = new User();
        user.setLogin("john");
        user.setPassword(new BCryptPasswordEncoder().encode("secretpassword"));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPhoneNumber("123-456-789");
        user.setRole("USER");
        user.setActive(true);

        admin = new User();
        admin.setLogin("ann");
        admin.setPassword(new BCryptPasswordEncoder().encode("secretpassword"));
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
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_whenUserIsAdmin_shouldFetchAllUsers() throws Exception {
        // given
        user.setId(createUser(user));

        // when & then
        mockMvc.perform(get("/users"))
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
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_whenThereAreNoUsersInDatabase_shouldThrowException() throws Exception {
        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_USERS_IN_DATABASE));
    }

    @Test
    @WithMockUser
    void getAllUsers_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    void whenUserIsAuthenticated_shouldCreateAndFetchOwnUserDetails() throws Exception {
        // when & then
//        System.out.println(user.getId());
//        user.setId(userId);
//        System.out.println(user.getId());
//        authenticateAs(user);

        user.setId(createUser(user));
        final Long userId = user.getId();
        authenticateAs(user);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userId))
                .andExpect(jsonPath("$.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(user.getPhoneNumber()));
    }

    @Test
    void getUser_whenUserNotFoundById_shouldReturnNotFound() throws Exception {
        // given
        Long userId = createUser(user);

        // when & then
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void registerUser_whenUserParamsAreInvalid_shouldReturnBadRequest() throws Exception {
        // given
        user.setLogin(null);
        user.setPassword(null);
        user.setFirstName(null);
        user.setLastName(null);
        user.setEmail(null);
        user.setPhoneNumber(null);

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.login")      .value(FIELD_REQUIRED + LOGIN_MESSAGE))
                .andExpect(jsonPath("$.password")   .value(FIELD_REQUIRED + PASSWORD_MESSAGE))
                .andExpect(jsonPath("$.firstName")  .value(FIELD_REQUIRED))
                .andExpect(jsonPath("$.lastName")   .value(FIELD_REQUIRED))
                .andExpect(jsonPath("$.email")      .value(FIELD_REQUIRED + EMAIL_MESSAGE))
                .andExpect(jsonPath("$.phoneNumber").value(FIELD_REQUIRED + PHONE_MESSAGE));
    }

    @Test
    void registerUser_whenUserParamsAreValid_shouldCreateUser() throws Exception {
        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.role")       .value(user.getRole()));
    }

    @Test
    void registerUser_whenUserAlreadyExists_shouldReturnBadRequest() throws Exception {
        // given
        createUser(user);

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.login")
                        .value("Login " + user.getLogin() + " already exists. It should be unique."))
                .andExpect(jsonPath("$.email")
                        .value("Email address " + user.getEmail() + " already exists. It should be unique."));
    }

    @Test
    void updateUser_shouldUpdateUser() throws Exception {
        // given
        final Long userId = createUser(user);

        final User updatedUser = new User();
        updatedUser.setLogin("smith");
        updatedUser.setPassword("qwerty123456");
        updatedUser.setFirstName("Sam");
        updatedUser.setLastName("Smith");
        updatedUser.setEmail("smith@mail.pl");
        updatedUser.setPhoneNumber("987-654 221");

        // when
        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userId))
                .andExpect(jsonPath("$.login")      .value(updatedUser.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(updatedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(updatedUser.getLastName()))
                .andExpect(jsonPath("$.email")      .value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedUser.getPhoneNumber()));

        // then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userId))
                .andExpect(jsonPath("$.login")      .value(updatedUser.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(updatedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(updatedUser.getLastName()))
                .andExpect(jsonPath("$.email")      .value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedUser.getPhoneNumber()));
    }

    @Test
    void updateUser_whenUserNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long userId = 1L;

        // when & then
        mockMvc.perform(put("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void updateUser_whenUserParamsAreInvalid_shouldThrowException() throws Exception {
        // given
        final Long userId = createUser(user);

        final User invalidUser = new User();
        invalidUser.setLogin("a");
        invalidUser.setPassword("a");
        invalidUser.setFirstName(null);
        invalidUser.setLastName(null);
        invalidUser.setEmail("a");
        invalidUser.setPhoneNumber("1");

        // when
        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.login")      .value(LOGIN_MESSAGE))
                .andExpect(jsonPath("$.password")   .value(PASSWORD_MESSAGE))
                .andExpect(jsonPath("$.firstName")  .value(FIELD_REQUIRED))
                .andExpect(jsonPath("$.lastName")   .value(FIELD_REQUIRED))
                .andExpect(jsonPath("$.email")      .value(WRONG_EMAIL))
                .andExpect(jsonPath("$.phoneNumber").value(PHONE_MESSAGE));
    }

    @Test
    void shouldPartiallyUpdateUser() throws Exception {
        // given
        final Long userId = createUser(user);

        final User updatedUser = new User();
        updatedUser.setLogin("smith");
        updatedUser.setEmail("smith@mail.pl");
        updatedUser.setPhoneNumber("987-654 221");

        // when
        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userId))
                .andExpect(jsonPath("$.login")      .value(updatedUser.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$.email")      .value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedUser.getPhoneNumber()));

        // then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userId))
                .andExpect(jsonPath("$.login")      .value(updatedUser.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$.email")      .value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedUser.getPhoneNumber()));
    }

    @Test
    void partiallyUpdateUser_whenUserNotFoundById_shouldThrowException() throws Exception {
        // given
        final Long userId = 1L;

        // when & then
        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void partiallyUpdateUser_whenUserParamsAreInvalid_shouldThrowException() throws Exception {
        // given
        final Long userId = createUser(user);

        final User invalidUser = new User();
        invalidUser.setLogin("a");
        invalidUser.setPassword("a");
        invalidUser.setEmail("a");
        invalidUser.setPhoneNumber("1");

        // when
        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.login")      .value(LOGIN_MESSAGE))
                .andExpect(jsonPath("$.password")   .value(PASSWORD_MESSAGE))
                .andExpect(jsonPath("$.email")      .value(WRONG_EMAIL))
                .andExpect(jsonPath("$.phoneNumber").value(PHONE_MESSAGE));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // given
        final Long userId = createUser(user);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        // when
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void whenUserNotExists_shouldThrowException() throws Exception {
        // given
        final Long userId = 1L;

        // when
        mockMvc.perform(delete("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void whenUserHasAssignedReservation_shouldThrowException() throws Exception {
        // given
        final Long userId = createUser(user);
        user.setId(userId);

        final DiningTable table = new DiningTable();
        table.setNumber(1);
        table.setSeats(2);

        final Integer tableId = createTable(table);
        table.setId(tableId);

        final Reservation reservation = new Reservation();
        reservation.setReservationDate(LocalDate.now().plusDays(1));
        reservation.setReservationTime(OPENING_TIME);
        reservation.setDuration(MIN_DURATION);
        reservation.setUser(user);
        reservation.setDiningTable(table);

        createReservation(reservation);

        // when & then
        mockMvc.perform(delete("/users/{id}",userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId
                                + " can not be deleted because he has at least one reservation assigned."));
    }

    @Test
    void shouldFindUsersByAnyStringField() throws Exception {
        // given
        final User user2 = new User();
        user2.setLogin("doe");
        user2.setPassword("secretpassword");
        user2.setFirstName("John");
        user2.setLastName("Smith");
        user2.setEmail("john2@mail.com");
        user2.setPhoneNumber("123-456-789");

        final User user3 = new User();
        user3.setLogin("johnny");
        user3.setPassword("secretpassword");
        user3.setFirstName("Doe");
        user3.setLastName("John");
        user3.setEmail("john3@mail.com");
        user3.setPhoneNumber("123-456-789");

        final User user4 = new User();
        user4.setLogin("sam");
        user4.setPassword("secretpassword");
        user4.setFirstName("John");
        user4.setLastName("John");
        user4.setEmail("doe@mail.com");
        user4.setPhoneNumber("123-456-789");

        final User user5 = new User();
        user5.setLogin("login");
        user5.setPassword("secretpassword");
        user5.setFirstName("John");
        user5.setLastName("John");
        user5.setEmail("sam@mail.com");
        user5.setPhoneNumber("123-456-789");

        createUser(user);
        createUser(user2);
        createUser(user3);
        createUser(user4);
        createUser(user5);

        final String searchingPhrase = "dO";

        // when & then
        mockMvc.perform(get("/users/search/{searchingPhrase}", searchingPhrase))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")  .value(4))
                .andExpect(jsonPath("$[0].login").value(user.getLogin()))
                .andExpect(jsonPath("$[0].email").value(user.getEmail()))
                .andExpect(jsonPath("$[1].login").value(user2.getLogin()))
                .andExpect(jsonPath("$[1].email").value(user2.getEmail()))
                .andExpect(jsonPath("$[2].login").value(user3.getLogin()))
                .andExpect(jsonPath("$[2].email").value(user3.getEmail()))
                .andExpect(jsonPath("$[3].login").value(user4.getLogin()))
                .andExpect(jsonPath("$[3].email").value(user4.getEmail()));
    }

    @Test
    void whenUserNotFoundByAnyStringField_shouldThrowException() throws Exception {
        // given
        final String searchingPhrase = "a";

        // when & then
        mockMvc.perform(get("/users/search/{searchingPhrase}", searchingPhrase))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no users containing login, name, email or phone number: "
                                + searchingPhrase));
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

    private Integer createTable(DiningTable table) throws Exception {
        final ResultActions tablePostResult = mockMvc.perform(post("/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(table)))
                .andExpect(status().isCreated());

        final String tableResponseContent = tablePostResult.andReturn().getResponse().getContentAsString();
        return JsonPath.read(tableResponseContent, "$.id");
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

    private void authenticateAs(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}