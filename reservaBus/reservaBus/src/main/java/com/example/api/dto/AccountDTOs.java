package com.example.api.dto;

import java.io.Serializable;
import java.util.Optional;

import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;

public class AccountDTOs {
    public record CreateAccountRequest(
            String name,
            String email,
            String phone,
            String password
    ) implements Serializable {}

    public record UpdateAccountRequest(
            Optional<String> name,
            Optional<String> email,
            Optional<String> phone,
            Optional<String> password,

            // Restricted to admins
            AccountRole role,
            AccountStatus status
    ) implements Serializable {}

    public record AccountResponse(
            Long id,
            String name,
            String email,
            String phone,
            AccountRole role,
            AccountStatus status
    ) implements Serializable {}
}
