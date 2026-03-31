package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.CapacityProfile;
import com.lorenzodm.librepm.core.entity.CalendarException;
import com.lorenzodm.librepm.core.entity.LeaveRecord;
import com.lorenzodm.librepm.core.entity.WorkCalendar;
import com.lorenzodm.librepm.core.entity.WorkDayRule;
import com.lorenzodm.librepm.repository.CapacityProfileRepository;
import com.lorenzodm.librepm.repository.LeaveRecordRepository;
import com.lorenzodm.librepm.repository.WorkCalendarRepository;
import com.lorenzodm.librepm.service.CapacityService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CapacityServiceImpl implements CapacityService {

    private final CapacityProfileRepository capacityProfileRepo;
    private final LeaveRecordRepository leaveRecordRepo;
    private final WorkCalendarRepository workCalendarRepo;

    public CapacityServiceImpl(CapacityProfileRepository capacityProfileRepo,
                               LeaveRecordRepository leaveRecordRepo,
                               WorkCalendarRepository workCalendarRepo) {
        this.capacityProfileRepo = capacityProfileRepo;
        this.leaveRecordRepo = leaveRecordRepo;
        this.workCalendarRepo = workCalendarRepo;
    }

    @Override
    public List<CapacityProfile> listByUser(String userId) {
        return capacityProfileRepo.findByUserId(userId);
    }

    @Override
    public CapacityProfile create(CapacityProfile profile) {
        return capacityProfileRepo.save(profile);
    }

    @Override
    public CapacityProfile update(String id, CapacityProfile updated) {
        CapacityProfile existing = capacityProfileRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CapacityProfile not found: " + id));
        if (updated.getHoursPerDay() > 0) existing.setHoursPerDay(updated.getHoursPerDay());
        if (updated.getEffectiveFrom() != null) existing.setEffectiveFrom(updated.getEffectiveFrom());
        existing.setEffectiveTo(updated.getEffectiveTo());
        if (updated.getCalendar() != null) existing.setCalendar(updated.getCalendar());
        return capacityProfileRepo.save(existing);
    }

    @Override
    public void delete(String id) {
        capacityProfileRepo.deleteById(id);
    }

    @Override
    public Optional<CapacityProfile> findActiveForUser(String userId, LocalDate date) {
        return capacityProfileRepo.findActiveForUserOnDate(userId, date);
    }

    @Override
    public List<LeaveRecord> listLeaveByUser(String userId) {
        return leaveRecordRepo.findByUserId(userId);
    }

    @Override
    public List<LeaveRecord> listLeaveByUserBetween(String userId, LocalDate from, LocalDate to) {
        return leaveRecordRepo.findByUserIdAndLeaveDateBetween(userId, from, to);
    }

    @Override
    public LeaveRecord createLeave(LeaveRecord record) {
        return leaveRecordRepo.save(record);
    }

    @Override
    public void deleteLeave(String id) {
        leaveRecordRepo.deleteById(id);
    }

    @Override
    public double getAvailableHours(String userId, LocalDate date) {
        // 1. Get active capacity profile (or default 8h)
        Optional<CapacityProfile> profileOpt = capacityProfileRepo.findActiveForUserOnDate(userId, date);
        double baseHours = profileOpt.map(CapacityProfile::getHoursPerDay).orElse(8.0);

        // 2. Check if it's a working day via calendar cascade:
        //    User-level calendar > Workspace calendar > default Mon-Fri
        if (profileOpt.isPresent() && profileOpt.get().getCalendar() != null) {
            WorkCalendar cal = profileOpt.get().getCalendar();
            if (!isWorkingDay(cal, date)) return 0.0;
        } else {
            // Fallback: workspace-level calendar
            Optional<WorkCalendar> wsCal = workCalendarRepo.findAll().stream()
                    .filter(c -> "WORKSPACE".equals(c.getScope()))
                    .findFirst();
            if (wsCal.isPresent()) {
                if (!isWorkingDay(wsCal.get(), date)) return 0.0;
            } else {
                // Default: Mon-Fri
                DayOfWeek dow = date.getDayOfWeek();
                if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return 0.0;
            }
        }

        // 3. Subtract leave hours
        if (leaveRecordRepo.existsByUserIdAndLeaveDate(userId, date)) {
            List<LeaveRecord> leaves = leaveRecordRepo.findByUserIdAndLeaveDateBetween(userId, date, date);
            for (LeaveRecord lr : leaves) {
                if (lr.getHours() != null) {
                    baseHours -= lr.getHours();
                } else {
                    return 0.0; // Full day leave
                }
            }
        }

        return Math.max(0.0, baseHours);
    }

    @Override
    public double getAvailableHoursInRange(String userId, LocalDate from, LocalDate to) {
        double total = 0.0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            total += getAvailableHours(userId, d);
        }
        return total;
    }

    private boolean isWorkingDay(WorkCalendar calendar, LocalDate date) {
        // Check exceptions first (holidays or extra working days)
        for (CalendarException ex : calendar.getExceptions()) {
            if (ex.getDate() != null && ex.getDate().equals(date)) {
                return ex.isWorkingDay();
            }
        }
        // Check day rules
        DayOfWeek dow = date.getDayOfWeek();
        for (WorkDayRule rule : calendar.getWorkDayRules()) {
            if (rule.getDayOfWeek() == dow) {
                return rule.isWorkingDay();
            }
        }
        // If no rules defined, default to Mon-Fri
        if (calendar.getWorkDayRules().isEmpty()) {
            return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        }
        return false;
    }
}
