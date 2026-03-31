package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.SavedView;
import com.lorenzodm.librepm.repository.SavedViewRepository;
import com.lorenzodm.librepm.service.SavedViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link SavedViewService} (PRD-01-FR-007, PRD-10-FR-003).
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
@Service
@Transactional
public class SavedViewServiceImpl implements SavedViewService {

    private static final Logger log = LoggerFactory.getLogger(SavedViewServiceImpl.class);

    private final SavedViewRepository savedViewRepository;

    public SavedViewServiceImpl(SavedViewRepository savedViewRepository) {
        this.savedViewRepository = savedViewRepository;
    }

    @Override
    public SavedView create(String userId, SavedView view) {
        view.setUserId(userId);

        // If setting as default, clear other defaults for same user/project/viewType
        if (view.isDefault()) {
            clearDefaults(userId, view.getProjectId(), view.getViewType());
        }

        SavedView saved = savedViewRepository.save(view);
        log.info("Saved view created: {} ({})", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    public SavedView update(String userId, String viewId, SavedView updates) {
        SavedView existing = savedViewRepository.findById(viewId)
                .orElseThrow(() -> new ResourceNotFoundException("View non trovata: " + viewId));

        if (!existing.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("View non trovata: " + viewId);
        }

        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getFiltersJson() != null) existing.setFiltersJson(updates.getFiltersJson());
        if (updates.getSortJson() != null) existing.setSortJson(updates.getSortJson());
        if (updates.getGroupingJson() != null) existing.setGroupingJson(updates.getGroupingJson());
        if (updates.getColumnsJson() != null) existing.setColumnsJson(updates.getColumnsJson());

        if (updates.isDefault() && !existing.isDefault()) {
            clearDefaults(userId, existing.getProjectId(), existing.getViewType());
        }
        existing.setDefault(updates.isDefault());
        existing.setShared(updates.isShared());

        return savedViewRepository.save(existing);
    }

    @Override
    public void delete(String userId, String viewId) {
        SavedView view = savedViewRepository.findById(viewId)
                .orElseThrow(() -> new ResourceNotFoundException("View non trovata: " + viewId));

        if (!view.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("View non trovata: " + viewId);
        }

        savedViewRepository.delete(view);
        log.info("Saved view deleted: {}", viewId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedView> listByProject(String userId, String projectId) {
        List<SavedView> own = savedViewRepository.findByUserIdAndProjectId(userId, projectId);
        List<SavedView> shared = savedViewRepository.findByProjectIdAndIsSharedTrue(projectId);

        // Merge: own views + shared views from others (avoid duplicates)
        shared.stream()
                .filter(sv -> !sv.getUserId().equals(userId))
                .filter(sv -> own.stream().noneMatch(o -> o.getId().equals(sv.getId())))
                .forEach(own::add);

        return own;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedView> listGlobal(String userId) {
        return savedViewRepository.findByUserIdAndProjectIdIsNull(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedView> listAll(String userId) {
        return savedViewRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public SavedView getDefault(String userId, String projectId, SavedView.ViewType viewType) {
        return savedViewRepository.findByUserIdAndProjectIdAndViewTypeAndIsDefaultTrue(userId, projectId, viewType)
                .orElse(null);
    }

    private void clearDefaults(String userId, String projectId, SavedView.ViewType viewType) {
        List<SavedView> defaults;
        if (projectId != null) {
            defaults = savedViewRepository.findByUserIdAndProjectId(userId, projectId).stream()
                    .filter(sv -> sv.getViewType() == viewType && sv.isDefault())
                    .toList();
        } else {
            defaults = savedViewRepository.findByUserIdAndProjectIdIsNull(userId).stream()
                    .filter(sv -> sv.getViewType() == viewType && sv.isDefault())
                    .toList();
        }
        defaults.forEach(sv -> {
            sv.setDefault(false);
            savedViewRepository.save(sv);
        });
    }
}
