package com.example.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.domain.entities.FareRule;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {

    FareRule findByRouteIdAndFromStopIdAndToStopId(Long routeId, Long fromStopId, Long toStopId);

    FareRule findByRouteId(Long routeId);

}
