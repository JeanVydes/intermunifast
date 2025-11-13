package com.example.services.definitions;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.domain.entities.Account;
import com.example.domain.enums.AccountRole;

/**
 * Service to handle authentication-related operations.
 * Provides easy access to the currently authenticated user's information and
 * role-based checks.
 */
@Service
public class AuthenticationService {

    /**
     * Gets the currently authenticated account.
     * 
     * @return the authenticated Account entity
     * @throws IllegalStateException if no user is authenticated
     */
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

    /**
     * Gets the ID of the currently authenticated account.
     * 
     * @return the ID of the authenticated account
     * @throws IllegalStateException if no user is authenticated
     */
    public Long getCurrentAccountId() {
        return getCurrentAccount().getId();
    }

    /**
     * Gets the email of the currently authenticated account.
     * 
     * @return the email of the authenticated account
     * @throws IllegalStateException if no user is authenticated
     */
    public String getCurrentAccountEmail() {
        return getCurrentAccount().getEmail();
    }

    /**
     * Gets the name of the currently authenticated account.
     * 
     * @return the name of the authenticated account
     * @throws IllegalStateException if no user is authenticated
     */
    public String getCurrentAccountName() {
        return getCurrentAccount().getName();
    }

    /**
     * Gets the role of the currently authenticated account.
     * 
     * @return the role of the authenticated account
     * @throws IllegalStateException if no user is authenticated
     */
    public AccountRole getCurrentAccountRole() {
        return getCurrentAccount().getRole();
    }

    /**
     * Checks if there is a currently authenticated user.
     * 
     * @return true if a user is authenticated, false otherwise
     */
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
