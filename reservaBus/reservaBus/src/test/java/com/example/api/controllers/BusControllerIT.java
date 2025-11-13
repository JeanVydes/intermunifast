package com.example.api.controllers;

import com.example.api.dto.AuthenticationDTOs;
import com.example.api.dto.BusDTOs;
import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;
import com.example.domain.enums.BusStatus;
import com.example.domain.repositories.AccountRepository;
import com.example.domain.repositories.BusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Bus Controller Integration Tests")
@Transactional
class BusControllerIT extends AbstractControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String passengerToken;

    @BeforeEach
    void setUp() throws Exception {
        // Limpiar datos
        busRepository.deleteAll();
        accountRepository.deleteAll();

        // Crear usuario admin
        Account adminAccount = Account.builder()
                .name("Admin User")
                .email("admin@example.com")
                .phone("123456789")
                .passwordHash(passwordEncoder.encode("adminpass"))
                .role(AccountRole.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();
        accountRepository.save(adminAccount);

        // Crear usuario passenger
        Account passengerAccount = Account.builder()
                .name("Passenger User")
                .email("passenger@example.com")
                .phone("987654321")
                .passwordHash(passwordEncoder.encode("passengerpass"))
                .role(AccountRole.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .build();
        accountRepository.save(passengerAccount);

        // Obtener tokens
        adminToken = getAuthToken("admin@example.com", "adminpass");
        passengerToken = getAuthToken("passenger@example.com", "passengerpass");
    }

    /**
     * Helper method para obtener token de autenticación
     */
    private String getAuthToken(String email, String password) throws Exception {
        AuthenticationDTOs.SignInRequest signInRequest = new AuthenticationDTOs.SignInRequest(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        AuthenticationDTOs.SignInResponse signInResponse = objectMapper.readValue(response,
                AuthenticationDTOs.SignInResponse.class);

        return signInResponse.token();
    }

    @Test
    @DisplayName("Should create bus as ADMIN with valid token")
    void shouldCreateBusAsAdmin() throws Exception {
        // Given
        BusDTOs.CreateBusRequest createRequest = new BusDTOs.CreateBusRequest(
                "ABC-123",
                40,
                List.of());

        // When & Then
        mockMvc.perform(post("/api/buses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.plate").value("ABC-123"))
                .andExpect(jsonPath("$.capacity").value(40));
    }

    @Test
    @DisplayName("Should return 403 when PASSENGER tries to create bus")
    void shouldReturn403WhenPassengerTriesToCreateBus() throws Exception {
        // Given
        BusDTOs.CreateBusRequest createRequest = new BusDTOs.CreateBusRequest(
                "ABC-456",
                45,
                List.of());

        // When & Then
        mockMvc.perform(post("/api/buses")
                .header("Authorization", "Bearer " + passengerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when creating bus without token")
    void shouldReturn401WhenCreatingBusWithoutToken() throws Exception {
        // Given
        BusDTOs.CreateBusRequest createRequest = new BusDTOs.CreateBusRequest(
                "ABC-789",
                50,
                List.of());

        // When & Then
        mockMvc.perform(post("/api/buses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get bus by ID with any authenticated user")
    void shouldGetBusByIdWithAuthenticatedUser() throws Exception {
        // Given - Create bus as admin first
        BusDTOs.CreateBusRequest createRequest = new BusDTOs.CreateBusRequest(
                "DEF-123",
                35,
                List.of());

        MvcResult createResult = mockMvc.perform(post("/api/buses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        BusDTOs.BusResponse busResponse = objectMapper.readValue(createResponse, BusDTOs.BusResponse.class);

        // When & Then - Passenger can read
        mockMvc.perform(get("/api/buses/" + busResponse.id())
                .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(busResponse.id()))
                .andExpect(jsonPath("$.plate").value("DEF-123"));
    }

    @Test
    @DisplayName("Should return 404 when bus not found")
    void shouldReturn404WhenBusNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/buses/999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update bus as ADMIN")
    void shouldUpdateBusAsAdmin() throws Exception {
        // Given - Create bus first
        BusDTOs.CreateBusRequest createRequest = new BusDTOs.CreateBusRequest(
                "GHI-123",
                40,
                List.of());

        MvcResult createResult = mockMvc.perform(post("/api/buses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        BusDTOs.BusResponse busResponse = objectMapper.readValue(createResponse, BusDTOs.BusResponse.class);

        // When - Update bus
        BusDTOs.UpdateBusRequest updateRequest = new BusDTOs.UpdateBusRequest(
                Optional.of("GHI-456"),
                Optional.of(45),
                Optional.of(List.of()),
                Optional.of(BusStatus.MAINTENANCE));

        // Then
        mockMvc.perform(patch("/api/buses/" + busResponse.id())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("GHI-456"))
                .andExpect(jsonPath("$.capacity").value(45))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    @DisplayName("Should return 403 when PASSENGER tries to update bus")
    void shouldReturn403WhenPassengerTriesToUpdateBus() throws Exception {
        // Given - Create bus as admin
        BusDTOs.CreateBusRequest createRequest = new BusDTOs.CreateBusRequest(
                "JKL-123",
                40,
                List.of());

        MvcResult createResult = mockMvc.perform(post("/api/buses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        BusDTOs.BusResponse busResponse = objectMapper.readValue(createResponse, BusDTOs.BusResponse.class);

        // When - Try to update as passenger
        BusDTOs.UpdateBusRequest updateRequest = new BusDTOs.UpdateBusRequest(
                Optional.of("JKL-456"),
                Optional.of(45),
                Optional.of(List.of()),
                Optional.of(BusStatus.ACTIVE));

        // Then
        mockMvc.perform(patch("/api/buses/" + busResponse.id())
                .header("Authorization", "Bearer " + passengerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should delete bus as ADMIN")
    void shouldDeleteBusAsAdmin() throws Exception {
        // Given - Create bus first
        BusDTOs.CreateBusRequest createRequest = new BusDTOs.CreateBusRequest(
                "MNO-123",
                40,
                List.of());

        MvcResult createResult = mockMvc.perform(post("/api/buses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        BusDTOs.BusResponse busResponse = objectMapper.readValue(createResponse, BusDTOs.BusResponse.class);

        // When & Then - Delete
        mockMvc.perform(delete("/api/buses/" + busResponse.id())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/buses/" + busResponse.id())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when PASSENGER tries to delete bus")
    void shouldReturn403WhenPassengerTriesToDeleteBus() throws Exception {
        // Given - Create bus as admin
        BusDTOs.CreateBusRequest createRequest = new BusDTOs.CreateBusRequest(
                "PQR-123",
                40,
                List.of());

        MvcResult createResult = mockMvc.perform(post("/api/buses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        BusDTOs.BusResponse busResponse = objectMapper.readValue(createResponse, BusDTOs.BusResponse.class);

        // When & Then - Try to delete as passenger
        mockMvc.perform(delete("/api/buses/" + busResponse.id())
                .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden());
    }
}
