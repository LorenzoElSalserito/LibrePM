package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request per cambiare lo stato di un Task (assegnando un TaskStatus diverso).
 */
public record ChangeTaskStatusRequest(
        @NotBlank(message = "ID stato obbligatorio")
        String statusId
) {}
