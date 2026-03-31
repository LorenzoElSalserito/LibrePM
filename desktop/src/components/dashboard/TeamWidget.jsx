import { useTranslation } from 'react-i18next';
import DashboardWidget from './DashboardWidget';

/**
 * TeamWidget - Shows team members with roles (PRD-17).
 */
export default function TeamWidget({ members = [], loading }) {
    const { t } = useTranslation();

    return (
        <DashboardWidget title={t("Team")} icon="bi-people" loading={loading}>
            {members.length === 0 ? (
                <p className="text-muted small mb-0">{t("No team members assigned")}</p>
            ) : (
                <div className="d-flex flex-column gap-2">
                    {members.map((m, i) => (
                        <div key={i} className="d-flex align-items-center gap-2">
                            <div className="rounded-circle bg-primary bg-opacity-10 text-primary d-flex align-items-center justify-content-center"
                                 style={{ width: 32, height: 32, fontSize: '0.75rem', fontWeight: 600 }}>
                                {(m.displayName || m.username || '?').substring(0, 2).toUpperCase()}
                            </div>
                            <div className="flex-grow-1">
                                <div className="small fw-semibold">{m.displayName || m.username}</div>
                                {m.roleName && <div className="text-muted" style={{ fontSize: '0.7rem' }}>{m.roleName}</div>}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </DashboardWidget>
    );
}
