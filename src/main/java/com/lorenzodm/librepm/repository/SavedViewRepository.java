package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.SavedView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SavedView entities (PRD-01-FR-007, PRD-10-FR-003).
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
@Repository
public interface SavedViewRepository extends JpaRepository<SavedView, String> {

    List<SavedView> findByUserIdAndProjectId(String userId, String projectId);

    List<SavedView> findByUserIdAndProjectIdIsNull(String userId);

    List<SavedView> findByUserId(String userId);

    List<SavedView> findByUserIdAndViewType(String userId, SavedView.ViewType viewType);

    List<SavedView> findByProjectIdAndIsSharedTrue(String projectId);

    Optional<SavedView> findByUserIdAndProjectIdAndViewTypeAndIsDefaultTrue(
            String userId, String projectId, SavedView.ViewType viewType);
}
