package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateTaskChecklistItemRequest(
        @Size(max = 1000) String text,
        Boolean done,
        Integer sortOrder
) {}
