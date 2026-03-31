import { useState } from 'react';
import { useTranslation } from 'react-i18next';

/**
 * DashboardWidget - Reusable wrapper for executive dashboard widgets.
 * Provides header with title, collapse toggle, and loading/error states.
 */
export default function DashboardWidget({ title, icon, children, loading, error, collapsible = true, className = '' }) {
    const { t } = useTranslation();
    const [collapsed, setCollapsed] = useState(false);

    return (
        <div className={`card shadow-sm h-100 ${className}`}>
            <div className="card-header bg-transparent d-flex align-items-center justify-content-between py-2">
                <div className="d-flex align-items-center gap-2">
                    {icon && <i className={`bi ${icon} text-primary`}></i>}
                    <h6 className="mb-0 fw-semibold">{title}</h6>
                </div>
                {collapsible && (
                    <button
                        className="btn btn-sm btn-link text-muted p-0"
                        onClick={() => setCollapsed(!collapsed)}
                    >
                        <i className={`bi ${collapsed ? 'bi-chevron-down' : 'bi-chevron-up'}`}></i>
                    </button>
                )}
            </div>
            {!collapsed && (
                <div className="card-body py-2">
                    {loading ? (
                        <div className="text-center py-3">
                            <div className="spinner-border spinner-border-sm text-primary"></div>
                        </div>
                    ) : error ? (
                        <div className="text-danger small">{error}</div>
                    ) : (
                        children
                    )}
                </div>
            )}
        </div>
    );
}
