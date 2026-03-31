package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.Phase;

import java.time.LocalDate;
import java.util.List;

public interface PhaseService {

    Phase create(String projectId, String name, String description,
                 LocalDate plannedStart, LocalDate plannedEnd, String color, int sortOrder);

    Phase update(String phaseId, String name, String description,
                 LocalDate plannedStart, LocalDate plannedEnd,
                 LocalDate actualStart, LocalDate actualEnd,
                 String status, String color, int sortOrder);

    List<Phase> listByProject(String projectId);

    void delete(String phaseId);
}
