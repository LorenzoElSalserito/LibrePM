package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.AddCalendarExceptionRequest;
import com.lorenzodm.librepm.api.dto.request.AddWorkDayRuleRequest;
import com.lorenzodm.librepm.api.dto.request.CreateWorkCalendarRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateWorkCalendarRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.CalendarException;
import com.lorenzodm.librepm.core.entity.WorkCalendar;
import com.lorenzodm.librepm.core.entity.WorkDayRule;
import com.lorenzodm.librepm.repository.CalendarExceptionRepository;
import com.lorenzodm.librepm.repository.WorkCalendarRepository;
import com.lorenzodm.librepm.repository.WorkDayRuleRepository;
import com.lorenzodm.librepm.service.WorkCalendarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WorkCalendarServiceImpl implements WorkCalendarService {

    private static final String DEFAULT_CALENDAR_NAME = "Standard";

    private final WorkCalendarRepository workCalendarRepository;
    private final WorkDayRuleRepository workDayRuleRepository;
    private final CalendarExceptionRepository calendarExceptionRepository;

    public WorkCalendarServiceImpl(WorkCalendarRepository workCalendarRepository,
                                   WorkDayRuleRepository workDayRuleRepository,
                                   CalendarExceptionRepository calendarExceptionRepository) {
        this.workCalendarRepository = workCalendarRepository;
        this.workDayRuleRepository = workDayRuleRepository;
        this.calendarExceptionRepository = calendarExceptionRepository;
    }

    @Override
    public WorkCalendar create(CreateWorkCalendarRequest req) {
        if (workCalendarRepository.existsByName(req.name())) {
            throw new ConflictException("Calendario con questo nome già esistente");
        }
        WorkCalendar calendar = new WorkCalendar();
        calendar.setName(req.name());
        calendar.setDescription(req.description());
        return workCalendarRepository.save(calendar);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkCalendar getById(String calendarId) {
        return workCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendario non trovato"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkCalendar> listAll() {
        return workCalendarRepository.findAll();
    }

    @Override
    public WorkCalendar update(String calendarId, UpdateWorkCalendarRequest req) {
        WorkCalendar calendar = getById(calendarId);
        if (req.name() != null && !req.name().equals(calendar.getName())) {
            if (workCalendarRepository.existsByName(req.name())) {
                throw new ConflictException("Calendario con questo nome già esistente");
            }
            calendar.setName(req.name());
        }
        if (req.description() != null) calendar.setDescription(req.description());
        return workCalendarRepository.save(calendar);
    }

    @Override
    public void delete(String calendarId) {
        WorkCalendar calendar = getById(calendarId);
        if (DEFAULT_CALENDAR_NAME.equals(calendar.getName())) {
            throw new ConflictException("Il calendario di default non può essere eliminato");
        }
        workCalendarRepository.delete(calendar);
    }

    @Override
    public WorkDayRule addWorkDayRule(String calendarId, AddWorkDayRuleRequest req) {
        WorkCalendar calendar = getById(calendarId);
        if (workDayRuleRepository.existsByCalendarIdAndDayOfWeek(calendarId, req.dayOfWeek())) {
            throw new ConflictException("Regola per questo giorno già presente nel calendario");
        }
        WorkDayRule rule = new WorkDayRule();
        rule.setCalendar(calendar);
        rule.setDayOfWeek(req.dayOfWeek());
        rule.setWorkingDay(req.isWorkingDay());
        rule.setStartTime(req.startTime());
        rule.setEndTime(req.endTime());
        rule.setBreakStartTime(req.breakStartTime());
        rule.setBreakEndTime(req.breakEndTime());
        return workDayRuleRepository.save(rule);
    }

    @Override
    public void removeWorkDayRule(String calendarId, String ruleId) {
        WorkDayRule rule = workDayRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Regola non trovata"));
        if (!rule.getCalendar().getId().equals(calendarId)) {
            throw new ResourceNotFoundException("Regola non trovata nel calendario");
        }
        workDayRuleRepository.delete(rule);
    }

    @Override
    public CalendarException addException(String calendarId, AddCalendarExceptionRequest req) {
        WorkCalendar calendar = getById(calendarId);
        if (calendarExceptionRepository.existsByCalendarIdAndDate(calendarId, req.date())) {
            throw new ConflictException("Eccezione per questa data già presente nel calendario");
        }
        CalendarException exception = new CalendarException();
        exception.setCalendar(calendar);
        exception.setDate(req.date());
        exception.setWorkingDay(req.isWorkingDay());
        exception.setDescription(req.description());
        return calendarExceptionRepository.save(exception);
    }

    @Override
    public void removeException(String calendarId, String exceptionId) {
        CalendarException exception = calendarExceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Eccezione non trovata"));
        if (!exception.getCalendar().getId().equals(calendarId)) {
            throw new ResourceNotFoundException("Eccezione non trovata nel calendario");
        }
        calendarExceptionRepository.delete(exception);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkCalendar getDefaultCalendar() {
        return workCalendarRepository.findByName(DEFAULT_CALENDAR_NAME)
                .orElseThrow(() -> new ResourceNotFoundException("Calendario di default non trovato"));
    }
}
