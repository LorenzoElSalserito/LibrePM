package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_statuses")
public class TaskStatus extends BaseSyncEntity {

    /**
     * Semantic category of the status.
     * This allows business logic to operate on categories (e.g., "is it done?")
     * regardless of the user-defined name (e.g., "Shipped", "Closed", "Finished").
     */
    public enum Category {
        TODO,
        IN_PROGRESS,
        DONE,
        BLOCKED,
        ARCHIVED
    }

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 7)
    private String color; // e.g., "#FF0000"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category = Category.TODO; // Default

    public TaskStatus() {
        super();
    }

    public TaskStatus(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isCompleted() {
        return this.category == Category.DONE || this.category == Category.ARCHIVED;
    }
}
