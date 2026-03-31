package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rate_cards")
public class RateCard {

    public enum Scope { ROLE, USER, PROJECT }

    @Id
    @Column(length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Scope scope;

    @Column(name = "scope_entity_id", nullable = false, length = 36)
    private String scopeEntityId;

    @Column(length = 3)
    private String currency = "EUR";

    @Column(name = "hourly_rate", nullable = false)
    private double hourlyRate;

    @Column(name = "daily_rate")
    private Double dailyRate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at")
    private Instant createdAt;

    public RateCard() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Scope getScope() { return scope; }
    public void setScope(Scope scope) { this.scope = scope; }
    public String getScopeEntityId() { return scopeEntityId; }
    public void setScopeEntityId(String scopeEntityId) { this.scopeEntityId = scopeEntityId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    public Double getDailyRate() { return dailyRate; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
