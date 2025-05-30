package com.proinwest.booking_table_app.user;

import com.proinwest.booking_table_app.exceptions.types.CustomSecurityException;
import com.proinwest.booking_table_app.exceptions.types.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.types.NotFoundException;
import com.proinwest.booking_table_app.exceptions.types.ValidationException;
import com.proinwest.booking_table_app.reservation.ReservationService;
import com.proinwest.booking_table_app.security.jwt.SecurityUtils;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDTOMapper userDTOMapper;
    @Mock
    private UserValidator userValidator;
    @Mock
    private ReservationService reservationService;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_whenExists_shouldFetchListOfAllUsers() {
        // given
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        final List<UserDTO> result = userService.getAllUsers();

        // then
        assertNotNull(result);
        assertEquals(List.of(userDTO), result);
        verify(userRepository, times(1)).findAll();
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void getAllUsers_whenUsersListIsEmpty_shouldThrowException() {
        // given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, userService::getAllUsers);
        verify(userRepository,times(1)).findAll();
    }

    @Test
    void getUser_whenExists_shouldGetUser() {
        // given
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);
        final Long userId = user.getId();

        doNothing().when(securityUtils).isAdminOrOwner(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        final UserDTO result = userService.getUser(userId);

        // then
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(userRepository, times(1)).findById(userId);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void getUser_whenNotExists_shouldThrowException() {
        // given
        final Long userId = 1L;

        doNothing().when(securityUtils).isAdminOrOwner(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.getUser(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void registerUser_whenParamsAreValid_shouldAddUser() {
        // given
        final User user = Instancio.create(User.class);
        user.setId(null);

        final User savedUser = user;
        savedUser.setId(1L);

        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(passwordEncoder.encode(user.getPassword())).thenReturn(user.getPassword());
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userDTOMapper.apply(savedUser)).thenReturn(userDTO);
        when(userValidator.validateNewUser(user)).thenReturn(Collections.emptyMap());

        // when
        final UserDTO result = userService.registerUser(user);

        // then
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(passwordEncoder, times(1)).encode(user.getPassword());
        verify(userRepository, times(1)).save(user);
        verify(userDTOMapper, times(1)).apply(savedUser);
        verify(userValidator, times(1)).validateNewUser(user);
    }

    @Test
    void location_shouldGenerateCorrectLocationUri() {
        // given
        final User user = mock(User.class);
        user.setId(7L);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setRequestURI("/users");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(user.getId()).thenReturn(7L);

        final URI expected = URI.create("http://localhost/users/7");

        // when
        final URI result = userService.location(user);

        // then
        assertEquals(expected, result);
    }

    @Test
    void updateUser_whenParamsAreValid_shouldUpdateUser() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();

        final User userToUpdate = Instancio.create(User.class);
        userToUpdate.setId(userId);

        final User savedUser = userToUpdate;

        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(securityUtils.isOwner(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(userToUpdate)).thenReturn(savedUser);
        when(userDTOMapper.apply(savedUser)).thenReturn(userDTO);
        when(userValidator.validateUserToUpdate(user, userId)).thenReturn(Collections.emptyMap());

        // when
        final UserDTO result = userService.updateUser(userId, user);

        // then
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(securityUtils, times(1)).isOwner(userId);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(userToUpdate);
        verify(userDTOMapper, times(1)).apply(savedUser);
        verify(userValidator, times(1)).validateUserToUpdate(user, userId);
    }

    @Test
    void updateUser_whenUserIsNotOwner_shouldThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();

        when(securityUtils.isOwner(userId)).thenReturn(false);

        // when & then
        assertThrows(CustomSecurityException.class, () -> userService.updateUser(userId, user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenUserNotFoundById_shouldThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();

        when(securityUtils.isOwner(userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.updateUser(userId, user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_whenParamsAreValid_shouldUpdateUserRole() {
        // given
        final String newRole = "ADMIN";
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();
        user.setRole("USER");
        user.setActive(true);

        final UserDTO userDTO = new UserDTO(
                userId,
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                newRole,
                user.isActive()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        final UserDTO result = userService.updateUserRole(userId, Map.of("role", newRole));

        // then
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void updateUserRole_whenUserNotFoundById_shouldThrowException() {
        // given
        final Long userId = 1L;
        final String newRole = "ADMIN";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.updateUserRole(userId, Map.of("role", newRole)));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_whenRoleIsBlank_shouldThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.updateUserRole(userId, Map.of("role", " ")));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_whenRoleIsInvalid_shouldThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = 1L;
        final String invalidRole = "INVALID_ROLE";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.updateUserRole(userId, Map.of("role", invalidRole)));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_whenUserExists_shouldDeactivateUser() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();
        user.setActive(true);

        doNothing().when(securityUtils).isAdminOrOwner(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // when
        userService.deactivateUser(userId);

        // then
        assertFalse(user.isActive());
        verify(securityUtils, times(1)).isAdminOrOwner(userId);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deactivateUser_whenUserNotFound_shouldThrowException() {
        // given
        final Long userId = 1L;

        doNothing().when(securityUtils).isAdminOrOwner(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.deactivateUser(userId));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void searchUsers_whenExists_shouldFindUser() {
        // given
        final String query = "any";
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userRepository.searchUsers(query)).thenReturn(List.of(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        final List<UserDTO> result = userService.searchUsers(query);

        // then
        assertNotNull(result);
        assertEquals(List.of(userDTO), result);
        verify(userRepository, times(1)).searchUsers(query);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void searchUsers_whenQueryIsBlank_shouldThrowException() {
        // given
        final String query = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.searchUsers(query));
        verify(userRepository, never()).searchUsers(query);
    }

    @Test
    void searchUsers_whenUserNotFoundByQuery_shouldThrowException() {
        // given
        final String query = "any";

        when(userRepository.searchUsers(query)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.searchUsers(query));
        verify(userRepository, times(1)).searchUsers(query);
    }

    @Test
    void existsById_whenUserExists_shouldReturnTrue() {
        // given
        final Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        // when
        final boolean result = userService.existsById(userId);

        // then
        assertTrue(result);
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    void existsById_whenUserNotExists_shouldReturnFalse() {
        // given
        final Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        // when
        final boolean result = userService.existsById(userId);

        // then
        assertFalse(result);
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    void existsByLogin_whenUserExists_shouldReturnTrue() {
        // given
        final String login = "any";

        when(userRepository.existsByLogin(login)).thenReturn(true);

        // when
        final boolean result = userService.existsByLogin(login);

        // then
        assertTrue(result);
        verify(userRepository, times(1)).existsByLogin(login);
    }

    @Test
    void existsByLogin_whenUserNotExists_shouldReturnFalse() {
        // given
        final String login = "any";

        when(userRepository.existsByLogin(login)).thenReturn(false);

        // when
        final boolean result = userService.existsByLogin(login);

        // then
        assertFalse(result);
        verify(userRepository, times(1)).existsByLogin(login);
    }

    @Test
    void existsByEmail_whenUserExists_shouldReturnTrue() {
        // given
        final String email = "any@mail.com";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when
        final boolean result = userService.existsByEmail(email);

        // then
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    void existsByEmail_whenUserNotExists_shouldReturnFalse() {
        // given
        final String email = "any@mail.com";

        when(userRepository.existsByEmail(email)).thenReturn(false);

        // when
        final boolean result = userService.existsByEmail(email);

        // then
        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    void validateUserToUpdate_whenValid_shouldNotThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();

        when(userValidator.validateUserToUpdate(user, userId)).thenReturn(Collections.emptyMap());

        // when & then
        assertDoesNotThrow(() -> userService.validateUserToUpdate(user, userId));
        verify(userValidator, times(1)).validateUserToUpdate(user, userId);
    }

    @Test
    void validateUserToUpdate_whenInvalid_shouldThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();
        final Map<String, String> validationMessages = Map.of("error", "Invalid data");

        when(userValidator.validateUserToUpdate(user, userId)).thenReturn(validationMessages);

        // when & then
        assertThrows(ValidationException.class, () -> userService.validateUserToUpdate(user, userId));
        verify(userValidator, times(1)).validateUserToUpdate(user, userId);
    }

    @Test
    void validateNewUser_whenValid_shouldNotThrowException() {
        // given
        final User user = Instancio.create(User.class);

        when(userValidator.validateNewUser(user)).thenReturn(Collections.emptyMap());

        // when & then
        assertDoesNotThrow(() -> userService.validateNewUser(user));
        verify(userValidator, times(1)).validateNewUser(user);
    }

    @Test
    void validateNewUser_whenInvalid_shouldThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Map<String, String> validationMessages = Map.of("error", "Invalid data");

        when(userValidator.validateNewUser(user)).thenReturn(validationMessages);

        // when & then
        assertThrows(ValidationException.class, () -> userService.validateNewUser(user));
        verify(userValidator, times(1)).validateNewUser(user);
    }
}