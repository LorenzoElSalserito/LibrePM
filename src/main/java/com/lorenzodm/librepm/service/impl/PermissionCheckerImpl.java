package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ForbiddenException;
import com.lorenzodm.librepm.core.entity.ProjectMember;
import com.lorenzodm.librepm.repository.PermissionPolicyRepository;
import com.lorenzodm.librepm.repository.ProjectMemberRepository;
import com.lorenzodm.librepm.service.PermissionChecker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implements permission checking by combining the project member role
 * (OWNER/ADMIN/EDITOR/VIEWER) with the optional system_role_id linked
 * to the roles/permission_policies tables.
 *
 * @author Lorenzo DM
 * @since 0.9.1
 */
@Service
@Transactional(readOnly = true)
public class PermissionCheckerImpl implements PermissionChecker {

    private final ProjectMemberRepository projectMemberRepository;
    private final PermissionPolicyRepository permissionPolicyRepository;

    public PermissionCheckerImpl(ProjectMemberRepository projectMemberRepository,
                                  PermissionPolicyRepository permissionPolicyRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.permissionPolicyRepository = permissionPolicyRepository;
    }

    @Override
    public boolean hasPermission(String userId, String projectId, String permission) {
        Optional<ProjectMember> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (memberOpt.isEmpty()) return false;

        ProjectMember member = memberOpt.get();

        // OWNER and ADMIN always have all permissions
        if (member.getRole() == ProjectMember.Role.OWNER || member.getRole() == ProjectMember.Role.ADMIN) {
            return true;
        }

        // Check system role permissions if assigned
        String systemRoleId = member.getSystemRoleId();
        if (systemRoleId != null) {
            List<String> permissions = permissionPolicyRepository.findPermissionsByRoleId(systemRoleId);
            return permissions.contains(permission);
        }

        // Fallback: EDITOR has basic read/write, VIEWER has read-only
        if (member.getRole() == ProjectMember.Role.EDITOR) {
            return permission.endsWith("_READ") || permission.endsWith("_WRITE");
        }
        if (member.getRole() == ProjectMember.Role.VIEWER) {
            return permission.endsWith("_READ");
        }

        return false;
    }

    @Override
    public void requirePermission(String userId, String projectId, String permission) {
        if (!hasPermission(userId, projectId, permission)) {
            throw new ForbiddenException("Insufficient permissions: " + permission);
        }
    }
}
