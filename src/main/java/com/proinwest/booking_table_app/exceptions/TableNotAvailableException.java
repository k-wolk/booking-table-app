package com.proinwest.booking_table_app.exceptions;

public class TableNotAvailableException extends RuntimeException {
    public TableNotAvailableException(String message) {
        super(message);
    }
}
