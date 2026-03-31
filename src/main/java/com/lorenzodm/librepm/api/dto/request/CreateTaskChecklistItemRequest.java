package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskChecklistItemRequest(
        @NotBlank @Size(max = 1000) String text,
        Integer sortOrder
) {}
