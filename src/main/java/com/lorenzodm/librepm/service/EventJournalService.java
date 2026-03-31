package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.EventJournalEntry;

import java.time.Instant;
import java.util.List;

/**
 * Service for recording and querying local event journal entries.
 * Provides observability without external services.
 */
public interface EventJournalService {

    void record(String eventType, String entityType, String entityId, String payload, String userId);

    List<EventJournalEntry> query(Instant from, Instant to, List<String> eventTypes, int limit);

    List<EventJournalEntry> recent(int limit);

    int cleanup(int retentionDays);
}
