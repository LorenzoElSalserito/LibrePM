package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Records the achievement of a target for a success metric at a specific point in time.
 * This allows for historical tracking of progress towards key results.
 *
 * @author Lorenzo DM
 * @since 1.0.0
 * @version 1.0.0
 */
@Entity
@Table(name = "target_achieved_records", indexes = {
    @Index(name = "idx_targetrecord_metric", columnList = "metric_id"),
    @Index(name = "idx_targetrecord_date", columnList = "record_date")
})
public class TargetAchievedRecord extends BaseSyncEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_id", nullable = false)
    private SuccessMetric metric;

    @Column(nullable = false)
    private double achievedValue;

    @Column(nullable = false)
    private LocalDateTime recordDate;

    @Column(length = 255)
    private String note;

    public TargetAchievedRecord() {
        super();
    }

    // Getters and Setters
    public SuccessMetric getMetric() { return metric; }
    public void setMetric(SuccessMetric metric) { this.metric = metric; }
    public double getAchievedValue() { return achievedValue; }
    public void setAchievedValue(double achievedValue) { this.achievedValue = achievedValue; }
    public LocalDateTime getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDateTime recordDate) { this.recordDate = recordDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
