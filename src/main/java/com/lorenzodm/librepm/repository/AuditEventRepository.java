package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {

    List<AuditEvent> findByEntityTypeAndEntityIdOrderByEventTimestampDesc(String entityType, String entityId);

    List<AuditEvent> findByUserIdOrderByEventTimestampDesc(String userId);

    List<AuditEvent> findByActionOrderByEventTimestampDesc(AuditEvent.Action action);

    List<AuditEvent> findByEventTimestampAfterOrderByEventTimestampDesc(Instant since);
}
