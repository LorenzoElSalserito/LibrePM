import { useTranslation } from 'react-i18next';
import DashboardWidget from './DashboardWidget';

/**
 * MetricsWidget - Success metrics with target vs achieved (PRD-17).
 */
export default function MetricsWidget({ charter, loading }) {
    const { t } = useTranslation();
    const metrics = charter?.successMetrics || [];

    return (
        <DashboardWidget title={t("Success Metrics")} icon="bi-graph-up-arrow" loading={loading}>
            {metrics.length === 0 ? (
                <p className="text-muted small mb-0">{t("No metrics defined")}</p>
            ) : (
                <div className="table-responsive">
                    <table className="table table-sm table-borderless mb-0">
                        <thead>
                            <tr className="text-muted" style={{ fontSize: '0.7rem' }}>
                                <th>{t("Metric")}</th>
                                <th>{t("Target")}</th>
                                <th>{t("Achieved")}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {metrics.map((m, i) => (
                                <tr key={i} className="small">
                                    <td>{m.name || m.metricName}</td>
                                    <td>{m.targetValue || '-'}</td>
                                    <td>
                                        {m.achievedRecords?.length > 0
                                            ? m.achievedRecords[m.achievedRecords.length - 1].value
                                            : <span className="text-muted">-</span>
                                        }
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </DashboardWidget>
    );
}
