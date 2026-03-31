package com.lorenzodm.librepm.api.dto.request;

/**
 * DTO Request per aggiornamento preferenze globali app
 *
 * @author Lorenzo DM
 * @since 0.3.0
 */
public record UpdateAppPreferencesRequest(
        /**
         * ID dell'ultimo utente selezionato
         * Può essere null per resettare
         */
        String lastUserId,

        /**
         * Abilita/disabilita autologin
         * Se null, non viene modificato
         */
        Boolean autologinEnabled
) {}