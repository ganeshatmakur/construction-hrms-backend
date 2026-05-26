package com.hrms.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.hrms.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

 
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ApiException (business logic exceptions)
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
        ApiException ex,
        WebRequest request) {
        
        log.warn("[v0] ApiException: error={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .error(ex.getErrorCode().getCode())
            .message(ex.getMessage())
            .status(ex.getErrorCode().getHttpStatus())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        if (ex.getDetails() != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("details", ex.getDetails());
            response.setDetails(details);
        }

        HttpStatus httpStatus = HttpStatus.valueOf(ex.getErrorCode().getHttpStatus());
        return new ResponseEntity<>(response, httpStatus);
    }

    /**
     * Handles validation errors (@Valid annotation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        WebRequest request) {

        log.warn("[v0] Validation error: {}", ex.getMessage());

        BindingResult bindingResult = ex.getBindingResult();
        Map<String, Object> details = new HashMap<>();
        details.put("fields", bindingResult.getFieldErrors().stream()
            .collect(Collectors.toMap(
                error -> error.getField().toLowerCase(),
                error -> error.getDefaultMessage()
            )));

        ErrorResponse response = ErrorResponse.builder()
            .error(ErrorCode.VALIDATION_ERROR.getCode())
            .message(ErrorCode.VALIDATION_ERROR.getMessage())
            .status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .details(details)
            .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex,
        WebRequest request) {
        
        log.warn("[v0] IllegalArgumentException: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .error(ErrorCode.INVALID_REQUEST.getCode())
            .message(ErrorCode.INVALID_REQUEST.getMessage())
            .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other exceptions (fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        WebRequest request) {
        
        log.error("[v0] Unhandled exception: ", ex);

        ErrorResponse response = ErrorResponse.builder()
            .error(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
            .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
