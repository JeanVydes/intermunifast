package com.example.domain.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("SELECT a FROM Assignment a JOIN a.trip t WHERE a.driver.id = :driverId AND DATE(t.departureAt) = DATE(:departureDate)")
    List<Assignment> findAssignmentsByDriverAndDate(@Param("driverId") Long driverId,
            @Param("departureDate") LocalDateTime departureDate);

    List<Assignment> findByTrip_Id(Long tripId);

    // Spring Data JPA automáticamente entiende la relación driver.id
    List<Assignment> findByDriver_Id(Long driverId);

    // Spring Data JPA automáticamente entiende la relación dispatcher.id
    List<Assignment> findByDispatcher_Id(Long dispatcherId);

    List<Assignment> findByTrip_IdAndChecklistOkTrue(Long tripId);

}
