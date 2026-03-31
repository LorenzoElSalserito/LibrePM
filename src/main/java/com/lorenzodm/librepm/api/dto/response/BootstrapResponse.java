package com.lorenzodm.librepm.api.dto.response;

import java.util.List;

/**
 * DTO Response per Bootstrap applicazione
 *
 * Contiene tutte le informazioni necessarie per inizializzare
 * l'applicazione al primo avvio o dopo un reload.
 *
 * @author Lorenzo DM
 * @since 0.3.0
 */
public record BootstrapResponse(
        /**
         * Lista di tutti i profili utente disponibili
         */
        List<UserResponse> users,

        /**
         * Preferenze globali dell'applicazione
         */
        AppPreferencesResponse preferences,

        /**
         * Informazioni sul sistema
         */
        SystemInfoResponse systemInfo
) {

    /**
     * DTO interno per preferenze app
     */
    public record AppPreferencesResponse(
            /**
             * ID dell'ultimo utente che ha effettuato il login
             */
            String lastUserId,

            /**
             * Se true, effettua login automatico con lastUserId
             */
            boolean autologinEnabled
    ) {}

    /**
     * DTO interno per info sistema
     */
    public record SystemInfoResponse(
            /**
             * Versione dell'applicazione
             */
            String version,

            /**
             * Modalità di esecuzione (desktop, web, mobile)
             */
            String mode,

            /**
             * True se il database è stato appena creato (primo avvio)
             */
            boolean freshInstall,

            /**
             * Numero totale di profili
             */
            int totalProfiles
    ) {}
}