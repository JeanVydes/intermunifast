package com.example.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.IncidentDTOs;
import com.example.domain.repositories.IncidentRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.IncidentService;
import com.example.services.mappers.IncidentMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository repo;
    private final IncidentMapper mapper;

    @Override 
    public IncidentDTOs.IncidentResponse createIncident(IncidentDTOs.CreateIncidentRequest req) {
        var incident = mapper.toEntity(req);
        return mapper.toResponse(repo.save(incident));
    }

    @Override
    @Transactional(readOnly = true) 
    public IncidentDTOs.IncidentResponse getIncidentById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Incident %d not found".formatted(id)));
    }

    @Override
    public void deleteIncident(Long id) {
        var incident = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Incident %d not found".formatted(id)));
        repo.delete(incident);
    }

    @Override
    public IncidentDTOs.IncidentResponse updateIncident(Long id, IncidentDTOs.UpdateIncidentRequest req) {
        var incident = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Incident %d not found".formatted(id)));
        mapper.patch( incident, req);
        return mapper.toResponse(repo.save(incident));
    }
}
