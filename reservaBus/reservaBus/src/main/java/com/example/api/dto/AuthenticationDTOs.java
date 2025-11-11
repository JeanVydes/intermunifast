package com.example.api.dto;

import java.io.Serializable;

public class AuthenticationDTOs {
    public record SignInRequest(
            String email,
            String password) implements Serializable {
    }

    public record SignInResponse(
            String token,
            Long userId,
            String role
            ) implements Serializable {
    }
}
