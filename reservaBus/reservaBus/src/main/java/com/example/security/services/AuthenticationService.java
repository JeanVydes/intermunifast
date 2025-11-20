package com.example.security.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;

@Service
public class AuthenticationService {
    public Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Account)) {
            throw new IllegalStateException("Principal is not an Account instance");
        }

        return (Account) principal;
    }

    public Long getCurrentAccountId() {
        return getCurrentAccount().getId();
    }

    public String getCurrentAccountEmail() {
        return getCurrentAccount().getEmail();
    }

    public String getCurrentAccountName() {
        return getCurrentAccount().getName();
    }

    public AccountRole getCurrentAccountRole() {
        return getCurrentAccount().getRole();
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public boolean hasRole(AccountRole role) {
        return getCurrentAccountRole() == role;
    }

    public boolean hasAnyRole(AccountRole... roles) {
        AccountRole currentRole = getCurrentAccountRole();
        for (AccountRole role : roles) {
            if (currentRole == role) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return hasRole(AccountRole.ADMIN);
    }

    public boolean isPassenger() {
        return hasRole(AccountRole.PASSENGER);
    }

    public boolean isClerk() {
        return hasRole(AccountRole.CLERK);
    }

    public boolean isDriver() {
        return hasRole(AccountRole.DRIVER);
    }

    public boolean isDispatcher() {
        return hasRole(AccountRole.DISPATCHER);
    }

    public void requireRole(AccountRole role) {
        if (!hasRole(role)) {
            throw new AccessDeniedException(
                    "Access denied. Required role: " + role + ", but user has: " + getCurrentAccountRole());
        }
    }

    public void requireAnyRole(AccountRole... roles) {
        if (!hasAnyRole(roles)) {
            StringBuilder allowedRoles = new StringBuilder();
            for (int i = 0; i < roles.length; i++) {
                allowedRoles.append(roles[i]);
                if (i < roles.length - 1) {
                    allowedRoles.append(", ");
                }
            }
            throw new AccessDeniedException(
                    "Access denied. Required one of: [" + allowedRoles + "], but user has: " + getCurrentAccountRole());
        }
    }
}
