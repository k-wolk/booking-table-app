package com.proinwest.booking_table_app.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proinwest.booking_table_app.security.config.AuthEntryPointJwt;
import com.proinwest.booking_table_app.security.userDetails.CustomUserDetailsService;
import com.proinwest.booking_table_app.security.jwt.JwtUtils;
import com.proinwest.booking_table_app.security.config.SecurityConfig;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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

        userDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllUsers() throws Exception {
        // given
        final List<UserDTO> allUsers = new ArrayList<>();
        allUsers.add(userDTO);

        when(userService.getAllUsers()).thenReturn(allUsers);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users"))
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
    @WithMockUser
    void shouldGetUserById() throws Exception {
        // given
        final Long userId = user.getId();

        when(userService.getUser(userId)).thenReturn(userDTO);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{userId}", userId))
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
    void shouldRegisterUser() throws Exception {
        // given
        final Long userId = user.getId();

        user.setId(null);

        when(userService.registerUser(any(User.class))).thenReturn(userDTO);
        when(userService.location(any(User.class))).thenReturn(URI.create("/users/" + userId));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
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
    void shouldUpdateUser() throws Exception {
        // given
        final Long userId = user.getId();

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(userDTO);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/{id}", userId)
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
    void shouldFindUsersByQuery() throws Exception {
        // given
        final String query = "oH";

        final List<UserDTO> searchResult = new ArrayList<>();
        searchResult.add(userDTO);

        when(userService.searchUsers(query)).thenReturn(searchResult);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search?query={name}", query))
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
}