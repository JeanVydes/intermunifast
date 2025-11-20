package com.example.services.extra;

import com.example.domain.repositories.ConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfigCacheService {

    private final ConfigRepository configRepository;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    private static final Map<String, String> DEFAULTS = Map.of(
            "MAX_SEAT_HOLD_MINUTES", "10",
            "MAX_BAGGAGE_WEIGHT_KG", "25",
            "BAGGAGE_FEE_PERCENTAGE", "0.03");

    public ConfigCacheService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @PostConstruct
    public void init() {
        configRepository.findAll().forEach(c -> cache.put(c.getKey(), c.getValue()));
    }

    public int getMaxSeatHoldMinutes() {
        return getInt("MAX_SEAT_HOLD_MINUTES");
    }

    public double getMaxBaggageWeightKg() {
        return getDouble("MAX_BAGGAGE_WEIGHT_KG");
    }

    public double getBaggageFeePercentage() {
        return getDouble("BAGGAGE_FEE_PERCENTAGE");
    }

    private int getInt(String key) {
        String value = cache.getOrDefault(key, DEFAULTS.get(key));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return Integer.parseInt(DEFAULTS.get(key));
        }
    }

    private double getDouble(String key) {
        String value = cache.getOrDefault(key, DEFAULTS.get(key));
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return Double.parseDouble(DEFAULTS.get(key));
        }
    }
}
