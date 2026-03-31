package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.response.ResourceAllocationResponse;
import com.lorenzodm.librepm.core.entity.ProjectMember;
import com.lorenzodm.librepm.core.entity.Task;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.ProjectMemberRepository;
import com.lorenzodm.librepm.repository.TaskRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ResourceServiceImpl implements ResourceService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ResourceServiceImpl(TaskRepository taskRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public ResourceAllocationResponse getResourceAllocation(String projectId, LocalDate startDate, LocalDate endDate) {
        // 1. Recupera tutti i membri del progetto (inclusi Ghost)
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        
        // Mappa ID utente -> Nome utente per tutti i membri
        Map<String, String> memberNames = members.stream()
                .collect(Collectors.toMap(
                        m -> m.getUser().getId(),
                        m -> m.getUser().getDisplayName() != null ? m.getUser().getDisplayName() : m.getUser().getUsername()
                ));

        // 2. Recupera tutti i task del progetto
        List<Task> allTasks = taskRepository.findByProjectId(projectId);

        // 3. Filtra task rilevanti per il periodo
        List<Task> relevantTasks = allTasks.stream()
                .filter(t -> !t.isArchived())
                .filter(t -> isTaskInPeriod(t, startDate, endDate))
                .toList();

        // 4. Raggruppa task per utente assegnato
        Map<String, List<Task>> tasksByUser = relevantTasks.stream()
                .collect(Collectors.groupingBy(t -> t.getAssignedTo() != null ? t.getAssignedTo().getId() : "unassigned"));

        List<ResourceAllocationResponse.UserAllocation> allocations = new ArrayList<>();

        // 5. Crea entry per ogni membro del team (anche se non ha task)
        for (String memberId : memberNames.keySet()) {
            List<Task> userTasks = tasksByUser.getOrDefault(memberId, new ArrayList<>());
            String userName = memberNames.get(memberId);

            Map<LocalDate, Integer> dailyMinutes = calculateDailyLoad(userTasks, startDate, endDate);
            int totalEstimated = userTasks.stream().mapToInt(t -> t.getEstimatedEffort() != null ? t.getEstimatedEffort() : 0).sum();
            int totalActual = userTasks.stream().mapToInt(t -> t.getActualEffort() != null ? t.getActualEffort() : 0).sum();
            allocations.add(new ResourceAllocationResponse.UserAllocation(memberId, userName, dailyMinutes, totalEstimated, totalActual));
        }

        // 6. Aggiungi entry per task non assegnati (se ce ne sono)
        if (tasksByUser.containsKey("unassigned")) {
            List<Task> unassignedTasks = tasksByUser.get("unassigned");
            if (!unassignedTasks.isEmpty()) {
                Map<LocalDate, Integer> dailyMinutes = calculateDailyLoad(unassignedTasks, startDate, endDate);
                int totalEstimated = unassignedTasks.stream().mapToInt(t -> t.getEstimatedEffort() != null ? t.getEstimatedEffort() : 0).sum();
                int totalActual = unassignedTasks.stream().mapToInt(t -> t.getActualEffort() != null ? t.getActualEffort() : 0).sum();
                allocations.add(new ResourceAllocationResponse.UserAllocation("unassigned", "Non assegnato", dailyMinutes, totalEstimated, totalActual));
            }
        }

        return new ResourceAllocationResponse(allocations);
    }

    private boolean isTaskInPeriod(Task t, LocalDate start, LocalDate end) {
        // Priorità: Planned Date > Deadline
        if (t.getPlannedStart() != null) {
            LocalDate taskStart = t.getPlannedStart().toLocalDate();
            LocalDate taskEnd = t.getPlannedFinish() != null ? t.getPlannedFinish().toLocalDate() : taskStart;
            return !taskEnd.isBefore(start) && !taskStart.isAfter(end);
        }

        if (t.getDeadline() != null) {
            return !t.getDeadline().isBefore(start) && !t.getDeadline().isAfter(end);
        }

        return false;
    }

    private Map<LocalDate, Integer> calculateDailyLoad(List<Task> tasks, LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> load = new HashMap<>();

        for (Task t : tasks) {
            int minutes = t.getEstimatedEffort() != null ? t.getEstimatedEffort() : 0;
            if (minutes == 0) continue;

            if (t.getPlannedStart() != null) {
                // Distribuisci carico tra start e end
                LocalDate taskStart = t.getPlannedStart().toLocalDate();
                LocalDate taskEnd = t.getPlannedFinish() != null ? t.getPlannedFinish().toLocalDate() : taskStart;
                
                // Clamp to requested period
                LocalDate effectiveStart = taskStart.isBefore(start) ? start : taskStart;
                LocalDate effectiveEnd = taskEnd.isAfter(end) ? end : taskEnd;

                if (!effectiveStart.isAfter(effectiveEnd)) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;
                    int minutesPerDay = (int) (minutes / days);

                    effectiveStart.datesUntil(effectiveEnd.plusDays(1)).forEach(date -> 
                        load.merge(date, minutesPerDay, Integer::sum)
                    );
                }
            } else if (t.getDeadline() != null) {
                // Se c'è solo deadline, attribuisci tutto al giorno della deadline
                if (!t.getDeadline().isBefore(start) && !t.getDeadline().isAfter(end)) {
                    load.merge(t.getDeadline(), minutes, Integer::sum);
                }
            }
        }
        return load;
    }
}
