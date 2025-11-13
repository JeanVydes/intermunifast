package com.example.domain.repositories;

import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountRepository Integration Tests")
class AccountRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Account: encuentra por email (case insensitive) y verifica existencia")
    void shouldFindByEmailAndCheckExistence() {
        // Given
        Account account = Account.builder()
                .name("John Doe")
                .email("test@example.com")
                .passwordHash("$2a$10$hash")
                .phone("1234567890")
                .role(AccountRole.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .build();
        account.setCreatedAt(Instant.now().toEpochMilli());
        account.setUpdatedAt(Instant.now().toEpochMilli());

        accountRepository.save(account);

        // When / Then
        assertThat(accountRepository.findByEmailIgnoreCase("TEST@example.com")).isPresent();
        assertThat(accountRepository.findByEmailIgnoreCase("test@example.com")).isPresent();
        assertThat(accountRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(accountRepository.existsByEmail("notfound@example.com")).isFalse();
    }
}
