package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.EventJournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventJournalRepository extends JpaRepository<EventJournalEntry, Long> {

    List<EventJournalEntry> findByTimestampBetweenOrderByTimestampDesc(Instant from, Instant to);

    List<EventJournalEntry> findByEventTypeInAndTimestampBetweenOrderByTimestampDesc(
            List<String> eventTypes, Instant from, Instant to);

    @Query("SELECT e FROM EventJournalEntry e ORDER BY e.timestamp DESC")
    List<EventJournalEntry> findRecent(@Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM EventJournalEntry e WHERE e.timestamp < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
