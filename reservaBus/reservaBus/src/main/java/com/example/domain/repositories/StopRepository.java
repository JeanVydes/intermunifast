package com.example.domain.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Stop;

public interface StopRepository extends JpaRepository<Stop, Long> {
    Optional<Stop> findById(Long id);

    List<Stop> findByRoute_IdOrderBySequenceAsc(Long routeId);
}
