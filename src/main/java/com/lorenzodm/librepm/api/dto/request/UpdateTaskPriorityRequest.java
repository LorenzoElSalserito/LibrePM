package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateTaskPriorityRequest(
        @Size(min = 1, max = 50)
        String name,
        Integer level,
        @Size(max = 7)
        String color
) {}
