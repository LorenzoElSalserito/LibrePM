package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record StartFocusSessionRequest(
        @Size(max = 2000) String notes,
        @Size(max = 50) String sessionType
) {}
