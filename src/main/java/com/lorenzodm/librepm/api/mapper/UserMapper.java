package com.lorenzodm.librepm.api.mapper;

import com.lorenzodm.librepm.api.dto.response.UserResponse;
import com.lorenzodm.librepm.core.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper per conversione User <-> UserResponse
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @updated 0.3.0 - Aggiunto lastLoginAt
 */
@Component
public class UserMapper {

    /**
     * Converte User entity in UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        int projectsCount = user.getProjects() != null ? user.getProjects().size() : 0;

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarPath(),
                user.isActive(),
                user.isGhost(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                user.getLastSyncedAt(),
                user.getSyncStatus() != null ? user.getSyncStatus().name() : null,
                projectsCount
        );
    }

    /**
     * Versione light senza conteggio progetti (per liste)
     */
    public UserResponse toResponseLight(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarPath(),
                user.isActive(),
                user.isGhost(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                user.getLastSyncedAt(),
                user.getSyncStatus() != null ? user.getSyncStatus().name() : null,
                0 // Skip conteggio per performance
        );
    }
}