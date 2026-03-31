package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.StartFocusSessionRequest;
import com.lorenzodm.librepm.api.dto.request.StopFocusSessionRequest;
import com.lorenzodm.librepm.api.dto.response.FocusHeatmapResponse;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.api.exception.OwnershipViolationException;
import com.lorenzodm.librepm.core.entity.FocusSession;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.FocusSessionRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.FocusSessionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FocusSessionServiceImpl implements FocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public FocusSessionServiceImpl(FocusSessionRepository focusSessionRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.focusSessionRepository = focusSessionRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public FocusSession start(String userId, String taskId, StartFocusSessionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User non trovato: " + userId));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + taskId));

        // ownership: task.project.owner == userId
        if (task.getProject() == null || task.getProject().getOwner() == null || !userId.equals(task.getProject().getOwner().getId())) {
            throw new OwnershipViolationException("Task non appartiene all'utente");
        }

        // Multi-timer: NON fermare sessioni attive su altri task.
        // Ferma solo se c'è già un timer sullo STESSO task.
        List<FocusSession> running = getAllRunning(userId);
        for (FocusSession r : running) {
            if (r.getTask() != null && taskId.equals(r.getTask().getId())) {
                r.endSession();
                focusSessionRepository.save(r);
            }
        }

        FocusSession fs = new FocusSession(task, user);
        if (req != null) {
            if (req.notes() != null) fs.setNotes(req.notes());
            if (req.sessionType() != null && !req.sessionType().isBlank()) fs.setSessionType(req.sessionType());
        }

        return focusSessionRepository.save(fs);
    }

    @Override
    public FocusSession stop(String userId, String sessionId, StopFocusSessionRequest req) {
        FocusSession fs = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("FocusSession non trovata: " + sessionId));

        if (fs.getUser() == null || !userId.equals(fs.getUser().getId())) {
            throw new OwnershipViolationException("FocusSession non appartiene all'utente");
        }

        if (req != null && req.notes() != null) {
            fs.setNotes(req.notes());
        }

        fs.endSession();
        
        // Aggiorna actualMinutes del task
        Task task = fs.getTask();
        if (task != null) {
            long durationMinutes = fs.getDurationMs() / 1000 / 60;
            if (durationMinutes > 0) {
                task.setActualEffort(task.getActualEffort() + (int) durationMinutes);
                taskRepository.save(task);
            }
        }
        
        return focusSessionRepository.save(fs);
    }

    @Override
    public List<FocusSession> listByUser(String userId, String taskId) {
        if (taskId != null && !taskId.isBlank()) {
            // filtra task e ownership
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task non trovato: " + taskId));
            if (task.getProject() == null || task.getProject().getOwner() == null || !userId.equals(task.getProject().getOwner().getId())) {
                throw new OwnershipViolationException("Task non appartiene all'utente");
            }
            return focusSessionRepository.findByTaskIdOrderByStartedAtDesc(taskId);
        }
        return focusSessionRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    @Override
    public FocusSession getCurrentRunning(String userId) {
        List<FocusSession> sessions = focusSessionRepository.findByUserIdOrderByStartedAtDesc(userId);
        return sessions.stream()
                .filter(fs -> fs.getEndedAt() == null)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<FocusSession> getAllRunning(String userId) {
        return focusSessionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .filter(fs -> fs.getEndedAt() == null)
                .collect(Collectors.toList());
    }

    @Override
    public FocusHeatmapResponse getHeatmap(String userId, int days) {
        List<FocusSession> sessions = focusSessionRepository.findByUserIdOrderByStartedAtDesc(userId);
        
        // Filtra per range (ultimi N giorni)
        LocalDate cutoff = LocalDate.now().minusDays(days);
        
        Map<LocalDate, List<FocusSession>> grouped = sessions.stream()
                .filter(s -> s.getStartedAt().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(cutoff))
                .collect(Collectors.groupingBy(s -> s.getStartedAt().atZone(ZoneId.systemDefault()).toLocalDate()));

        List<FocusHeatmapResponse.DayEntry> entries = grouped.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<FocusSession> dailySessions = entry.getValue();
                    int totalMinutes = dailySessions.stream()
                            .mapToInt(s -> (int) (s.getDurationMs() / 1000 / 60))
                            .sum();
                    return new FocusHeatmapResponse.DayEntry(date, dailySessions.size(), totalMinutes);
                })
                .sorted((a, b) -> a.date().compareTo(b.date()))
                .toList();

        return new FocusHeatmapResponse(entries);
    }
}
