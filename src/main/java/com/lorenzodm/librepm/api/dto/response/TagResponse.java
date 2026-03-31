package com.lorenzodm.librepm.api.dto.response;

import java.time.Instant;

/**
 * Response DTO per Tag
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
public record TagResponse(
        String id,
        String name,
        String color,
        Instant createdAt,
        String ownerId,
        Integer taskCount // Numero di task che usano questo tag
) {
}
