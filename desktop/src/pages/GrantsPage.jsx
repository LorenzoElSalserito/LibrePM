import { useState, useEffect, useCallback } from 'react';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

/**
 * GrantsPage — Manages grant calls, eligibility, submissions, obligations, and reporting.
 * Tabs: Calls, Requirements, Obligations, Reporting.
 */
export default function GrantsPage({ shell }) {
    const { t } = useTranslation();
    const projectId = shell?.currentProject?.id;
    const [tab, setTab] = useState('calls');

    // Calls state
    const [calls, setCalls] = useState([]);
    const [selectedCall, setSelectedCall] = useState(null);
    const [editingCall, setEditingCall] = useState(null);

    // Requirements state
    const [requirements, setRequirements] = useState([]);

    // Obligations state
    const [obligations, setObligations] = useState([]);

    // Reporting periods state
    const [periods, setPeriods] = useState([]);

    useEffect(() => { shell?.setTitle?.(t("Grants")); }, []);

    // =========== LOADERS ===========

    const loadCalls = useCallback(async () => {
        if (!projectId) return;
        try {
            const list = await librepm.grantCallsList(projectId);
            setCalls(list || []);
        } catch (e) { console.error('Error loading calls:', e); }
    }, [projectId]);

    const loadRequirements = useCallback(async () => {
        if (!selectedCall) return;
        try {
            const list = await librepm.grantRequirementsList(projectId, selectedCall.id);
            setRequirements(list || []);
        } catch (e) { console.error('Error loading requirements:', e); }
    }, [projectId, selectedCall]);

    const loadObligations = useCallback(async () => {
        if (!projectId) return;
        try {
            const list = await librepm.grantObligationsList(projectId);
            setObligations(list || []);
        } catch (e) { console.error('Error loading obligations:', e); }
    }, [projectId]);

    const loadPeriods = useCallback(async () => {
        if (!projectId) return;
        try {
            const list = await librepm.grantReportingPeriodsList(projectId);
            setPeriods(list || []);
        } catch (e) { console.error('Error loading periods:', e); }
    }, [projectId]);

    useEffect(() => { loadCalls(); loadObligations(); loadPeriods(); }, [loadCalls, loadObligations, loadPeriods]);
    useEffect(() => { loadRequirements(); }, [loadRequirements]);

    // =========== HANDLERS ===========

    const handleCreateCall = async () => {
        try {
            const c = await librepm.grantCallCreate(projectId, { title: t("New call") });
            setCalls(prev => [...prev, c]);
            setSelectedCall(c);
            setEditingCall(c);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleUpdateCall = async (callId, data) => {
        try {
            const updated = await librepm.grantCallUpdate(projectId, callId, data);
            setCalls(prev => prev.map(c => c.id === callId ? updated : c));
            if (selectedCall?.id === callId) setSelectedCall(updated);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleDeleteCall = async (callId) => {
        try {
            await librepm.grantCallDelete(projectId, callId);
            setCalls(prev => prev.filter(c => c.id !== callId));
            if (selectedCall?.id === callId) { setSelectedCall(null); setEditingCall(null); }
            toast.success(t("Deleted successfully"));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleToggleRequirement = async (req) => {
        try {
            await librepm.grantRequirementUpdate(projectId, selectedCall.id, req.id, { isMet: !req.isMet });
            loadRequirements();
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleAddRequirement = async () => {
        try {
            await librepm.grantRequirementCreate(projectId, selectedCall.id, {
                description: t("New requirement"),
                requirementType: 'ELIGIBILITY'
            });
            loadRequirements();
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleCreateObligation = async () => {
        try {
            const o = await librepm.grantObligationCreate(projectId, { title: t("New obligation") });
            setObligations(prev => [...prev, o]);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleUpdateObligation = async (oblId, data) => {
        try {
            const updated = await librepm.grantObligationUpdate(projectId, oblId, data);
            setObligations(prev => prev.map(o => o.id === oblId ? updated : o));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleCreatePeriod = async () => {
        try {
            const today = new Date().toISOString().split('T')[0];
            const endDate = new Date(Date.now() + 90 * 86400000).toISOString().split('T')[0];
            const rp = await librepm.grantReportingPeriodCreate(projectId, {
                name: t("New period"),
                periodStart: today,
                periodEnd: endDate
            });
            setPeriods(prev => [...prev, rp]);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    // =========== STATUS HELPERS ===========

    const callStatusColor = (status) => {
        const colors = {
            IDENTIFIED: 'secondary', EVALUATING: 'info', PREPARING: 'warning',
            SUBMITTED: 'primary', AWARDED: 'success', REJECTED: 'danger', CLOSED: 'dark'
        };
        return colors[status] || 'secondary';
    };

    const oblStatusColor = (status) => {
        const colors = { PENDING: 'warning', IN_PROGRESS: 'info', COMPLETED: 'success', OVERDUE: 'danger' };
        return colors[status] || 'secondary';
    };

    const rpStatusColor = (status) => {
        const colors = { UPCOMING: 'secondary', OPEN: 'info', SUBMITTED: 'primary', ACCEPTED: 'success' };
        return colors[status] || 'secondary';
    };

    // =========== RENDER ===========

    if (!projectId) {
        return <div className="container-fluid p-4 text-center text-muted">
            <i className="bi bi-folder2-open fs-1 d-block mb-3"></i>
            <p>{t("Select a project")}</p>
        </div>;
    }

    const CALL_STATUSES = ['IDENTIFIED', 'EVALUATING', 'PREPARING', 'SUBMITTED', 'AWARDED', 'REJECTED', 'CLOSED'];
    const OBL_STATUSES = ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE'];
    const RP_STATUSES = ['UPCOMING', 'OPEN', 'SUBMITTED', 'ACCEPTED'];
    const OBL_TYPES = ['CONTRACTUAL', 'GRANT', 'REGULATORY', 'REPORTING'];
    const REQ_TYPES = ['DOCUMENT', 'ELIGIBILITY', 'BUDGET', 'PARTNER', 'TECHNICAL'];

    return (
        <div className="container-fluid p-4">
            {/* Tabs */}
            <ul className="nav nav-tabs mb-4">
                {[
                    { key: 'calls', icon: 'bi-megaphone', label: t("Calls") },
                    { key: 'obligations', icon: 'bi-clipboard-check', label: t("Obligations") },
                    { key: 'reporting', icon: 'bi-calendar-range', label: t("Reporting") },
                ].map(({ key, icon, label }) => (
                    <li className="nav-item" key={key}>
                        <button className={`nav-link ${tab === key ? 'active' : ''}`} onClick={() => setTab(key)}>
                            <i className={`bi ${icon} me-1`}></i>{label}
                        </button>
                    </li>
                ))}
            </ul>

            {/* CALLS TAB */}
            {tab === 'calls' && (
                <div className="row">
                    {/* Call list */}
                    <div className="col-md-4">
                        <div className="d-flex justify-content-between align-items-center mb-3">
                            <h6 className="mb-0">{t("Grant Calls")}</h6>
                            <button className="btn btn-sm btn-primary" onClick={handleCreateCall}>
                                <i className="bi bi-plus me-1"></i>{t("New Call")}
                            </button>
                        </div>
                        <div className="list-group">
                            {calls.map(c => (
                                <button key={c.id}
                                    className={`list-group-item list-group-item-action ${selectedCall?.id === c.id ? 'active' : ''}`}
                                    onClick={() => { setSelectedCall(c); setEditingCall(null); }}>
                                    <div className="d-flex justify-content-between align-items-start">
                                        <div>
                                            <div className="fw-semibold">{c.title}</div>
                                            {c.issuer && <small className="text-muted">{c.issuer}</small>}
                                        </div>
                                        <span className={`badge bg-${callStatusColor(c.status)}`}>{t(`grants.callStatus.${c.status}`)}</span>
                                    </div>
                                    {c.deadline && <small className="d-block mt-1"><i className="bi bi-calendar me-1"></i>{c.deadline}</small>}
                                </button>
                            ))}
                            {calls.length === 0 && <p className="text-muted small p-3">{t("No grant calls yet")}</p>}
                        </div>
                    </div>

                    {/* Call detail / edit */}
                    <div className="col-md-8">
                        {selectedCall ? (
                            <div className="card">
                                <div className="card-header d-flex justify-content-between align-items-center">
                                    <h6 className="mb-0">{selectedCall.title}</h6>
                                    <div>
                                        <button className="btn btn-sm btn-outline-primary me-1" onClick={() => setEditingCall(editingCall ? null : selectedCall)}>
                                            <i className={`bi ${editingCall ? 'bi-eye' : 'bi-pencil'} me-1`}></i>
                                            {editingCall ? t("View") : t("Edit")}
                                        </button>
                                        <button className="btn btn-sm btn-outline-danger" onClick={() => handleDeleteCall(selectedCall.id)}>
                                            <i className="bi bi-trash me-1"></i>{t("Delete")}
                                        </button>
                                    </div>
                                </div>
                                <div className="card-body">
                                    {editingCall ? (
                                        <div className="row g-3">
                                            <div className="col-md-8">
                                                <label className="form-label">{t("Title")}</label>
                                                <input className="form-control" value={editingCall.title || ''} onChange={e => setEditingCall({ ...editingCall, title: e.target.value })} onBlur={() => handleUpdateCall(editingCall.id, { title: editingCall.title })} />
                                            </div>
                                            <div className="col-md-4">
                                                <label className="form-label">{t("Status")}</label>
                                                <select className="form-select" value={editingCall.status || 'IDENTIFIED'} onChange={e => { setEditingCall({ ...editingCall, status: e.target.value }); handleUpdateCall(editingCall.id, { status: e.target.value }); }}>
                                                    {CALL_STATUSES.map(s => <option key={s} value={s}>{t(`grants.callStatus.${s}`)}</option>)}
                                                </select>
                                            </div>
                                            <div className="col-md-6">
                                                <label className="form-label">{t("Issuer")}</label>
                                                <input className="form-control" value={editingCall.issuer || ''} onChange={e => setEditingCall({ ...editingCall, issuer: e.target.value })} onBlur={() => handleUpdateCall(editingCall.id, { issuer: editingCall.issuer })} />
                                            </div>
                                            <div className="col-md-3">
                                                <label className="form-label">{t("Deadline")}</label>
                                                <input type="date" className="form-control" value={editingCall.deadline || ''} onChange={e => { setEditingCall({ ...editingCall, deadline: e.target.value }); handleUpdateCall(editingCall.id, { deadline: e.target.value || null }); }} />
                                            </div>
                                            <div className="col-md-3">
                                                <label className="form-label">{t("Budget Available")}</label>
                                                <input type="number" className="form-control" value={editingCall.budgetAvailable || ''} onChange={e => setEditingCall({ ...editingCall, budgetAvailable: e.target.value })} onBlur={() => handleUpdateCall(editingCall.id, { budgetAvailable: editingCall.budgetAvailable ? Number(editingCall.budgetAvailable) : null })} />
                                            </div>
                                            <div className="col-12">
                                                <label className="form-label">{t("Description")}</label>
                                                <textarea className="form-control" rows={3} value={editingCall.description || ''} onChange={e => setEditingCall({ ...editingCall, description: e.target.value })} onBlur={() => handleUpdateCall(editingCall.id, { description: editingCall.description })} />
                                            </div>
                                            <div className="col-md-6">
                                                <label className="form-label">{t("Reference Code")}</label>
                                                <input className="form-control" value={editingCall.referenceCode || ''} onChange={e => setEditingCall({ ...editingCall, referenceCode: e.target.value })} onBlur={() => handleUpdateCall(editingCall.id, { referenceCode: editingCall.referenceCode })} />
                                            </div>
                                            <div className="col-md-6">
                                                <label className="form-label">{t("URL")}</label>
                                                <input className="form-control" value={editingCall.url || ''} onChange={e => setEditingCall({ ...editingCall, url: e.target.value })} onBlur={() => handleUpdateCall(editingCall.id, { url: editingCall.url })} />
                                            </div>
                                        </div>
                                    ) : (
                                        <>
                                            <div className="row mb-3">
                                                <div className="col-md-4"><strong>{t("Status")}:</strong> <span className={`badge bg-${callStatusColor(selectedCall.status)}`}>{t(`grants.callStatus.${selectedCall.status}`)}</span></div>
                                                <div className="col-md-4"><strong>{t("Issuer")}:</strong> {selectedCall.issuer || '-'}</div>
                                                <div className="col-md-4"><strong>{t("Deadline")}:</strong> {selectedCall.deadline || '-'}</div>
                                            </div>
                                            {selectedCall.description && <p>{selectedCall.description}</p>}
                                            <div className="row">
                                                <div className="col-md-4"><strong>{t("Reference Code")}:</strong> {selectedCall.referenceCode || '-'}</div>
                                                <div className="col-md-4"><strong>{t("Budget Available")}:</strong> {selectedCall.budgetAvailable ? `${selectedCall.budgetAvailable.toLocaleString()} ${selectedCall.currency}` : '-'}</div>
                                            </div>
                                        </>
                                    )}

                                    {/* Requirements (eligibility checklist) */}
                                    <hr className="my-4" />
                                    <div className="d-flex justify-content-between align-items-center mb-3">
                                        <h6 className="mb-0"><i className="bi bi-check2-square me-1"></i>{t("Requirements")}</h6>
                                        <button className="btn btn-sm btn-outline-primary" onClick={handleAddRequirement}>
                                            <i className="bi bi-plus me-1"></i>{t("Add")}
                                        </button>
                                    </div>
                                    {requirements.length > 0 ? (
                                        <div className="list-group">
                                            {requirements.map(r => (
                                                <div key={r.id} className="list-group-item d-flex align-items-center">
                                                    <input type="checkbox" className="form-check-input me-3" checked={!!r.isMet}
                                                        onChange={() => handleToggleRequirement(r)} />
                                                    <div className="flex-grow-1">
                                                        <span className={r.isMet ? 'text-decoration-line-through text-muted' : ''}>
                                                            {r.description}
                                                        </span>
                                                        {r.requirementType && <span className="badge bg-light text-dark ms-2">{t(`grants.reqType.${r.requirementType}`)}</span>}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    ) : (
                                        <p className="text-muted small">{t("No requirements added")}</p>
                                    )}
                                    <div className="mt-2 small text-muted">
                                        {t("Met")}: {requirements.filter(r => r.isMet).length}/{requirements.length}
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div className="text-center text-muted p-5">
                                <i className="bi bi-megaphone fs-1 d-block mb-3"></i>
                                <p>{t("Select a call to view details")}</p>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* OBLIGATIONS TAB */}
            {tab === 'obligations' && (
                <div>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h6 className="mb-0">{t("Obligations")}</h6>
                        <button className="btn btn-sm btn-primary" onClick={handleCreateObligation}>
                            <i className="bi bi-plus me-1"></i>{t("Add")}
                        </button>
                    </div>
                    {obligations.length > 0 ? (
                        <div className="table-responsive">
                            <table className="table table-hover align-middle">
                                <thead>
                                    <tr>
                                        <th>{t("Title")}</th>
                                        <th>{t("Type")}</th>
                                        <th>{t("Deadline")}</th>
                                        <th>{t("Status")}</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {obligations.map(o => (
                                        <tr key={o.id}>
                                            <td>
                                                <input className="form-control form-control-sm border-0" value={o.title}
                                                    onChange={e => setObligations(prev => prev.map(x => x.id === o.id ? { ...x, title: e.target.value } : x))}
                                                    onBlur={() => handleUpdateObligation(o.id, { title: o.title })} />
                                            </td>
                                            <td>
                                                <select className="form-select form-select-sm border-0" value={o.type || ''}
                                                    onChange={e => handleUpdateObligation(o.id, { type: e.target.value })}>
                                                    <option value="">-</option>
                                                    {OBL_TYPES.map(t2 => <option key={t2} value={t2}>{t(`grants.oblType.${t2}`)}</option>)}
                                                </select>
                                            </td>
                                            <td>
                                                <input type="date" className="form-control form-control-sm border-0" value={o.deadline || ''}
                                                    onChange={e => handleUpdateObligation(o.id, { deadline: e.target.value || null })} />
                                            </td>
                                            <td>
                                                <select className={`form-select form-select-sm border-0 text-${oblStatusColor(o.status)}`} value={o.status}
                                                    onChange={e => handleUpdateObligation(o.id, { status: e.target.value })}>
                                                    {OBL_STATUSES.map(s => <option key={s} value={s}>{t(`grants.oblStatus.${s}`)}</option>)}
                                                </select>
                                            </td>
                                            <td>
                                                <button className="btn btn-sm btn-outline-danger" onClick={async () => {
                                                    await librepm.grantObligationDelete(projectId, o.id);
                                                    setObligations(prev => prev.filter(x => x.id !== o.id));
                                                }}><i className="bi bi-trash"></i></button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p className="text-muted">{t("No obligations")}</p>
                    )}
                </div>
            )}

            {/* REPORTING PERIODS TAB */}
            {tab === 'reporting' && (
                <div>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h6 className="mb-0">{t("Reporting Periods")}</h6>
                        <button className="btn btn-sm btn-primary" onClick={handleCreatePeriod}>
                            <i className="bi bi-plus me-1"></i>{t("Add")}
                        </button>
                    </div>
                    {periods.length > 0 ? (
                        <div className="row g-3">
                            {periods.map(rp => (
                                <div className="col-md-4" key={rp.id}>
                                    <div className="card h-100">
                                        <div className="card-body">
                                            <div className="d-flex justify-content-between align-items-start mb-2">
                                                <input className="form-control form-control-sm fw-semibold border-0 p-0" value={rp.name}
                                                    onChange={e => setPeriods(prev => prev.map(x => x.id === rp.id ? { ...x, name: e.target.value } : x))}
                                                    onBlur={() => librepm.grantReportingPeriodUpdate(projectId, rp.id, { name: rp.name })} />
                                                <select className={`form-select form-select-sm ms-2 text-${rpStatusColor(rp.status)}`} style={{ width: 'auto' }}
                                                    value={rp.status}
                                                    onChange={e => {
                                                        const newStatus = e.target.value;
                                                        librepm.grantReportingPeriodUpdate(projectId, rp.id, { status: newStatus });
                                                        setPeriods(prev => prev.map(x => x.id === rp.id ? { ...x, status: newStatus } : x));
                                                    }}>
                                                    {RP_STATUSES.map(s => <option key={s} value={s}>{t(`grants.rpStatus.${s}`)}</option>)}
                                                </select>
                                            </div>
                                            <div className="small text-muted">
                                                <i className="bi bi-calendar me-1"></i>
                                                {rp.periodStart} — {rp.periodEnd}
                                            </div>
                                            {rp.dueDate && <div className="small text-muted mt-1">
                                                <i className="bi bi-clock me-1"></i>{t("Due")}: {rp.dueDate}
                                            </div>}
                                        </div>
                                        <div className="card-footer bg-transparent d-flex justify-content-end">
                                            <button className="btn btn-sm btn-outline-danger" onClick={async () => {
                                                await librepm.grantReportingPeriodDelete(projectId, rp.id);
                                                setPeriods(prev => prev.filter(x => x.id !== rp.id));
                                            }}><i className="bi bi-trash me-1"></i>{t("Delete")}</button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="text-muted">{t("No reporting periods")}</p>
                    )}
                </div>
            )}
        </div>
    );
}
