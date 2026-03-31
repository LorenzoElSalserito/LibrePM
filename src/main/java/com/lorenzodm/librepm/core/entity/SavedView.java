package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Persistent saved view configuration (PRD-01-FR-007, PRD-10-FR-003).
 * <p>
 * Stores filter, sort, grouping, and column configurations
 * for different view types (list, board, gantt, calendar, workload).
 * Views can be project-scoped or global, default or shared.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
@Entity
@Table(name = "saved_views", indexes = {
        @Index(name = "idx_saved_views_user", columnList = "user_id"),
        @Index(name = "idx_saved_views_project", columnList = "project_id"),
        @Index(name = "idx_saved_views_type", columnList = "view_type")
})
@SQLDelete(sql = "UPDATE saved_views SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class SavedView extends BaseSyncEntity {

    public enum ViewType {
        LIST, BOARD, GANTT, CALENDAR, WORKLOAD
    }

    @Column(nullable = false, length = 128)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_id", length = 36)
    private String projectId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "view_type", nullable = false, length = 32)
    private ViewType viewType;

    /** JSON: active filter criteria */
    @Column(name = "filters_json", columnDefinition = "TEXT")
    private String filtersJson;

    /** JSON: sort criteria */
    @Column(name = "sort_json", columnDefinition = "TEXT")
    private String sortJson;

    /** JSON: grouping configuration */
    @Column(name = "grouping_json", columnDefinition = "TEXT")
    private String groupingJson;

    /** JSON: visible columns configuration */
    @Column(name = "columns_json", columnDefinition = "TEXT")
    private String columnsJson;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "is_shared", nullable = false)
    private boolean isShared = false;

    public SavedView() { super(); }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public ViewType getViewType() { return viewType; }
    public void setViewType(ViewType viewType) { this.viewType = viewType; }
    public String getFiltersJson() { return filtersJson; }
    public void setFiltersJson(String filtersJson) { this.filtersJson = filtersJson; }
    public String getSortJson() { return sortJson; }
    public void setSortJson(String sortJson) { this.sortJson = sortJson; }
    public String getGroupingJson() { return groupingJson; }
    public void setGroupingJson(String groupingJson) { this.groupingJson = groupingJson; }
    public String getColumnsJson() { return columnsJson; }
    public void setColumnsJson(String columnsJson) { this.columnsJson = columnsJson; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public boolean isShared() { return isShared; }
    public void setShared(boolean shared) { isShared = shared; }
}
