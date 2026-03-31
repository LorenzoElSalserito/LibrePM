import { useTranslation } from 'react-i18next';
import DashboardWidget from './DashboardWidget';

/**
 * HealthSummaryWidget - Project health KPIs (PRD-17).
 * Shows completion, status, overdue tasks, overbooked users.
 */
export default function HealthSummaryWidget({ data, loading }) {
    const { t } = useTranslation();

    if (!data) return <DashboardWidget title={t("Project Health")} icon="bi-heart-pulse" loading={loading}><p className="text-muted small">{t("No data")}</p></DashboardWidget>;

    const statusColor = {
        'ON_TRACK': 'success',
        'AT_RISK': 'warning',
        'DELAYED': 'danger',
        'BLOCKED': 'danger',
    };

    const color = statusColor[data.projectStatus] || 'secondary';

    return (
        <DashboardWidget title={t("Project Health")} icon="bi-heart-pulse" loading={loading}>
            <div className="d-flex flex-column gap-3">
                {/* Status badge */}
                <div className="d-flex align-items-center gap-2">
                    <span className={`badge bg-${color}`}>{(data.projectStatus || '').replace(/_/g, ' ')}</span>
                    <span className="text-muted small">{data.projectName}</span>
                </div>

                {/* Completion bar */}
                <div>
                    <div className="d-flex justify-content-between small mb-1">
                        <span>{t("Completion")}</span>
                        <span className="fw-semibold">{(data.completionPercentage || 0).toFixed(0)}%</span>
                    </div>
                    <div className="progress" style={{ height: 8 }}>
                        <div className={`progress-bar bg-${color}`} style={{ width: `${data.completionPercentage || 0}%` }}></div>
                    </div>
                </div>

                {/* KPI row */}
                <div className="row g-2 text-center">
                    <div className="col-4">
                        <div className={`fw-bold ${data.overdueTaskCount > 0 ? 'text-danger' : ''}`}>
                            {data.overdueTaskCount || 0}
                        </div>
                        <div className="text-muted" style={{ fontSize: '0.65rem' }}>{t("Overdue")}</div>
                    </div>
                    <div className="col-4">
                        <div className="fw-bold">
                            {data.completedDeliverables || 0}/{data.totalDeliverables || 0}
                        </div>
                        <div className="text-muted" style={{ fontSize: '0.65rem' }}>{t("Deliverables")}</div>
                    </div>
                    <div className="col-4">
                        <div className={`fw-bold ${data.overbookedUsersCount > 0 ? 'text-warning' : ''}`}>
                            {data.overbookedUsersCount || 0}
                        </div>
                        <div className="text-muted" style={{ fontSize: '0.65rem' }}>{t("Overbooked")}</div>
                    </div>
                </div>
            </div>
        </DashboardWidget>
    );
}
