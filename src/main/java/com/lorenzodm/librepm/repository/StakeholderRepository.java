package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Stakeholder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StakeholderRepository extends JpaRepository<Stakeholder, String> {
    List<Stakeholder> findByProjectIdOrderByNameAsc(String projectId);
}
