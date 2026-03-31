import { useTranslation } from 'react-i18next';
import DashboardWidget from './DashboardWidget';

/**
 * OkrWidget - Objectives & Key Results (PRD-17).
 */
export default function OkrWidget({ okrs = [], loading }) {
    const { t } = useTranslation();

    return (
        <DashboardWidget title={t("OKRs")} icon="bi-trophy" loading={loading}>
            {okrs.length === 0 ? (
                <p className="text-muted small mb-0">{t("No OKRs defined")}</p>
            ) : (
                <div className="d-flex flex-column gap-3">
                    {okrs.map((okr, i) => (
                        <div key={i}>
                            <div className="fw-semibold small">{okr.objective}</div>
                            {okr.keyResults && (
                                <ul className="mb-0 ps-3 small text-muted">
                                    {(typeof okr.keyResults === 'string'
                                        ? okr.keyResults.split('\n')
                                        : Array.isArray(okr.keyResults) ? okr.keyResults : []
                                    ).filter(Boolean).map((kr, j) => (
                                        <li key={j}>{typeof kr === 'string' ? kr : kr.description || kr.name}</li>
                                    ))}
                                </ul>
                            )}
                            {okr.progress != null && (
                                <div className="mt-1">
                                    <div className="progress" style={{ height: 4 }}>
                                        <div className="progress-bar" style={{ width: `${okr.progress}%` }}></div>
                                    </div>
                                    <div className="text-muted" style={{ fontSize: '0.65rem' }}>{okr.progress}%</div>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </DashboardWidget>
    );
}
