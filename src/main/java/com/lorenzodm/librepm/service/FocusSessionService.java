package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.StartFocusSessionRequest;
import com.lorenzodm.librepm.api.dto.request.StopFocusSessionRequest;
import com.lorenzodm.librepm.api.dto.response.FocusHeatmapResponse;
import com.lorenzodm.librepm.core.entity.FocusSession;

import java.util.List;

public interface FocusSessionService {
    FocusSession start(String userId, String taskId, StartFocusSessionRequest req);
    FocusSession stop(String userId, String sessionId, StopFocusSessionRequest req);
    List<FocusSession> listByUser(String userId, String taskId);
    
    /**
     * Ottiene la sessione focus attualmente in corso per l'utente (se esiste)
     */
    FocusSession getCurrentRunning(String userId);

    /**
     * Ottiene tutte le sessioni focus attive per l'utente (multi-timer)
     */
    List<FocusSession> getAllRunning(String userId);

    /**
     * Ottiene i dati per la heatmap delle sessioni focus
     */
    FocusHeatmapResponse getHeatmap(String userId, int days);
}
