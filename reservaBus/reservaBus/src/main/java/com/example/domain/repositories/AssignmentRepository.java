package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByTrip_Id(Long tripId);

    // Spring Data JPA automáticamente entiende la relación driver.id
    List<Assignment> findByDriver_Id(Long driverId);

    // Spring Data JPA automáticamente entiende la relación dispatcher.id
    List<Assignment> findByDispatcher_Id(Long dispatcherId);

}
