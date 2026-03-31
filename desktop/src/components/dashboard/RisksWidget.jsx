import React, { useState } from 'react';
import { toast } from 'react-toastify';

const SeverityBadge = ({ level }) => {
    const map = {
        CRITICAL: 'danger',
        HIGH: 'danger',
        MEDIUM: 'warning',
        LOW: 'info',
    };
    return <span className={`badge bg-${map[level] || 'secondary'}`}>{level}</span>;
};

const RisksWidget = ({ risks, onCreate, onDelete }) => {
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
                <h6 className="mb-0"><i className="bi bi-exclamation-triangle me-1"></i>Rischi</h6>
                <div className="d-flex align-items-center gap-2">
                    <span className="badge bg-danger">{risks.length}</span>
                    <button className="btn btn-sm btn-outline-primary py-0 px-1" onClick={() => setShowForm(!showForm)}>
                        <i className="bi bi-plus-lg"></i>
                    </button>
                </div>
            </div>
            {showForm && (
                <div className="card-body border-bottom bg-light py-2">
                    <input type="text" className="form-control form-control-sm mb-2" placeholder="Descrizione rischio..." value={newDesc} onChange={e => setNewDesc(e.target.value)} />
                    <div className="row g-1 mb-2">
                        <div className="col-6">
                            <label className="form-label small mb-0">Probabilità</label>
                            <select className="form-select form-select-sm" value={newProb} onChange={e => setNewProb(e.target.value)}>
                                <option value="LOW">Bassa</option>
                                <option value="MEDIUM">Media</option>
                                <option value="HIGH">Alta</option>
                                <option value="CRITICAL">Critica</option>
                            </select>
                        </div>
                        <div className="col-6">
                            <label className="form-label small mb-0">Impatto</label>
                            <select className="form-select form-select-sm" value={newImpact} onChange={e => setNewImpact(e.target.value)}>
                                <option value="LOW">Basso</option>
                                <option value="MEDIUM">Medio</option>
                                <option value="HIGH">Alto</option>
                                <option value="CRITICAL">Critico</option>
                            </select>
                        </div>
                    </div>
                    <textarea className="form-control form-control-sm mb-2" rows={2} placeholder="Strategia di mitigazione..." value={newMitigation} onChange={e => setNewMitigation(e.target.value)} />
                    <button className="btn btn-sm btn-primary w-100" onClick={handleCreate} disabled={!newDesc.trim()}>Aggiungi</button>
                </div>
            )}
            <ul className="list-group list-group-flush flex-grow-1" style={{ maxHeight: '250px', overflowY: 'auto' }}>
                {risks.map(risk => (
                    <li key={risk.id} className="list-group-item px-3 py-2">
                        <div className="d-flex justify-content-between align-items-start">
                            <div className="flex-grow-1 me-2 cursor-pointer" onClick={() => setExpandedId(expandedId === risk.id ? null : risk.id)}>
                                <span className="text-truncate d-block" title={risk.description}>{risk.description}</span>
                                <div className="d-flex gap-1 mt-1">
                                    <small className="text-muted">P:</small><SeverityBadge level={risk.probability} />
                                    <small className="text-muted ms-1">I:</small><SeverityBadge level={risk.impact} />
                                </div>
                            </div>
                            <button className="btn btn-sm btn-link text-danger p-0" onClick={() => onDelete(risk.id)}>
                                <i className="bi bi-trash"></i>
                            </button>
                        </div>
                        {expandedId === risk.id && risk.mitigationStrategy && (
                            <div className="mt-2 p-2 bg-light rounded small">
                                <strong>Mitigazione:</strong> {risk.mitigationStrategy}
                            </div>
                        )}
                    </li>
                ))}
                {risks.length === 0 && <li className="list-group-item text-center text-muted small py-4">Nessun rischio identificato</li>}
            </ul>
        </div>
    );
};

export default RisksWidget;
