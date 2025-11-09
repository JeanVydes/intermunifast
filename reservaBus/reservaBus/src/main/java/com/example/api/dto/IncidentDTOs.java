package com.example.api.dto;

import java.io.Serializable;

import org.hibernate.type.EntityType;

import com.example.domain.enums.IncidentType;

public class IncidentDTOs {
    public record CreateIncidentRequest(
        IncidentType type,
        String note,
        EntityType relatedEntity,
        Long relatedEntityId
    ) implements Serializable {}

    public record UpdateIncidentRequest(
        IncidentType type,
        String note,
        EntityType relatedEntity,
        Long relatedEntityId
    ) implements Serializable {}

    public record IncidentResponse(
        Long id,
        IncidentType type,
        String note,
        EntityType relatedEntity,
        Long relatedEntityId
    ) implements Serializable {}
}
