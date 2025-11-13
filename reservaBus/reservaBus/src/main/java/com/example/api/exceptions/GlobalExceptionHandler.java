package com.example.api.exceptions;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<APIErrorResponse> handleNotFound(NotFoundException e, WebRequest req) {
                APIErrorResponse errorResponse = APIErrorResponse.of(
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found resource",
                                e.getMessage(),
                                req.getDescription(false).replace("uri=", ""),
                                null);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<APIErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e,
                        WebRequest req) {
                APIErrorResponse errorResponse = APIErrorResponse.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Failed",
                                e.getMessage(),
                                req.getDescription(false).replace("uri=", ""),
                                e.getFieldErrors().stream()
                                                .map(fieldError -> new APIErrorResponse.FieldViolation(
                                                                fieldError.getField(),
                                                                fieldError.getDefaultMessage()))
                                                .toList());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<APIErrorResponse> handleConstraintViolation(ConstraintViolationException e,
                        WebRequest req) {
                APIErrorResponse errorResponse = APIErrorResponse.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "Database Constraint Violation",
                                e.getMessage(),
                                req.getDescription(false).replace("uri=", ""),
                                null);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<APIErrorResponse> handleIllegalArgument(IllegalArgumentException e, WebRequest req) {
                APIErrorResponse errorResponse = APIErrorResponse.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Argument",
                                e.getMessage(),
                                req.getDescription(false).replace("uri=", ""),
                                null);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<APIErrorResponse> handleGenericException(Exception e, WebRequest req) {
                APIErrorResponse errorResponse = APIErrorResponse.of(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                e.getMessage(),
                                req.getDescription(false).replace("uri=", ""),
                                null);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}
