package com.proinwest.booking_table_app.user;

import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.reservation.ReservationService;
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
    private PasswordEncoder passwordEncoder;
    @Spy
    @InjectMocks
    private UserService userService;

    @Test
    void shouldGetAllUsers() {
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
    void whenUsersListIsEmpty_shouldThrowException() {
        // given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, userService::getAllUsers);
        verify(userRepository,times(1)).findAll();
    }

    @Test
    void shouldGetUser() {
        // given
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);
        final Long userId = user.getId();

        doNothing().when(userService).isAdminOrOwner(userId);
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
    void getUser_whenUserNotFoundById_shouldThrowException() {
        // given
        final Long userId = 1L;

        doNothing().when(userService).isAdminOrOwner(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.getUser(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void shouldAddUser() {
        // given
        final User user = Instancio.create(User.class);
        user.setId(null);

        final User savedUser = user;
        savedUser.setId(1L);

        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(passwordEncoder.encode(user.getPassword())).thenReturn(user.getPassword());
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userDTOMapper.apply(savedUser)).thenReturn(userDTO);

        // when
        final UserDTO result = userService.registerUser(user);

        // then
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(userRepository, times(1)).save(user);
        verify(userDTOMapper, times(1)).apply(savedUser);
    }

    @Test
    void shouldGenerateCorrectLocationUri() {
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
    void shouldUpdateUser() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();

        final User userToUpdate = Instancio.create(User.class);
        userToUpdate.setId(userId);

        final User savedUser = userToUpdate;

        final UserDTO userDTO = Instancio.create(UserDTO.class);

        doNothing().when(userService).isCurrentUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(userToUpdate)).thenReturn(savedUser);
        when(userDTOMapper.apply(savedUser)).thenReturn(userDTO);

        // when
        final UserDTO result = userService.updateUser(userId, user);

        // then
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(userService, times(1)).isCurrentUser(userId);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(userToUpdate);
        verify(userDTOMapper, times(1)).apply(savedUser);
    }

    @Test
    void updateUser_whenUserNotFoundById_shouldThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();

        doNothing().when(userService).isCurrentUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.updateUser(userId, user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldFindUserByAnyString() {
        // given
        final String searchPhrase = "any";
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userRepository.searchUsers(searchPhrase)).thenReturn(List.of(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        final List<UserDTO> result = userService.searchUsers(searchPhrase);

        // then
        assertNotNull(result);
        assertEquals(List.of(userDTO), result);
        verify(userRepository, times(1)).searchUsers(searchPhrase);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenSearchPhraseIsBlank_shouldThrowException() {
        // given
        final String searchPhrase = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.searchUsers(searchPhrase));
        verify(userRepository, never()).searchUsers(searchPhrase);
    }

    @Test
    void whenUserNotFoundByStringPhrase_shouldThrowException() {
        // given
        final String searchPhrase = "any";

        when(userRepository.searchUsers(searchPhrase)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.searchUsers(searchPhrase));
        verify(userRepository, times(1)).searchUsers(searchPhrase);
    }

    @Test
    void whenUserExistsById_shouldReturnTrue() {
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
    void whenUserNotExistsById_shouldReturnFalse() {
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
    void whenUserExistsByLogin_shouldReturnTrue() {
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
    void whenUserNotExistsByLogin_shouldReturnFalse() {
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
    void whenUserExistsByEmail_shouldReturnTrue() {
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
    void whenUserNotExistsByEmail_shouldReturnFalse() {
        // given
        final String email = "any@mail.com";

        when(userRepository.existsByEmail(email)).thenReturn(false);

        // when
        final boolean result = userService.existsByEmail(email);

        // then
        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }
}