package com.example.domain.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Bus;
import com.example.domain.enums.BusStatus;

public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByPlate(String plate);

    // NOTE: Bus doesn't have 'type' field, commented out
    // List<Bus> findByType(String type);

    List<Bus> findByCapacityGreaterThanEqual(int capacity);

    List<Bus> findByStatus(BusStatus status);

    // NOTE: Bus doesn't have 'assigned' field either, commented out
    // List<Bus> findByAssignedFalse();

    // NOTE: Bus doesn't have 'currentTripId' field, commented out
    // List<Bus> findByCurrentTripId(Long tripId);

}
