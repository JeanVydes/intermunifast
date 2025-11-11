package com.example.api.dto;

import java.io.Serializable;
import java.util.List;

public class FareRuleDTOs {
    public record CreateFareRuleRequest(
            Long fromStopId,
            Long toStopId,
            Long routeId
    ) implements Serializable {}

    public record UpdateFareRuleRequest(
            Long fromStopId,
            Long toStopId,
            Long routeId
    ) implements Serializable {}

    public record FareRuleResponse(
            Long id,
            Long fromStopId,
            Long toStopId,
            Long routeId,
            List<Double> discounts,
            boolean dynamicPricing
    ) implements Serializable {}
}
