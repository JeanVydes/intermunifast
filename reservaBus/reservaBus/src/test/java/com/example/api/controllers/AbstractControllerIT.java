package com.example.api.controllers;

import com.example.reservaBus.ReservaBusApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para tests de controladores con integración completa.
 * - @SpringBootTest: levanta todo el contexto de Spring Boot
 * - @AutoConfigureMockMvc: configura MockMvc para testing HTTP
 * - @Testcontainers: usa PostgreSQL real en contenedor
 * - Perfil 'test' con configuración específica
 */
@SpringBootTest(classes = ReservaBusApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // Helpers comunes para tests de controladores
}
