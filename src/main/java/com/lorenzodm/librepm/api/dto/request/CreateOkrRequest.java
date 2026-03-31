package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOkrRequest(
        @NotBlank @Size(max = 255) String objective
) {}
