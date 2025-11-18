package com.example.api.dto;

import java.io.Serializable;

public class FareRuleDTOs {
        public record CreateFareRuleRequest(
                        Long routeId,
                        Double basePrice,
                        boolean dynamicPricing,
                        Double childrenDiscount,
                        Double seniorDiscount,
                        Double studentDiscount) implements Serializable {
        }

        public record UpdateFareRuleRequest(
                        Long routeId,
                        Double basePrice,
                        boolean dynamicPricing,
                        Double childrenDiscount,
                        Double seniorDiscount,
                        Double studentDiscount) implements Serializable {
        }

        public record FareRuleResponse(
                        Long id,
                        Long routeId,
                        Double basePrice,
                        Double childrenDiscount,
                        Double seniorDiscount,
                        Double studentDiscount,
                        boolean dynamicPricing) implements Serializable {
        }
}
