package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fund_allocations")
public class FundAllocation {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "funding_source_id", nullable = false)
    private String fundingSourceId;

    @Column(name = "budget_line_id", nullable = false)
    private String budgetLineId;

    @Column(name = "allocated_amount")
    private double allocatedAmount;

    @Column(name = "allocation_date")
    private LocalDate allocationDate;

    @Column(name = "notes")
    private String notes;

    public FundAllocation() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFundingSourceId() {
        return fundingSourceId;
    }

    public void setFundingSourceId(String fundingSourceId) {
        this.fundingSourceId = fundingSourceId;
    }

    public String getBudgetLineId() {
        return budgetLineId;
    }

    public void setBudgetLineId(String budgetLineId) {
        this.budgetLineId = budgetLineId;
    }

    public double getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(double allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public LocalDate getAllocationDate() {
        return allocationDate;
    }

    public void setAllocationDate(LocalDate allocationDate) {
        this.allocationDate = allocationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
