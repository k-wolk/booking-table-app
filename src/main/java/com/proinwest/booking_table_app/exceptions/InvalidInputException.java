package com.proinwest.booking_table_app.exceptions;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
