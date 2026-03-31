package com.lorenzodm.librepm.service;

import java.nio.file.Path;

/**
 * Service for generating local diagnostics bundles.
 * Collects system info, health data, and recent events without personal data.
 */
public interface DiagnosticsService {

    /**
     * Generates a diagnostic ZIP bundle containing:
     * - system-info.json (app version, Java, OS, DB size, entity counts)
     * - health-report.json (health check results)
     * - recent-events.json (anonymized event journal entries)
     * - migration-history.json (Flyway migration state)
     *
     * Does NOT collect: task/note contents, passwords, personal data.
     */
    Path generateBundle();
}
