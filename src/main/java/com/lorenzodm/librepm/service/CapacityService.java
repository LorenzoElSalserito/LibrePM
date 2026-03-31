package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.CapacityProfile;
import com.lorenzodm.librepm.core.entity.LeaveRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CapacityService {

    // Capacity profiles
    List<CapacityProfile> listByUser(String userId);
    CapacityProfile create(CapacityProfile profile);
    CapacityProfile update(String id, CapacityProfile profile);
    void delete(String id);
    Optional<CapacityProfile> findActiveForUser(String userId, LocalDate date);

    // Leave records
    List<LeaveRecord> listLeaveByUser(String userId);
    List<LeaveRecord> listLeaveByUserBetween(String userId, LocalDate from, LocalDate to);
    LeaveRecord createLeave(LeaveRecord record);
    void deleteLeave(String id);

    // Computed capacity
    double getAvailableHours(String userId, LocalDate date);
    double getAvailableHoursInRange(String userId, LocalDate from, LocalDate to);
}
