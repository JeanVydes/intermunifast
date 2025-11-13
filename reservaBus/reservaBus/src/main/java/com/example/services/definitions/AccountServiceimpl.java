package com.example.services.definitions;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.AccountDTOs;
import com.example.domain.entities.Account;
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

    @Override
    public AccountDTOs.AccountResponse createAccount(AccountDTOs.CreateAccountRequest req) {
        Account account = mapper.toEntity(req);
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
        mapper.patch(account, req);
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
        return repo.findByEmailIgnoreCase(email).map(mapper::toResponse).
                orElseThrow(() -> new NotFoundException("Account with email %s not found".formatted(email)));

    }

}
