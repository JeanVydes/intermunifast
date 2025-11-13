package com.example.api.controllers;

import java.util.Map;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.dto.AuthenticationDTOs;
import com.example.domain.entities.Account;
import com.example.domain.repositories.AccountRepository;
import com.example.security.jwt.JwtService;
import com.example.services.definitions.AuthenticationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final AccountRepository accountRepository;
        private final AuthenticationService authenticationService;

        @PostMapping("/signin")
        public ResponseEntity<AuthenticationDTOs.SignInResponse> signIn(
                        @RequestBody AuthenticationDTOs.SignInRequest authRequest) {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                                authRequest.email(),
                                authRequest.password()));

                Account account = accountRepository.findByEmailIgnoreCase(authRequest.email()).orElseThrow();

                var token = jwtService.generateToken(account, Map.of("roles", account.getRole().name()));
                return ResponseEntity.ok()
                                .body(new AuthenticationDTOs.SignInResponse(token, account.getId(),
                                                account.getRole().name()));
        }

        @GetMapping("/me")
        public ResponseEntity<Map<String, Object>> getCurrentUser() {
                Account account = authenticationService.getCurrentAccount();
                return ResponseEntity.ok(Map.of(
                                "id", account.getId(),
                                "name", account.getName(),
                                "email", account.getEmail(),
                                "phone", account.getPhone(),
                                "role", account.getRole().name(),
                                "status", account.getStatus().name()));
        }
}
