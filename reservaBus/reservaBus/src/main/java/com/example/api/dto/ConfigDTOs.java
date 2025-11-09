package com.example.api.dto;

public class ConfigDTOs {
    public record CreateConfigRequest(
            String key,
            String value
    ) implements java.io.Serializable {}

    public record UpdateConfigRequest(
            String key,
            String value
    ) implements java.io.Serializable {}

    public record ConfigResponse(
            Long id,
            String key,
            String value
    ) implements java.io.Serializable {}
}
