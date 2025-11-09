package com.example.security.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.*;

@Component
public class Http403AccessDenied implements AccessDeniedHandler {
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) {
                
        response.setStatus(403);
        response.setContentType("application/json");

        try {
            new com.fasterxml.jackson.databind.ObjectMapper().writeValue(
                    response.getOutputStream(),
                    java.util.Map.of(
                            "status", 403,
                            "error", "Forbidden",
                            "message", accessDeniedException.getMessage(),
                            "path", request.getRequestURI()));
        } catch (Exception ignoredException) {
            // Ignore
        }
    }
}
