package com.example.services.definitions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.FareRuleDTOs;
import com.example.domain.repositories.FareRuleRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.FareRuleMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository repo;
    private final FareRuleMapper mapper;

    @Override 
    public FareRuleDTOs.FareRuleResponse createFareRule(FareRuleDTOs.CreateFareRuleRequest req) {
        var fareRule = mapper.toEntity(req);
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
        var fareRule = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("FareRule %d not found".formatted(id)));
        mapper.patch( fareRule, req);
        return mapper.toResponse(repo.save(fareRule));
    }
}
