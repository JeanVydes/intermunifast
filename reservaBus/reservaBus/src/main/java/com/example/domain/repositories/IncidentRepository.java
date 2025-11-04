package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Incident;
import com.example.domain.enums.EntityType;
import com.example.domain.enums.IncidentType;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
    

    List<Incident> findByType(IncidentType type);

    List<Incident> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT i FROM Incident i WHERE i.entityId IN (SELECT a.tripId FROM Assignment a WHERE a.driverId = :userId OR a.dispatcherId = :userId)")
    List<Incident> findIncidentsByResponsibleUser(@Param("userId") Long userId);

}
