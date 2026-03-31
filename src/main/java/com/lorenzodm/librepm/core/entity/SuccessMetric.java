package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;

/**
 * Represents a key result or success metric for an OKR.
 * It defines a measurable outcome and tracks its progress.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "success_metrics", indexes = {
    @Index(name = "idx_successmetric_okr", columnList = "okr_id")
})
public class SuccessMetric extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "okr_id", nullable = false)
    private Okr okr;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private double targetValue;

    @Column(nullable = false)
    private double currentValue;

    @Column(length = 50)
    private String unit;

    public SuccessMetric() {
        super();
    }

    // Getters and Setters
    public Okr getOkr() { return okr; }
    public void setOkr(Okr okr) { this.okr = okr; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }
    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
