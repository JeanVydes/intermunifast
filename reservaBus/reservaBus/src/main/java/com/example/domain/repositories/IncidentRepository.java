package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.domain.entities.Incident;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @Query("SELECT i FROM Incident i WHERE i.entityType = 'TICKET' AND i.entityId = :ticketId")
    List<Incident> findByTicketId(Long ticketId);

    @Query("SELECT i FROM Incident i WHERE i.entityType = 'TRIP' AND i.entityId = :tripId")
    List<Incident> findByTripId(Long tripId);

}
