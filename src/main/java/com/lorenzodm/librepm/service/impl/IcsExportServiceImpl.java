package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.UpdateCalendarFeedRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.CalendarFeedToken;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.CalendarFeedTokenRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.IcsExportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * ICS calendar feed implementation (PRD-14-FR-001, FR-002, BR-001).
 * Generates RFC 5545-compliant iCalendar content without external libraries.
 */
@Service
@Transactional
public class IcsExportServiceImpl implements IcsExportService {

    private static final DateTimeFormatter ICS_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter ICS_DATE_ONLY_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CalendarFeedTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public IcsExportServiceImpl(CalendarFeedTokenRepository tokenRepository,
                                 UserRepository userRepository,
                                 TaskRepository taskRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public CalendarFeedToken getOrCreateToken(String userId) {
        return tokenRepository.findByUserId(userId).orElseGet(() -> createToken(userId));
    }

    @Override
    public CalendarFeedToken regenerateToken(String userId) {
        // PRD-14-BR-001: token MUST be regenerable; old token invalidated on regeneration
        CalendarFeedToken token = tokenRepository.findByUserId(userId)
                .orElseGet(() -> createToken(userId));
        token.setToken(generateToken());
        return tokenRepository.save(token);
    }

    @Override
    public CalendarFeedToken updateFeedConfig(String userId, UpdateCalendarFeedRequest req) {
        CalendarFeedToken token = getOrCreateToken(userId);
        if (req.includedEntityTypes() != null) token.setIncludedEntityTypes(req.includedEntityTypes());
        if (req.description() != null) token.setDescription(req.description());
        return tokenRepository.save(token);
    }

    @Override
    @Transactional
    public String generateIcsContent(String rawToken) {
        CalendarFeedToken feedToken = tokenRepository.findByToken(rawToken)
                .orElse(null);
        if (feedToken == null) return null;

        // Update last accessed
        feedToken.setLastAccessedAt(Instant.now());
        tokenRepository.save(feedToken);

        String userId = feedToken.getUser().getId();
        String includedTypes = feedToken.getIncludedEntityTypes() != null
                ? feedToken.getIncludedEntityTypes() : "Task";

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//LibrePM//LibrePM Calendar//IT\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("X-WR-CALNAME:LibrePM - ").append(sanitize(feedToken.getUser().getDisplayName())).append("\r\n");

        if (includedTypes.contains("Task")) {
            appendTaskEvents(ics, userId);
        }

        ics.append("END:VCALENDAR\r\n");
        return ics.toString();
    }

    // --- Private helpers ---

    private CalendarFeedToken createToken(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + userId));
        CalendarFeedToken token = new CalendarFeedToken();
        token.setUser(user);
        token.setToken(generateToken());
        token.setIncludedEntityTypes("Task");
        return tokenRepository.save(token);
    }

    private String generateToken() {
        // Cryptographically strong opaque token
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    private void appendTaskEvents(StringBuilder ics, String userId) {
        List<Task> tasks = taskRepository.findByAssignedToId(userId);
        for (Task task : tasks) {
            if (task.getDeadline() == null && task.getPlannedStart() == null) continue;

            ics.append("BEGIN:VEVENT\r\n");
            ics.append("UID:librepm-task-").append(task.getId()).append("@librepm.app\r\n");
            ics.append("DTSTAMP:").append(ICS_DATE_FMT.format(Instant.now())).append("\r\n");
            ics.append("SUMMARY:").append(sanitize(task.getTitle())).append("\r\n");

            if (task.getDescription() != null && !task.getDescription().isBlank()) {
                ics.append("DESCRIPTION:").append(sanitize(task.getDescription())).append("\r\n");
            }

            // Dates: prefer plannedStart/plannedFinish, fall back to deadline
            if (task.getPlannedStart() != null) {
                ics.append("DTSTART:").append(ICS_DATE_FMT.format(
                        task.getPlannedStart().toInstant(ZoneOffset.UTC))).append("\r\n");
                if (task.getPlannedFinish() != null) {
                    ics.append("DTEND:").append(ICS_DATE_FMT.format(
                            task.getPlannedFinish().toInstant(ZoneOffset.UTC))).append("\r\n");
                } else {
                    ics.append("DTEND:").append(ICS_DATE_FMT.format(
                            task.getPlannedStart().toInstant(ZoneOffset.UTC))).append("\r\n");
                }
            } else if (task.getDeadline() != null) {
                ics.append("DTSTART;VALUE=DATE:")
                   .append(ICS_DATE_ONLY_FMT.format(task.getDeadline())).append("\r\n");
                ics.append("DTEND;VALUE=DATE:")
                   .append(ICS_DATE_ONLY_FMT.format(task.getDeadline().plusDays(1))).append("\r\n");
            }

            // Status mapping
            String statusName = task.getStatus() != null ? task.getStatus().getName().toUpperCase() : "TODO";
            String icsStatus = switch (statusName) {
                case "DONE", "COMPLETED" -> "COMPLETED";
                case "CANCELLED" -> "CANCELLED";
                default -> "IN-PROCESS";
            };
            ics.append("STATUS:").append(icsStatus).append("\r\n");

            // Priority mapping (HIGH=1, MEDIUM=5, LOW=9 per RFC 5545)
            if (task.getPriority() != null) {
                int icsP = switch (task.getPriority().getName().toUpperCase()) {
                    case "CRITICAL" -> 1;
                    case "HIGH" -> 2;
                    case "LOW" -> 9;
                    default -> 5;
                };
                ics.append("PRIORITY:").append(icsP).append("\r\n");
            }

            if (task.getCreatedAt() != null) {
                ics.append("CREATED:").append(ICS_DATE_FMT.format(task.getCreatedAt())).append("\r\n");
            }
            if (task.getUpdatedAt() != null) {
                ics.append("LAST-MODIFIED:").append(ICS_DATE_FMT.format(task.getUpdatedAt())).append("\r\n");
            }

            ics.append("END:VEVENT\r\n");
        }
    }

    /** Escapes special characters per RFC 5545. */
    private String sanitize(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
