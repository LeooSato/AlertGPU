package com.sato.alertsgpu.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_ARGUMENT", e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(AlertNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAlertNotFound(AlertNotFoundException e) {
        log.warn("Alert not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ALERT_NOT_FOUND", e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(InvalidPriceRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPriceRange(InvalidPriceRangeException e) {
        log.warn("Invalid price range: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_PRICE_RANGE", e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse("VALIDATION_ERROR", "Validation failed", errors, Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", Instant.now()));
    }
}
