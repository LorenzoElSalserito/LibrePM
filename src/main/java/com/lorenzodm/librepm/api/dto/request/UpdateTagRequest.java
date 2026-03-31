package com.lorenzodm.librepm.api.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request per aggiornamento tag
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
public record UpdateTagRequest(
        @Size(min = 1, max = 50, message = "Nome tag deve essere tra 1 e 50 caratteri")
        String name,

        @Size(max = 7, message = "Colore deve essere in formato hex (#RRGGBB)")
        String color
) {
}
