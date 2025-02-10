package com.proinwest.booking_table_app.user;

import com.proinwest.booking_table_app.exceptions.InvalidInputException;
import com.proinwest.booking_table_app.exceptions.NotFoundException;
import com.proinwest.booking_table_app.reservation.ReservationDTO;
import com.proinwest.booking_table_app.reservation.ReservationService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
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

        when(userRepository.save(user)).thenReturn(savedUser);
        when(userDTOMapper.apply(savedUser)).thenReturn(userDTO);

        // when
        final UserDTO result = userService.addUser(user);

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(userToUpdate)).thenReturn(savedUser);
        when(userDTOMapper.apply(savedUser)).thenReturn(userDTO);

        // when
        final UserDTO result = userService.updateUser(userId, user);

        // then
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(userToUpdate);
        verify(userDTOMapper, times(1)).apply(savedUser);
    }

    @Test
    void updateUser_whenUserNotFoundById_shouldThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.updateUser(userId, user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldPartiallyUpdateUser() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();

        final User userToUpdate = Instancio.create(User.class);
        userToUpdate.setId(userId);

        final User savedUser = userToUpdate;

        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(userToUpdate)).thenReturn(savedUser);
        when(userDTOMapper.apply(savedUser)).thenReturn(userDTO);

        // when
        final UserDTO result = userService.partiallyUpdateUser(userId, user);

        // then
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(userToUpdate);
        verify(userDTOMapper, times(1)).apply(savedUser);
    }

    @Test
    void partiallyUpdateUser_whenUserNotFoundById_shouldThrowException() {
        // given
        final User user = Instancio.create(User.class);
        final Long userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.partiallyUpdateUser(userId, user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteUser() {
        // given
        final Long userId = 1L;

        when(userService.existsById(userId)).thenReturn(true);
        when(reservationService.findAllByUserId(userId)).thenReturn(Collections.emptyList());

        // when
        userService.deleteUser(userId);

        // then
        verify(userRepository, times(1)).existsById(userId);
        verify(reservationService, times(1)).findAllByUserId(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void shouldFindUserByLoginFragment() {
        // given
        final String loginFragment = "any";
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByLogin(loginFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByLogin(loginFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(userDTO), result);
        verify(userRepository, times(1)).findAllByLogin(loginFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenLoginFragmentIsBlank_shouldThrowException() {
        // given
        final String loginFragment = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByLogin(loginFragment));
        verify(userRepository, never()).findAllByLogin(loginFragment);
    }

    @Test
    void whenUserNotFoundByLoginFragment_shouldThrowException() {
        // given
        final String loginFragment = "any";

        when(userRepository.findAllByLogin(loginFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByLogin(loginFragment));
        verify(userRepository, times(1)).findAllByLogin(loginFragment);
    }

    @Test
    void shouldFindUserByFirstNameFragment() {
        // given
        final String firstNameFragment = "any";
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByFirstName(firstNameFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByFirstName(firstNameFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(userDTO), result);
        verify(userRepository, times(1)).findAllByFirstName(firstNameFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenFirstNameFragmentIsBlank_shouldThrowException() {
        // given
        final String firstNameFragment = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByFirstName(firstNameFragment));
        verify(userRepository, never()).findAllByFirstName(firstNameFragment);
    }

    @Test
    void whenUserNotFoundByFirstNameFragment_shouldThrowException() {
        // given
        final String firstNameFragment = "any";

        when(userRepository.findAllByFirstName(firstNameFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByFirstName(firstNameFragment));
        verify(userRepository, times(1)).findAllByFirstName(firstNameFragment);
    }

    @Test
    void shouldFindUserByLastNameFragment() {
        // given
        final String lastNameFragment = "any";
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByLastName(lastNameFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByLastName(lastNameFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(userDTO), result);
        verify(userRepository, times(1))
                .findAllByLastName(lastNameFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenLastNameFragmentIsBlank_shouldThrowException() {
        // given
        final String lastNameFragment = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByLastName(lastNameFragment));
        verify(userRepository, never()).findAllByLastName(lastNameFragment);
    }

    @Test
    void whenUserNotFoundByLastNameFragment_shouldThrowException() {
        // given
        final String lastNameFragment = "any";

        when(userRepository.findAllByLastName(lastNameFragment))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByLastName(lastNameFragment));
        verify(userRepository, times(1)).findAllByLastName(lastNameFragment);
    }

    @Test
    void shouldFindUserByEmailFragment() {
        // given
        final String emailFragment = "any";
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByEmail(emailFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByEmail(emailFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(userDTO), result);
        verify(userRepository, times(1)).findAllByEmail(emailFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenEmailFragmentIsBlank_shouldThrowException() {
        // given
        final String emailFragment = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByEmail(emailFragment));
        verify(userRepository, never()).findAllByEmail(emailFragment);
    }

    @Test
    void whenUserNotFoundByEmailFragment_shouldThrowException() {
        // given
        final String emailFragment = "any";

        when(userRepository.findAllByEmail(emailFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByEmail(emailFragment));
        verify(userRepository, times(1)).findAllByEmail(emailFragment);
    }

    @Test
    void shouldFindUserByPhoneNumberFragment() {
        // given
        final String phoneNumberFragment = "234";
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByPhoneNumber(phoneNumberFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByPhoneNumber(phoneNumberFragment);

        // then
        assertNotNull(result);
        assertEquals(List.of(userDTO), result);
        verify(userRepository, times(1)).findAllByPhoneNumber(phoneNumberFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenPhoneNumberFragmentIsNull_shouldThrowException() {
        // given
        final String phoneNumberFragment = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByPhoneNumber(phoneNumberFragment));
        verify(userRepository, never()).findAllByPhoneNumber(phoneNumberFragment);
    }

    @Test
    void whenUserNotFoundByPhoneNumberFragment_shouldThrowException() {
        // given
        final String phoneNumberFragment = "234";

        when(userRepository.findAllByPhoneNumber(phoneNumberFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByPhoneNumber(phoneNumberFragment));
        verify(userRepository, times(1)).findAllByPhoneNumber(phoneNumberFragment);
    }

    @Test
    void shouldFindUserByAnyString() {
        // given
        final String searchPhrase = "any";
        final User user = Instancio.create(User.class);
        final UserDTO userDTO = Instancio.create(UserDTO.class);

        when(userRepository.findAllByAnyString(searchPhrase)).thenReturn(List.of(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        final List<UserDTO> result = userService.findAllByAnyStringField(searchPhrase);

        // then
        assertNotNull(result);
        assertEquals(List.of(userDTO), result);
        verify(userRepository, times(1)).findAllByAnyString(searchPhrase);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenSearchPhraseIsBlank_shouldThrowException() {
        // given
        final String searchPhrase = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByAnyStringField(searchPhrase));
        verify(userRepository, never()).findAllByAnyString(searchPhrase);
    }

    @Test
    void whenUserNotFoundByStringPhrase_shouldThrowException() {
        // given
        final String searchPhrase = "any";

        when(userRepository.findAllByAnyString(searchPhrase)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByAnyStringField(searchPhrase));
        verify(userRepository, times(1)).findAllByAnyString(searchPhrase);
    }

    @Test
    void whenUserNotExists_shouldThrowException() {
        // given
        final Long userId = 1L;

        when(userService.existsById(userId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void whenUserHasReservationAssigned_shouldThrowException() {
        // given
        final Long userId = 1L;
        final ReservationDTO reservationDTO = Instancio.create(ReservationDTO.class);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(reservationService.findAllByUserId(userId)).thenReturn(List.of(reservationDTO));

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(userId);
    }
    
    @Test
    void shouldFindLoginById() {
        // given
        final Long userId = 1L;
        final String expectedLogin = "any";

        when(userRepository.findLoginByUserId(userId)).thenReturn(expectedLogin);

        // when
        final String result = userService.findLoginByUserId(userId);

        // then
        assertNotNull(result);
        assertEquals(expectedLogin, result);
        verify(userRepository, times(1)).findLoginByUserId(userId);
    }

    @Test
    void shouldFindEmailById() {
        // given
        final Long userId = 1L;
        final String expectedEmail = "any@mail.com";

        when(userRepository.findEmailByUserId(userId)).thenReturn(expectedEmail);

        // when
        final String result = userService.findEmailByUserId(userId);

        // then
        assertNotNull(result);
        assertEquals(expectedEmail, result);
        verify(userRepository, times(1)).findEmailByUserId(userId);
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