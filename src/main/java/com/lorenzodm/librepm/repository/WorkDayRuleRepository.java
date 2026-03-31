package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.WorkDayRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkDayRuleRepository extends JpaRepository<WorkDayRule, String> {

    List<WorkDayRule> findByCalendarId(String calendarId);

    Optional<WorkDayRule> findByCalendarIdAndDayOfWeek(String calendarId, DayOfWeek dayOfWeek);

    boolean existsByCalendarIdAndDayOfWeek(String calendarId, DayOfWeek dayOfWeek);
}
