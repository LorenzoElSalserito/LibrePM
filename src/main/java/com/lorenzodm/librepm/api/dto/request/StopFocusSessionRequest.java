package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

public record StopFocusSessionRequest(
        @Size(max = 2000) String notes
) {}
