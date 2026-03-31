package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTimeEntryRequest(
        LocalDate entryDate,
        @Min(value = 1, message = "Durata deve essere almeno 1 minuto")
        Integer durationMinutes,
        @Size(max = 500)
        String description
) {}
