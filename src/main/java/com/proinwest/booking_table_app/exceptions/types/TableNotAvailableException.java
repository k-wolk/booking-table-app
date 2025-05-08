package com.proinwest.booking_table_app.exceptions.types;

public class TableNotAvailableException extends RuntimeException {
    public TableNotAvailableException(String message) {
        super(message);
    }
}
