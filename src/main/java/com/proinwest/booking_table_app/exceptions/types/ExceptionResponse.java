package com.proinwest.booking_table_app.exceptions.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExceptionResponse {
    private int statusCode;
    private String message;
    private String timestamp;
}