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
        boolean isAdmin = hasRole("ROLE_ADMIN");
        System.out.println("isAdmin: " + isAdmin);
        return isAdmin;
    }

    public boolean isUser() {
        return hasRole("ROLE_USER");
    }

    public void isAdminOrOwner(Long userId) {
        if (!isAdmin() && !isOwner(userId))
            throw new CustomSecurityException(ACCESS_DENIED_AN_ADMIN_OR_THE_OWNER);
    }

    public boolean isOwner(Long userId) {
        Long currentUserId = getCurrentUserId();
        boolean isOwner = userId.equals(currentUserId);
        System.out.println("current user ID: " + currentUserId);
        System.out.println("isOwner: " + isOwner);
        return isOwner;
//        return userId.isOwner(getCurrentUserId());
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

        System.out.println("DEBUG1: Authentication: " + authentication);
        System.out.println("DEBUG2: Principal: " + authentication.getPrincipal());
        System.out.println("DEBUG3: Principal class: " + authentication.getPrincipal().getClass().getName());

        if (authentication == null || authentication.getPrincipal() == null)
            throw new CustomSecurityException("No authenticated user found.");

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) return userDetails.getId();

        throw new CustomSecurityException("Expected CustomUserDetails, but got: " + principal.getClass().getName());
    }


}
