package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.UpdateCalendarFeedRequest;
import com.lorenzodm.librepm.core.entity.CalendarFeedToken;

/**
 * ICS calendar feed service (PRD-14-FR-001, FR-002, BR-001).
 */
public interface IcsExportService {

    /**
     * Returns or creates the CalendarFeedToken for a user.
     */
    CalendarFeedToken getOrCreateToken(String userId);

    /**
     * Regenerates the token (PRD-14-BR-001: token MUST be regenerable).
     * The old token is invalidated immediately.
     */
    CalendarFeedToken regenerateToken(String userId);

    /**
     * Updates feed configuration (included entity types, description).
     * PRD-14-FR-002: configurable entity inclusion.
     */
    CalendarFeedToken updateFeedConfig(String userId, UpdateCalendarFeedRequest req);

    /**
     * Generates the ICS feed content for a given token (PRD-14-AC-001).
     * Returns null if the token is invalid.
     */
    String generateIcsContent(String token);
}
