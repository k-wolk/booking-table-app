package com.proinwest.booking_table_app.exceptionHandler;

import com.proinwest.booking_table_app.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        var errors = new HashMap<String, String>();
        exception.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var fieldName = ((FieldError) error).getField();
                    var errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String now = LocalDateTime.now().format(formatter);

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleTableExistsException(AlreadyExistsException exception) {
        return new ExceptionResponse(HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(), now);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleTableNotFoundException(NotFoundException exception) {
        return new ExceptionResponse(HttpStatus.NOT_FOUND.value(),
                exception.getMessage(), now);
    }

    @ExceptionHandler(TableNotAvailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleTableNotAvailableException(TableNotAvailableException exception) {
        return new ExceptionResponse(HttpStatus.CONFLICT.value(),
                exception.getMessage(), now);
    }

    @ExceptionHandler(InvalidInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleInvalidInputException(InvalidInputException exception) {
        return new ExceptionResponse(HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(), now);
    }
}
