package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.response.GanttTaskResponse;
import com.lorenzodm.librepm.core.entity.Task;

import java.util.List;
import java.util.Set;

/**
 * Core planning engine for the advanced planning model (PRD-08).
 * Responsible for:
 * - Cycle detection in dependency graphs
 * - Forward/backward pass (critical path method)
 * - Recalculating planned dates based on dependencies and calendars
 */
public interface PlanningEngine {

    /**
     * Checks whether adding a dependency from predecessorId to successorId
     * would create a cycle in the project dependency graph.
     * PRD-08-FR-007: must detect and prevent cyclic dependencies.
     *
     * @return true if a cycle would be introduced
     */
    boolean wouldCreateCycle(String projectId, String predecessorId, String successorId);

    /**
     * Recalculates all planned dates for the tasks in the given project
     * based on the current dependency graph and calendar.
     * PRD-08-FR-008: planning recalculation engine.
     */
    void recalculatePlan(String projectId);

    /**
     * Returns the set of task IDs that are on the critical path.
     */
    Set<String> getCriticalPath(String projectId);

    /**
     * Builds the Gantt-ready task list with dependency refs and critical path info.
     */
    List<GanttTaskResponse> buildGanttData(String projectId);
}
