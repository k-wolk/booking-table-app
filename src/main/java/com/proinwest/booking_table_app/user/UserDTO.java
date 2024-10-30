package com.proinwest.booking_table_app.user;

public record UserDTO(
        Long id,
        String login,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}

