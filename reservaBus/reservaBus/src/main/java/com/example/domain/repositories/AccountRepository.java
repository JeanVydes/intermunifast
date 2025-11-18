package com.example.domain.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

      Optional<Account> findByEmailIgnoreCase(String email);

      boolean existsByEmail(String email);

}
