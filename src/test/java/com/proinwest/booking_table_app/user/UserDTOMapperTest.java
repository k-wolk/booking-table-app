package com.proinwest.booking_table_app.user;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDTOMapperTest {

    private UserDTOMapper userDTOMapper = new UserDTOMapper();
//    private User user;
//
//    @BeforeEach
//    void setUp() {
//        userDTOMapper = new UserDTOMapper();
//
////        user = new User();
//
//        user.setId(13L);
//        user.setLogin("johndoe");
//        user.setPassword("password123");
//        user.setFirstName("John");
//        user.setLastName("Doe");
//        user.setEmail("john@mail.com");
//        user.setPhoneNumber("+48-600-700-800");
//    }

    @Test
    public void MapUserToUserDTO() {
        // given
        User user = Instancio.create(User.class);

        // when
        UserDTO userDTO = userDTOMapper.apply(user);

        // then
        assertEquals(user.getId(), userDTO.id());
        assertEquals(user.getLogin(), userDTO.login());
        assertEquals(user.getFirstName(), userDTO.firstName());
        assertEquals(user.getLastName(), userDTO.lastName());
        assertEquals(user.getEmail(), userDTO.email());
        assertEquals(user.getPhoneNumber(), userDTO.phoneNumber());
    }

}