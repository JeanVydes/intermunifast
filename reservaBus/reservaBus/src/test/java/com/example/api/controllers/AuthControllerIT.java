package com.example.api.controllers;

import com.example.api.dto.AccountDTOs;
import com.example.api.dto.AuthenticationDTOs;
import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;
import com.example.domain.repositories.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth Controller Integration Tests")
@Transactional
class AuthControllerIT extends AbstractControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Limpiar y crear cuenta de prueba
        accountRepository.deleteAll();

        testAccount = Account.builder()
                .name("Test User")
                .email("test@example.com")
                .phone("123456789")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(AccountRole.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(testAccount);
    }

    @Test
    @DisplayName("Should sign in successfully with valid credentials")
    void shouldSignInSuccessfully() throws Exception {
        // Given
        AuthenticationDTOs.SignInRequest signInRequest = new AuthenticationDTOs.SignInRequest("test@example.com",
                "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").value(testAccount.getId()))
                .andExpect(jsonPath("$.role").value("PASSENGER"));
    }

    @Test
    @DisplayName("Should return 401 with invalid password")
    void shouldReturn401WithInvalidPassword() throws Exception {
        // Given
        AuthenticationDTOs.SignInRequest signInRequest = new AuthenticationDTOs.SignInRequest("test@example.com",
                "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 with non-existent email")
    void shouldReturn401WithNonExistentEmail() throws Exception {
        // Given
        AuthenticationDTOs.SignInRequest signInRequest = new AuthenticationDTOs.SignInRequest("nonexistent@example.com",
                "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should sign in admin user successfully")
    void shouldSignInAdminSuccessfully() throws Exception {
        // Given - Create admin account
        Account adminAccount = Account.builder()
                .name("Admin User")
                .email("admin@example.com")
                .phone("987654321")
                .passwordHash(passwordEncoder.encode("adminpass"))
                .role(AccountRole.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();
        accountRepository.save(adminAccount);

        AuthenticationDTOs.SignInRequest signInRequest = new AuthenticationDTOs.SignInRequest("admin@example.com",
                "adminpass");

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Should sign in different roles successfully")
    void shouldSignInDifferentRoles() throws Exception {
        // Given - Create clerk account
        Account clerkAccount = Account.builder()
                .name("Clerk User")
                .email("clerk@example.com")
                .phone("555666777")
                .passwordHash(passwordEncoder.encode("clerkpass"))
                .role(AccountRole.CLERK)
                .status(AccountStatus.ACTIVE)
                .build();
        accountRepository.save(clerkAccount);

        AuthenticationDTOs.SignInRequest signInRequest = new AuthenticationDTOs.SignInRequest("clerk@example.com",
                "clerkpass");

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("CLERK"));
    }
}
