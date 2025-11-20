package com.example.domain.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByBus_Id(Long busId);
}
