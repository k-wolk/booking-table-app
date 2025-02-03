package com.proinwest.booking_table_app.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
class UserControllerWebLayerTest {
    @MockBean
    private UserService userService;
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
                user.getPhoneNumber()
        );
    }

    @Test
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
    void shouldGetUserById() throws Exception {
        // given
        final Long userId = user.getId();

        when(userService.getUser(userId)).thenReturn(userDTO);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", userId))
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
    void shouldAddUser() throws Exception {
        // given
        final Long userId = user.getId();

        user.setId(null);

        when(userService.addUser(any(User.class))).thenReturn(userDTO);
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

        verify(userService, times(1)).addUser(any(User.class));
        verify(userService, times(1)).location(any(User.class));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        // given
        final Long userId = user.getId();

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(userDTO);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/{id}", userId)
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
    void shouldPartiallyUpdateUser() throws Exception {
        // given
        final Long userId = user.getId();

        when(userService.partiallyUpdateUser(eq(userId), any(User.class))).thenReturn(userDTO);

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

        verify(userService, times(1)).partiallyUpdateUser(eq(userId), any(User.class));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // given
        final Long userId = user.getId();

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void shouldFindAllUsersByLoginFragment() throws Exception {
        // given
        final String loginFragment = "oH";

        final List<UserDTO> allUsersByLogin = new ArrayList<>();
        allUsersByLogin.add(userDTO);

        when(userService.findAllByLogin(loginFragment)).thenReturn(allUsersByLogin);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/login/{login}", loginFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(allUsersByLogin.size()))
                .andExpect(jsonPath("$[0].id")          .value(userDTO.id()))
                .andExpect(jsonPath("$[0].login")       .value(userDTO.login()))
                .andExpect(jsonPath("$[0].firstName")   .value(userDTO.firstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(userDTO.lastName()))
                .andExpect(jsonPath("$[0].email")       .value(userDTO.email()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(userDTO.phoneNumber()));

        verify(userService, times(1)).findAllByLogin(loginFragment);
    }

    @Test
    void shouldFindAllUsersByFirstNameFragment() throws Exception {
        // given
        final String firstNameFragment = "Am";

        final List<UserDTO> allUsersByFirstName = new ArrayList<>();
        allUsersByFirstName.add(userDTO);

        when(userService.findAllByFirstName(firstNameFragment)).thenReturn(allUsersByFirstName);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/firstname/{firstname}", firstNameFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(allUsersByFirstName.size()))
                .andExpect(jsonPath("$[0].id")          .value(userDTO.id()))
                .andExpect(jsonPath("$[0].login")       .value(userDTO.login()))
                .andExpect(jsonPath("$[0].firstName")   .value(userDTO.firstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(userDTO.lastName()))
                .andExpect(jsonPath("$[0].email")       .value(userDTO.email()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(userDTO.phoneNumber()));

        verify(userService, times(1)).findAllByFirstName(firstNameFragment);
    }

    @Test
    void shouldFindAllUsersByLastNameFragment() throws Exception {
        // given
        final String lastNameFragment = "dO";

        final List<UserDTO> allUsersByLastName = new ArrayList<>();
        allUsersByLastName.add(userDTO);

        when(userService.findAllByLastName(lastNameFragment)).thenReturn(allUsersByLastName);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/lastname/{lastname}", lastNameFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(allUsersByLastName.size()))
                .andExpect(jsonPath("$[0].id")          .value(userDTO.id()))
                .andExpect(jsonPath("$[0].login")       .value(userDTO.login()))
                .andExpect(jsonPath("$[0].firstName")   .value(userDTO.firstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(userDTO.lastName()))
                .andExpect(jsonPath("$[0].email")       .value(userDTO.email()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(userDTO.phoneNumber()));

        verify(userService, times(1)).findAllByLastName(lastNameFragment);
    }

    @Test
    void shouldFindAllUsersByEmailFragment() throws Exception {
        // given
        final String emailFragment = "Nn";

        final List<UserDTO> allUsersByEmail = new ArrayList<>();
        allUsersByEmail.add(userDTO);

        when(userService.findAllByEmail(emailFragment)).thenReturn(allUsersByEmail);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/email/{email}", emailFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(allUsersByEmail.size()))
                .andExpect(jsonPath("$[0].id")          .value(userDTO.id()))
                .andExpect(jsonPath("$[0].login")       .value(userDTO.login()))
                .andExpect(jsonPath("$[0].firstName")   .value(userDTO.firstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(userDTO.lastName()))
                .andExpect(jsonPath("$[0].email")       .value(userDTO.email()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(userDTO.phoneNumber()));

        verify(userService, times(1)).findAllByEmail(emailFragment);
    }

    @Test
    void shouldFindAllUsersByPhoneNumberFragment() throws Exception {
        // given
        final String phoneNumberFragment = "34";

        final List<UserDTO> allUsersByPhoneNumber = new ArrayList<>();
        allUsersByPhoneNumber.add(userDTO);

        when(userService.findAllByPhoneNumber(phoneNumberFragment)).thenReturn(allUsersByPhoneNumber);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/phone/{phonenumber}", phoneNumberFragment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(allUsersByPhoneNumber.size()))
                .andExpect(jsonPath("$[0].id")          .value(userDTO.id()))
                .andExpect(jsonPath("$[0].login")       .value(userDTO.login()))
                .andExpect(jsonPath("$[0].firstName")   .value(userDTO.firstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(userDTO.lastName()))
                .andExpect(jsonPath("$[0].email")       .value(userDTO.email()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(userDTO.phoneNumber()));

        verify(userService, times(1)).findAllByPhoneNumber(phoneNumberFragment);
    }

    @Test
    void shouldFindAllUsersByAnyString() throws Exception {
        // given
        final String anyString = "oH";

        final List<UserDTO> allUsersByAnyString = new ArrayList<>();
        allUsersByAnyString.add(userDTO);

        when(userService.findAllByAnyString(anyString)).thenReturn(allUsersByAnyString);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/users/search/{name}", anyString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")         .value(allUsersByAnyString.size()))
                .andExpect(jsonPath("$[0].id")          .value(userDTO.id()))
                .andExpect(jsonPath("$[0].login")       .value(userDTO.login()))
                .andExpect(jsonPath("$[0].firstName")   .value(userDTO.firstName()))
                .andExpect(jsonPath("$[0].lastName")    .value(userDTO.lastName()))
                .andExpect(jsonPath("$[0].email")       .value(userDTO.email()))
                .andExpect(jsonPath("$[0].phoneNumber") .value(userDTO.phoneNumber()));

        verify(userService, times(1)).findAllByAnyString(anyString);
    }
}