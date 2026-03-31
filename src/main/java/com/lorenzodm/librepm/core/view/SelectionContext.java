package com.lorenzodm.librepm.core.view;

import com.lorenzodm.librepm.core.entity.Project;
import com.lorenzodm.librepm.core.entity.Task;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the current selection state shared across different views (List, Board, Timeline, Gantt).
 * This ensures that selecting an item in one view reflects in others, maintaining context.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
public class SelectionContext {

    private Project selectedProject;
    private Set<Task> selectedTasks = new HashSet<>();
    // Can be extended for other entities like Notes, Deliverables, etc.

    public SelectionContext() {
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(Project selectedProject) {
        this.selectedProject = selectedProject;
    }

    public Set<Task> getSelectedTasks() {
        return selectedTasks;
    }

    public void setSelectedTasks(Set<Task> selectedTasks) {
        this.selectedTasks = selectedTasks;
    }

    public void selectTask(Task task) {
        this.selectedTasks.add(task);
    }

    public void deselectTask(Task task) {
        this.selectedTasks.remove(task);
    }

    public void clearTaskSelection() {
        this.selectedTasks.clear();
    }
}
