package com.example.services.definitions;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.AccountDTOs;
import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;
import com.example.domain.repositories.AccountRepository;
import com.example.services.mappers.AccountMapper;
import com.example.exceptions.NotFoundException;

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
        Account existingAccount = repo.findByEmailIgnoreCase(req.email()).orElse(null);
        if (existingAccount != null) {
            throw new IllegalStateException("An account with email %s already exists".formatted(req.email()));
        }

        Account account = mapper.toEntity(req);
        // Encrypt password before saving
        account.setPasswordHash(passwordEncoder.encode(req.password()));
        account.setRole(AccountRole.PASSENGER);
        account.setStatus(AccountStatus.ACTIVE);

        if (req.isAdmin()) {
            account.setRole(AccountRole.ADMIN);
        }

        return mapper.toResponse(repo.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTOs.AccountResponse getAccountById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(id)));
    }

    @Override
    public AccountDTOs.AccountResponse updateAccount(Long id, AccountDTOs.UpdateAccountRequest req) {
        Account currentAccount = authenticationService.getCurrentAccount();

        if (!currentAccount.getId().equals(id)) {
            throw new IllegalStateException("You can only update your own account");
        }

        Account account = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Account %d not found".formatted(id)));

        // Update only fields that are present in the request
        req.name().ifPresent(account::setName);
        req.email().ifPresent(account::setEmail);
        req.phone().ifPresent(account::setPhone);
        req.password().ifPresent(pwd -> account.setPasswordHash(passwordEncoder.encode(pwd)));

        return mapper.toResponse(repo.save(account));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Override
    public void deleteAccount(Long id) {
        Account account = repo.findById(id)
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
