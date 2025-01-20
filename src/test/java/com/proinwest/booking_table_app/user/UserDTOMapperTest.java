package com.proinwest.booking_table_app.user;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDTOMapperTest {

    private UserDTOMapper userDTOMapper = new UserDTOMapper();

    @Test
    public void shouldMapUserToUserDTO() {
        // given
        final User user = Instancio.create(User.class);

        // when
        final UserDTO userDTO = userDTOMapper.apply(user);

        // then
        assertEquals(user.getId(), userDTO.id());
        assertEquals(user.getLogin(), userDTO.login());
        assertEquals(user.getFirstName(), userDTO.firstName());
        assertEquals(user.getLastName(), userDTO.lastName());
        assertEquals(user.getEmail(), userDTO.email());
        assertEquals(user.getPhoneNumber(), userDTO.phoneNumber());
    }
}