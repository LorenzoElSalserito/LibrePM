import React, { useState, useEffect } from "react";
import { librepm } from "@api/librepm.js";
import { useTranslation } from "react-i18next";

/**
 * EffortVarianceWidget - Shows estimated vs actual effort per task (PRD-03-FR-002/003).
 * Displays horizontal bars with deviation percentages.
 */
export default function EffortVarianceWidget({ projectId }) {
    const { t } = useTranslation();
    const [variance, setVariance] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!projectId) return;
        setLoading(true);
        librepm.timeEntriesProjectVariance(projectId)
            .then(data => setVariance(data || []))
            .catch(err => console.error("Variance error:", err))
            .finally(() => setLoading(false));
    }, [projectId]);

    if (loading) {
        return (
            <div className="card border-0 shadow-sm">
                <div className="card-body text-center py-4">
                    <div className="spinner-border spinner-border-sm text-primary"></div>
                </div>
            </div>
        );
    }

    if (variance.length === 0) {
        return (
            <div className="card border-0 shadow-sm">
                <div className="card-body">
                    <h6 className="card-title mb-2">
                        <i className="bi bi-bar-chart me-2"></i>
                        {t("Effort Variance")}
                    </h6>
                    <p className="text-muted small mb-0">{t("No estimated tasks yet")}</p>
                </div>
            </div>
        );
    }

    const maxMinutes = Math.max(...variance.map(v => Math.max(v.estimatedMinutes || 0, v.actualMinutes || 0)));

    return (
        <div className="card border-0 shadow-sm">
            <div className="card-body">
                <h6 className="card-title mb-3">
                    <i className="bi bi-bar-chart me-2"></i>
                    {t("Effort Variance")}
                </h6>
                {variance.map(v => {
                    const estPct = maxMinutes > 0 ? ((v.estimatedMinutes || 0) / maxMinutes) * 100 : 0;
                    const actPct = maxMinutes > 0 ? ((v.actualMinutes || 0) / maxMinutes) * 100 : 0;
                    const devColor = v.deviationPercentage > 20 ? "#dc3545"
                        : v.deviationPercentage > 0 ? "#ffc107"
                        : v.deviationPercentage < -10 ? "#198754"
                        : "#6c757d";

                    return (
                        <div key={v.taskId} className="mb-3">
                            <div className="d-flex justify-content-between align-items-center mb-1">
                                <span className="small text-truncate" style={{ maxWidth: "60%" }}>
                                    {v.taskTitle}
                                </span>
                                <span className="badge" style={{ backgroundColor: devColor, color: "#fff", fontSize: "0.7rem" }}>
                                    {v.deviationPercentage > 0 ? "+" : ""}{v.deviationPercentage}%
                                </span>
                            </div>
                            <div className="d-flex gap-1 align-items-center">
                                <div className="flex-grow-1">
                                    <div className="progress" style={{ height: 6 }}>
                                        <div className="progress-bar bg-primary" style={{ width: `${estPct}%` }}></div>
                                    </div>
                                    <div className="progress mt-1" style={{ height: 6 }}>
                                        <div className="progress-bar" style={{ width: `${actPct}%`, backgroundColor: devColor }}></div>
                                    </div>
                                </div>
                                <div className="text-end" style={{ minWidth: 80 }}>
                                    <div className="small text-primary">{Math.round((v.estimatedMinutes || 0) / 60 * 10) / 10}h {t("est")}</div>
                                    <div className="small" style={{ color: devColor }}>{Math.round(v.actualMinutes / 60 * 10) / 10}h {t("act")}</div>
                                </div>
                            </div>
                        </div>
                    );
                })}
                <div className="mt-2 d-flex gap-3 small text-muted">
                    <span><span className="d-inline-block rounded me-1" style={{ width: 10, height: 10, backgroundColor: "#0d6efd" }}></span>{t("Estimated")}</span>
                    <span><span className="d-inline-block rounded me-1" style={{ width: 10, height: 10, backgroundColor: "#6c757d" }}></span>{t("Actual")}</span>
                </div>
            </div>
        </div>
    );
}
