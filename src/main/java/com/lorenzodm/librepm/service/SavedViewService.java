package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.SavedView;

import java.util.List;

/**
 * Service interface for saved views (PRD-01-FR-007, PRD-10-FR-003).
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
public interface SavedViewService {

    SavedView create(String userId, SavedView view);

    SavedView update(String userId, String viewId, SavedView view);

    void delete(String userId, String viewId);

    List<SavedView> listByProject(String userId, String projectId);

    List<SavedView> listGlobal(String userId);

    List<SavedView> listAll(String userId);

    SavedView getDefault(String userId, String projectId, SavedView.ViewType viewType);
}
