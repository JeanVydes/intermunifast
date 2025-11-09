package com.example.api.controllers;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.dto.AuthenticationDTOs;
import com.example.domain.entities.Account;
import com.example.domain.repositories.AccountRepository;
import com.example.security.jwt.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;

    @PostMapping("/signin")
    public ResponseEntity<AuthenticationDTOs.SignInResponse> signIn(
            @RequestBody AuthenticationDTOs.SignInRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.email(),
                authRequest.password()));

        Account account = accountRepository.findByEmailIgnoreCase(authRequest.email()).orElseThrow();

        var principal = User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .authorities(account.getRole().name())
                .build();

        var token = jwtService.generateToken(principal, Map.of("roles", account.getRole().name()));
        return ResponseEntity.ok()
                .body(new AuthenticationDTOs.SignInResponse(token, account.getId(), account.getRole().name()));
    }
}
