package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.api.exception.UnauthorizedException;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.UserSettings;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.UserSettingsRepository;
import com.lorenzodm.librepm.service.CalendarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CalendarServiceImpl implements CalendarService {

    private final TaskRepository taskRepository;
    private final UserSettingsRepository userSettingsRepository;

    public CalendarServiceImpl(TaskRepository taskRepository, UserSettingsRepository userSettingsRepository) {
        this.taskRepository = taskRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    @Override
    public String generateIcsFeed(String userId, String token) {
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Settings non trovati"));

        if (!token.equals(settings.getCalendarToken())) {
            throw new UnauthorizedException("Token calendario non valido");
        }

        // Recupera task assegnati all'utente che non sono archiviati
        List<Task> tasks = taskRepository.findByAssignedToId(userId).stream()
                .filter(t -> !t.isArchived())
                .filter(t -> t.getDeadline() != null || t.getPlannedStart() != null)
                .toList();

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\n");
        ics.append("VERSION:2.0\n");
        ics.append("PRODID:-//LibrePM//NONSGML v1.0//EN\n");
        ics.append("CALSCALE:GREGORIAN\n");
        ics.append("METHOD:PUBLISH\n");
        ics.append("X-WR-CALNAME:LibrePM Tasks\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneId.of("UTC"));

        for (Task t : tasks) {
            ics.append("BEGIN:VEVENT\n");
            ics.append("UID:").append(t.getId()).append("@librepm.local\n");
            ics.append("SUMMARY:").append(escape(t.getTitle())).append("\n");
            
            if (t.getDescription() != null) {
                ics.append("DESCRIPTION:").append(escape(t.getDescription())).append("\n");
            }

            // Date logic
            if (t.getPlannedStart() != null) {
                ics.append("DTSTART:").append(formatter.format(t.getPlannedStart().atZone(ZoneId.systemDefault()))).append("\n");
                if (t.getPlannedFinish() != null) {
                    ics.append("DTEND:").append(formatter.format(t.getPlannedFinish().atZone(ZoneId.systemDefault()))).append("\n");
                } else {
                    // Default 1h duration if no end
                    ics.append("DTEND:").append(formatter.format(t.getPlannedStart().plusHours(1).atZone(ZoneId.systemDefault()))).append("\n");
                }
            } else if (t.getDeadline() != null) {
                // All day event for deadline
                ics.append("DTSTART;VALUE=DATE:").append(t.getDeadline().format(DateTimeFormatter.BASIC_ISO_DATE)).append("\n");
            }

            String statusName = t.getStatus() != null ? t.getStatus().getName() : "";
            ics.append("STATUS:").append(mapStatus(statusName)).append("\n");
            ics.append("END:VEVENT\n");
        }

        ics.append("END:VCALENDAR");
        return ics.toString();
    }

    @Override
    public String rotateToken(String userId) {
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Settings non trovati"));
        
        String newToken = UUID.randomUUID().toString();
        settings.setCalendarToken(newToken);
        userSettingsRepository.save(settings);
        
        return newToken;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    private String mapStatus(String status) {
        if ("DONE".equals(status) || "COMPLETED".equals(status)) return "CONFIRMED";
        if ("CANCELLED".equals(status)) return "CANCELLED";
        return "TENTATIVE";
    }
}
