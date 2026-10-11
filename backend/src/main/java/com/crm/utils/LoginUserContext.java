package com.crm.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * ThreadLocal holder for current logged-in user context.
 * Set by JwtAuthenticationFilter, cleared after request.
 *
 * @author CRM Team
 */
public class LoginUserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLES_HOLDER = ThreadLocal.withInitial(ArrayList::new);

    /**
     * Set current user context
     */
    public static void setCurrentUser(Long userId, String username) {
        USER_ID_HOLDER.set(userId);
        USERNAME_HOLDER.set(username);
    }

    /**
     * Set current user context with roles
     */
    public static void setCurrentUser(Long userId, String username, List<String> roles) {
        USER_ID_HOLDER.set(userId);
        USERNAME_HOLDER.set(username);
        ROLES_HOLDER.set(roles != null ? roles : new ArrayList<>());
    }

    /**
     * Get current user ID
     */
    public static Long getCurrentUserId() {
        Long userId = USER_ID_HOLDER.get();
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return userId;
    }

    /**
     * Get current username
     */
    public static String getCurrentUsername() {
        return USERNAME_HOLDER.get();
    }

    /**
     * Get current user roles
     */
    public static List<String> getCurrentUserRoles() {
        List<String> roles = ROLES_HOLDER.get();
        return roles != null ? roles : new ArrayList<>();
    }

    /**
     * Check if current user has ADMIN role
     */
    public static boolean isAdmin() {
        return getCurrentUserRoles().contains("ADMIN");
    }

    /**
     * Clear context after request completes
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        ROLES_HOLDER.remove();
    }
}
