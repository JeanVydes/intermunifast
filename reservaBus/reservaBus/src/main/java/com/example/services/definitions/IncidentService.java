package com.example.services.definitions;

import com.example.api.dto.IncidentDTOs;

public interface IncidentService {
    IncidentDTOs.IncidentResponse createIncident(IncidentDTOs.CreateIncidentRequest req);

    IncidentDTOs.IncidentResponse getIncidentById(Long id);

    IncidentDTOs.IncidentResponse updateIncident(Long id, IncidentDTOs.UpdateIncidentRequest req);

    void deleteIncident(Long id);
}
