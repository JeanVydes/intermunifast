package com.example.services.definitions;

import com.example.api.dto.FareRuleDTOs;

public interface FareRuleService {
    FareRuleDTOs.FareRuleResponse createFareRule(FareRuleDTOs.CreateFareRuleRequest req);

    FareRuleDTOs.FareRuleResponse getFareRuleById(Long id);

    FareRuleDTOs.FareRuleResponse updateFareRule(Long id, FareRuleDTOs.UpdateFareRuleRequest req);

    void deleteFareRule(Long id);
}
