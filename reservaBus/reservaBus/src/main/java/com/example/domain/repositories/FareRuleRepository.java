package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.FareRule;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {

    FareRule findByRouteIdAndFromStopIdAndToStopId(Long routeId, Long fromStopId, Long toStopId);

    List<FareRule> findByRouteId(Long routeId);

}
