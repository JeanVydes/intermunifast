package com.example.services;

import com.example.domain.entities.Config;
import com.example.domain.repositories.ConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de caché para configuraciones del sistema.
 * Evita queries repetidas a la base de datos cargando las configuraciones en
 * memoria al inicio.
 */
@Service
public class ConfigCacheService {

    private final ConfigRepository configRepository;
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    // Valores por defecto si no existen en la base de datos
    private static final Map<String, String> DEFAULT_VALUES = Map.of(
            "MAX_SEAT_HOLD_MINUTES", "10",
            "MAX_BAGGAGE_WEIGHT_KG", "25",
            "BAGGAGE_FEE_PERCENTAGE", "0.03",
            "CANCEL_BEFORE_MINUTES", "5",
            "SYSTEM_NAME", "InterMuniFast",
            "SUPPORT_EMAIL", "soporte@intermunifast.com.co",
            "SUPPORT_PHONE", "+57 300 123 4567");

    public ConfigCacheService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    /**
     * Carga todas las configuraciones en el caché al iniciar la aplicación
     */
    @PostConstruct
    public void loadConfigurations() {
        configRepository.findAll().forEach(config -> configCache.put(config.getKey(), config.getValue()));
    }

    /**
     * Obtiene un valor de configuración como String
     */
    public String getString(String key) {
        return configCache.getOrDefault(key, DEFAULT_VALUES.getOrDefault(key, ""));
    }

    /**
     * Obtiene un valor de configuración como Integer
     */
    public int getInt(String key) {
        String value = getString(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Obtiene un valor de configuración como Double
     */
    public double getDouble(String key) {
        String value = getString(key);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Obtiene un valor de configuración como Boolean
     */
    public boolean getBoolean(String key) {
        String value = getString(key);
        return Boolean.parseBoolean(value);
    }

    /**
     * Actualiza un valor de configuración en la base de datos y en el caché
     */
    public void updateConfig(String key, String value) {
        Optional<Config> configOpt = configRepository.findByKey(key);
        Config config;

        if (configOpt.isPresent()) {
            config = configOpt.get();
            config.setValue(value);
        } else {
            config = Config.builder()
                    .key(key)
                    .value(value)
                    .build();
        }

        configRepository.save(config);

        // Actualizar caché
        configCache.put(key, value);
    }

    /**
     * Recarga todas las configuraciones desde la base de datos
     */
    public void reloadConfigurations() {
        configCache.clear();
        loadConfigurations();
    }

    /**
     * Obtiene el peso máximo de equipaje permitido sin cargo extra
     */
    public double getMaxBaggageWeightKg() {
        return getDouble("MAX_BAGGAGE_WEIGHT_KG");
    }

    /**
     * Obtiene el porcentaje de cargo por equipaje excedente
     */
    public double getBaggageFeePercentage() {
        return getDouble("BAGGAGE_FEE_PERCENTAGE");
    }

    /**
     * Obtiene los minutos máximos para mantener un asiento reservado
     */
    public int getMaxSeatHoldMinutes() {
        return getInt("MAX_SEAT_HOLD_MINUTES");
    }

    /**
     * Obtiene los minutos antes del viaje para permitir cancelación
     */
    public int getCancelBeforeMinutes() {
        return getInt("CANCEL_BEFORE_MINUTES");
    }
}
