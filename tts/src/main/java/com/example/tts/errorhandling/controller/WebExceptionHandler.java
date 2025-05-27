package com.example.tts.errorhandling.controller;

import com.example.tts.errorhandling.ErrorMessage;
import com.example.tts.errorhandling.exception.InvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(value = {InvalidRequestException.class})
    public ResponseEntity<Object> invalidRequestException(InvalidRequestException exception){
        ErrorMessage errorMessage = new ErrorMessage(400, exception.getMessage());
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {IllegalStateException.class})
    public ResponseEntity<Object> illegalStateException(IllegalStateException exception){
        ErrorMessage errorMessage = new ErrorMessage(500, exception.getMessage());
        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
