package com.example.security.exception;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class Http401EntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)  {
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        try {
            new ObjectMapper().writeValue(
                response.getOutputStream(),
                Map.of(
                    "status", 401,
                    "error", "Unauthorized",
                    "message", authException.getMessage(),
                    "path", request.getRequestURI()
                )
            );
        } catch (Exception ignoredException) {
            // Ignore
        }
    }
}
