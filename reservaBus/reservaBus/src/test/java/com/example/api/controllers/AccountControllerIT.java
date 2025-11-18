package com.example.api.controllers;

import com.example.api.dto.AccountDTOs;
import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;
import com.example.domain.repositories.AccountRepository;
import com.example.security.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountControllerIT extends AbstractControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        String uniqueId = UUID.randomUUID().toString();

        // Crear cuenta admin
        Account admin = Account.builder()
                .name("Admin Test")
                .email("admin-account-" + uniqueId + "@test.com")
                .phone("+111222333")
                .role(AccountRole.ADMIN)
                .status(AccountStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        accountRepository.save(admin);
        adminToken = jwtService.generateToken(admin, Map.of("roles", admin.getRole().name()));

        // Crear cuenta usuario regular
        Account user = Account.builder()
                .name("User Test")
                .email("user-account-" + uniqueId + "@test.com")
                .phone("+444555666")
                .role(AccountRole.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        accountRepository.save(user);
        userToken = jwtService.generateToken(user, Map.of("roles", user.getRole().name()));
    }

    @Test
    void shouldCreateAccountWithoutAuth() throws Exception {
        AccountDTOs.CreateAccountRequest request = new AccountDTOs.CreateAccountRequest(
                "John Doe",
                "john.doe@test.com",
                "+123456789",
                "password123",
                false);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@test.com")))
                .andExpect(jsonPath("$.phone", is("+123456789")))
                .andExpect(jsonPath("$.role", is("PASSENGER")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void shouldCreateAdminAccount() throws Exception {
        AccountDTOs.CreateAccountRequest request = new AccountDTOs.CreateAccountRequest(
                "Admin User",
                "admin.user@test.com",
                "+987654321",
                "adminpass123",
                true);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Admin User")))
                .andExpect(jsonPath("$.email", is("admin.user@test.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    void shouldNotCreateAccountWithDuplicateEmail() throws Exception {
        AccountDTOs.CreateAccountRequest request1 = new AccountDTOs.CreateAccountRequest(
                "User One",
                "duplicate@test.com",
                "+111111111",
                "password123",
                false);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        AccountDTOs.CreateAccountRequest request2 = new AccountDTOs.CreateAccountRequest(
                "User Two",
                "duplicate@test.com",
                "+222222222",
                "password456",
                false);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetAccountById() throws Exception {
        AccountDTOs.CreateAccountRequest request = new AccountDTOs.CreateAccountRequest(
                "Get Test User",
                "gettest@test.com",
                "+333333333",
                "password123",
                false);

        String response = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AccountDTOs.AccountResponse createdAccount = objectMapper.readValue(response,
                AccountDTOs.AccountResponse.class);

        mockMvc.perform(get("/api/accounts/" + createdAccount.id())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdAccount.id().intValue())))
                .andExpect(jsonPath("$.email", is("gettest@test.com")));
    }

    @Test
    void shouldGetAllAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts/all")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(Iterable.class)));
    }

    @Test
    void shouldUpdateOwnAccount() throws Exception {
        // Crear cuenta
        AccountDTOs.CreateAccountRequest createRequest = new AccountDTOs.CreateAccountRequest(
                "Update Test",
                "update@test.com",
                "+444444444",
                "password123",
                false);

        String createResponse = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AccountDTOs.AccountResponse createdAccount = objectMapper.readValue(createResponse,
                AccountDTOs.AccountResponse.class);

        // Generar token para la cuenta creada
        Account account = accountRepository.findById(createdAccount.id()).orElseThrow();
        String token = jwtService.generateToken(account, Map.of("roles", account.getRole().name()));

        // Actualizar nombre
        String updateJson = "{\"name\": \"Updated Name\"}";

        mockMvc.perform(patch("/api/accounts/" + createdAccount.id())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    void shouldNotUpdateOtherUserAccount() throws Exception {
        // Crear cuenta
        AccountDTOs.CreateAccountRequest createRequest = new AccountDTOs.CreateAccountRequest(
                "Another User",
                "another@test.com",
                "+555555555",
                "password123",
                false);

        String createResponse = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AccountDTOs.AccountResponse createdAccount = objectMapper.readValue(createResponse,
                AccountDTOs.AccountResponse.class);

        // Intentar actualizar con otro usuario
        String updateJson = "{\"name\": \"Hacked Name\"}";

        mockMvc.perform(patch("/api/accounts/" + createdAccount.id())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteAccountAsAdmin() throws Exception {
        // Crear cuenta
        AccountDTOs.CreateAccountRequest request = new AccountDTOs.CreateAccountRequest(
                "To Delete",
                "todelete@test.com",
                "+666666666",
                "password123",
                false);

        String response = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AccountDTOs.AccountResponse createdAccount = objectMapper.readValue(response,
                AccountDTOs.AccountResponse.class);

        // Eliminar como admin
        mockMvc.perform(delete("/api/accounts/" + createdAccount.id())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verificar que no existe
        mockMvc.perform(get("/api/accounts/" + createdAccount.id())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteAccountWithoutAdminRole() throws Exception {
        // Crear cuenta
        AccountDTOs.CreateAccountRequest request = new AccountDTOs.CreateAccountRequest(
                "Protected Account",
                "protected@test.com",
                "+777777777",
                "password123",
                false);

        String response = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AccountDTOs.AccountResponse createdAccount = objectMapper.readValue(response,
                AccountDTOs.AccountResponse.class);

        // Intentar eliminar como usuario regular
        mockMvc.perform(delete("/api/accounts/" + createdAccount.id())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
