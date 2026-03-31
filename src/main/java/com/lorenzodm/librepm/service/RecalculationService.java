package com.lorenzodm.librepm.service;

/**
 * Service for recalculating project schedules, critical path, and float values.
 * Implements CPM (Critical Path Method) forward/backward pass.
 */
public interface RecalculationService {

    /**
     * Recalculates the schedule for all tasks in a project.
     * Performs forward pass (early start/finish) and backward pass (late start/finish),
     * then identifies the critical path (tasks with zero total float).
     */
    void recalculate(String projectId);
}
