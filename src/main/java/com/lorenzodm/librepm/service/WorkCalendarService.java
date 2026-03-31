package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.AddCalendarExceptionRequest;
import com.lorenzodm.librepm.api.dto.request.AddWorkDayRuleRequest;
import com.lorenzodm.librepm.api.dto.request.CreateWorkCalendarRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateWorkCalendarRequest;
import com.lorenzodm.librepm.core.entity.CalendarException;
import com.lorenzodm.librepm.core.entity.WorkCalendar;
import com.lorenzodm.librepm.core.entity.WorkDayRule;

import java.util.List;

public interface WorkCalendarService {

    WorkCalendar create(CreateWorkCalendarRequest req);

    WorkCalendar getById(String calendarId);

    List<WorkCalendar> listAll();

    WorkCalendar update(String calendarId, UpdateWorkCalendarRequest req);

    void delete(String calendarId);

    WorkDayRule addWorkDayRule(String calendarId, AddWorkDayRuleRequest req);

    void removeWorkDayRule(String calendarId, String ruleId);

    CalendarException addException(String calendarId, AddCalendarExceptionRequest req);

    void removeException(String calendarId, String exceptionId);

    WorkCalendar getDefaultCalendar();
}
