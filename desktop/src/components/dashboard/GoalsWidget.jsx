import { useTranslation } from 'react-i18next';
import DashboardWidget from './DashboardWidget';

/**
 * GoalsWidget - Problem statement + objectives from charter (PRD-17).
 */
export default function GoalsWidget({ charter, loading }) {
    const { t } = useTranslation();

    return (
        <DashboardWidget title={t("Goals & Objectives")} icon="bi-bullseye" loading={loading}>
            {!charter ? (
                <p className="text-muted small mb-0">{t("No charter defined")}</p>
            ) : (
                <div>
                    {charter.problemStatement && (
                        <div className="mb-3">
                            <div className="text-muted small fw-semibold mb-1">{t("Problem Statement")}</div>
                            <p className="small mb-0">{charter.problemStatement}</p>
                        </div>
                    )}
                    {charter.objectives && (
                        <div>
                            <div className="text-muted small fw-semibold mb-1">{t("Objectives")}</div>
                            <p className="small mb-0" style={{ whiteSpace: 'pre-line' }}>{charter.objectives}</p>
                        </div>
                    )}
                </div>
            )}
        </DashboardWidget>
    );
}
