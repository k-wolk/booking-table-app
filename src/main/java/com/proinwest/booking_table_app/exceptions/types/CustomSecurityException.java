package com.proinwest.booking_table_app.exceptions.types;

public class CustomSecurityException extends RuntimeException {
    public CustomSecurityException(String message) {
        super(message);
    }
}
