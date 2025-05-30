package com.proinwest.booking_table_app.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.diningTable.DiningTableRepository;
import com.proinwest.booking_table_app.reservation.ReservationRepository;
import com.proinwest.booking_table_app.reservation.ReservationService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static com.proinwest.booking_table_app.user.UserService.*;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {
    @Container
    @ServiceConnection
    private static final MySQLContainer mySQLContainer = new MySQLContainer<>("mysql:8.4.0");
    @MockBean
    private JwtUtils jwtUtils;
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
    private User admin;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        tableRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setLogin("john");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("ann@mail.com");
        user.setPhoneNumber("123-456-789");
        user.setPassword(new BCryptPasswordEncoder().encode("secretpassword"));
        user.setRole("USER");
        user.setActive(true);

        admin = new User();
        admin.setLogin("ann");
        admin.setFirstName("Ann");
        admin.setLastName("Doe");
        admin.setEmail("ann@mail.com");
        admin.setPhoneNumber("999888777");
        admin.setPassword(new BCryptPasswordEncoder().encode("secretpassword"));
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
    void getAllUsers_whenUserIsAdmin_shouldReturnAllUsers() throws Exception {
        // given
        userRepository.save(user);

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
    void getAllUsers_whenUsersNotFound_shouldReturnNotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(NO_USERS_IN_DATABASE));
    }

    @Test
    void getUser_whenUserIsAdmin_shouldGetUserById() throws Exception {
        // given
        userRepository.save(user);
        authenticateAs(admin);

        // when & then
        mockMvc.perform(get("/users/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")          .value(user.getId()))
                .andExpect(jsonPath("$.login")       .value(user.getLogin()))
                .andExpect(jsonPath("$.firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$.email")       .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void getUser_whenUserIsOwner_shouldGetUserById() throws Exception {
        // given
        userRepository.save(user);
        authenticateAs(user);

        // when & then
        mockMvc.perform(get("/users/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")          .value(user.getId()))
                .andExpect(jsonPath("$.login")       .value(user.getLogin()))
                .andExpect(jsonPath("$.firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$.email")       .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber") .value(user.getPhoneNumber()));
    }

    @Test
    void getUser_whenUserNotFoundById_shouldReturnNotFound() throws Exception {
        // given
        final Long notExistingId = 111L;
        userRepository.save(admin);
        authenticateAs(admin);

        // when & then
        mockMvc.perform(get("/users/{userId}", notExistingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + notExistingId + " was not found."));
    }

    @Test
    void registerUser_whenUserNotAuthenticated_shouldAddUser() throws Exception {
        // when & then
        mockMvc.perform(post("/users")
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
    void updateUser_whenUserIsOwner_shouldUpdateUser() throws Exception {
        // given
        userRepository.save(user);
        authenticateAs(user);
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
        mockMvc.perform(patch("/users/{userId}", userId)
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
    void updateUser_whenUserIsAdmin_shouldReturnForbidden() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();
        userRepository.save(admin);
        authenticateAs(admin);

        final User userToUpdate = new User();
        userToUpdate.setId(userId);
        userToUpdate.setLogin("johnny");
        userToUpdate.setFirstName("John");
        userToUpdate.setLastName("Smith");
        userToUpdate.setEmail("john@gmail.com");
        userToUpdate.setPhoneNumber("147-258-369");
        userToUpdate.setPassword("newPassword1");

        // when & then
        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value(ACCESS_DENIED));
    }

    @Test
    void updateUser_whenUserNotFoundById_shouldReturnForbidden() throws Exception {
        // given
        final Long userId = 111L;
        userRepository.save(user);
        authenticateAs(user);

        final User userToUpdate = new User();
//        userToUpdate.setId(userId);
        userToUpdate.setLogin("johnny");
        userToUpdate.setFirstName("John");
        userToUpdate.setLastName("Smith");
        userToUpdate.setEmail("john@gmail.com");
        userToUpdate.setPhoneNumber("147-258-369");
        userToUpdate.setPassword("newPassword1");

        // when & then
        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value(ACCESS_DENIED));
    }

    @Test
    void updateUser_whenUserIsOwner_shouldPartiallyUpdateUser() throws Exception {
        // given
        userRepository.save(user);
        authenticateAs(user);
        final Long userId = user.getId();

        final User userToUpdate = new User();
        userToUpdate.setLogin("johnny");
        userToUpdate.setFirstName("John");
        userToUpdate.setLastName("Smith");

        // when & then
        mockMvc.perform(patch("/users/{userId}", userId)
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
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenUserIsAdmin_shouldUpdatedUserRole() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();
        final Map<String, String> role = Map.of("role", "ADMIN");

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")          .value(userId))
                .andExpect(jsonPath("$.login")       .value(user.getLogin()))
                .andExpect(jsonPath("$.firstName")   .value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName")    .value(user.getLastName()))
                .andExpect(jsonPath("$.email")       .value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNumber") .value(user.getPhoneNumber()))
                .andExpect(jsonPath("$.role")        .value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenUserNotFoundById_shouldReturnNotFound() throws Exception {
        // given
        final Long userId = 111L;
        final Map<String, String> role = Map.of("role", "ADMIN");

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(role)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenRoleIsNull_shouldReturnBadRequest() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();
        final Map<String, String> role = new HashMap<>();
        role.put("role", null);

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(role)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(ROLE_IS_REQUIRED));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenRoleIsBlank_shouldReturnBadRequest() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();
        final Map<String, String> role = new HashMap<>();
        role.put("role", " ");

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(role)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(ROLE_IS_REQUIRED));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRoles_whenRoleIsInvalid_shouldReturnBadRequest() throws Exception {
        // given
        userRepository.save(user);
        final Long userId = user.getId();
        final Map<String, String> role = new HashMap<>();
        role.put("role", "ADMINISTRATOR");

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(role)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(INVALID_ROLE));
    }

    @Test
    void deactivateUser_whenUserIsAdmin_shouldDeactivateUser() throws Exception {
        //
        userRepository.save(admin);
        authenticateAs(admin);
        userRepository.save(user);
        final Long userId = user.getId();

        // when & then
        mockMvc.perform(patch("/users/deactivate/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " deactivated successfully."));

        User deactivatedUser = userRepository.findById(userId).orElseThrow();
        assertFalse(deactivatedUser.isActive());
    }

    @Test
    void deactivateUser_whenUserIsOwner_shouldDeactivateUser() throws Exception {
        //
        userRepository.save(user);
        authenticateAs(user);
        final Long userId = user.getId();

        // when & then
        mockMvc.perform(patch("/users/deactivate/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " deactivated successfully."));

        User deactivatedUser = userRepository.findById(userId).orElseThrow();
        assertFalse(deactivatedUser.isActive());
    }

    @Test
    void deactivateUser_whenUserNotFoundById_shouldReturnNotFound() throws Exception {
        //
        userRepository.save(admin);
        authenticateAs(admin);
        final Long userId = admin.getId() + 111;

        // when & then
        mockMvc.perform(patch("/users/deactivate/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " was not found."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_whenUserIsAdmin_shouldFindAllUsersByQuery() throws Exception {
        // given
        userRepository.save(admin);
        userRepository.save(user);

        final String query = "john ann";

        // when & then
        mockMvc.perform(get("/users/search?query={query}", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_whenQueryIsBlank_shouldReturnBadRequest() throws Exception {
        // given
        final String query = " ";

        // when & then
        mockMvc.perform(get("/users/search?query={query}", query))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(ReservationService.INPUT_IS_MISSING));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_whenUserNotFound_shouldReturnNotFound() throws Exception {
        // given
        final String query = "asdasdad";

        // when & then
        mockMvc.perform(get("/users/search?query={query}", query))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("There are no users containing: " + query));
    }

    private void authenticateAs(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}