package com.example.api.controllers;

import com.example.api.dto.RouteDTOs;
import com.example.api.dto.StopDTOs;
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

class StopControllerIT extends AbstractControllerIT {

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
    private Long routeId;

    @BeforeEach
    void setUp() throws Exception {
        String uniqueId = UUID.randomUUID().toString();

        // Crear cuenta admin
        Account admin = Account.builder()
                .name("Admin Test")
                .email("admin-stop-" + uniqueId + "@test.com")
                .phone("+123456789")
                .role(AccountRole.ADMIN)
                .status(AccountStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        accountRepository.save(admin);
        adminToken = jwtService.generateToken(admin, Map.of("roles", admin.getRole().name()));

        // Crear una ruta para usar en los tests
        RouteDTOs.CreateRouteRequest routeRequest = new RouteDTOs.CreateRouteRequest(
                "RT-STOP-" + uniqueId.substring(0, 8),
                "Test Route for Stops",
                "Origin",
                "Destination",
                120,
                100.0,
                1.5);

        String routeResponse = mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(routeRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        RouteDTOs.RouteResponse createdRoute = objectMapper.readValue(routeResponse, RouteDTOs.RouteResponse.class);
        routeId = createdRoute.id();
    }

    @Test
    void shouldCreateStopAsAdmin() throws Exception {
        StopDTOs.CreateStopRequest request = new StopDTOs.CreateStopRequest(
                "Stop 1",
                1,
                11.0,
                -74.0,
                routeId);

        mockMvc.perform(post("/api/stops")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Stop 1")))
                .andExpect(jsonPath("$.sequence", is(1)))
                .andExpect(jsonPath("$.latitude", is(11.0)))
                .andExpect(jsonPath("$.longitude", is(-74.0)));
    }

    @Test
    void shouldNotCreateStopWithoutAuth() throws Exception {
        StopDTOs.CreateStopRequest request = new StopDTOs.CreateStopRequest(
                "Stop 2",
                2,
                11.1,
                -74.1,
                routeId);

        mockMvc.perform(post("/api/stops")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetAllStops() throws Exception {
        // Crear un stop primero
        StopDTOs.CreateStopRequest request = new StopDTOs.CreateStopRequest(
                "Stop 3",
                1,
                11.2,
                -74.2,
                routeId);

        mockMvc.perform(post("/api/stops")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Obtener todos los stops
        mockMvc.perform(get("/api/stops/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(Iterable.class)));
    }

    @Test
    void shouldGetStopById() throws Exception {
        // Crear stop
        StopDTOs.CreateStopRequest request = new StopDTOs.CreateStopRequest(
                "Stop 4",
                1,
                11.3,
                -74.3,
                routeId);

        String response = mockMvc.perform(post("/api/stops")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        StopDTOs.StopResponse createdStop = objectMapper.readValue(response, StopDTOs.StopResponse.class);

        // Obtener por ID
        mockMvc.perform(get("/api/stops/" + createdStop.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdStop.id().intValue())))
                .andExpect(jsonPath("$.name", is("Stop 4")));
    }

    @Test
    void shouldGetStopsByRouteId() throws Exception {
        // Crear varios stops para la misma ruta
        StopDTOs.CreateStopRequest request1 = new StopDTOs.CreateStopRequest(
                "Stop 5A",
                1,
                11.4,
                -74.4,
                routeId);

        StopDTOs.CreateStopRequest request2 = new StopDTOs.CreateStopRequest(
                "Stop 5B",
                2,
                11.5,
                -74.5,
                routeId);

        mockMvc.perform(post("/api/stops")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/stops")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // Obtener stops por route ID
        mockMvc.perform(get("/api/stops/by-route/" + routeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void shouldUpdateStopAsAdmin() throws Exception {
        // Crear stop
        StopDTOs.CreateStopRequest createRequest = new StopDTOs.CreateStopRequest(
                "Stop 6",
                1,
                11.6,
                -74.6,
                routeId);

        String response = mockMvc.perform(post("/api/stops")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        StopDTOs.StopResponse createdStop = objectMapper.readValue(response, StopDTOs.StopResponse.class);

        // Actualizar stop
        String updateJson = "{\"name\": \"Updated Stop Name\"}";

        mockMvc.perform(patch("/api/stops/" + createdStop.id())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Stop Name")));
    }

    @Test
    void shouldDeleteStopAsAdmin() throws Exception {
        // Crear stop
        StopDTOs.CreateStopRequest request = new StopDTOs.CreateStopRequest(
                "Stop 7",
                1,
                11.7,
                -74.7,
                routeId);

        String response = mockMvc.perform(post("/api/stops")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        StopDTOs.StopResponse createdStop = objectMapper.readValue(response, StopDTOs.StopResponse.class);

        // Eliminar stop
        mockMvc.perform(delete("/api/stops/" + createdStop.id())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verificar que no existe
        mockMvc.perform(get("/api/stops/" + createdStop.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateSequenceOrder() throws Exception {
        // Crear stop con secuencia 1
        StopDTOs.CreateStopRequest request1 = new StopDTOs.CreateStopRequest(
                "Stop Seq 1",
                1,
                11.8,
                -74.8,
                routeId);

        mockMvc.perform(post("/api/stops")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Crear stop con secuencia 2
        StopDTOs.CreateStopRequest request2 = new StopDTOs.CreateStopRequest(
                "Stop Seq 2",
                2,
                11.9,
                -74.9,
                routeId);

        mockMvc.perform(post("/api/stops")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
    }
}
