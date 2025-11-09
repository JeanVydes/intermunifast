package com.example.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.api.dto.FareRuleDTOs;

@RestController
@RequestMapping("/api/fare-rules")
public class FareRuleController {
    private final FareRuleService fareRuleService;

    public FareRuleController(FareRuleService fareRuleService) {
        this.fareRuleService = fareRuleService;
    }

    @PostMapping
    public ResponseEntity<FareRuleDTOs.FareRuleResponse> create(
            @Validated @RequestBody FareRuleDTOs.CreateFareRuleRequest req,
            UriComponentsBuilder uriBuilder) {
        FareRuleDTOs.FareRuleResponse createdFareRule = fareRuleService.createFareRule(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/fare-rules/{id}").buildAndExpand(createdFareRule.id()).toUri())
                .body(createdFareRule);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FareRuleDTOs.FareRuleResponse> getById(@PathVariable Long id) {
        FareRuleDTOs.FareRuleResponse fareRule = fareRuleService.getFareRuleById(id);
        return ResponseEntity.ok(fareRule);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FareRuleDTOs.FareRuleResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody FareRuleDTOs.UpdateFareRuleRequest req) {
        FareRuleDTOs.FareRuleResponse updatedFareRule = fareRuleService.updateFareRule(id, req);
        return ResponseEntity.ok(updatedFareRule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fareRuleService.deleteFareRule(id);
        return ResponseEntity.noContent().build();
    }
}
