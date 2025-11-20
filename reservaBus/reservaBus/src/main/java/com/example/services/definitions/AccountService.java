package com.example.services.definitions;

import java.util.List;

import com.example.api.dto.AccountDTOs;

public interface AccountService {
    AccountDTOs.AccountResponse createAccount(AccountDTOs.CreateAccountRequest req);

    AccountDTOs.AccountResponse getAccountById(Long id);

    List<AccountDTOs.AccountResponse> getAllAccounts();

    AccountDTOs.AccountResponse updateAccount(Long id, AccountDTOs.UpdateAccountRequest req);

    void deleteAccount(Long id);

    AccountDTOs.AccountResponse getAccountByEmail(String email);
}
