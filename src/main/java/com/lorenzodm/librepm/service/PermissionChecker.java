package com.lorenzodm.librepm.service;

/**
 * Checks whether a user has a specific permission within a project context.
 *
 * @author Lorenzo DM
 * @since 0.9.1
 */
public interface PermissionChecker {

    /**
     * Checks if a user has the given permission in the context of a project.
     * Owner and Admin always have all permissions.
     *
     * @param userId     the user ID
     * @param projectId  the project ID
     * @param permission the permission string (e.g., "BUDGET_WRITE")
     * @return true if the user has the permission
     */
    boolean hasPermission(String userId, String projectId, String permission);

    /**
     * Throws ForbiddenException if the user lacks the permission.
     */
    void requirePermission(String userId, String projectId, String permission);
}
