package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.core.entity.Team;
import com.lorenzodm.librepm.core.entity.TeamMember;
import com.lorenzodm.librepm.core.entity.TeamMemberId;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.TeamMemberRepository;
import com.lorenzodm.librepm.repository.TeamRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    public Team createTeam(String name, String description, String ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Team team = new Team(name, owner);
        team.setDescription(description);
        team = teamRepository.save(team);

        // Add owner as TEAM_OWNER
        addMember(team.getId(), ownerId, TeamMember.Role.TEAM_OWNER);

        return team;
    }

    public List<Team> getTeamsForUser(String userId) {
        // Returns teams where user is a member
        return teamMemberRepository.findByUserId(userId).stream()
                .map(TeamMember::getTeam)
                .toList();
    }

    public Team getTeam(String teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
    }

    public void addMember(String teamId, String userId, TeamMember.Role role) {
        Team team = getTeam(teamId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TeamMember member = new TeamMember(team, user, role);
        teamMemberRepository.save(member);
    }

    public void removeMember(String teamId, String userId) {
        TeamMemberId id = new TeamMemberId(teamId, userId);
        if (teamMemberRepository.existsById(id)) {
            teamMemberRepository.deleteById(id);
        }
    }

    public void updateMemberRole(String teamId, String userId, TeamMember.Role newRole) {
        TeamMember member = teamMemberRepository.findById(new TeamMemberId(teamId, userId))
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        member.setRole(newRole);
        teamMemberRepository.save(member);
    }

    public User createGhostMember(String teamId, String username, String displayName) {
        Team team = getTeam(teamId);
        
        // Verifica univocità username
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username già esistente");
        }

        // Crea Ghost User
        User ghost = new User(username, displayName);
        ghost.setGhost(true);
        ghost.setCreatedBy(team.getOwner()); // Owner del team è il creatore del ghost
        ghost.setEmail(username + "@ghost.local"); // Email fittizia
        
        ghost = userRepository.save(ghost);

        // Aggiungi al team come MEMBER
        addMember(teamId, ghost.getId(), TeamMember.Role.TEAM_MEMBER);

        return ghost;
    }
}
