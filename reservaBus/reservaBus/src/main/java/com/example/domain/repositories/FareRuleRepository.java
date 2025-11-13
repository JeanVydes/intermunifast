package com.example.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.domain.entities.FareRule;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {

    FareRule findByRouteId(Long routeId);

}
