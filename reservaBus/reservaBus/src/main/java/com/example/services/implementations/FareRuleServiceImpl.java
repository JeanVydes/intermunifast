package com.example.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.FareRuleDTOs;
import com.example.domain.entities.FareRule;
import com.example.domain.entities.Route;
import com.example.domain.repositories.FareRuleRepository;
import com.example.domain.repositories.RouteRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.FareRuleService;
import com.example.services.mappers.FareRuleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository repo;
    private final FareRuleMapper mapper;
    private final RouteRepository routeRepo;

    @Override
    public FareRuleDTOs.FareRuleResponse createFareRule(FareRuleDTOs.CreateFareRuleRequest req) {
        Route route = routeRepo.findById(req.routeId())
                .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(req.routeId())));
        FareRule fareRule = FareRule.builder()
                .route(route)
                .build();
        return mapper.toResponse(repo.save(fareRule));
    }

    @Override
    @Transactional(readOnly = true)
    public FareRuleDTOs.FareRuleResponse getFareRuleById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));
    }

    @Override
    public void deleteFareRule(Long id) {
        var fareRule = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));
        repo.delete(fareRule);
    }

    @Override
    public FareRuleDTOs.FareRuleResponse updateFareRule(Long id, FareRuleDTOs.UpdateFareRuleRequest req) {
        if (req.routeId() != null) {
            routeRepo.findById(req.routeId())
                    .orElseThrow(() -> new NotFoundException("Route %d not found".formatted(req.routeId())));

        }
        var fareRule = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));
        mapper.patch(fareRule, req);
        return mapper.toResponse(repo.save(fareRule));
    }
}
