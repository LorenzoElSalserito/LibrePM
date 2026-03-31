package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateProjectRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateProjectRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.api.exception.OwnershipViolationException;
import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.ProjectMember;
import com.lorenzodm.librepm.core.entity.Team;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.ProjectMemberRepository;
import com.lorenzodm.librepm.repository.ProjectRepository;
import com.lorenzodm.librepm.repository.TeamRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.ProjectService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TeamRepository teamRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository, TeamRepository teamRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public Project create(String userId, CreateProjectRequest req) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User non trovato: " + userId));

        Project p = new Project();
        p.setName(req.name());
        p.setDescription(req.description());
        if (req.color() != null) p.setColor(req.color());
        if (req.icon() != null) p.setIcon(req.icon());
        if (req.favorite() != null) p.setFavorite(req.favorite());
        p.setOwner(owner);

        // Gestione Visibility e Team
        if (req.visibility() != null) {
            try {
                p.setVisibility(Project.Visibility.valueOf(req.visibility()));
            } catch (IllegalArgumentException e) {
                // Default to PERSONAL if invalid
                p.setVisibility(Project.Visibility.PERSONAL);
            }
        }

        if (req.teamId() != null && !req.teamId().isBlank()) {
            Team team = teamRepository.findById(req.teamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team non trovato: " + req.teamId()));
            p.setTeam(team);
            // Se associato a un team, la visibilità dovrebbe essere coerente (es. TEAM)
            if (p.getVisibility() == Project.Visibility.PERSONAL) {
                p.setVisibility(Project.Visibility.TEAM);
            }
        }

        p = projectRepository.save(p);

        // Aggiungi owner come membro OWNER
        ProjectMember member = new ProjectMember(p, owner, ProjectMember.Role.OWNER);
        projectMemberRepository.save(member);

        return p;
    }

    @Override
    public Project getOwned(String userId, String projectId) {
        // Verifica se l'utente è membro del progetto (non solo owner)
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new OwnershipViolationException("Non sei membro di questo progetto"));

        // Calcola health on-read (opzionale: spostare in job async se lento)
        calculateProjectHealth(member.getProject());

        return member.getProject();
    }

    @Override
    public List<Project> listOwned(String userId, Boolean archived, Boolean favorite, String search) {
        // Nuova logica: cerca tutti i progetti di cui l'utente è membro
        List<Project> projects;
        
        if (archived != null) {
            projects = archived ? projectRepository.findAllArchivedByMemberId(userId) : projectRepository.findAllActiveByMemberId(userId);
        } else {
            projects = projectRepository.findAllByMemberId(userId);
        }

        // Filter out inbox projects (internal implementation detail)
        projects = projects.stream()
                .filter(p -> !p.getName().startsWith("__INBOX__"))
                .collect(Collectors.toList());

        // Filtri in-memory (o ottimizzare query repository)
        if (favorite != null && favorite) {
            projects = projects.stream().filter(Project::isFavorite).collect(Collectors.toList());
        }
        
        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            projects = projects.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }
        
        // Calcola health per tutti i progetti restituiti
        projects.forEach(this::calculateProjectHealth);

        return projects;
    }

    @Override
    public Project update(String userId, String projectId, UpdateProjectRequest req) {
        Project p = getOwned(userId, projectId);
        
        // Verifica permessi (solo OWNER o EDITOR possono modificare)
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new OwnershipViolationException("Non sei membro"));
        
        if (member.getRole() == ProjectMember.Role.VIEWER) {
            throw new OwnershipViolationException("Permessi insufficienti per modificare il progetto");
        }

        if (req.name() != null) p.setName(req.name());
        if (req.description() != null) p.setDescription(req.description());
        if (req.color() != null) p.setColor(req.color());
        if (req.icon() != null) p.setIcon(req.icon());
        if (req.archived() != null) p.setArchived(req.archived());
        if (req.favorite() != null) p.setFavorite(req.favorite());
        if (req.timeTrackingEnabled() != null) p.setTimeTrackingEnabled(req.timeTrackingEnabled());
        if (req.planningEnabled() != null) p.setPlanningEnabled(req.planningEnabled());
        if (req.financeEnabled() != null) p.setFinanceEnabled(req.financeEnabled());
        if (req.grantsEnabled() != null) p.setGrantsEnabled(req.grantsEnabled());

        return projectRepository.save(p);
    }

    @Override
    public Project setArchived(String userId, String projectId, boolean archived) {
        Project p = getOwned(userId, projectId);
        p.setArchived(archived);
        return projectRepository.save(p);
    }

    @Override
    public void delete(String userId, String projectId) {
        Project p = getOwned(userId, projectId);
        
        // Solo OWNER può eliminare
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new OwnershipViolationException("Non sei membro"));
        
        if (member.getRole() != ProjectMember.Role.OWNER) {
            throw new OwnershipViolationException("Solo l'owner può eliminare il progetto");
        }

        projectRepository.delete(p);
    }

    @Override
    public ProjectMember addMember(String requesterId, String projectId, String userId, ProjectMember.Role role) {
        Project p = getOwned(requesterId, projectId);
        
        // Verifica permessi requester (solo OWNER può aggiungere membri)
        ProjectMember requester = projectMemberRepository.findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new OwnershipViolationException("Non sei membro"));
        
        if (requester.getRole() != ProjectMember.Role.OWNER) {
            throw new OwnershipViolationException("Solo l'owner può aggiungere membri");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente da aggiungere non trovato"));

        if (projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isPresent()) {
            throw new ConflictException("Utente già membro del progetto");
        }

        ProjectMember newMember = new ProjectMember(p, userToAdd, role);
        return projectMemberRepository.save(newMember);
    }

    @Override
    public ProjectMember updateMemberRole(String requesterId, String projectId, String memberId, String role, String systemRoleId) {
        // Only OWNER or ADMIN can change roles
        ProjectMember requester = projectMemberRepository.findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new OwnershipViolationException("Non sei membro"));
        if (requester.getRole() != ProjectMember.Role.OWNER && requester.getRole() != ProjectMember.Role.ADMIN) {
            throw new OwnershipViolationException("Permessi insufficienti per modificare i ruoli");
        }

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro non trovato"));

        // Cannot change OWNER role
        if (member.getRole() == ProjectMember.Role.OWNER) {
            throw new ConflictException("Non è possibile modificare il ruolo dell'owner");
        }

        if (role != null) {
            member.setRole(ProjectMember.Role.valueOf(role));
        }
        // systemRoleId can be null to clear it, or a valid role id
        member.setSystemRoleId(systemRoleId);

        return projectMemberRepository.save(member);
    }

    @Override
    public void removeMember(String requesterId, String projectId, String userId) {
        // Verifica permessi requester (solo OWNER può rimuovere membri)
        ProjectMember requester = projectMemberRepository.findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new OwnershipViolationException("Non sei membro"));
        
        if (requester.getRole() != ProjectMember.Role.OWNER) {
            throw new OwnershipViolationException("Solo l'owner può rimuovere membri");
        }

        ProjectMember memberToRemove = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro non trovato"));

        // Non puoi rimuovere te stesso se sei l'unico owner (logica semplificata)
        if (userId.equals(requesterId)) {
             throw new ConflictException("Non puoi rimuovere te stesso dal progetto");
        }

        projectMemberRepository.delete(memberToRemove);
    }

    @Override
    public List<ProjectMember> getMembers(String requesterId, String projectId) {
        getOwned(requesterId, projectId); // Verifica accesso base
        return projectMemberRepository.findByProjectId(projectId);
    }

    @Override
    public User createGhostUser(String ownerId, String username, String displayName) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner non trovato"));

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username già esistente");
        }

        User ghost = new User(username, displayName);
        ghost.setGhost(true);
        ghost.setCreatedBy(owner);
        ghost.setEmail(username + "@ghost.local"); // Email fittizia univoca
        
        return userRepository.save(ghost);
    }
    
    @Override
    public User createGhostUserAndAddToProject(String ownerId, String projectId, String username, String displayName) {
        User ghost = createGhostUser(ownerId, username, displayName);
        addMember(ownerId, projectId, ghost.getId(), ProjectMember.Role.EDITOR); // Default role
        return ghost;
    }

    // --- Helper Methods ---

    private void calculateProjectHealth(Project p) {
        // Conta task scaduti (deadline < oggi e non completati)
        long overdueCount = p.getTasks().stream()
                .filter(t -> t.getDeadline() != null)
                .filter(t -> LocalDate.now().isAfter(t.getDeadline()))
                .filter(t -> {
                    String statusName = t.getStatus() != null ? t.getStatus().getName().toUpperCase() : "";
                    return !"DONE".equals(statusName) && !"COMPLETED".equals(statusName);
                })
                .count();

        p.setOverdueCount((int) overdueCount);

        // Determina Health
        if (overdueCount == 0) {
            p.setHealth(Project.Health.OK);
        } else if (overdueCount <= p.getOverdueWarningThreshold()) {
            p.setHealth(Project.Health.WARNING);
        } else {
            p.setHealth(Project.Health.CRITICAL);
        }
    }
}
