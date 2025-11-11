package com.ahss.exception;

import com.ahss.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        String message = errors.isEmpty() ? "Validation failed" : errors.get(0);
        String path = request.getDescription(false).replace("uri=", "");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.notOk(null, message, path));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(
            BadRequestException ex,
            WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.notOk(null, ex.getMessage(), path));
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex,
            WebRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .findFirst()
                .orElse("Validation failed");
        String path = request.getDescription(false).replace("uri=", "");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.notOk(null, message, path));
    }
}