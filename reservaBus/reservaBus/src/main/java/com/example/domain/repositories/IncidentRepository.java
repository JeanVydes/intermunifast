package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Incident;
import com.example.domain.enums.EntityType;
import com.example.domain.enums.IncidentType;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    // NOTE: Incident doesn't have direct ticket/trip relationships, only entityType
    // + entityId
    // These queries should probably filter by EntityType too
    @Query("SELECT i FROM Incident i WHERE i.entityType = 'TICKET' AND i.entityId = :ticketId")
    List<Incident> findByTicketId(Long ticketId);

    @Query("SELECT i FROM Incident i WHERE i.entityType = 'TRIP' AND i.entityId = :tripId")
    List<Incident> findByTripId(Long tripId);

    List<Incident> findByType(IncidentType type);

    // createdAt y updatedAt son epoch millis (Long) en TimestampedEntity.
    // Ajustamos la firma para evitar el error de tipo (Long vs LocalDateTime)
    List<Incident> findByCreatedAtBetween(Long startEpochMillis, Long endEpochMillis);

    @Query("SELECT i FROM Incident i WHERE i.entityId IN (SELECT a.trip.id FROM Assignment a WHERE a.driver.id = :userId OR a.dispatcher.id = :userId)")
    List<Incident> findIncidentsByResponsibleUser(@Param("userId") Long userId);

}
