package com.example.domain.repositories;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.example.domain.entities.Account;

/**
 * Configuración mínima para tests de repositorio.
 * Solo escanea el dominio (entities + repos) y evita cargar Security.
 */
@SpringBootApplication
@EntityScan(basePackageClasses = { Account.class })
@EnableJpaRepositories(basePackageClasses = { AccountRepository.class })
public class TestJpaConfiguration {
}
