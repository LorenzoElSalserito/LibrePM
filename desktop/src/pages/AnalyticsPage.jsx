import React, { useState, useEffect, useCallback } from 'react';
import { librepm } from '@api/librepm.js';
import FocusHeatmapWidget from '../components/FocusHeatmapWidget';
import EstimatesWidget from '../components/EstimatesWidget';
import EffortVarianceWidget from '../components/EffortVarianceWidget';
import { useTranslation } from 'react-i18next';

const AnalyticsPage = ({ shell }) => {
    const { t } = useTranslation();
    const [projects, setProjects] = useState([]);
    const [selectedProjectId, setSelectedProjectId] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Task completion stats
    const [taskStats, setTaskStats] = useState(null);
    const [timeStats, setTimeStats] = useState(null);

    useEffect(() => {
        shell?.setTitle?.(t("Analytics Hub"));
        shell?.setHeaderActions?.(null);
        return () => { shell?.setHeaderActions?.(null); };
    }, [shell, t]);

    useEffect(() => {
        loadProjects();
    }, []);

    const loadProjects = async () => {
        try {
            setLoading(true);
            const data = await librepm.projectsList({ archived: false });
            setProjects(data);
        } catch (err) {
            setError(t("Unable to load projects"));
        } finally {
            setLoading(false);
        }
    };

    const loadProjectStats = useCallback(async (projId) => {
        if (!projId) { setTaskStats(null); setTimeStats(null); return; }
        try {
            const [tasks, entries] = await Promise.all([
                librepm.tasksList(projId),
                librepm.timeEntriesByProject(projId)
            ]);

            // Task completion stats
            const total = tasks.length;
            const completed = tasks.filter(t => t.status === 'DONE' || t.status === 'done' || t.statusName === 'Done').length;
            const inProgress = tasks.filter(t => t.status === 'IN_PROGRESS' || t.status === 'in_progress' || t.statusName === 'In Progress').length;
            const byType = {};
            tasks.forEach(task => {
                const type = task.type || 'STANDARD';
                byType[type] = (byType[type] || 0) + 1;
            });

            setTaskStats({ total, completed, inProgress, byType });

            // Time stats
            const totalMinutes = entries.reduce((sum, e) => sum + (e.durationMinutes || 0), 0);
            const byDay = {};
            entries.forEach(e => {
                const day = e.entryDate || 'unknown';
                byDay[day] = (byDay[day] || 0) + (e.durationMinutes || 0);
            });
            const sortedDays = Object.keys(byDay).sort();
            const last7 = sortedDays.slice(-7);

            setTimeStats({ totalMinutes, byDay, last7 });
        } catch (err) {
            console.error("Stats error:", err);
        }
    }, []);

    useEffect(() => {
        loadProjectStats(selectedProjectId);
    }, [selectedProjectId, loadProjectStats]);

    const handleProjectChange = (e) => {
        setSelectedProjectId(e.target.value);
    };

    if (loading) {
        return (
            <div className="container-fluid p-4 d-flex justify-content-center align-items-center" style={{ minHeight: '80vh' }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">{t("Loading")}</span>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container-fluid p-4">
                <div className="alert alert-danger">{error}</div>
            </div>
        );
    }

    return (
        <div className="container-fluid p-4 fade-in">
            {/* Header */}
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="mb-1">{t("Analytics Hub")}</h2>
                    <p className="text-muted mb-0">{t("Productivity and estimate accuracy analysis")}</p>
                </div>
                <div className="d-flex gap-2">
                    <select
                        className="form-select"
                        value={selectedProjectId}
                        onChange={handleProjectChange}
                        style={{ minWidth: '250px' }}
                    >
                        <option value="">{t("All projects")}</option>
                        {projects.map(p => (
                            <option key={p.id} value={p.id}>{p.name}</option>
                        ))}
                    </select>
                </div>
            </div>

            {/* Summary Cards */}
            {taskStats && (
                <div className="row g-3 mb-4">
                    <div className="col-md-3">
                        <div className="card border-0 shadow-sm text-center">
                            <div className="card-body">
                                <div className="fs-2 fw-bold text-primary">{taskStats.total}</div>
                                <div className="small text-muted">{t("Total Tasks")}</div>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-3">
                        <div className="card border-0 shadow-sm text-center">
                            <div className="card-body">
                                <div className="fs-2 fw-bold text-success">{taskStats.completed}</div>
                                <div className="small text-muted">{t("Completed")}</div>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-3">
                        <div className="card border-0 shadow-sm text-center">
                            <div className="card-body">
                                <div className="fs-2 fw-bold text-warning">{taskStats.inProgress}</div>
                                <div className="small text-muted">{t("In Progress")}</div>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-3">
                        <div className="card border-0 shadow-sm text-center">
                            <div className="card-body">
                                <div className="fs-2 fw-bold text-info">
                                    {timeStats ? (timeStats.totalMinutes / 60).toFixed(1) : 0}h
                                </div>
                                <div className="small text-muted">{t("Total Hours Logged")}</div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <div className="row g-4">
                {/* Focus Heatmap */}
                <div className="col-12">
                    <div className="card shadow-sm border-0 h-100">
                        <div className="card-header bg-transparent border-0 pt-3 pb-0">
                            <h5 className="card-title mb-0">{t("Focus Heatmap")}</h5>
                        </div>
                        <div className="card-body">
                            <FocusHeatmapWidget projectId={selectedProjectId} range={365} />
                        </div>
                    </div>
                </div>

                {/* Estimates & Variance */}
                <div className="col-md-6">
                    <div className="card shadow-sm border-0 h-100">
                        <div className="card-header bg-transparent border-0 pt-3 pb-0">
                            <h5 className="card-title mb-0">{t("Estimate Accuracy")}</h5>
                        </div>
                        <div className="card-body">
                            <EstimatesWidget projectId={selectedProjectId} />
                        </div>
                    </div>
                </div>

                <div className="col-md-6">
                    <EffortVarianceWidget projectId={selectedProjectId} />
                </div>

                {/* Time Distribution (last 7 days) */}
                {timeStats && timeStats.last7.length > 0 && (
                    <div className="col-12">
                        <div className="card shadow-sm border-0">
                            <div className="card-header bg-transparent border-0 pt-3 pb-0">
                                <h5 className="card-title mb-0">{t("Recent Time Distribution")}</h5>
                            </div>
                            <div className="card-body">
                                <div className="d-flex align-items-end gap-2" style={{ height: 150 }}>
                                    {timeStats.last7.map(day => {
                                        const mins = timeStats.byDay[day] || 0;
                                        const maxMins = Math.max(...timeStats.last7.map(d => timeStats.byDay[d] || 0));
                                        const pct = maxMins > 0 ? (mins / maxMins) * 100 : 0;
                                        return (
                                            <div key={day} className="flex-grow-1 text-center">
                                                <div
                                                    className="bg-primary bg-opacity-75 rounded-top mx-auto"
                                                    style={{ height: `${Math.max(pct, 5)}%`, maxWidth: 40, minHeight: 4 }}
                                                    title={`${mins} min`}
                                                ></div>
                                                <div className="small text-muted mt-1" style={{ fontSize: '0.65rem' }}>
                                                    {new Date(day + "T00:00:00").toLocaleDateString(undefined, { weekday: 'short' })}
                                                </div>
                                                <div className="small fw-bold" style={{ fontSize: '0.7rem' }}>
                                                    {Math.round(mins / 60 * 10) / 10}h
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Task Type Distribution */}
                {taskStats && Object.keys(taskStats.byType).length > 1 && (
                    <div className="col-md-6">
                        <div className="card shadow-sm border-0">
                            <div className="card-header bg-transparent border-0 pt-3 pb-0">
                                <h5 className="card-title mb-0">{t("Tasks by Type")}</h5>
                            </div>
                            <div className="card-body">
                                {Object.entries(taskStats.byType).map(([type, count]) => (
                                    <div key={type} className="d-flex align-items-center mb-2">
                                        <span className="small me-2" style={{ minWidth: 100 }}>{t(`taskTypes.${type}`, type)}</span>
                                        <div className="progress flex-grow-1" style={{ height: 8 }}>
                                            <div
                                                className="progress-bar"
                                                style={{ width: `${(count / taskStats.total) * 100}%` }}
                                            ></div>
                                        </div>
                                        <span className="small ms-2 fw-bold">{count}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                )}

                {/* Completion Rate */}
                {taskStats && taskStats.total > 0 && (
                    <div className="col-md-6">
                        <div className="card shadow-sm border-0">
                            <div className="card-header bg-transparent border-0 pt-3 pb-0">
                                <h5 className="card-title mb-0">{t("Completion Rate")}</h5>
                            </div>
                            <div className="card-body text-center">
                                <div className="position-relative d-inline-block" style={{ width: 150, height: 150 }}>
                                    <svg viewBox="0 0 36 36" className="w-100 h-100">
                                        <path
                                            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                                            fill="none"
                                            stroke="#e9ecef"
                                            strokeWidth="3"
                                        />
                                        <path
                                            d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                                            fill="none"
                                            stroke="#198754"
                                            strokeWidth="3"
                                            strokeDasharray={`${(taskStats.completed / taskStats.total) * 100}, 100`}
                                        />
                                    </svg>
                                    <div className="position-absolute top-50 start-50 translate-middle text-center">
                                        <div className="fs-4 fw-bold">{Math.round((taskStats.completed / taskStats.total) * 100)}%</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AnalyticsPage;
