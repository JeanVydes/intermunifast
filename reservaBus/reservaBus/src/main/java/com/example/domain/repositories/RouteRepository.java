package com.example.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
