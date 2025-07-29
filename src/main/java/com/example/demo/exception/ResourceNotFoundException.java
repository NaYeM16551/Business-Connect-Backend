package com.example.demo.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to handle resource not found scenarios.
 * This exception will return a 404 Not Found status when thrown.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends Exception{

    public ResourceNotFoundException(String message) {
        super(message);
    }
}