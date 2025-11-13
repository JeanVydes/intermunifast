package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Stop;

public interface StopRepository extends JpaRepository<Stop, Long> {

    List<Stop> findByRoute_IdOrderBySequenceAsc(Long routeId);
}
