// src/main/java/com/example/demo/exception/GlobalExceptionHandler.java
package com.example.demo.exception;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.security.access.AccessDeniedException;


/**
 * This class catches exceptions thrown from any @RestController and
 * turns them into consistent HTTP responses. No need to replicate try‐catch
 * in each controller method.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle IllegalArgumentException (400 Bad Request).
     * Any controller method that throws IllegalArgumentException
     * will end up here.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handle MethodArgumentTypeMismatchException (e.g. path variable cannot
     * be converted to Long). Return 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String param = ex.getName();
        String msg = String.format("Invalid value for parameter '%s': %s", param, ex.getValue());
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", msg));
    }

    /**
     * Catch any other uncaught exception and return 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllUncaught(Exception ex, WebRequest request) {
        // You can log the exception here if you like
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }


   
    /**
     * Handle AccessDeniedException (403 Forbidden).
     * This is a custom exception that you can define to indicate
     * that access to a resource is denied.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    
}
