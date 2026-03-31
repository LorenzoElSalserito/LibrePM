package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateLocalProfileRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateAppPreferencesRequest;
import com.lorenzodm.librepm.api.dto.response.BootstrapResponse;
import com.lorenzodm.librepm.core.entity.User;

/**
 * Service per gestione bootstrap applicazione
 *
 * Responsabile di:
 * - Fornire dati iniziali all'avvio
 * - Gestire preferenze globali (autologin, lastUserId)
 * - Creare profili locali
 * - Selezionare profilo attivo
 * - Eliminare profili locali
 * - Auto-detect dati esistenti (v0.4.0)
 *
 * @author Lorenzo DM
 * @since 0.3.0
 * @version 0.5.3
 */
public interface BootstrapService {

    /**
     * Ottiene tutti i dati necessari per il bootstrap dell'applicazione
     *
     * @return BootstrapResponse con users, preferences e system info
     */
    BootstrapResponse getBootstrapData();

    /**
     * Crea un nuovo profilo locale (senza password)
     *
     * @param request dati del nuovo profilo
     * @return User creato
     */
    User createLocalProfile(CreateLocalProfileRequest request);

    /**
     * Seleziona un profilo come attivo e aggiorna lastLoginAt
     *
     * @param userId ID del profilo da selezionare
     * @return User selezionato con lastLoginAt aggiornato
     */
    User selectProfile(String userId);

    /**
     * Effettua il login con password e seleziona il profilo
     *
     * @param userId ID del profilo
     * @param password Password in chiaro
     * @return User autenticato e selezionato
     */
    User login(String userId, String password);

    /**
     * Elimina (disattiva) un profilo locale
     *
     * @param userId ID del profilo da eliminare
     */
    void deleteProfile(String userId);

    /**
     * Aggiorna le preferenze globali dell'applicazione
     *
     * @param request preferenze da aggiornare
     * @return BootstrapResponse.AppPreferencesResponse aggiornato
     */
    BootstrapResponse.AppPreferencesResponse updatePreferences(UpdateAppPreferencesRequest request);

    /**
     * Ottiene solo le preferenze globali
     *
     * @return AppPreferencesResponse corrente
     */
    BootstrapResponse.AppPreferencesResponse getPreferences();

    /**
     * Verifica se un userId è valido per autologin
     *
     * @param userId ID da verificare
     * @return true se l'utente esiste ed è attivo
     */
    boolean isValidForAutologin(String userId);

    /**
     * Esegue auto-detect dei dati esistenti (es. dopo connessione a DB esterno)
     * e prepara l'ambiente.
     */
    void autoDetectData();
}