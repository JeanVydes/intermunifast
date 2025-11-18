package com.example.api.controllers;

import com.example.api.dto.RouteDTOs;
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

class RouteControllerIT extends AbstractControllerIT {

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
                .email("admin-route-" + uniqueId + "@test.com")
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
                .email("user-route-" + uniqueId + "@test.com")
                .phone("+987654321")
                .role(AccountRole.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        accountRepository.save(user);
        userToken = jwtService.generateToken(user, Map.of("roles", user.getRole().name()));
    }

    @Test
    void shouldCreateRouteAsAdmin() throws Exception {
        RouteDTOs.CreateRouteRequest request = new RouteDTOs.CreateRouteRequest(
                "RT-TEST-001",
                "Test Route",
                "Origin City",
                "Destination City",
                120,
                100.0,
                1.5);

        mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.code", is("RT-TEST-001")))
                .andExpect(jsonPath("$.name", is("Test Route")))
                .andExpect(jsonPath("$.origin", is("Origin City")))
                .andExpect(jsonPath("$.destination", is("Destination City")))
                .andExpect(jsonPath("$.durationMinutes", is(120)))
                .andExpect(jsonPath("$.distanceKm", is(100.0)))
                .andExpect(jsonPath("$.pricePerKm", is(1.5)));
    }

    @Test
    void shouldNotCreateRouteWithoutAuth() throws Exception {
        RouteDTOs.CreateRouteRequest request = new RouteDTOs.CreateRouteRequest(
                "RT-TEST-002",
                "Test Route 2",
                "City A",
                "City B",
                60,
                50.0,
                2.0);

        mockMvc.perform(post("/api/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotCreateRouteAsRegularUser() throws Exception {
        RouteDTOs.CreateRouteRequest request = new RouteDTOs.CreateRouteRequest(
                "RT-TEST-003",
                "Test Route 3",
                "City C",
                "City D",
                90,
                75.0,
                1.8);

        mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetAllRoutes() throws Exception {
        // Primero crear una ruta
        RouteDTOs.CreateRouteRequest request = new RouteDTOs.CreateRouteRequest(
                "RT-TEST-004",
                "Test Route 4",
                "City E",
                "City F",
                150,
                120.0,
                1.3);

        mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Obtener todas las rutas
        mockMvc.perform(get("/api/routes/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(Iterable.class)));
    }

    @Test
    void shouldGetRouteById() throws Exception {
        // Crear ruta
        RouteDTOs.CreateRouteRequest request = new RouteDTOs.CreateRouteRequest(
                "RT-TEST-005",
                "Test Route 5",
                "City G",
                "City H",
                180,
                150.0,
                1.4);

        String response = mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        RouteDTOs.RouteResponse createdRoute = objectMapper.readValue(response, RouteDTOs.RouteResponse.class);

        // Obtener por ID
        mockMvc.perform(get("/api/routes/" + createdRoute.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdRoute.id().intValue())))
                .andExpect(jsonPath("$.code", is("RT-TEST-005")));
    }

    @Test
    void shouldUpdateRouteAsAdmin() throws Exception {
        // Crear ruta
        RouteDTOs.CreateRouteRequest createRequest = new RouteDTOs.CreateRouteRequest(
                "RT-TEST-006",
                "Test Route 6",
                "City I",
                "City J",
                200,
                180.0,
                1.6);

        String response = mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        RouteDTOs.RouteResponse createdRoute = objectMapper.readValue(response, RouteDTOs.RouteResponse.class);

        // Actualizar ruta
        String updateJson = "{\"name\": \"Updated Route Name\"}";

        mockMvc.perform(patch("/api/routes/" + createdRoute.id())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Route Name")));
    }

    @Test
    void shouldDeleteRouteAsAdmin() throws Exception {
        // Crear ruta
        RouteDTOs.CreateRouteRequest request = new RouteDTOs.CreateRouteRequest(
                "RT-TEST-007",
                "Test Route 7",
                "City K",
                "City L",
                240,
                200.0,
                1.2);

        String response = mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        RouteDTOs.RouteResponse createdRoute = objectMapper.readValue(response, RouteDTOs.RouteResponse.class);

        // Eliminar ruta
        mockMvc.perform(delete("/api/routes/" + createdRoute.id())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verificar que no existe
        mockMvc.perform(get("/api/routes/" + createdRoute.id()))
                .andExpect(status().isNotFound());
    }
}
