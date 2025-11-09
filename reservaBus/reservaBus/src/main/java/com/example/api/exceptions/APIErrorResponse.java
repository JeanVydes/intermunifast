package com.example.api.exceptions;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record APIErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldViolation> fieldViolations
) {
    public static APIErrorResponse of(
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> fieldViolations
    ) {
        return new APIErrorResponse(
            OffsetDateTime.now(),
            status,
            error,
            message,
            path,
            fieldViolations
        );
    }

    public record FieldViolation(String field, String message) { }
}
