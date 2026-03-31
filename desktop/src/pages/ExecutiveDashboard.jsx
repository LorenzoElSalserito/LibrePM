import { useState, useEffect, useCallback } from 'react';
import { librepm } from '@api/librepm.js';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-toastify';

// Dashboard widgets
import HealthSummaryWidget from '../components/dashboard/HealthSummaryWidget';
import GoalsWidget from '../components/dashboard/GoalsWidget';
import MetricsWidget from '../components/dashboard/MetricsWidget';
import OkrWidget from '../components/dashboard/OkrWidget';
import TeamWidget from '../components/dashboard/TeamWidget';
import DashboardWidget from '../components/dashboard/DashboardWidget';

/**
 * ExecutiveDashboard - Mature executive dashboard with modular widgets (PRD-17).
 * Uses the aggregated /dashboard endpoint for a single optimized load.
 */
export default function ExecutiveDashboard({ shell }) {
    const { t } = useTranslation();
    const projectId = shell?.currentProject?.id;
    const [dashboard, setDashboard] = useState(null);
    const [members, setMembers] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        shell?.setTitle?.(t("Executive Dashboard"));
        shell?.setHeaderActions?.(
            <div className="d-flex gap-2 align-items-center">
                {/* Project selector */}
                {shell?.projects?.length > 1 && (
                    <select
                        className="form-select form-select-sm"
                        value={projectId || ''}
                        onChange={(e) => {
                            const p = shell.projects.find(pr => pr.id === e.target.value);
                            if (p) shell.setCurrentProject(p);
                        }}
                        style={{ width: 200 }}
                    >
                        {shell.projects.map(p => (
                            <option key={p.id} value={p.id}>{p.name}</option>
                        ))}
                    </select>
                )}
                {/* Export buttons */}
                <button className="btn btn-sm btn-outline-secondary" onClick={() => window.print()} title={t("Print")}>
                    <i className="bi bi-printer"></i>
                </button>
                <button className="btn btn-sm btn-outline-secondary" onClick={handleExportPdf} title={t("Export PDF")}>
                    <i className="bi bi-file-earmark-pdf"></i>
                </button>
            </div>
        );
        return () => shell?.setHeaderActions?.(null);
    }, [shell, t, projectId]);

    const loadDashboard = useCallback(async () => {
        if (!projectId) return;
        setLoading(true);
        try {
            const data = await librepm.executiveDashboard(projectId);
            setDashboard(data);
        } catch (e) {
            console.error('[ExecDashboard] Error loading:', e);
        }

        // Load team members separately
        try {
            const teamMembers = await librepm.projectMembersList(projectId);
            setMembers(teamMembers || []);
        } catch { /* ignore */ }

        setLoading(false);
    }, [projectId]);

    useEffect(() => {
        loadDashboard();
    }, [loadDashboard]);

    const handleExportPdf = async () => {
        if (!projectId) return;
        try {
            const blob = await librepm.reportGenerate(projectId, 'EXECUTIVE_SUMMARY');
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `executive-dashboard-${new Date().toISOString().split('T')[0]}.pdf`;
            a.click();
            URL.revokeObjectURL(url);
            toast.success(t("PDF exported"));
        } catch (e) {
            toast.error(t("Error") + ': ' + e.message);
        }
    };

    if (!projectId) {
        return (
            <div className="container-fluid p-4 text-center py-5">
                <i className="bi bi-folder2-open fs-1 text-muted d-block mb-3"></i>
                <h5 className="text-muted">{t("Select a project to view the executive dashboard")}</h5>
            </div>
        );
    }

    if (loading && !dashboard) {
        return (
            <div className="d-flex justify-content-center align-items-center py-5">
                <div className="spinner-border text-primary"></div>
            </div>
        );
    }

    return (
        <div className="container-fluid p-4 executive-dashboard-page">
            {/* Row 1: Health + Goals */}
            <div className="row g-3 mb-3">
                <div className="col-lg-4">
                    <HealthSummaryWidget data={dashboard} loading={loading} />
                </div>
                <div className="col-lg-8">
                    <GoalsWidget charter={dashboard?.charter} loading={loading} />
                </div>
            </div>

            {/* Row 2: Risks + Deliverables */}
            <div className="row g-3 mb-3">
                <div className="col-lg-6">
                    <DashboardWidget title={t("Risk Register")} icon="bi-shield-exclamation" loading={loading}>
                        {(!dashboard?.risks || dashboard.risks.length === 0) ? (
                            <p className="text-muted small mb-0">{t("No risks identified")}</p>
                        ) : (
                            <div className="table-responsive">
                                <table className="table table-sm table-borderless mb-0">
                                    <thead>
                                        <tr className="text-muted" style={{ fontSize: '0.7rem' }}>
                                            <th>{t("Description")}</th>
                                            <th>{t("Probability")}</th>
                                            <th>{t("Impact")}</th>
                                            <th>{t("Mitigation")}</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {dashboard.risks.map((r, i) => (
                                            <tr key={i} className="small">
                                                <td>{r.description}</td>
                                                <td><span className={`badge bg-${r.probability === 'HIGH' ? 'danger' : r.probability === 'MEDIUM' ? 'warning' : 'success'}-subtle text-${r.probability === 'HIGH' ? 'danger' : r.probability === 'MEDIUM' ? 'warning' : 'success'}`} style={{ fontSize: '0.65rem' }}>{r.probability}</span></td>
                                                <td><span className={`badge bg-${r.impact === 'HIGH' ? 'danger' : r.impact === 'MEDIUM' ? 'warning' : 'success'}-subtle text-${r.impact === 'HIGH' ? 'danger' : r.impact === 'MEDIUM' ? 'warning' : 'success'}`} style={{ fontSize: '0.65rem' }}>{r.impact}</span></td>
                                                <td className="text-muted">{r.mitigationStrategy || '-'}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                                {dashboard.highImpactRisks > 0 && (
                                    <div className="text-danger small mt-1">
                                        <i className="bi bi-exclamation-triangle me-1"></i>
                                        {dashboard.highImpactRisks} {t("high-impact risks")}
                                    </div>
                                )}
                            </div>
                        )}
                    </DashboardWidget>
                </div>
                <div className="col-lg-6">
                    <DashboardWidget title={t("Deliverables")} icon="bi-box-seam" loading={loading}>
                        {(!dashboard?.deliverables || dashboard.deliverables.length === 0) ? (
                            <p className="text-muted small mb-0">{t("No deliverables defined")}</p>
                        ) : (
                            <div className="d-flex flex-column gap-2">
                                {dashboard.deliverables.map((d, i) => {
                                    const statusColors = {
                                        'ON_TRACK': 'success', 'AT_RISK': 'warning', 'DELAYED': 'danger',
                                        'COMPLETED': 'primary', 'NOT_STARTED': 'secondary'
                                    };
                                    const c = statusColors[d.riskStatus] || 'secondary';
                                    return (
                                        <div key={i} className="d-flex align-items-center gap-2">
                                            <span className={`badge bg-${c}`} style={{ fontSize: '0.6rem', minWidth: 70 }}>
                                                {(d.riskStatus || 'N/A').replace(/_/g, ' ')}
                                            </span>
                                            <span className="small flex-grow-1">{d.title}</span>
                                            {d.dueDate && (
                                                <span className="text-muted" style={{ fontSize: '0.65rem' }}>
                                                    {new Date(d.dueDate).toLocaleDateString()}
                                                </span>
                                            )}
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </DashboardWidget>
                </div>
            </div>

            {/* Row 3: OKRs + Metrics + Team */}
            <div className="row g-3 mb-3">
                <div className="col-lg-4">
                    <OkrWidget okrs={dashboard?.okrs} loading={loading} />
                </div>
                <div className="col-lg-4">
                    <MetricsWidget charter={dashboard?.charter} loading={loading} />
                </div>
                <div className="col-lg-4">
                    <TeamWidget members={members} loading={loading} />
                </div>
            </div>

            {/* Row 4: Variance (if baseline exists) */}
            {dashboard?.latestVariance && (
                <div className="row g-3 mb-3">
                    <div className="col-12">
                        <DashboardWidget title={t("Baseline Variance")} icon="bi-arrow-left-right" loading={loading}>
                            <div className="row text-center">
                                <div className="col-md-3">
                                    <div className="fw-bold">{dashboard.latestVariance.changedCount || 0}</div>
                                    <div className="text-muted small">{t("Changed")}</div>
                                </div>
                                <div className="col-md-3">
                                    <div className="fw-bold">{dashboard.latestVariance.addedCount || 0}</div>
                                    <div className="text-muted small">{t("Added")}</div>
                                </div>
                                <div className="col-md-3">
                                    <div className="fw-bold">{dashboard.latestVariance.removedCount || 0}</div>
                                    <div className="text-muted small">{t("Removed")}</div>
                                </div>
                                <div className="col-md-3">
                                    <div className="fw-bold">{dashboard.latestVariance.scheduleVarianceDays || 0}</div>
                                    <div className="text-muted small">{t("Schedule Variance (days)")}</div>
                                </div>
                            </div>
                        </DashboardWidget>
                    </div>
                </div>
            )}

            {/* Print-friendly styles */}
            <style>{`
                @media print {
                    .jl-sidebar, .jl-topheader, .jl-rightpanel, .btn { display: none !important; }
                    .executive-dashboard-page { padding: 0 !important; }
                    .card { break-inside: avoid; }
                }
            `}</style>
        </div>
    );
}
