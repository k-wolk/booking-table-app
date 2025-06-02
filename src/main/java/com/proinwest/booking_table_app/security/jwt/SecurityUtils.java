package com.proinwest.booking_table_app.security.jwt;

import com.proinwest.booking_table_app.exceptions.types.CustomSecurityException;
import com.proinwest.booking_table_app.security.userDetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public static final String ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER = "Access denied: You must be an admin or the owner.";

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isUser() {
        return hasRole("ROLE_USER");
    }

    public void isAdminOrOwner(Long userId) {
        if (!isAdmin() && !isOwner(userId))
            throw new CustomSecurityException(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER);
    }

    public boolean isOwner(Long userId) {
        return userId.equals(getCurrentUserId());
    }

    private boolean hasRole(String role) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null)
            throw new CustomSecurityException("No authenticated user found.");

        final Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) return userDetails.getId();

        throw new CustomSecurityException("Expected CustomUserDetails, but got: " + principal.getClass().getName());
    }
}
