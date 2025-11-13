package com.example.api.dto;

import java.io.Serializable;

public class FareRuleDTOs {
        public record CreateFareRuleRequest(
                        Long routeId,
                        boolean dynamicPricing,
                        Double childrenDiscount,
                        Double seniorDiscount,
                        Double studentDiscount) implements Serializable {
        }

        public record UpdateFareRuleRequest(
                        Long routeId,
                        boolean dynamicPricing,
                        Double childrenDiscount,
                        Double seniorDiscount,
                        Double studentDiscount) implements Serializable {
        }

        public record FareRuleResponse(
                        Long id,
                        Long routeId,
                        Double childrenDiscount,
                        Double seniorDiscount,
                        Double studentDiscount,
                        boolean dynamicPricing) implements Serializable {
        }
}
