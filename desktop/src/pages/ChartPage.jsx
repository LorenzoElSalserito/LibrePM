import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import MDEditor from "@uiw/react-md-editor";
import { useModal } from '../hooks/useModal.js';

// ========================================
// Status Badge Component
// ========================================
const StatusBadge = ({ status }) => {
    const { t } = useTranslation();
    const map = {
        ON_TRACK: { label: t('On Track'), bg: 'success' },
        AT_RISK: { label: t('At Risk'), bg: 'warning' },
        DELAYED: { label: t('Delayed'), bg: 'danger' },
        BLOCKED: { label: t('Blocked'), bg: 'dark' },
    };
    const s = map[status] || { label: status || '—', bg: 'secondary' };
    return <span className={`badge bg-${s.bg}`}>{s.label}</span>;
};

// ========================================
// Severity Badge (for probability/impact)
// ========================================
const SeverityBadge = ({ level }) => {
    const map = {
        CRITICAL: 'danger',
        HIGH: 'danger',
        MEDIUM: 'warning',
        LOW: 'info',
    };
    return <span className={`badge bg-${map[level] || 'secondary'}`}>{level}</span>;
};

// ========================================
// Charter Section (Markdown editor)
// ========================================
const CharterSection = ({ title, content, onSave }) => {
    const { t } = useTranslation();
    const [isEditing, setIsEditing] = useState(false);
    const [localContent, setLocalContent] = useState(content || '');

    useEffect(() => setLocalContent(content || ''), [content]);

    return (
        <div className="card shadow-sm border mb-3">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h6 className="mb-0 fw-bold">{title}</h6>
                <button
                    className="btn btn-sm btn-link text-primary text-decoration-none"
                    onClick={() => {
                        if (isEditing) onSave(localContent);
                        setIsEditing(!isEditing);
                    }}
                >
                    {isEditing ? t('Save') : t('Edit')}
                </button>
            </div>
            <div className="card-body">
                {isEditing ? (
                    <div data-color-mode="light">
                        <MDEditor
                            value={localContent}
                            onChange={setLocalContent}
                            preview="edit"
                            height={200}
                            visibleDragbar={false}
                        />
                    </div>
                ) : (
                    <div className="markdown-body" style={{ fontSize: '0.9rem' }}>
                        {content ? (
                            <MDEditor.Markdown source={content} style={{ whiteSpace: 'pre-wrap' }} />
                        ) : (
                            <span className="text-muted fst-italic">{t('No content provided. Click Edit to add.')}</span>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

// ========================================
// Charter Info Card (sponsor, PM, objectives)
// ========================================
const CharterInfoCard = ({ charter, onSave }) => {
    const { t } = useTranslation();
    const [editing, setEditing] = useState(false);
    const [sponsor, setSponsor] = useState(charter?.sponsor || '');
    const [pm, setPm] = useState(charter?.projectManager || '');
    const [objectives, setObjectives] = useState(charter?.objectives || '');

    useEffect(() => {
        setSponsor(charter?.sponsor || '');
        setPm(charter?.projectManager || '');
        setObjectives(charter?.objectives || '');
    }, [charter]);

    const handleSave = () => {
        onSave({ sponsor, projectManager: pm, objectives });
        setEditing(false);
    };

    return (
        <div className="card shadow-sm border mb-3">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h6 className="mb-0 fw-bold">{t('Project Charter')}</h6>
                <button className="btn btn-sm btn-link text-primary text-decoration-none" onClick={() => editing ? handleSave() : setEditing(true)}>
                    {editing ? t('Save') : t('Edit')}
                </button>
            </div>
            <div className="card-body">
                {editing ? (
                    <div className="row g-3">
                        <div className="col-md-6">
                            <label className="form-label small fw-semibold">{t('Sponsor')}</label>
                            <input type="text" className="form-control form-control-sm" value={sponsor} onChange={e => setSponsor(e.target.value)} />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label small fw-semibold">{t('Project Manager')}</label>
                            <input type="text" className="form-control form-control-sm" value={pm} onChange={e => setPm(e.target.value)} />
                        </div>
                        <div className="col-12">
                            <label className="form-label small fw-semibold">{t('Objectives')}</label>
                            <textarea className="form-control form-control-sm" rows={3} value={objectives} onChange={e => setObjectives(e.target.value)} />
                        </div>
                    </div>
                ) : (
                    <div className="row g-2">
                        <div className="col-md-6">
                            <small className="text-muted d-block">{t('Sponsor')}</small>
                            <span className="fw-semibold">{charter?.sponsor || <span className="text-muted fst-italic">—</span>}</span>
                        </div>
                        <div className="col-md-6">
                            <small className="text-muted d-block">{t('Project Manager')}</small>
                            <span className="fw-semibold">{charter?.projectManager || <span className="text-muted fst-italic">—</span>}</span>
                        </div>
                        {charter?.objectives && (
                            <div className="col-12 mt-2">
                                <small className="text-muted d-block">{t('Objectives')}</small>
                                <span style={{ whiteSpace: 'pre-wrap' }}>{charter.objectives}</span>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

// ========================================
// Risk Widget (enhanced with probability + mitigation)
// ========================================
const RiskWidget = ({ risks, onCreate, onDelete, onUpdate }) => {
    const { t } = useTranslation();
    const [showForm, setShowForm] = useState(false);
    const [newDesc, setNewDesc] = useState('');
    const [newProb, setNewProb] = useState('MEDIUM');
    const [newImpact, setNewImpact] = useState('MEDIUM');
    const [newMitigation, setNewMitigation] = useState('');
    const [expandedId, setExpandedId] = useState(null);

    const handleCreate = () => {
        if (!newDesc.trim()) return;
        onCreate({ description: newDesc, probability: newProb, impact: newImpact, mitigationStrategy: newMitigation });
        setNewDesc(''); setNewMitigation(''); setShowForm(false);
    };

    return (
        <div className="card shadow-sm border h-100">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h6 className="mb-0"><i className="bi bi-exclamation-triangle me-1"></i>{t('Risks')}</h6>
                <div className="d-flex align-items-center gap-2">
                    <span className="badge bg-danger">{risks.length}</span>
                    <button className="btn btn-sm btn-outline-primary py-0 px-1" onClick={() => setShowForm(!showForm)}>
                        <i className="bi bi-plus-lg"></i>
                    </button>
                </div>
            </div>
            {showForm && (
                <div className="card-body border-bottom bg-light py-2">
                    <input type="text" className="form-control form-control-sm mb-2" placeholder={t('Risk description...')} value={newDesc} onChange={e => setNewDesc(e.target.value)} />
                    <div className="row g-1 mb-2">
                        <div className="col-6">
                            <label className="form-label small mb-0">{t('Probability')}</label>
                            <select className="form-select form-select-sm" value={newProb} onChange={e => setNewProb(e.target.value)}>
                                <option value="LOW">{t('Low')}</option>
                                <option value="MEDIUM">{t('Medium')}</option>
                                <option value="HIGH">{t('High')}</option>
                                <option value="CRITICAL">{t('Critical')}</option>
                            </select>
                        </div>
                        <div className="col-6">
                            <label className="form-label small mb-0">{t('Impact')}</label>
                            <select className="form-select form-select-sm" value={newImpact} onChange={e => setNewImpact(e.target.value)}>
                                <option value="LOW">{t('Low')}</option>
                                <option value="MEDIUM">{t('Medium')}</option>
                                <option value="HIGH">{t('High')}</option>
                                <option value="CRITICAL">{t('Critical')}</option>
                            </select>
                        </div>
                    </div>
                    <textarea className="form-control form-control-sm mb-2" rows={2} placeholder={t('Mitigation strategy...')} value={newMitigation} onChange={e => setNewMitigation(e.target.value)} />
                    <button className="btn btn-sm btn-primary w-100" onClick={handleCreate} disabled={!newDesc.trim()}>{t('Add')}</button>
                </div>
            )}
            <ul className="list-group list-group-flush flex-grow-1" style={{ maxHeight: '250px', overflowY: 'auto' }}>
                {risks.map(risk => (
                    <li key={risk.id} className="list-group-item px-3 py-2">
                        <div className="d-flex justify-content-between align-items-start">
                            <div className="flex-grow-1 me-2 cursor-pointer" onClick={() => setExpandedId(expandedId === risk.id ? null : risk.id)}>
                                <span className="text-truncate d-block" title={risk.description}>{risk.description}</span>
                                <div className="d-flex gap-1 mt-1">
                                    <small className="text-muted">{t('P')}:</small><SeverityBadge level={risk.probability} />
                                    <small className="text-muted ms-1">{t('I')}:</small><SeverityBadge level={risk.impact} />
                                </div>
                            </div>
                            <button className="btn btn-sm btn-link text-danger p-0" onClick={() => onDelete(risk.id)}>
                                <i className="bi bi-trash"></i>
                            </button>
                        </div>
                        {expandedId === risk.id && risk.mitigationStrategy && (
                            <div className="mt-2 p-2 bg-light rounded small">
                                <strong>{t('Mitigation')}:</strong> {risk.mitigationStrategy}
                            </div>
                        )}
                    </li>
                ))}
                {risks.length === 0 && <li className="list-group-item text-center text-muted small py-4">{t('No risks identified')}</li>}
            </ul>
        </div>
    );
};

// ========================================
// Deliverables Widget (progress bar + riskStatus)
// ========================================
const DeliverablesWidget = ({ deliverables, onCreate, onDelete, onUpdate }) => {
    const { t } = useTranslation();
    const [showForm, setShowForm] = useState(false);
    const [newName, setNewName] = useState('');
    const [newDueDate, setNewDueDate] = useState('');

    const handleCreate = () => {
        if (!newName.trim()) return;
        onCreate({ name: newName, dueDate: newDueDate || null, progress: 0, riskStatus: 'OK' });
        setNewName(''); setNewDueDate(''); setShowForm(false);
    };

    const riskStatusColor = (s) => ({ OK: 'success', AT_RISK: 'warning', BLOCKED: 'danger' }[s] || 'secondary');

    return (
        <div className="card shadow-sm border h-100">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h6 className="mb-0"><i className="bi bi-box-seam me-1"></i>{t('Key Deliverables')}</h6>
                <button className="btn btn-sm btn-outline-primary py-0 px-1" onClick={() => setShowForm(!showForm)}>
                    <i className="bi bi-plus-lg"></i>
                </button>
            </div>
            {showForm && (
                <div className="card-body border-bottom bg-light py-2">
                    <input type="text" className="form-control form-control-sm mb-2" placeholder={t('Deliverable name...')} value={newName} onChange={e => setNewName(e.target.value)} />
                    <input type="date" className="form-control form-control-sm mb-2" value={newDueDate} onChange={e => setNewDueDate(e.target.value)} />
                    <button className="btn btn-sm btn-primary w-100" onClick={handleCreate} disabled={!newName.trim()}>{t('Add')}</button>
                </div>
            )}
            <ul className="list-group list-group-flush flex-grow-1" style={{ maxHeight: '300px', overflowY: 'auto' }}>
                {deliverables.map(d => (
                    <li key={d.id} className="list-group-item px-3 py-2">
                        <div className="d-flex justify-content-between align-items-start mb-1">
                            <div className="flex-grow-1 me-2">
                                <span className={`d-block ${d.progress >= 100 ? 'text-decoration-line-through text-muted' : ''}`}>{d.name}</span>
                                <div className="d-flex align-items-center gap-2 mt-1">
                                    <span className={`badge bg-${riskStatusColor(d.riskStatus)} bg-opacity-75`} style={{ fontSize: '0.65rem' }}>
                                        {d.riskStatus || 'OK'}
                                    </span>
                                    {d.dueDate && <small className="text-muted"><i className="bi bi-calendar3 me-1"></i>{d.dueDate}</small>}
                                </div>
                            </div>
                            <div className="d-flex align-items-center gap-1">
                                <select className="form-select form-select-sm" style={{ width: '80px', fontSize: '0.7rem' }}
                                    value={d.riskStatus || 'OK'}
                                    onChange={e => onUpdate(d.id, { riskStatus: e.target.value })}>
                                    <option value="OK">OK</option>
                                    <option value="AT_RISK">{t('At Risk')}</option>
                                    <option value="BLOCKED">{t('Blocked')}</option>
                                </select>
                                <button className="btn btn-sm btn-link text-danger p-0" onClick={() => onDelete(d.id)}>
                                    <i className="bi bi-x"></i>
                                </button>
                            </div>
                        </div>
                        <div className="d-flex align-items-center gap-2">
                            <div className="progress flex-grow-1" style={{ height: '6px' }}>
                                <div className="progress-bar bg-primary" style={{ width: `${d.progress || 0}%` }}></div>
                            </div>
                            <input type="number" className="form-control form-control-sm" style={{ width: '55px', fontSize: '0.7rem' }}
                                min={0} max={100} value={d.progress || 0}
                                onChange={e => onUpdate(d.id, { progress: parseInt(e.target.value) || 0 })} />
                            <small className="text-muted">%</small>
                        </div>
                    </li>
                ))}
                {deliverables.length === 0 && <li className="list-group-item text-center text-muted small py-4">{t('No deliverables defined')}</li>}
            </ul>
        </div>
    );
};

// ========================================
// OKR Widget
// ========================================
const OkrWidget = ({ okrs, projectId, onReload }) => {
    const { t } = useTranslation();
    const modal = useModal();
    const [showNewOkr, setShowNewOkr] = useState(false);
    const [newObjective, setNewObjective] = useState('');
    const [expandedOkr, setExpandedOkr] = useState(null);
    const [showNewKr, setShowNewKr] = useState(null);
    const [krName, setKrName] = useState('');
    const [krTarget, setKrTarget] = useState(100);
    const [krUnit, setKrUnit] = useState('%');

    const handleCreateOkr = async () => {
        if (!newObjective.trim()) return;
        try {
            await librepm.okrsCreate(projectId, { objective: newObjective });
            setNewObjective(''); setShowNewOkr(false);
            onReload();
        } catch (e) { toast.error(t('Error creating')); }
    };

    const handleDeleteOkr = async (okrId) => {
        const confirmed = await modal.confirm({ title: t('Delete?') });
        if (!confirmed) return;
        try {
            await librepm.okrsDelete(projectId, okrId);
            onReload();
        } catch (e) { toast.error(t('Error')); }
    };

    const handleAddKr = async (okrId) => {
        if (!krName.trim()) return;
        try {
            await librepm.okrsAddKeyResult(projectId, okrId, { name: krName, targetValue: parseFloat(krTarget), currentValue: 0, unit: krUnit });
            setKrName(''); setShowNewKr(null);
            onReload();
        } catch (e) { toast.error(t('Error creating')); }
    };

    const handleUpdateKrValue = async (okrId, metricId, currentValue) => {
        try {
            await librepm.okrsUpdateKeyResult(projectId, okrId, metricId, { currentValue: parseFloat(currentValue) });
            onReload();
        } catch (e) { toast.error(t('Error saving')); }
    };

    const handleDeleteKr = async (okrId, metricId) => {
        try {
            await librepm.okrsDeleteKeyResult(projectId, okrId, metricId);
            onReload();
        } catch (e) { toast.error(t('Error')); }
    };

    return (
        <div className="card shadow-sm border mb-3">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h6 className="mb-0"><i className="bi bi-bullseye me-1"></i>{t('OKRs')}</h6>
                <button className="btn btn-sm btn-outline-primary py-0 px-1" onClick={() => setShowNewOkr(!showNewOkr)}>
                    <i className="bi bi-plus-lg"></i>
                </button>
            </div>
            {showNewOkr && (
                <div className="card-body border-bottom bg-light py-2">
                    <div className="input-group input-group-sm">
                        <input type="text" className="form-control" placeholder={t('Objective...')} value={newObjective} onChange={e => setNewObjective(e.target.value)}
                            onKeyDown={e => { if (e.key === 'Enter') handleCreateOkr(); }} />
                        <button className="btn btn-primary" onClick={handleCreateOkr} disabled={!newObjective.trim()}>{t('Add')}</button>
                    </div>
                </div>
            )}
            <div className="list-group list-group-flush" style={{ maxHeight: '400px', overflowY: 'auto' }}>
                {okrs.map(okr => (
                    <div key={okr.id} className="list-group-item p-2">
                        <div className="d-flex justify-content-between align-items-start">
                            <div className="flex-grow-1 cursor-pointer" onClick={() => setExpandedOkr(expandedOkr === okr.id ? null : okr.id)}>
                                <div className="fw-semibold small">{okr.objective}</div>
                                <div className="d-flex align-items-center gap-2 mt-1">
                                    <div className="progress flex-grow-1" style={{ height: '5px', maxWidth: '120px' }}>
                                        <div className={`progress-bar ${okr.overallProgress >= 100 ? 'bg-success' : okr.overallProgress >= 50 ? 'bg-primary' : 'bg-warning'}`}
                                            style={{ width: `${Math.min(100, okr.overallProgress || 0)}%` }}></div>
                                    </div>
                                    <small className="text-muted">{Math.round(okr.overallProgress || 0)}%</small>
                                </div>
                            </div>
                            <div className="d-flex gap-1">
                                <button className="btn btn-sm btn-link text-primary p-0" onClick={() => setShowNewKr(showNewKr === okr.id ? null : okr.id)} title={t('Add Key Result')}>
                                    <i className="bi bi-plus-circle"></i>
                                </button>
                                <button className="btn btn-sm btn-link text-danger p-0" onClick={() => handleDeleteOkr(okr.id)}>
                                    <i className="bi bi-trash"></i>
                                </button>
                            </div>
                        </div>

                        {/* Add Key Result form */}
                        {showNewKr === okr.id && (
                            <div className="mt-2 p-2 bg-light rounded">
                                <div className="row g-1 mb-1">
                                    <div className="col-5">
                                        <input type="text" className="form-control form-control-sm" placeholder={t('Key Result name')} value={krName} onChange={e => setKrName(e.target.value)} />
                                    </div>
                                    <div className="col-3">
                                        <input type="number" className="form-control form-control-sm" placeholder={t('Target')} value={krTarget} onChange={e => setKrTarget(e.target.value)} />
                                    </div>
                                    <div className="col-2">
                                        <input type="text" className="form-control form-control-sm" placeholder={t('Unit')} value={krUnit} onChange={e => setKrUnit(e.target.value)} />
                                    </div>
                                    <div className="col-2">
                                        <button className="btn btn-sm btn-primary w-100" onClick={() => handleAddKr(okr.id)} disabled={!krName.trim()}>{t('Add')}</button>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Expanded Key Results */}
                        {expandedOkr === okr.id && okr.keyResults && (
                            <div className="mt-2">
                                {okr.keyResults.map(kr => (
                                    <div key={kr.id} className="d-flex align-items-center gap-2 py-1 border-top">
                                        <small className="flex-grow-1 text-muted">{kr.name}</small>
                                        <input type="number" className="form-control form-control-sm" style={{ width: '65px', fontSize: '0.7rem' }}
                                            value={kr.currentValue || 0}
                                            onBlur={e => handleUpdateKrValue(okr.id, kr.id, e.target.value)}
                                            onChange={e => {/* controlled locally, save on blur */}} />
                                        <small className="text-muted">/ {kr.targetValue} {kr.unit}</small>
                                        <div className="progress" style={{ width: '60px', height: '4px' }}>
                                            <div className="progress-bar bg-success" style={{ width: `${Math.min(100, kr.achievementPercentage || 0)}%` }}></div>
                                        </div>
                                        <button className="btn btn-link btn-sm text-danger p-0" onClick={() => handleDeleteKr(okr.id, kr.id)}>
                                            <i className="bi bi-x-circle" style={{ fontSize: '0.7rem' }}></i>
                                        </button>
                                    </div>
                                ))}
                                {(!okr.keyResults || okr.keyResults.length === 0) && (
                                    <small className="text-muted fst-italic d-block mt-1">{t('No key results. Click + to add.')}</small>
                                )}
                            </div>
                        )}
                    </div>
                ))}
                {okrs.length === 0 && (
                    <div className="list-group-item text-center text-muted small py-4">
                        <i className="bi bi-bullseye d-block fs-4 mb-1"></i>
                        {t('No OKRs defined. Add objectives to track progress.')}
                    </div>
                )}
            </div>
        </div>
    );
};

// ========================================
// Baseline & Variance Widget
// ========================================
const BaselineWidget = ({ projectId, variance, onReload }) => {
    const { t } = useTranslation();
    const modal = useModal();
    const [baselines, setBaselines] = useState([]);
    const [showCreate, setShowCreate] = useState(false);
    const [newName, setNewName] = useState('');
    const [selectedVariance, setSelectedVariance] = useState(null);
    const [loadingVariance, setLoadingVariance] = useState(false);

    useEffect(() => {
        loadBaselines();
    }, [projectId]);

    const loadBaselines = async () => {
        try {
            const data = await librepm.baselinesList(projectId);
            setBaselines(data || []);
        } catch (e) { /* ignore */ }
    };

    const handleCreate = async () => {
        if (!newName.trim()) return;
        try {
            await librepm.baselinesCreate(projectId, { name: newName });
            setNewName(''); setShowCreate(false);
            loadBaselines();
            onReload();
            toast.success(t('Created'));
        } catch (e) { toast.error(t('Error creating')); }
    };

    const handleDelete = async (id) => {
        const confirmed = await modal.confirm({ title: t('Delete?') });
        if (!confirmed) return;
        try {
            await librepm.baselinesDelete(projectId, id);
            loadBaselines();
            onReload();
        } catch (e) { toast.error(t('Error')); }
    };

    const handleViewVariance = async (baselineId) => {
        if (selectedVariance?.baselineId === baselineId) { setSelectedVariance(null); return; }
        setLoadingVariance(true);
        try {
            const v = await librepm.baselinesVariance(projectId, baselineId);
            setSelectedVariance(v);
        } catch (e) { toast.error(t('Error')); }
        setLoadingVariance(false);
    };

    return (
        <div className="card shadow-sm border mb-3">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h6 className="mb-0"><i className="bi bi-bookmarks me-1"></i>{t('Baselines')}</h6>
                <button className="btn btn-sm btn-outline-primary py-0 px-1" onClick={() => setShowCreate(!showCreate)}>
                    <i className="bi bi-plus-lg"></i>
                </button>
            </div>

            {/* Latest Variance Summary */}
            {variance && (
                <div className="card-body border-bottom py-2">
                    <div className="d-flex justify-content-between align-items-center">
                        <small className="text-muted">{t('Latest Variance')}: <strong>{variance.baselineName}</strong></small>
                        <StatusBadge status={variance.projectStatus} />
                    </div>
                    <div className="row g-2 mt-1">
                        <div className="col-6">
                            <small className="text-muted d-block">{t('Schedule Variance')}</small>
                            <span className={`fw-bold ${variance.scheduleVarianceDays > 0 ? 'text-danger' : variance.scheduleVarianceDays < 0 ? 'text-success' : ''}`}>
                                {variance.scheduleVarianceDays > 0 ? '+' : ''}{variance.scheduleVarianceDays || 0} {t('days')}
                            </span>
                        </div>
                        <div className="col-6">
                            <small className="text-muted d-block">{t('Effort Variance')}</small>
                            <span className={`fw-bold ${variance.effortVarianceMinutes > 0 ? 'text-danger' : variance.effortVarianceMinutes < 0 ? 'text-success' : ''}`}>
                                {variance.effortVarianceMinutes > 0 ? '+' : ''}{Math.round((variance.effortVarianceMinutes || 0) / 60)}h
                            </span>
                        </div>
                    </div>
                </div>
            )}

            {showCreate && (
                <div className="card-body border-bottom bg-light py-2">
                    <div className="input-group input-group-sm">
                        <input type="text" className="form-control" placeholder={t('Baseline name...')} value={newName} onChange={e => setNewName(e.target.value)}
                            onKeyDown={e => { if (e.key === 'Enter') handleCreate(); }} />
                        <button className="btn btn-primary" onClick={handleCreate} disabled={!newName.trim()}>{t('Create')}</button>
                    </div>
                </div>
            )}
            <ul className="list-group list-group-flush" style={{ maxHeight: '200px', overflowY: 'auto' }}>
                {baselines.map(b => (
                    <li key={b.id} className="list-group-item px-3 py-2">
                        <div className="d-flex justify-content-between align-items-center">
                            <div className="cursor-pointer flex-grow-1" onClick={() => handleViewVariance(b.id)}>
                                <small className="fw-semibold">{b.name}</small>
                                <small className="text-muted d-block">{b.snapshotDate ? new Date(b.snapshotDate).toLocaleDateString() : ''} - {b.taskSnapshots?.length || 0} {t('tasks')}</small>
                            </div>
                            <button className="btn btn-sm btn-link text-danger p-0" onClick={() => handleDelete(b.id)}>
                                <i className="bi bi-trash"></i>
                            </button>
                        </div>
                        {selectedVariance?.baselineId === b.id && (
                            <div className="mt-2 p-2 bg-light rounded small">
                                <div className="row g-1">
                                    <div className="col-6"><strong>{t('Schedule')}:</strong> {selectedVariance.scheduleVarianceDays > 0 ? '+' : ''}{selectedVariance.scheduleVarianceDays} {t('days')}</div>
                                    <div className="col-6"><strong>{t('Effort')}:</strong> {Math.round((selectedVariance.effortVarianceMinutes || 0) / 60)}h</div>
                                </div>
                                <div className="mt-1"><StatusBadge status={selectedVariance.projectStatus} /></div>
                            </div>
                        )}
                    </li>
                ))}
                {baselines.length === 0 && <li className="list-group-item text-center text-muted small py-3">{t('No baselines. Create a snapshot to track variance.')}</li>}
            </ul>
            {loadingVariance && <div className="card-body text-center py-2"><div className="spinner-border spinner-border-sm text-primary"></div></div>}
        </div>
    );
};

// ========================================
// Summary Stats Row
// ========================================
const StatsRow = ({ dashboard }) => {
    const { t } = useTranslation();
    const stats = [
        { label: t('Completion'), value: `${Math.round(dashboard.completionPercentage || 0)}%`, icon: 'bi-check-circle', color: 'primary' },
        { label: t('Overdue'), value: dashboard.overdueTaskCount || 0, icon: 'bi-exclamation-circle', color: dashboard.overdueTaskCount > 0 ? 'danger' : 'success' },
        { label: t('Deliverables'), value: `${dashboard.completedDeliverables || 0}/${dashboard.totalDeliverables || 0}`, icon: 'bi-box-seam', color: 'info' },
        { label: t('High Risks'), value: dashboard.highImpactRisks || 0, icon: 'bi-shield-exclamation', color: dashboard.highImpactRisks > 0 ? 'warning' : 'success' },
    ];

    return (
        <div className="row g-3 mb-4">
            {stats.map((s, i) => (
                <div className="col-md-3 col-6" key={i}>
                    <div className="card shadow-sm border h-100">
                        <div className="card-body d-flex align-items-center gap-3 py-3">
                            <div className={`rounded-circle bg-${s.color} bg-opacity-10 d-flex align-items-center justify-content-center`} style={{ width: 44, height: 44 }}>
                                <i className={`bi ${s.icon} fs-5 text-${s.color}`}></i>
                            </div>
                            <div>
                                <div className="fw-bold fs-5">{s.value}</div>
                                <small className="text-muted">{s.label}</small>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

// ========================================
// ChartPage - Executive Dashboard & Project Charter
// Implements PRD-17
// ========================================
export default function ChartPage({ shell }) {
    const { t } = useTranslation();
    const modal = useModal();
    const [loading, setLoading] = useState(true);
    const [dashboard, setDashboard] = useState({});
    const [charter, setCharter] = useState({});
    const [risks, setRisks] = useState([]);
    const [deliverables, setDeliverables] = useState([]);
    const [okrs, setOkrs] = useState([]);
    const [variance, setVariance] = useState(null);

    const loadData = useCallback(async () => {
        if (!shell?.currentProject) {
            setLoading(false);
            return;
        }
        try {
            setLoading(true);
            const projectId = shell.currentProject.id;
            const data = await librepm.executiveDashboard(projectId);
            setDashboard(data || {});
            setCharter(data?.charter || {});
            setRisks(data?.risks || []);
            setDeliverables(data?.deliverables || []);
            setOkrs(data?.okrs || []);
            setVariance(data?.latestVariance || null);
        } catch (e) {
            console.error("Error loading dashboard", e);
            // Fallback: load individual endpoints
            try {
                const projectId = shell.currentProject.id;
                const [charterData, risksList, deliverablesList, okrsList] = await Promise.all([
                    librepm.projectCharterGet(projectId).catch(() => ({})),
                    librepm.projectRisksList(projectId).catch(() => []),
                    librepm.projectDeliverablesList(projectId).catch(() => []),
                    librepm.okrsList(projectId).catch(() => []),
                ]);
                setCharter(charterData || {});
                setRisks(risksList || []);
                setDeliverables(deliverablesList || []);
                setOkrs(okrsList || []);
                const latestVar = await librepm.baselinesLatestVariance(projectId).catch(() => null);
                setVariance(latestVar);
            } catch (fallbackErr) {
                toast.error(t("Error loading dashboard"));
            }
        } finally {
            setLoading(false);
        }
    }, [shell?.currentProject, t]);

    // Load data when project changes
    useEffect(() => {
        loadData();
    }, [loadData]);

    useEffect(() => {
        shell?.setTitle?.(t("Executive Dashboard"));
        shell?.setHeaderActions?.(
            <div className="d-flex gap-2 align-items-center">
                <select
                    className="form-select form-select-sm"
                    value={shell?.currentProject?.id || "__SELECT__"}
                    onChange={(e) => {
                        if (e.target.value === "__SELECT__") {
                            shell?.navigate?.("menu");
                        } else {
                            const project = shell?.projects?.find(p => p.id === e.target.value);
                            shell?.setCurrentProject?.(project);
                        }
                    }}
                    style={{ width: 200 }}
                >
                    <option value="__SELECT__">{t("Select project...")}</option>
                    {shell?.projects?.map((p) => (
                        <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                </select>
                <button
                    className="btn btn-sm btn-outline-secondary"
                    onClick={loadData}
                    title={t("Refresh")}
                >
                    <i className="bi bi-arrow-clockwise"></i>
                </button>
            </div>
        );

        return () => {
            shell?.setHeaderActions?.(null);
        };
    }, [shell, t, loadData]);

    const projectId = shell?.currentProject?.id;

    // Charter handlers
    const handleUpdateCharter = async (field, value) => {
        try {
            const updated = { ...charter, [field]: value };
            await librepm.projectCharterUpdate(projectId, updated);
            setCharter(updated);
            toast.success(t("Saved"));
        } catch (e) { toast.error(t("Error saving")); }
    };

    const handleUpdateCharterFields = async (fields) => {
        try {
            const updated = { ...charter, ...fields };
            await librepm.projectCharterUpdate(projectId, updated);
            setCharter(updated);
            toast.success(t("Saved"));
        } catch (e) { toast.error(t("Error saving")); }
    };

    // Risk handlers
    const handleCreateRisk = async (data) => {
        try {
            await librepm.projectRisksCreate(projectId, data);
            loadData();
        } catch (e) { toast.error(t("Error creating")); }
    };

    const handleDeleteRisk = async (id) => {
        const confirmed = await modal.confirm({ title: t("Delete?") });
        if (!confirmed) return;
        await librepm.projectRisksDelete(projectId, id).catch(() => {});
        loadData();
    };

    // Deliverable handlers
    const handleCreateDeliverable = async (data) => {
        try {
            await librepm.projectDeliverablesCreate(projectId, data);
            loadData();
        } catch (e) { toast.error(t("Error creating")); }
    };

    const handleDeleteDeliverable = async (id) => {
        const confirmed = await modal.confirm({ title: t("Delete?") });
        if (!confirmed) return;
        await librepm.projectDeliverablesDelete(projectId, id).catch(() => {});
        loadData();
    };

    const handleUpdateDeliverable = async (id, data) => {
        try {
            await librepm.projectDeliverablesUpdate(projectId, id, data);
            loadData();
        } catch (e) { toast.error(t("Error saving")); }
    };

    if (!shell?.currentProject) return <div className="text-center py-5 text-muted">{t("Select a project")}</div>;
    if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>;

    return (
        <div className="container-fluid p-4 h-100 overflow-auto bg-light">
            {/* Header */}
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div className="d-flex align-items-center gap-3">
                    <div>
                        <h4 className="mb-0">{shell.currentProject.name}</h4>
                        <p className="text-muted small mb-0">{t("Executive Dashboard & Charter")}</p>
                    </div>
                    {dashboard.projectStatus && <StatusBadge status={dashboard.projectStatus} />}
                </div>
                <button className="btn btn-outline-secondary btn-sm" onClick={loadData}>
                    <i className="bi bi-arrow-clockwise me-1"></i> {t("Refresh")}
                </button>
            </div>

            {/* Stats Row */}
            <StatsRow dashboard={dashboard} />

            <div className="row g-3">
                {/* Left Column */}
                <div className="col-lg-8">
                    {/* Charter Info (Sponsor, PM, Objectives) */}
                    <CharterInfoCard charter={charter} onSave={handleUpdateCharterFields} />

                    {/* Problem Statement */}
                    <CharterSection
                        title={t("Goals / Problem Statement")}
                        content={charter.problemStatement}
                        onSave={(val) => handleUpdateCharter('problemStatement', val)}
                    />

                    {/* Business Case */}
                    <CharterSection
                        title={t("Business Case")}
                        content={charter.businessCase}
                        onSave={(val) => handleUpdateCharter('businessCase', val)}
                    />

                    {/* OKRs */}
                    <OkrWidget okrs={okrs} projectId={projectId} onReload={loadData} />

                    {/* Baselines & Variance */}
                    <BaselineWidget projectId={projectId} variance={variance} onReload={loadData} />
                </div>

                {/* Right Column */}
                <div className="col-lg-4">
                    {/* Risks */}
                    <div className="mb-3">
                        <RiskWidget
                            risks={risks}
                            onCreate={handleCreateRisk}
                            onDelete={handleDeleteRisk}
                        />
                    </div>

                    {/* Deliverables */}
                    <div className="mb-3">
                        <DeliverablesWidget
                            deliverables={deliverables}
                            onCreate={handleCreateDeliverable}
                            onDelete={handleDeleteDeliverable}
                            onUpdate={handleUpdateDeliverable}
                        />
                    </div>

                    {/* Team Summary */}
                    <div className="card shadow-sm border mb-3">
                        <div className="card-header bg-white d-flex justify-content-between align-items-center">
                            <h6 className="mb-0"><i className="bi bi-people me-1"></i>{t("Team")}</h6>
                            <button className="btn btn-sm btn-link" onClick={() => shell.navigate('team')}>
                                {t("Manage")}
                            </button>
                        </div>
                        <div className="card-body py-2">
                            {(charter?.sponsor || charter?.projectManager) ? (
                                <div className="row g-1">
                                    {charter.sponsor && (
                                        <div className="col-12">
                                            <small className="text-muted">{t('Sponsor')}:</small> <span className="fw-semibold small">{charter.sponsor}</span>
                                        </div>
                                    )}
                                    {charter.projectManager && (
                                        <div className="col-12">
                                            <small className="text-muted">{t('Project Manager')}:</small> <span className="fw-semibold small">{charter.projectManager}</span>
                                        </div>
                                    )}
                                    {dashboard.overbookedUsersCount > 0 && (
                                        <div className="col-12 mt-1">
                                            <span className="badge bg-warning">{dashboard.overbookedUsersCount} {t('overbooked')}</span>
                                        </div>
                                    )}
                                </div>
                            ) : (
                                <div className="text-center text-muted small fst-italic py-2">
                                    {t("View Team page for full roster")}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Quick Links */}
                    <div className="card shadow-sm border">
                        <div className="card-body py-2">
                            <button className="btn btn-sm btn-outline-primary w-100 mb-1" onClick={() => shell.navigate('gantt')}>
                                <i className="bi bi-bar-chart-steps me-1"></i> {t("Go to Gantt Chart")}
                            </button>
                            <button className="btn btn-sm btn-outline-secondary w-100" onClick={() => shell.navigate('resources')}>
                                <i className="bi bi-people-fill me-1"></i> {t("Resources & Workload")}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
