package com.lorenzodm.librepm.service;

public interface ReportGenerationService {

    enum ReportType {
        EXECUTIVE_SUMMARY, CHARTER, DELIVERABLE_STATUS, RISK_MATRIX
    }

    /**
     * Generates a PDF report for the specified project.
     * @return PDF bytes
     */
    byte[] generateReport(String projectId, ReportType type);
}
