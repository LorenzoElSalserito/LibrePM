package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateTaskStatusRequest(
        @Size(min = 1, max = 50)
        String name,

        @Size(max = 255)
        String description,

        @Size(max = 7)
        String color
) {}
