package com.proinwest.booking_table_app.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.security.config.AuthEntryPointJwt;
import com.proinwest.booking_table_app.security.config.SecurityConfig;
import com.proinwest.booking_table_app.security.jwt.JwtUtils;
import com.proinwest.booking_table_app.security.userDetails.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class UserControllerWebLayerTest {
    @MockBean
    private UserService userService;
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(111L);
        user.setLogin("john");
        user.setFirstName("Sam");
        user.setLastName("Doe");
        user.setEmail("ann@mail.com");
        user.setPhoneNumber("123-456-789");
        user.setPassword("secretpassword");
        user.setRole("USER");
        user.setActive(true);

        userDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isActive()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_whenUserIsAdmin_shouldFetchAllUsers() throws Exception {
        // given
        final List<UserDTO> allUsers = new ArrayList<>();
        allUsers.add(userDTO);

        when(userService.getAllUsers()).thenReturn(allUsers);

        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(allUsers.size()))
                .andExpect(jsonPath("$[0].id")          .value(userDTO.id()))
                .andExpect(jsonPath("$[0].login")       .value(userDTO.login()))
                .andExpect(jsonPath("$[0].firstName")   .value(userDTO.firstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(userDTO.lastName()))
                .andExpect(jsonPath("$[0].email")       .value(userDTO.email()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(userDTO.phoneNumber()));

        verify(userService,  times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    @Test
    @WithMockUser
    void getUser_whenUserIsAuthenticated_shouldFetchUserById() throws Exception {
        // given
        final Long userId = user.getId();

        when(userService.getUser(userId)).thenReturn(userDTO);

        // when & then
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userDTO.id()))
                .andExpect(jsonPath("$.login")      .value(userDTO.login()))
                .andExpect(jsonPath("$.firstName")  .value(userDTO.firstName()))
                .andExpect(jsonPath("$.lastName")   .value(userDTO.lastName()))
                .andExpect(jsonPath("$.email")      .value(userDTO.email()))
                .andExpect(jsonPath("$.phoneNumber").value(userDTO.phoneNumber()));

        verify(userService, times(1)).getUser(userId);
    }

    @Test
    @WithMockUser
    void registerUser_whenUserIsAuthenticated_shouldRegisterUser() throws Exception {
        // given
        final Long userId = user.getId();
        user.setId(null);

        when(userService.registerUser(any(User.class))).thenReturn(userDTO);
        when(userService.location(any(User.class))).thenReturn(URI.create("/users/" + userId));

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + userId))
                .andExpect(jsonPath("$.id")         .value(userDTO.id()))
                .andExpect(jsonPath("$.login")      .value(userDTO.login()))
                .andExpect(jsonPath("$.firstName")  .value(userDTO.firstName()))
                .andExpect(jsonPath("$.lastName")   .value(userDTO.lastName()))
                .andExpect(jsonPath("$.email")      .value(userDTO.email()))
                .andExpect(jsonPath("$.phoneNumber").value(userDTO.phoneNumber()));

        verify(userService, times(1)).registerUser(any(User.class));
        verify(userService, times(1)).location(any(User.class));
    }

    @Test
    @WithMockUser
    void updateUser_whenUserIsAuthenticated_shouldUpdateUser() throws Exception {
        // given
        final Long userId = user.getId();

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(userDTO);

        // when & then
        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(userDTO.id()))
                .andExpect(jsonPath("$.login")      .value(userDTO.login()))
                .andExpect(jsonPath("$.firstName")  .value(userDTO.firstName()))
                .andExpect(jsonPath("$.lastName")   .value(userDTO.lastName()))
                .andExpect(jsonPath("$.email")      .value(userDTO.email()))
                .andExpect(jsonPath("$.phoneNumber").value(userDTO.phoneNumber()));

        verify(userService, times(1)).updateUser(eq(userId), any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_whenUserIsAdmin_shouldUpdateUserRole() throws Exception {
        // given
        final Long userId = user.getId();
        final String newRole = "ADMIN";

        UserDTO updatedUserDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                newRole,
                user.isActive()
        );

        when(userService.updateUserRole(eq(userId), any(Map.class))).thenReturn(updatedUserDTO);

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"" + newRole + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")         .value(updatedUserDTO.id()))
                .andExpect(jsonPath("$.login")      .value(updatedUserDTO.login()))
                .andExpect(jsonPath("$.firstName")  .value(updatedUserDTO.firstName()))
                .andExpect(jsonPath("$.lastName")   .value(updatedUserDTO.lastName()))
                .andExpect(jsonPath("$.email")      .value(updatedUserDTO.email()))
                .andExpect(jsonPath("$.phoneNumber").value(updatedUserDTO.phoneNumber()))
                .andExpect(jsonPath("$.role")       .value(newRole));

        verify(userService, times(1)).updateUserRole(eq(userId), any(Map.class));
    }

    @Test
    @WithMockUser
    void updateUserRole_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        final Long userId = user.getId();
        final String newRole = "ADMIN";

        // when & then
        mockMvc.perform(patch("/users/role/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"" + newRole + "\"}"))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUserRole(eq(userId), any(Map.class));
    }

    @Test
    @WithMockUser
    void deactivateUser_whenUserIsAuthenticated_shouldDeactivateUser() throws Exception {
        // given
        final Long userId = user.getId();

        doNothing().when(userService).deactivateUser(userId);

        // when & then
        mockMvc.perform(patch("/users/deactivate/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User with id " + userId + " deactivated successfully."));

        verify(userService, times(1)).deactivateUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_whenUserIsAdmin_shouldFindUserByQuery() throws Exception {
        // given
        final String query = "oH";

        final List<UserDTO> searchResult = new ArrayList<>();
        searchResult.add(userDTO);

        when(userService.searchUsers(query)).thenReturn(searchResult);

        // when & then
        mockMvc.perform(get("/users/search?query={query}", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(searchResult.size()))
                .andExpect(jsonPath("$[0].id")          .value(userDTO.id()))
                .andExpect(jsonPath("$[0].login")       .value(userDTO.login()))
                .andExpect(jsonPath("$[0].firstName")   .value(userDTO.firstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(userDTO.lastName()))
                .andExpect(jsonPath("$[0].email")       .value(userDTO.email()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(userDTO.phoneNumber()));

        verify(userService, times(1)).searchUsers(query);
    }

    @Test
    @WithMockUser
    void searchUsers_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        // given
        final String query = "oH";

        // when & then
        mockMvc.perform(get("/users/search?query={query}", query))
                .andExpect(status().isForbidden());

        verify(userService, never()).searchUsers(query);
    }
}