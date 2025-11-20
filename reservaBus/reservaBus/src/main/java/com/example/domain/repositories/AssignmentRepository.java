package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByTrip_Id(Long tripId);

}
