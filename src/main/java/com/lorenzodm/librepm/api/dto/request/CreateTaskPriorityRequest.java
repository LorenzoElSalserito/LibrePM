package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskPriorityRequest(
        @NotBlank(message = "Nome priorità obbligatorio")
        @Size(min = 1, max = 50)
        String name,
        int level,
        @Size(max = 7)
        String color
) {}
