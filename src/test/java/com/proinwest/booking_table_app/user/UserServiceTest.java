package com.proinwest.booking_table_app.user;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DataJpaTest
class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDTOMapper userDTOMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllUsers() {

        // Given
        List<User> allUsers = new ArrayList<>();

        User user1 = new User();
        user1.setId(1L);
        user1.setLogin("john");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@mail.com");
        user1.setPassword("secret");
        user1.setPhoneNumber("123456789");

        User user2 = new User();
        user2.setId(2L);
        user2.setLogin("samsmith");
        user2.setFirstName("Sam");
        user2.setLastName("Smith");
        user2.setEmail("smith@gmail.com");
        user2.setPassword("qwerty@1");
        user2.setPhoneNumber("11111111111");

        allUsers.add(user1);
        allUsers.add(user2);

        List<UserDTO> allUsersDTO = new ArrayList<>();
        allUsersDTO.add(new UserDTO(1L, "john", "John", "Doe", "john@mail.com", "123456789"));
        allUsersDTO.add(new UserDTO(2L, "samsmith", "Sam", "Smith", "smith@gmail.com", "11111111111"));

        // When
        when(userRepository.findAll()).thenReturn(allUsers);
        when(userDTOMapper.apply(allUsers.get(0))).thenReturn(allUsersDTO.get(0));
        when(userDTOMapper.apply(allUsers.get(1))).thenReturn(allUsersDTO.get(1));

        List<UserDTO> getAllUsers = userService.getAllUsers();

        // Then
        assertEquals(allUsersDTO.size(), getAllUsers.size());
        assertEquals(allUsers.get(0).getLastName(), allUsersDTO.get(0).lastName());
        assertEquals(allUsers.get(0).getEmail(), allUsersDTO.get(0).email());
        assertEquals(allUsers.get(1).getLogin(), allUsersDTO.get(1).login());
        assertEquals(allUsers.get(1).getFirstName(), allUsersDTO.get(1).firstName());
        assertEquals(allUsers.get(1).getPhoneNumber(), allUsersDTO.get(1).phoneNumber());
        verify(userRepository, times(1)).findAll();
        verify(userDTOMapper, times(1)).apply(allUsers.get(0));
        verify(userDTOMapper, times(1)).apply(allUsers.get(1));
    }

    @Test
    public void testGetUser() {

        // Given
        Long id = 11L;

        User user = new User();
        user.setId(11L);
        user.setLogin("john");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPassword("secret");
        user.setPhoneNumber("123456789");

        UserDTO userDTO = new UserDTO(11L, "john", "John", "Doe", "john@mail.com", "123456789");

        // When
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userDTOMapper.apply(user)).thenReturn(userDTO);

        Optional<UserDTO> foundUserDTO = userService.getUser(id);

        // Then
        assertEquals(foundUserDTO.get().id(), user.getId());
        assertEquals(foundUserDTO.get().login(), user.getLogin());
        assertEquals(foundUserDTO.get().firstName(), user.getFirstName());
        assertEquals(foundUserDTO.get().lastName(), user.getLastName());
        assertEquals(foundUserDTO.get().email(), user.getEmail());
        assertEquals(foundUserDTO.get().phoneNumber(), user.getPhoneNumber());
        verify(userRepository, times(1)).findById(id);
        verify(userDTOMapper, times(1)).apply(user);
    }

    @Test
    public void testAddUser() {

        // Given
        User user = new User();
        user.setId(13L);
        user.setLogin("johndoe");
        user.setPassword("password123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@mail.com");
        user.setPhoneNumber("+48-600-700-800");

        User savedUser = user;

        UserDTO userDTO = new UserDTO(13L, "johndoe", "John", "Doe", "john@mail.com", "+48-600-700-800");

        // When
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userDTOMapper.apply(savedUser)).thenReturn(userDTO);

        UserDTO addedUserDTO = userService.addUser(user);

        // Then
        assertEquals(user.getId(),addedUserDTO.id());
        assertEquals(user.getLogin(),addedUserDTO.login());
        assertEquals(user.getFirstName(),addedUserDTO.firstName());
        assertEquals(user.getLastName(),addedUserDTO.lastName());
        assertEquals(user.getEmail(),addedUserDTO.email());
        assertEquals(user.getPhoneNumber(),addedUserDTO.phoneNumber());
    }

//    @Test
//    public void testUpdateUser() {
//
//        // Given
//        Integer id = 2;
//        User existingUser = new User();
//        existingUser.setId(2);
//        existingUser.setLogin("johndoe0000");
//        existingUser.setPassword("password0000");
//        existingUser.setName("John0000");
//        existingUser.setLastName("Doe0000");
//        existingUser.setEmail("john0000@mail.com");
//        existingUser.setPhoneNumber("+48-000-000-000");
//
//        User updatingUser = new User();
//        updatingUser.setId(null);
//        updatingUser.setLogin("johndoe");
//        updatingUser.setPassword("password123");
//        updatingUser.setName("John");
//        updatingUser.setLastName("Doe");
//        updatingUser.setEmail("john@mail.com");
//        updatingUser.setPhoneNumber("+48-600-700-800");
//
//        User updatedUser = new User();
//        updatedUser.setId(2);
//        updatedUser.setLogin("johndoe");
//        updatedUser.setPassword("password123");
//        updatedUser.setName("John");
//        updatedUser.setLastName("Doe");
//        updatedUser.setEmail("john@mail.com");
//        updatedUser.setPhoneNumber("+48-600-700-800");
//
//        UserDTO updatedUserDTO = new UserDTO(2, "johndoe", "John", "Doe", "john@mail.com", "+48-600-700-800");
//
//        // When
//        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
//        when(userRepository.save(updatingUser)).thenReturn(updatedUser);
//        when(userDTOMapper.apply(updatedUser)).thenReturn(updatedUserDTO);
//
//        UserDTO result = userService.updateUser(id, updatingUser);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(updatedUserDTO, result);

