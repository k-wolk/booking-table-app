package com.proinwest.booking_table_app.security.jwt;

import com.proinwest.booking_table_app.security.userDetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isUser() {
        return hasRole("ROLE_USER");
    }

    public void isAdminOrOwner(Long userId) {
        if (!isAdmin() && !isCurrentUser(userId))
            throw new SecurityException("Access denied: You must be an admin or the owner.");
    }

    public boolean isCurrentUser(Long userId) {
        return userId.equals(getCurrentUserId());
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("DEBUG: Authentication: " + authentication);
        System.out.println("DEBUG: Principal: " + authentication.getPrincipal());
        System.out.println("DEBUG: Principal class: " + authentication.getPrincipal().getClass().getName());

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("No authenticated user found.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        throw new SecurityException("Expected CustomUserDetails, but got: " + principal.getClass().getName());
    }


}
