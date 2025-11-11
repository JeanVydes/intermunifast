package com.example.domain.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findAll();

    Optional<Route> findByCode(String code);

    List<Route> findByNameContainingIgnoreCase(String name);

    List<Route> findByOrigin(String origin);

    List<Route> findByDestination(String destination);

    List<Route> findByOriginAndDestination(String origin, String destination);
}
