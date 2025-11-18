package com.example.domain.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Bus;
import com.example.domain.enums.BusStatus;

public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByPlate(String plate);

    List<Bus> findByStatus(BusStatus status);

}
