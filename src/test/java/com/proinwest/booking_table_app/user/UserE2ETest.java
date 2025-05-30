package com.proinwest.booking_table_app.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.proinwest.booking_table_app.security.auth.LoginRequest;
import com.proinwest.booking_table_app.security.userDetails.CustomUserDetails;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.jayway.jsonpath.JsonPath.read;
import static com.proinwest.booking_table_app.reservation.ReservationService.INPUT_IS_MISSING;
import static com.proinwest.booking_table_app.user.UserService.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserE2ETest {
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
    void getAllUsers_whenUserIsAdmin_shouldFetchAllUsers() throws Exception {
        // given
        user.setId(createUser(user));
        admin.setId(createUser(admin));
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());

        // when & then
        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(2))
                .andExpect(jsonPath("$[0].id")          .value(user.getId()))
                .andExpect(jsonPath("$[0].login")       .value(user.getLogin()))
                .andExpect(jsonPath("$[0].firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$[0].email")       .value(user.getEmail()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$[0].role")        .value(user.getRole()))
                .andExpect(jsonPath("$[0].active")      .value(user.isActive()))
                .andExpect(jsonPath("$[1].id")          .value(admin.getId()))
                .andExpect(jsonPath("$[1].login")       .value(admin.getLogin()))
                .andExpect(jsonPath("$[1].firstName")   .value(admin.getFirstName()))
                .andExpect(jsonPath("$[1].lastName")    .value(admin.getLastName()))
                .andExpect(jsonPath("$[1].email")       .value(admin.getEmail()))
                .andExpect(jsonPath("$[1].phoneNumber") .value(admin.getPhoneNumber()))
                .andExpect(jsonPath("$[1].role")        .value(admin.getRole()))
                .andExpect(jsonPath("$[1].active")      .value(admin.isActive()));
    }

    @Test
    void getAllUsers_whenThereAreNoUsersInDatabase_shouldReturnNotFound() throws Exception {
        // given
        authenticateAs(admin);

        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_USERS_IN_DATABASE));
    }

    @Test
    void getAllUsers_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        // when & then
        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    void whenUserIsAuthenticated_shouldCreateAndFetchOwnUserDetails() throws Exception {
        // when & then
        final Long userId = createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        mockMvc.perform(get("/users/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
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
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long userId = 111L;

        // when & then
        mockMvc.perform(get("/users/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
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
    void updateUser_whenParamsAreValid_shouldUpdateUser() throws Exception {
        // given
        final Long userId = createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        final User updatedUser = new User();
        updatedUser.setLogin("smith");
        updatedUser.setPassword("qwerty123456");
        updatedUser.setFirstName("Sam");
        updatedUser.setLastName("Smith");
        updatedUser.setEmail("smith@mail.pl");
        updatedUser.setPhoneNumber("987-654 221");

        // when
        mockMvc.perform(patch("/users/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userId))
                .andExpect(jsonPath("$.login")      .value(updatedUser.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(updatedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(updatedUser.getLastName()))
                .andExpect(jsonPath("$.email")      .value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedUser.getPhoneNumber()))
                .andExpect(jsonPath("$.role")       .value(user.getRole()))
                .andExpect(jsonPath("$.active")     .value(user.isActive()));

        // then
        final String newToken = obtainJwtToken(updatedUser.getLogin(), updatedUser.getPassword());

        mockMvc.perform(get("/users/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userId))
                .andExpect(jsonPath("$.login")      .value(updatedUser.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(updatedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(updatedUser.getLastName()))
                .andExpect(jsonPath("$.email")      .value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedUser.getPhoneNumber()));
    }

    @Test
    void updateUser_whenUserParamsAreInvalid_shouldReturnBadRequest() throws Exception {
        // given
        final Long userId = createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        final User invalidUser = new User();
        invalidUser.setLogin("a");
        invalidUser.setPassword("a");
        invalidUser.setFirstName(null);
        invalidUser.setLastName(" ");
        invalidUser.setEmail("a");
        invalidUser.setPhoneNumber("1");

        // when
        mockMvc.perform(patch("/users/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.login")      .value(LOGIN_MESSAGE))
                .andExpect(jsonPath("$.password")   .value(PASSWORD_MESSAGE))
                .andExpect(jsonPath("$.lastName")   .value(FIELD_REQUIRED))
                .andExpect(jsonPath("$.email")      .value(WRONG_EMAIL))
                .andExpect(jsonPath("$.phoneNumber").value(PHONE_MESSAGE));
    }

    @Test
    void updateUserRole_whenUserIsAdmin_shouldUpdateUserRole() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long userId = createUser(user);
        final String newRole = "ADMIN";

        // when
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"" + newRole + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userId))
                .andExpect(jsonPath("$.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.role")       .value(newRole));

        // then
        mockMvc.perform(get("/users/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userId))
                .andExpect(jsonPath("$.login")      .value(user.getLogin()))
                .andExpect(jsonPath("$.firstName")  .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")   .value(user.getLastName()))
                .andExpect(jsonPath("$.email")      .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber").value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.role")       .value(newRole));
    }

    @Test
    void updateUserRole_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        final Long userId = createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        final String newRole = "ADMIN";

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"" + newRole + "\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    @Test
    void updateUserRole_whenUserNotExists_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long userId = 111L;
        final String newRole = "ADMIN";

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"" + newRole + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    void updateUserRole_whenRoleIsBlank_shouldReturnBadRequest() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long userId = createUser(user);
        final String newRole = " ";

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"" + newRole + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ROLE_IS_REQUIRED));
    }

    @Test
    void updateUserRole_whenRoleIsInvalid_shouldReturnBadRequest() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long userId = createUser(user);
        final String newRole = "ANDRZEJ";

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"" + newRole + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(INVALID_ROLE));
    }

    @Test
    void deactivateUser_whenUserIsAdmin_shouldDeactivateUser() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final Long userId = createUser(user);

        // when
        mockMvc.perform(patch("/users/deactivate/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " deactivated successfully."));

        // then
        mockMvc.perform(get("/users/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deactivateUser_whenUserIsOwner_shouldDeactivateUser() throws Exception {
        // given
        final Long userId = createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());

        // when
        mockMvc.perform(patch("/users/deactivate/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " deactivated successfully."));

        // then
        mockMvc.perform(get("/users/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void searchUser_whenUserIsAdmin_shouldReturnFoundUsers() throws Exception {
        // given
        createUser(admin);
        createUser(user);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final String query = "john";

        // when & then
        mockMvc.perform(get("/users/search?query={query}", query)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")  .value(1))
                .andExpect(jsonPath("$[0].login").value(user.getLogin()))
                .andExpect(jsonPath("$[0].email").value(user.getEmail()));
    }

    @Test
    void searchUser_whenQueryIsBlank_shouldReturnBadRequest() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final String query = " ";

        // when & then
        mockMvc.perform(get("/users/search?query={query}", query)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(INPUT_IS_MISSING));
    }

    @Test
    void searchUser_whenNoUserWasFound_shouldReturnNotFound() throws Exception {
        // given
        createUser(admin);
        final String token = obtainJwtToken(admin.getLogin(), admin.getPassword());
        final String query = "aaaaaa";

        // when & then
        mockMvc.perform(get("/users/search?query={query}", query)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no users containing: " + query));
    }

    @Test
    void searchUser_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        createUser(user);
        final String token = obtainJwtToken(user.getLogin(), user.getPassword());
        final String query = "a";

        // when & then
        mockMvc.perform(get("/users/search?query={query}", query)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED));
    }

    private Long createUser(User user) throws Exception {
        final ResultActions postResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        final String responseContent = postResult.andReturn().getResponse().getContentAsString();
        final Integer userId = read(responseContent, "$.id");
        return Long.valueOf(userId);
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

    private void authenticateAs(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute("TRUNCATE TABLE dining_table");
        jdbcTemplate.execute("TRUNCATE TABLE user");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }
}