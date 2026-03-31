package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a label or category that can be attached to tasks.
 * <p>
 * Tags are user-specific and help in organizing and filtering tasks across different projects.
 * They support custom colors for visual distinction.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @version 0.5.2
 */
@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tag_name", columnList = "name"),
        @Index(name = "idx_tag_owner", columnList = "owner_id")
})
@SQLDelete(sql = "UPDATE tags SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Tag extends BaseSyncEntity {

    @NotBlank(message = "Nome tag obbligatorio")
    @Size(min = 1, max = 50, message = "Nome tag deve essere tra 1 e 50 caratteri")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 7) // Hex color: #RRGGBB
    private String color;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(mappedBy = "tags")
    private Set<Task> tasks = new HashSet<>();

    /**
     * Default constructor.
     * Initializes ID via superclass.
     */
    public Tag() {
        super();
    }

    /**
     * Convenience constructor for creating a new tag.
     *
     * @param name  The name of the tag.
     * @param color The hex color code (e.g., #FF0000).
     * @param owner The user who owns this tag.
     */
    public Tag(String name, String color, User owner) {
        this();
        this.name = name;
        this.color = color;
        this.owner = owner;
    }

    // Getters & Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    // Helper methods

    /**
     * Associates a task with this tag.
     * @param task The task to tag.
     */
    public void addTask(Task task) {
        tasks.add(task);
        task.getTags().add(this);
    }

    /**
     * Removes the association between a task and this tag.
     * @param task The task to untag.
     */
    public void removeTask(Task task) {
        tasks.remove(task);
        task.getTags().remove(this);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
