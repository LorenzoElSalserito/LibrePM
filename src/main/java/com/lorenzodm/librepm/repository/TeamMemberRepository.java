package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.TeamMember;
import com.lorenzodm.librepm.core.entity.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
    List<TeamMember> findByUserId(String userId);
    List<TeamMember> findByTeamId(String teamId);
}
