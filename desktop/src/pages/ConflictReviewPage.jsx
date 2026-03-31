import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';

/**
 * ConflictReviewPage — UI for reviewing and resolving sync conflicts.
 * PRD-13-FR-005: Side-by-side comparison with resolution actions.
 */
export default function ConflictReviewPage({ shell }) {
    const { t } = useTranslation();
    const [conflicts, setConflicts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedConflict, setSelectedConflict] = useState(null);

    useEffect(() => {
        shell?.setTitle?.(t("Conflict Review"));
        shell?.setHeaderActions?.(null);
        return () => shell?.setHeaderActions?.(null);
    }, [shell, t]);

    const loadConflicts = useCallback(async () => {
        setLoading(true);
        try {
            const data = await librepm.syncConflictsList();
            setConflicts(data || []);
        } catch (e) {
            console.error("Error loading conflicts", e);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { loadConflicts(); }, [loadConflicts]);

    const handleResolve = async (conflictId, strategy) => {
        try {
            await librepm.syncConflictResolve(conflictId, { strategy });
            toast.success(t("Conflict resolved"));
            setSelectedConflict(null);
            loadConflicts();
        } catch (e) {
            toast.error(t("Error resolving conflict"));
        }
    };

    const parseJson = (str) => {
        try { return JSON.parse(str); } catch { return str; }
    };

    if (loading) {
        return (
            <div className="container-fluid p-4 text-center">
                <div className="spinner-border text-primary"></div>
            </div>
        );
    }

    return (
        <div className="container-fluid p-4">
            <h4 className="mb-4">
                <i className="bi bi-arrow-left-right me-2"></i>
                {t("Conflict Review")}
                {conflicts.length > 0 && (
                    <span className="badge bg-warning text-dark ms-2">{conflicts.length}</span>
                )}
            </h4>

            {conflicts.length === 0 ? (
                <div className="text-center py-5">
                    <i className="bi bi-check-circle text-success" style={{ fontSize: '3rem' }}></i>
                    <p className="text-muted mt-3">{t("No pending conflicts")}</p>
                </div>
            ) : (
                <div className="row">
                    {/* Conflict list */}
                    <div className="col-md-4">
                        <div className="list-group">
                            {conflicts.map(c => (
                                <button
                                    key={c.id}
                                    className={`list-group-item list-group-item-action ${selectedConflict?.id === c.id ? 'active' : ''}`}
                                    onClick={() => setSelectedConflict(c)}
                                >
                                    <div className="d-flex justify-content-between">
                                        <strong>{c.entityType}</strong>
                                        <small className="text-muted">{c.conflictType}</small>
                                    </div>
                                    <small className="d-block text-truncate">{c.entityId}</small>
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Conflict detail */}
                    <div className="col-md-8">
                        {selectedConflict ? (
                            <div className="card shadow-sm">
                                <div className="card-header">
                                    <h6 className="mb-0">
                                        {selectedConflict.entityType} — {selectedConflict.conflictType}
                                    </h6>
                                </div>
                                <div className="card-body">
                                    <div className="row">
                                        <div className="col-md-6">
                                            <h6 className="text-primary">{t("Local Version")}</h6>
                                            <pre className="bg-light p-3 rounded small" style={{ maxHeight: 300, overflow: 'auto' }}>
                                                {JSON.stringify(parseJson(selectedConflict.localPayload), null, 2)}
                                            </pre>
                                        </div>
                                        <div className="col-md-6">
                                            <h6 className="text-danger">{t("Remote Version")}</h6>
                                            <pre className="bg-light p-3 rounded small" style={{ maxHeight: 300, overflow: 'auto' }}>
                                                {JSON.stringify(parseJson(selectedConflict.remotePayload), null, 2)}
                                            </pre>
                                        </div>
                                    </div>
                                </div>
                                <div className="card-footer d-flex gap-2">
                                    <button
                                        className="btn btn-primary"
                                        onClick={() => handleResolve(selectedConflict.id, 'KEEP_LOCAL')}
                                    >
                                        <i className="bi bi-check-lg me-1"></i>{t("Keep Local")}
                                    </button>
                                    <button
                                        className="btn btn-outline-danger"
                                        onClick={() => handleResolve(selectedConflict.id, 'ACCEPT_REMOTE')}
                                    >
                                        <i className="bi bi-cloud-download me-1"></i>{t("Accept Remote")}
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="text-center text-muted py-5">
                                <i className="bi bi-hand-index" style={{ fontSize: '2rem' }}></i>
                                <p className="mt-2">{t("Select a conflict to review")}</p>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
