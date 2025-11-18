package com.example.api.controllers;

import com.example.api.dto.ConfigDTOs;
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

class ConfigControllerIT extends AbstractControllerIT {

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
                .email("admin-config-" + uniqueId + "@test.com")
                .phone("+123456789")
                .role(AccountRole.ADMIN)
                .status(AccountStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        accountRepository.save(admin);
        adminToken = jwtService.generateToken(admin, Map.of("roles", admin.getRole().name()));

        // Crear cuenta usuario regular
        Account user = Account.builder()
                .name("User Test")
                .email("user-config-" + uniqueId + "@test.com")
                .phone("+987654321")
                .role(AccountRole.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        accountRepository.save(user);
        userToken = jwtService.generateToken(user, Map.of("roles", user.getRole().name()));
    }

    @Test
    void shouldCreateConfigAsAdmin() throws Exception {
        ConfigDTOs.CreateConfigRequest request = new ConfigDTOs.CreateConfigRequest(
                "test_config_key",
                "test_config_value");

        mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.key", is("test_config_key")))
                .andExpect(jsonPath("$.value", is("test_config_value")));
    }

    @Test
    void shouldNotCreateConfigWithoutAuth() throws Exception {
        ConfigDTOs.CreateConfigRequest request = new ConfigDTOs.CreateConfigRequest(
                "unauthorized_key",
                "unauthorized_value");

        mockMvc.perform(post("/api/configs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotCreateConfigAsRegularUser() throws Exception {
        ConfigDTOs.CreateConfigRequest request = new ConfigDTOs.CreateConfigRequest(
                "forbidden_key",
                "forbidden_value");

        mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetAllConfigs() throws Exception {
        // Crear una config primero
        ConfigDTOs.CreateConfigRequest request = new ConfigDTOs.CreateConfigRequest(
                "list_test_key",
                "list_test_value");

        mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Obtener todas las configs
        mockMvc.perform(get("/api/configs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(Iterable.class)));
    }

    @Test
    void shouldGetConfigById() throws Exception {
        // Crear config
        ConfigDTOs.CreateConfigRequest request = new ConfigDTOs.CreateConfigRequest(
                "get_by_id_key",
                "get_by_id_value");

        String response = mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ConfigDTOs.ConfigResponse createdConfig = objectMapper.readValue(response, ConfigDTOs.ConfigResponse.class);

        // Obtener por ID
        mockMvc.perform(get("/api/configs/" + createdConfig.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdConfig.id().intValue())))
                .andExpect(jsonPath("$.key", is("get_by_id_key")));
    }

    @Test
    void shouldGetConfigByKey() throws Exception {
        // Crear config
        ConfigDTOs.CreateConfigRequest request = new ConfigDTOs.CreateConfigRequest(
                "get_by_key_test",
                "get_by_key_value");

        mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Obtener por key
        mockMvc.perform(get("/api/configs/by-key/get_by_key_test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", is("get_by_key_test")))
                .andExpect(jsonPath("$.value", is("get_by_key_value")));
    }

    @Test
    void shouldReturnNotFoundForNonExistentKey() throws Exception {
        mockMvc.perform(get("/api/configs/by-key/non_existent_key"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateConfigAsAdmin() throws Exception {
        // Crear config
        ConfigDTOs.CreateConfigRequest createRequest = new ConfigDTOs.CreateConfigRequest(
                "update_test_key",
                "original_value");

        String response = mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ConfigDTOs.ConfigResponse createdConfig = objectMapper.readValue(response, ConfigDTOs.ConfigResponse.class);

        // Actualizar config
        String updateJson = "{\"value\": \"updated_value\"}";

        mockMvc.perform(patch("/api/configs/" + createdConfig.id())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value", is("updated_value")));
    }

    @Test
    void shouldNotUpdateConfigAsRegularUser() throws Exception {
        // Crear config
        ConfigDTOs.CreateConfigRequest createRequest = new ConfigDTOs.CreateConfigRequest(
                "protected_key",
                "protected_value");

        String response = mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ConfigDTOs.ConfigResponse createdConfig = objectMapper.readValue(response, ConfigDTOs.ConfigResponse.class);

        // Intentar actualizar como usuario regular
        String updateJson = "{\"value\": \"hacked_value\"}";

        mockMvc.perform(patch("/api/configs/" + createdConfig.id())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteConfigAsAdmin() throws Exception {
        // Crear config
        ConfigDTOs.CreateConfigRequest request = new ConfigDTOs.CreateConfigRequest(
                "delete_test_key",
                "delete_test_value");

        String response = mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ConfigDTOs.ConfigResponse createdConfig = objectMapper.readValue(response, ConfigDTOs.ConfigResponse.class);

        // Eliminar config
        mockMvc.perform(delete("/api/configs/" + createdConfig.id())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verificar que no existe
        mockMvc.perform(get("/api/configs/" + createdConfig.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateSystemConfigs() throws Exception {
        // Crear config de tiempo de retención de asientos
        ConfigDTOs.CreateConfigRequest seatHoldConfig = new ConfigDTOs.CreateConfigRequest(
                "max_seat_hold_minutes",
                "15");

        mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seatHoldConfig)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key", is("max_seat_hold_minutes")))
                .andExpect(jsonPath("$.value", is("15")));

        // Crear config de peso máximo de equipaje
        ConfigDTOs.CreateConfigRequest baggageWeightConfig = new ConfigDTOs.CreateConfigRequest(
                "max_baggage_weight_kg",
                "23");

        mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(baggageWeightConfig)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key", is("max_baggage_weight_kg")))
                .andExpect(jsonPath("$.value", is("23")));

        // Crear config de tarifa de equipaje
        ConfigDTOs.CreateConfigRequest baggageFeeConfig = new ConfigDTOs.CreateConfigRequest(
                "baggage_fee_percentage",
                "0.15");

        mockMvc.perform(post("/api/configs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(baggageFeeConfig)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key", is("baggage_fee_percentage")))
                .andExpect(jsonPath("$.value", is("0.15")));
    }
}
