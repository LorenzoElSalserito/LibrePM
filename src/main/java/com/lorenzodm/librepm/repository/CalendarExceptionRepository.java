package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.CalendarException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarExceptionRepository extends JpaRepository<CalendarException, String> {

    List<CalendarException> findByCalendarId(String calendarId);

    List<CalendarException> findByCalendarIdAndDateBetween(String calendarId, LocalDate from, LocalDate to);

    Optional<CalendarException> findByCalendarIdAndDate(String calendarId, LocalDate date);

    boolean existsByCalendarIdAndDate(String calendarId, LocalDate date);
}
