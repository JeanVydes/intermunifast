package com.example.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.api.dto.AccountDTOs;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDTOs.AccountResponse> create(
            @Validated @RequestBody AccountDTOs.CreateAccountRequest req,
            UriComponentsBuilder uriBuilder) {
        AccountDTOs.AccountResponse createdAccount = accountService.createAccount(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/accounts/{id}").buildAndExpand(createdAccount.id()).toUri())
                .body(createdAccount);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTOs.AccountResponse> getById(@PathVariable Long id) {
        AccountDTOs.AccountResponse account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountDTOs.AccountResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody AccountDTOs.UpdateAccountRequest req) {
        AccountDTOs.AccountResponse updatedAccount = accountService.updateAccount(id, req);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<AccountDTOs.AccountResponse> getByEmail(@PathVariable String email) {
        AccountDTOs.AccountResponse account = accountService.getAccountByEmail(email);
        return ResponseEntity.ok(account);
    }
}
