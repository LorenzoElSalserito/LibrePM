package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the Work Breakdown Structure (WBS) hierarchy.
 * This entity allows for a structured decomposition of the project scope.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "wbs_nodes", indexes = {
    @Index(name = "idx_wbs_project", columnList = "project_id"),
    @Index(name = "idx_wbs_parent", columnList = "parent_id")
})
public class WbsNode extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private WbsNode parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<WbsNode> children = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task; // Optional link to a task

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String wbsCode; // e.g., "1.2.3"

    @Column(nullable = false)
    private int sortOrder;

    public WbsNode() {
        super();
    }

    // Getters and Setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public WbsNode getParent() { return parent; }
    public void setParent(WbsNode parent) { this.parent = parent; }
    public List<WbsNode> getChildren() { return children; }
    public void setChildren(List<WbsNode> children) { this.children = children; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWbsCode() { return wbsCode; }
    public void setWbsCode(String wbsCode) { this.wbsCode = wbsCode; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
