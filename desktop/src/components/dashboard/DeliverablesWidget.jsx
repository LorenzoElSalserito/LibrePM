import React, { useState } from 'react';

const DeliverablesWidget = ({ deliverables, onCreate, onDelete, onUpdate }) => {
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
                <h6 className="mb-0"><i className="bi bi-box-seam me-1"></i>Key Deliverables</h6>
                <button className="btn btn-sm btn-outline-primary py-0 px-1" onClick={() => setShowForm(!showForm)}>
                    <i className="bi bi-plus-lg"></i>
                </button>
            </div>
            {showForm && (
                <div className="card-body border-bottom bg-light py-2">
                    <input type="text" className="form-control form-control-sm mb-2" placeholder="Nome deliverable..." value={newName} onChange={e => setNewName(e.target.value)} />
                    <input type="date" className="form-control form-control-sm mb-2" value={newDueDate} onChange={e => setNewDueDate(e.target.value)} />
                    <button className="btn btn-sm btn-primary w-100" onClick={handleCreate} disabled={!newName.trim()}>Aggiungi</button>
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
                                    <option value="AT_RISK">At Risk</option>
                                    <option value="BLOCKED">Blocked</option>
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
                {deliverables.length === 0 && <li className="list-group-item text-center text-muted small py-4">Nessun deliverable definito</li>}
            </ul>
        </div>
    );
};

export default DeliverablesWidget;
