package com.example.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Baggage;

public interface BaggageRepository extends JpaRepository<Baggage, Long> {
    
     Baggage findByTagCode(String tagCode);
     
}
