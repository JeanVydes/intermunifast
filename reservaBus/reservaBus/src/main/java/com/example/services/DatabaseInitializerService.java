package com.example.services;

import com.example.domain.repositories.AccountRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * Servicio que inicializa la base de datos con datos de prueba
 * Solo se ejecuta si la base de datos está vacía (no hay cuentas)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializerService {

    private final AccountRepository accountRepository;
    private final DataSource dataSource;

    @PostConstruct
    public void initializeDatabase() {
        try {
            // Verificar si ya existen datos
            long accountCount = accountRepository.count();

            if (accountCount == 0) {
                log.info("Base de datos vacía. Ejecutando script de inicialización...");

                // Ejecutar el script data.sql
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("data.sql"));
                populator.setSeparator(";");
                populator.execute(dataSource);

                log.info("✅ Script de inicialización ejecutado exitosamente!");
                log.info("✅ {} cuentas creadas", accountRepository.count());
            } else {
                log.info("La base de datos ya contiene {} cuentas. Saltando inicialización.", accountCount);
            }
        } catch (Exception e) {
            log.error("❌ Error al inicializar la base de datos: {}", e.getMessage(), e);
        }
    }
}
