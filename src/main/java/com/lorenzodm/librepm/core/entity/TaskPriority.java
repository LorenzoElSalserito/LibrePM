package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_priorities")
public class TaskPriority extends BaseSyncEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private int level; // Higher value means higher priority

    @Column(length = 7)
    private String color; // e.g., "#FF0000"

    public TaskPriority() {
        super();
    }

    public TaskPriority(String name, int level) {
        this.name = name;
        this.level = level;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
