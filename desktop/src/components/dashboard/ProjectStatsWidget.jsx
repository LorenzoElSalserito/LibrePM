import React, { useState, useEffect } from 'react';
import librepm from '@api/librepm.js';

/**
 * Widget for executive-level project stats (PRD-17).
 * Displays completion status, overdue tasks, and overall health.
 */
const ProjectStatsWidget = ({ projectId }) => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchStats = async () => {
            if (!projectId) return;
            try {
                setLoading(true);
                const data = await librepm.executiveDashboard(projectId);
                setStats(data);
                setError(null);
            } catch (err) {
                console.error(err);
                setError('Impossibile caricare le statistiche.');
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, [projectId]);

    if (loading) return <div className="widget-placeholder">Caricamento Statistiche...</div>;
    if (error) return <div className="widget-error">{error}</div>;
    if (!stats) return <div className="widget-placeholder">Dati non disponibili.</div>;

    const { projectStatus, completionPercentage, overdueTasksCount } = stats;
    const statusClass = `status-${projectStatus.toLowerCase()}`;

    return (
        <div className="dashboard-widget project-summary">
            <h3>Riepilogo Progetto</h3>
            <div className={`project-status ${statusClass}`}>
                Stato: <strong>{projectStatus.replace('_', ' ')}</strong>
            </div>
            <div className="stat-row">
                <span>Completamento:</span>
                <span className="stat-value">{completionPercentage.toFixed(1)}%</span>
            </div>
            <div className="stat-row">
                <span>Task in Ritardo:</span>
                <span className={`stat-value ${overdueTasksCount > 0 ? 'text-danger' : ''}`}>
                    {overdueTasksCount}
                </span>
            </div>
        </div>
    );
};

export default ProjectStatsWidget;
