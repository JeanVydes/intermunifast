package com.example.domain.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;

public interface AccountRepository extends JpaRepository<Account, Long> {

     Optional<Account> findByEmail(String email);
     List<Account> findByRole(AccountRole role);
     List<Account> findByStatus(AccountStatus status);
      Optional<Account> findByPhone(String phone);

}
