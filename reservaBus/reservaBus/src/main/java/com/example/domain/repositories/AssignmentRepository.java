package com.example.domain.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.domain.entities.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("SELECT a FROM Assignment a JOIN a.trip t WHERE a.driverId = :driverId AND t.date = :date")
    List<Assignment> findAssignmentsByDriverAndDate(@Param("driverId") Long driverId, @Param("date") LocalDate date);

    List<Assignment> findByTripId(Long tripId);

    List<Assignment> findByDriverId(Long driverId);

    List<Assignment> findByDispatcherId(Long dispatcherId);

    List<Assignment> findByTripIdAndChecklistOkTrue(Long tripId);

    @Query("SELECT a FROM Assignment a JOIN a.trip t WHERE t.busId = :busId AND t.status = 'SCHEDULED'")
    List<Assignment> findPendingAssignmentsByBus(@Param("busId") Long busId);

}
