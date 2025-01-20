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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.proinwest.booking_table_app.user.UserService.partiallyUpdateUser;
import static com.proinwest.booking_table_app.user.UserService.updateUser;
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
        final User user1 = Instancio.create(User.class);
        final User user2 = Instancio.create(User.class);

        final List<User> allUsers = new ArrayList<>();
        allUsers.add(user1);
        allUsers.add(user2);

        final UserDTO userDTO1 = new UserDTO(
                user1.getId(),
                user1.getLogin(),
                user1.getFirstName(),
                user1.getLastName(),
                user1.getEmail(),
                user1.getPhoneNumber()
        );

        final UserDTO userDTO2 = new UserDTO(
                user2.getId(),
                user2.getLogin(),
                user2.getFirstName(),
                user2.getLastName(),
                user2.getEmail(),
                user2.getPhoneNumber()
        );

        final List<UserDTO> usersDTO = new ArrayList<>();
        usersDTO.add(userDTO1);
        usersDTO.add(userDTO2);

        when(userRepository.findAll()).thenReturn(allUsers);
        when(userDTOMapper.apply(user1)).thenReturn(userDTO1);
        when(userDTOMapper.apply(user2)).thenReturn(userDTO2);

        // when
        final List<UserDTO> result = userService.getAllUsers();

        // then
        assertNotNull(result);
        assertEquals(usersDTO, result);
        verify(userRepository, times(1)).findAll();
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
        final Long userId = user.getId();

        final UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        // when
        final UserDTO result = userService.getUser(userId);

        // then
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(userRepository, times(1)).findById(userId);
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
        user.setId(1L);

        final User savedUser = user;
        savedUser.setId(1L);

        final UserDTO userDTO = new UserDTO(
                savedUser.getId(),
                savedUser.getLogin(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber()
        );

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

        // when
        final URI result = userService.location(user);

        // then
        final URI expected = URI.create("http://localhost/users/7");
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

        final UserDTO userDTO = new UserDTO(
                userId,
                savedUser.getLogin(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber()
        );

        when(userRepository.findById(userId)
                .map(updatingUser -> updateUser(user, updatingUser)))
                .thenReturn(Optional.of(userToUpdate));
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

        when(userRepository.findById(userId)
                .map(updatingUser -> updateUser(user, updatingUser)))
                .thenReturn(Optional.empty());

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

        final UserDTO userDTO = new UserDTO(
                userId,
                savedUser.getLogin(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber()
        );

        when(userRepository.findById(userId)
                .map(updatingUser -> partiallyUpdateUser(user, updatingUser)))
                .thenReturn(Optional.of(userToUpdate));
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

        when(userRepository.findById(userId)
                .map(updatingUser -> partiallyUpdateUser(user, updatingUser)))
                .thenReturn(Optional.empty());

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
        final String loginFragment = "jOh";

        final User user = new User();
        user.setId(1L);
        user.setLogin("john");
        user.setEmail("doe@mail.com");
        user.setPassword("secret");
        user.setPhoneNumber("123-456-789");

        final UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber()
        );

        final List<UserDTO> usersDTO = new ArrayList<>();
        usersDTO.add(userDTO);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByLoginContainingIgnoreCase(loginFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByLogin(loginFragment);

        // then
        assertNotNull(result);
        assertEquals(usersDTO, result);
        verify(userRepository, times(1)).findAllByLoginContainingIgnoreCase(loginFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenLoginFragmentIsNull_shouldThrowException() {
        // given
        final String loginFragment = null;

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByLogin(loginFragment));
        verify(userRepository, never()).findAllByLoginContainingIgnoreCase(loginFragment);
    }

    @Test
    void whenUserNotFoundByLoginFragment_shouldThrowException() {
        // given
        final String loginFragment = "john";
        when(userRepository.findAllByLoginContainingIgnoreCase(loginFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByLogin(loginFragment));
    }

    @Test
    void shouldFindUserByFirstNameFragment() {
        // given
        final String firstNameFragment = "jOh";

        final User user = new User();
        user.setId(1L);
        user.setLogin("john");
        user.setFirstName("John");
        user.setEmail("doe@mail.com");
        user.setPassword("secret");
        user.setPhoneNumber("123-456-789");

        final UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber()
        );

        final List<UserDTO> usersDTO = new ArrayList<>();
        usersDTO.add(userDTO);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByFirstNameContainingIgnoreCase(firstNameFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByFirstName(firstNameFragment);

        // then
        assertNotNull(result);
        assertEquals(usersDTO, result);
        verify(userRepository, times(1)).findAllByFirstNameContainingIgnoreCase(firstNameFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenFirstNameFragmentIsNull_shouldThrowException() {
        // given
        final String firstNameFragment = null;

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByFirstName(firstNameFragment));
        verify(userRepository, never()).findAllByFirstNameContainingIgnoreCase(firstNameFragment);
    }

    @Test
    void whenUserNotFoundByFirstNameFragment_shouldThrowException() {
        // given
        final String firstNameFragment = "john";
        when(userRepository.findAllByFirstNameContainingIgnoreCase(firstNameFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByFirstName(firstNameFragment));
    }

    @Test
    void shouldFindUserByLastNameFragment() {
        // given
        final String lastNameFragment = "dO";

        final User user = new User();
        user.setId(1L);
        user.setLogin("john");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPassword("secret");
        user.setPhoneNumber("123-456-789");

        final UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber()
        );

        final List<UserDTO> usersDTO = new ArrayList<>();
        usersDTO.add(userDTO);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByLastNameContainingIgnoreCase(lastNameFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByLastName(lastNameFragment);

        // then
        assertNotNull(result);
        assertEquals(usersDTO, result);
        verify(userRepository, times(1))
                .findAllByLastNameContainingIgnoreCase(lastNameFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenLastNameFragmentIsNull_shouldThrowException() {
        // given
        final String lastNameFragment = null;

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByLastName(lastNameFragment));
        verify(userRepository, never()).findAllByLastNameContainingIgnoreCase(lastNameFragment);
    }

    @Test
    void whenUserNotFoundByLastNameFragment_shouldThrowException() {
        // given
        final String lastNameFragment = "Do";
        when(userRepository.findAllByLastNameContainingIgnoreCase(lastNameFragment))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByLastName(lastNameFragment));
    }

    @Test
    void shouldFindUserByEmailFragment() {
        // given
        final String emailFragment = "jOh";

        final User user = new User();
        user.setId(1L);
        user.setLogin("sam");
        user.setEmail("john@mail.com");
        user.setPassword("secret");
        user.setPhoneNumber("123-456-789");

        final UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber()
        );

        final List<UserDTO> usersDTO = new ArrayList<>();
        usersDTO.add(userDTO);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByEmailContainingIgnoreCase(emailFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByEmail(emailFragment);

        // then
        assertNotNull(result);
        assertEquals(usersDTO, result);
        verify(userRepository, times(1)).findAllByEmailContainingIgnoreCase(emailFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenEmailFragmentIsNull_shouldThrowException() {
        // given
        final String emailFragment = null;

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByEmail(emailFragment));
        verify(userRepository, never()).findAllByEmailContainingIgnoreCase(emailFragment);
    }

    @Test
    void whenUserNotFoundByEmailFragment_shouldThrowException() {
        // given
        final String emailFragment = "john";
        when(userRepository.findAllByEmailContainingIgnoreCase(emailFragment)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByEmail(emailFragment));
    }

    @Test
    void shouldFindUserByPhoneNumberFragment() {
        // given
        final String phoneNumberFragment = "234";

        final User user = new User();
        user.setId(1L);
        user.setLogin("john");
        user.setEmail("john@mail.com");
        user.setPassword("secret");
        user.setPhoneNumber("123-456-789");

        final UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber()
        );

        final List<UserDTO> usersDTO = new ArrayList<>();
        usersDTO.add(userDTO);

        when(userDTOMapper.apply(user)).thenReturn(userDTO);
        when(userRepository.findAllByPhoneNumberContaining(phoneNumberFragment)).thenReturn(List.of(user));

        // when
        final List<UserDTO> result = userService.findAllByPhoneNumber(phoneNumberFragment);

        // then
        assertNotNull(result);
        assertEquals(usersDTO, result);
        verify(userRepository, times(1)).findAllByPhoneNumberContaining(phoneNumberFragment);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    void whenPhoneNumberFragmentIsNull_shouldThrowException() {
        // given
        final String phoneNumberFragment = " ";

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByPhoneNumber(phoneNumberFragment));
        verify(userRepository, never()).findAllByPhoneNumberContaining(phoneNumberFragment);
    }

    @Test
    void whenUserNotFoundByPhoneNumberFragment_shouldThrowException() {
        // given
        final String phoneNumber = "234";
        when(userRepository.findAllByPhoneNumberContaining(phoneNumber)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByPhoneNumber(phoneNumber));
    }

    @Test
    void shouldFindUserByAnyString() {
        // given
        final String searchPhrase = "jOh";

        final User user1 = new User();
        user1.setId(1L);
        user1.setLogin("john");
        user1.setEmail("mail@mail.com");
        user1.setPassword("secret");
        user1.setPhoneNumber("123-456-789");

        final UserDTO userDTO1 = new UserDTO(
                user1.getId(),
                user1.getLogin(),
                user1.getFirstName(),
                user1.getLastName(),
                user1.getEmail(),
                user1.getPhoneNumber()
        );

        final User user2 = new User();
        user2.setId(1L);
        user2.setLogin("aaa");
        user2.setFirstName("John");
        user2.setLastName("Doe");
        user2.setEmail("aaa@mail.com");
        user2.setPassword("aaaaaaa");
        user2.setPhoneNumber("111-111-111");

        final UserDTO userDTO2 = new UserDTO(
                user2.getId(),
                user2.getLogin(),
                user2.getFirstName(),
                user2.getLastName(),
                user2.getEmail(),
                user2.getPhoneNumber()
        );

        final User user3 = new User();
        user3.setId(1L);
        user3.setLogin("bbb");
        user3.setFirstName("bbb");
        user3.setLastName("John");
        user3.setEmail("bbb@mail.com");
        user3.setPassword("bbbbbbbb");
        user3.setPhoneNumber("222-222-222");

        final UserDTO userDTO3 = new UserDTO(
                user3.getId(),
                user3.getLogin(),
                user3.getFirstName(),
                user3.getLastName(),
                user3.getEmail(),
                user3.getPhoneNumber()
        );

        final User user4 = new User();
        user4.setId(1L);
        user4.setLogin("ccc");
        user4.setFirstName("ccc");
        user4.setLastName("ccc");
        user4.setEmail("john@mail.com");
        user4.setPassword("cccccccc");
        user4.setPhoneNumber("333-333-333");

        final UserDTO userDTO4 = new UserDTO(
                user4.getId(),
                user4.getLogin(),
                user4.getFirstName(),
                user4.getLastName(),
                user4.getEmail(),
                user4.getPhoneNumber()
        );

        final List<UserDTO> usersDTO = new ArrayList<>();
        usersDTO.add(userDTO1);
        usersDTO.add(userDTO2);
        usersDTO.add(userDTO3);
        usersDTO.add(userDTO4);

        when(userDTOMapper.apply(user1)).thenReturn(userDTO1);
        when(userDTOMapper.apply(user2)).thenReturn(userDTO2);
        when(userDTOMapper.apply(user3)).thenReturn(userDTO3);
        when(userDTOMapper.apply(user4)).thenReturn(userDTO4);
        when(userRepository.findAllByLoginContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchPhrase, searchPhrase, searchPhrase, searchPhrase))
                .thenReturn(List.of(user1, user2,user3, user4));

        // when
        final List<UserDTO> result = userService.findAllByAnyString(searchPhrase);

        // then
        assertNotNull(result);
        assertEquals(usersDTO, result);
        verify(userRepository, times(1)).findAllByLoginContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchPhrase, searchPhrase, searchPhrase, searchPhrase);
        verify(userDTOMapper, times(1)).apply(user1);
    }

    @Test
    void whenSearchPhraseIsNull_shouldThrowException() {
        // given
        final String searchPhrase = null;

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.findAllByAnyString(searchPhrase));
        verify(userRepository, never()).findAllByLoginContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchPhrase, searchPhrase, searchPhrase, searchPhrase);
    }

    @Test
    void whenUserNotFoundByStringPhrase_shouldThrowException() {
        // given
        final String searchPhrase = "john";
        when(userRepository.findAllByLoginContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchPhrase, searchPhrase, searchPhrase, searchPhrase))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.findAllByAnyString(searchPhrase));
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

        final List<ReservationDTO> reservationsList = new ArrayList<>();
        reservationsList.add(reservationDTO);

        when(userService.existsById(userId)).thenReturn(true);
        when(reservationService.findAllByUserId(userId)).thenReturn(reservationsList);

        // when & then
        assertThrows(InvalidInputException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(userId);
    }
    
    @Test
    void shouldFindLoginById() {
        // given
        final Long userId = 1L;
        final String expectedLogin = "john";
        when(userRepository.findLoginById(userId)).thenReturn(expectedLogin);

        // when
        final String result = userService.findLoginById(userId);

        // then
        assertNotNull(result);
        assertEquals(expectedLogin, result);
        verify(userRepository, times(1)).findLoginById(userId);
    }

    @Test
    void shouldFindEmailById() {
        // given
        final Long userId = 1L;
        final String expectedEmail = "john@mail.com";
        when(userRepository.findEmailById(userId)).thenReturn(expectedEmail);

        // when
        final String result = userService.findEmailById(userId);

        // then
        assertNotNull(result);
        assertEquals(expectedEmail, result);
        verify(userRepository, times(1)).findEmailById(userId);
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
        final String login = "john";
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
        final String login = "john";
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
        final String email = "john@mail.com";
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
        final String email = "john@mail.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // when
        final boolean result = userService.existsByEmail(email);

        // then
        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }
}