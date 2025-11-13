package com.example.services;

import com.example.api.dto.AccountDTOs;
import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;
import com.example.domain.repositories.AccountRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.AccountServiceimpl;
import com.example.services.definitions.AuthenticationService;
import com.example.services.mappers.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Service Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceimpl accountService;

    private Account testAccount;
    private AccountDTOs.AccountResponse accountResponse;
    private AccountDTOs.CreateAccountRequest createRequest;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .passwordHash("hashed_password")
                .role(AccountRole.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .build();

        accountResponse = new AccountDTOs.AccountResponse(
                1L,
                "Test User",
                "test@example.com",
                "1234567890",
                AccountRole.PASSENGER,
                AccountStatus.ACTIVE);

        createRequest = new AccountDTOs.CreateAccountRequest(
                "New User",
                "newuser@example.com",
                "1234567890",
                "password123");
    }

    @Test
    @DisplayName("Should get account by ID successfully")
    void shouldGetAccountById() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountMapper.toResponse(testAccount)).thenReturn(accountResponse);

        // When
        AccountDTOs.AccountResponse result = accountService.getAccountById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(accountRepository).findById(1L);
        verify(accountMapper).toResponse(testAccount);
    }

    @Test
    @DisplayName("Should throw NotFoundException when account not found")
    void shouldThrowNotFoundExceptionWhenAccountNotFound() {
        // Given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.getAccountById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Account");

        verify(accountRepository).findById(999L);
        verify(accountMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should create account successfully")
    void shouldCreateAccountSuccessfully() {
        // Given
        when(accountMapper.toEntity(createRequest)).thenReturn(testAccount);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toResponse(testAccount)).thenReturn(accountResponse);

        // When
        AccountDTOs.AccountResponse result = accountService.createAccount(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should get account by email")
    void shouldFindAccountByEmail() {
        // Given
        when(accountRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testAccount));
        when(accountMapper.toResponse(testAccount)).thenReturn(accountResponse);

        // When
        AccountDTOs.AccountResponse result = accountService.getAccountByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(accountRepository).findByEmailIgnoreCase("test@example.com");
    }

    @Test
    @DisplayName("Should update account successfully")
    void shouldUpdateAccountSuccessfully() {
        // Given
        AccountDTOs.UpdateAccountRequest updateRequest = new AccountDTOs.UpdateAccountRequest(
                Optional.of("Updated Name"),
                Optional.of("test@example.com"),
                Optional.of("9876543210"),
                Optional.empty(),
                AccountRole.PASSENGER,
                AccountStatus.ACTIVE);

        when(authenticationService.getCurrentAccount()).thenReturn(testAccount);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toResponse(testAccount)).thenReturn(accountResponse);

        // When
        AccountDTOs.AccountResponse result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should delete account by ID")
    void shouldDeleteAccountById() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).delete(testAccount);

        // When
        accountService.deleteAccount(1L);

        // Then
        verify(accountRepository).findById(1L);
        verify(accountRepository).delete(testAccount);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent account")
    void shouldThrowExceptionWhenDeletingNonExistentAccount() {
        // Given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.deleteAccount(999L))
                .isInstanceOf(NotFoundException.class);

        verify(accountRepository).findById(999L);
        verify(accountRepository, never()).delete(any());
    }
}
