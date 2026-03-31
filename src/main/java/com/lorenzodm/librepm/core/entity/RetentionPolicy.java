package com.lorenzodm.librepm.core.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "retention_policies")
public class RetentionPolicy {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "data_category_id")
    private String dataCategoryId;

    @Column(name = "scope")
    private String scope = "WORKSPACE";

    @Column(name = "scope_entity_id")
    private String scopeEntityId;

    @Column(name = "retention_days")
    private Integer retentionDays;

    @Column(name = "action_on_expiry")
    private String actionOnExpiry = "ARCHIVE";

    @Column(name = "is_active")
    private Boolean isActive = true;

    public RetentionPolicy() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataCategoryId() {
        return dataCategoryId;
    }

    public void setDataCategoryId(String dataCategoryId) {
        this.dataCategoryId = dataCategoryId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScopeEntityId() {
        return scopeEntityId;
    }

    public void setScopeEntityId(String scopeEntityId) {
        this.scopeEntityId = scopeEntityId;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public String getActionOnExpiry() {
        return actionOnExpiry;
    }

    public void setActionOnExpiry(String actionOnExpiry) {
        this.actionOnExpiry = actionOnExpiry;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
