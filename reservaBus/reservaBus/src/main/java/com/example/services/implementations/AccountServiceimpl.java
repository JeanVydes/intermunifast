package com.example.services.implementations;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.AccountDTOs;
import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;
import com.example.domain.repositories.AccountRepository;
import com.example.exceptions.NotFoundException;
import com.example.security.services.AuthenticationService;
import com.example.services.definitions.AccountService;
import com.example.services.mappers.AccountMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceimpl implements AccountService {
    private final AccountRepository repo;
    private final AccountMapper mapper;
    private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AccountDTOs.AccountResponse createAccount(AccountDTOs.CreateAccountRequest req) {
        var existingAccount = repo.findByEmailIgnoreCase(req.email()).orElse(null);
        if (existingAccount != null) {
            throw new IllegalStateException("An account with email %s already exists".formatted(req.email()));
        }

        var account = mapper.toEntity(req);
        account.setPasswordHash(passwordEncoder.encode(req.password()));
        account.setRole(req.isAdmin() ? AccountRole.ADMIN : AccountRole.PASSENGER);
        account.setStatus(AccountStatus.ACTIVE);

        return mapper.toResponse(repo.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTOs.AccountResponse getAccountById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(id)));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'DISPATCHER')")
    @Override
    @Transactional(readOnly = true)
    public List<AccountDTOs.AccountResponse> getAllAccounts() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public AccountDTOs.AccountResponse updateAccount(Long id, AccountDTOs.UpdateAccountRequest req) {
        var currentAccount = authenticationService.getCurrentAccount();
        var isAdmin = currentAccount.getRole() == AccountRole.ADMIN;

        if (!isAdmin && !currentAccount.getId().equals(id)) {
            throw new IllegalStateException("You can only update your own account");
        }

        var account = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(id)));

        req.name().ifPresent(account::setName);
        req.email().ifPresent(account::setEmail);
        req.phone().ifPresent(account::setPhone);
        req.password().ifPresent(pwd -> account.setPasswordHash(passwordEncoder.encode(pwd)));

        if (isAdmin) {
            req.role().ifPresent(account::setRole);
            req.status().ifPresent(account::setStatus);
        }

        return mapper.toResponse(repo.save(account));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Override
    public void deleteAccount(Long id) {
        var account = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(id)));
        repo.delete(account);
    }

    @PreAuthorize("hasAnyAuthority('CLERK', 'DISPATCHER', 'ADMIN')")
    @Override
    @Transactional(readOnly = true)
    public AccountDTOs.AccountResponse getAccountByEmail(String email) {
        return repo.findByEmailIgnoreCase(email).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Account with email %s not found".formatted(email)));
    }
}
