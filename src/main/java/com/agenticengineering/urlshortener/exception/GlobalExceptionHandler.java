
package com.agenticengineering.urlshortener.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.agenticengineering.urlshortener.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private ResponseEntity<ApiErrorResponse> buildResponse(
                        HttpStatus status,
                        String message,
                        HttpServletRequest request) {

                ApiErrorResponse response = new ApiErrorResponse(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                message,
                                request.getRequestURI());

                return ResponseEntity.status(status).body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(error -> error.getDefaultMessage())
                                .orElse("Request validation failed");

                return buildResponse(HttpStatus.BAD_REQUEST, message, request);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Invalid or missing request body",
                                request);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Request validation failed",
                                request);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
                        NoResourceFoundException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.NOT_FOUND,
                                "Resource not found",
                                request);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
                        Exception ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "An unexpected error occurred",
                                request);
        }

        @ExceptionHandler(UrlNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleUrlNotFound(
                        UrlNotFoundException ex,
                        HttpServletRequest request) {

                return buildResponse(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage(),
                                request);
        }

        // Add this method inside your existing @RestControllerAdvice class
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<String> handleAccessDenied(
                        AccessDeniedException exception) {

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body("You do not have permission to access this resource");
        }
}