//        verify(userRepository, times(1)).findById(id);
//        verify(userRepository, times(1)).save(updatingUser);
//        verify(userDTOMapper, times(1)).apply(updatedUser);
//    }

//    @Test
//    public void testPartiallyUpdateUser() {
//
//        // Given
//        Integer id = 2;
//        User user = new User();
//        user.setId(2);
//        user.setLogin("johndoe0000");
//        user.setPassword("password0000");
//        user.setName("John0000");
//        user.setLastName("Doe0000");
//        user.setEmail("john0000@mail.com");
//        user.setPhoneNumber("+48-600-700-800");
//
//        User updatingUser = new User();
//        updatingUser.setId(null);
//        updatingUser.setLogin("johndoe");
//        updatingUser.setPassword("password123");
//        updatingUser.setName("John");
//        updatingUser.setLastName("Doe");
//        updatingUser.setEmail("john@mail.com");
//        updatingUser.setPhoneNumber("+48-600-700-800");
//
//        User updatedUser = new User();
//        updatedUser.setId(2);
//        updatedUser.setLogin("johndoe");
//        updatedUser.setPassword("password123");
//        updatedUser.setName("John");
//        updatedUser.setLastName("Doe");
//        updatedUser.setEmail("john@mail.com");
//        updatedUser.setPhoneNumber("+48-600-700-800");
//
//        UserDTO updatedUserDTO = new UserDTO(2, "johndoe", "John", "Doe", "john@mail.com", "+48-600-700-800");
//
//        // When
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//        when(userRepository.save(updatingUser)).thenReturn(updatedUser);
//        when(userDTOMapper.apply(updatedUser)).thenReturn(updatedUserDTO);
//
//        UserDTO result = userService.partiallyUpdateUser(id, updatingUser);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(updatedUserDTO, result);
//
//        verify(userRepository, times(1)).findById(id);
//        verify(userRepository, times(1)).save(updatingUser);
//        verify(userDTOMapper, times(1)).apply(updatedUser);
//
//    }

    @Test
    public void testFindAllByLoginContaining() {

        // POPRAWIĆ KOD. TEST DZIAŁA NIEZALEŻNIE OD POLA LOGIN

        // Given
        String loginFragment = "jo";

        List<User> users = new ArrayList<>();
        List<UserDTO> usersDTO = new ArrayList<>();

        User user1 = new User();
        user1.setId(11L);
        user1.setLogin("john");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@mail.com");
        user1.setPassword("secret");
        user1.setPhoneNumber("123456789");

        User user2 = new User();
        user2.setId(13L);
        user2.setLogin("johnny");
        user2.setFirstName("Johnny");
        user2.setLastName("Bravo");
        user2.setEmail("johnny@mail.com");
        user2.setPassword("secret");
        user2.setPhoneNumber("111-111-111");

        User user3 = new User();
        user3.setId(17L);
        user3.setLogin("jsmith");
        user3.setFirstName("Joanna");
        user3.setLastName("Smith");
        user3.setEmail("joanna@mail.com");
        user3.setPassword("secret");
        user3.setPhoneNumber("111-222-333");

        users.add(user1);
        users.add(user2);
        users.add(user3);

        UserDTO userDTO1 = new UserDTO(11L, "john", "John", "Doe", "john@mail.com", "123456789");
        UserDTO userDTO2 = new UserDTO(13L, "johnny", "Johnny", "Bravo", "johnny@mail.com", "111-111-111");
//        UserDTO userDTO3 = new UserDTO(17, "jsmith", "Joanna", "Smith", "joanna@mail.com", "111-222-333");

        usersDTO.add(userDTO1);
        usersDTO.add(userDTO2);
        usersDTO.add(null);

        // When
        when(userRepository.findAllByLoginContaining(loginFragment)).thenReturn(users);
        when(userDTOMapper.apply(user1)).thenReturn(userDTO1);
        when(userDTOMapper.apply(user2)).thenReturn(userDTO2);
//        when(userDTOMapper.apply(user3)).thenReturn(userDTO3);

        List<UserDTO> result = userService.findAllByLogin(loginFragment);

        // Then
        assertNotNull(result);
        assertEquals(usersDTO, result);

        verify(userRepository, times(1)).findAllByLoginContaining(loginFragment);
        verify(userDTOMapper, times(1)).apply(user1);
        verify(userDTOMapper, times(1)).apply(user2);
        verify(userRepository).findAllByLoginContaining(loginFragment);
    }
}

