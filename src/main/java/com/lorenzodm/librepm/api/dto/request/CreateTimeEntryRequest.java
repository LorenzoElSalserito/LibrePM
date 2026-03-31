package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTimeEntryRequest(
        @NotBlank(message = "ID task obbligatorio")
        String taskId,
        @NotNull(message = "Data registrazione obbligatoria")
        LocalDate entryDate,
        @Min(value = 1, message = "Durata deve essere almeno 1 minuto")
        int durationMinutes,
        @NotBlank(message = "Tipo registrazione obbligatorio")
        String type,
        @Size(max = 500)
        String description
) {}
