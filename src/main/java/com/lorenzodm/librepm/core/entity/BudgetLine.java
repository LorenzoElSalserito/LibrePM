package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "budget_lines")
public class BudgetLine {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "budget_id", nullable = false)
    private String budgetId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "phase_id")
    private String phaseId;

    @Column(name = "deliverable_id")
    private String deliverableId;

    @Column(name = "planned_amount")
    private double plannedAmount;

    @Column(name = "committed_amount")
    private double committedAmount;

    @Column(name = "reserved_amount")
    private double reservedAmount;

    @Column(name = "actual_amount")
    private double actualAmount;

    @Column(name = "forecast_amount")
    private double forecastAmount;

    @Column(name = "currency")
    private String currency = "EUR";

    @Column(name = "sort_order")
    private int sortOrder;

    public BudgetLine() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(String phaseId) {
        this.phaseId = phaseId;
    }

    public String getDeliverableId() {
        return deliverableId;
    }

    public void setDeliverableId(String deliverableId) {
        this.deliverableId = deliverableId;
    }

    public double getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(double plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public double getCommittedAmount() {
        return committedAmount;
    }

    public void setCommittedAmount(double committedAmount) {
        this.committedAmount = committedAmount;
    }

    public double getReservedAmount() {
        return reservedAmount;
    }

    public void setReservedAmount(double reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public double getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(double actualAmount) {
        this.actualAmount = actualAmount;
    }

    public double getForecastAmount() {
        return forecastAmount;
    }

    public void setForecastAmount(double forecastAmount) {
        this.forecastAmount = forecastAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
