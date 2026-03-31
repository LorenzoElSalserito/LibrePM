package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAssignmentRequest(
        @NotBlank(message = "ID task obbligatorio")
        String taskId,
        @NotBlank(message = "ID utente obbligatorio")
        String userId,
        String roleId
) {}
