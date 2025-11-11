package com.example.domain.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Config;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    
     Optional<Config> findByKey(String key);
      List<Config> findAll();
}
