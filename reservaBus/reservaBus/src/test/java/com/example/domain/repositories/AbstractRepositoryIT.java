package com.example.domain.repositories;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para tests de repositorios.
 * - @DataJpaTest: levanta solo la capa JPA (rápido)
 * - @Testcontainers + @ServiceConnection: autoconfigura el DataSource con
 * Postgres 16 en contenedor
 * - @AutoConfigureTestDatabase(replace = NONE): usa el contenedor en lugar de
 * H2
 * - Excluye configuraciones de seguridad y servicios para tests de repositorio
 */
@DataJpaTest(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.example\\.(security|services|api)\\..*"))
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = TestJpaConfiguration.class)
public abstract class AbstractRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // Punto de extensión si necesitas helpers comunes
}
