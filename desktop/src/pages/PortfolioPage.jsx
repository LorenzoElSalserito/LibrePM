import { useState, useEffect, useCallback } from 'react';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

/**
 * PortfolioPage — Manages programmes, project memberships, and milestones.
 */
export default function PortfolioPage({ shell }) {
    const { t } = useTranslation();
    const [programmes, setProgrammes] = useState([]);
    const [selectedProg, setSelectedProg] = useState(null);
    const [overview, setOverview] = useState(null);
    const [milestones, setMilestones] = useState([]);
    const [projects, setProjects] = useState([]);
    const [allProjects, setAllProjects] = useState([]);

    useEffect(() => { shell?.setTitle?.(t("Portfolio")); }, []);

    const loadProgrammes = useCallback(async () => {
        try { setProgrammes(await librepm.programmesList() || []); } catch (e) { console.error(e); }
    }, []);

    const loadAllProjects = useCallback(async () => {
        try { setAllProjects(await librepm.projectsList() || []); } catch (e) { console.error(e); }
    }, []);

    useEffect(() => { loadProgrammes(); loadAllProjects(); }, [loadProgrammes, loadAllProjects]);

    const loadOverview = useCallback(async () => {
        if (!selectedProg) return;
        try {
            const ov = await librepm.programmeOverview(selectedProg.id);
            setOverview(ov);
            setMilestones(ov?.milestones || []);
            setProjects(ov?.projects || []);
        } catch (e) { console.error(e); }
    }, [selectedProg]);

    useEffect(() => { loadOverview(); }, [loadOverview]);

    const handleCreateProgramme = async () => {
        try {
            const p = await librepm.programmeCreate({ name: t("New programme"), ownerId: shell?.currentUser?.id });
            setProgrammes(prev => [...prev, p]);
            setSelectedProg(p);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleUpdateProgramme = async (progId, data) => {
        try {
            const updated = await librepm.programmeUpdate(progId, data);
            setProgrammes(prev => prev.map(p => p.id === progId ? updated : p));
            if (selectedProg?.id === progId) setSelectedProg(updated);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleAddProject = async (projectId) => {
        if (!selectedProg) return;
        try {
            await librepm.programmeProjectAdd(selectedProg.id, { projectId });
            loadOverview();
            toast.success(t("Project added"));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleAddMilestone = async () => {
        if (!selectedProg) return;
        try {
            const ms = await librepm.programmeMilestoneCreate(selectedProg.id, { name: t("New milestone") });
            setMilestones(prev => [...prev, ms]);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const progStatusColor = (status) => {
        const colors = { ACTIVE: 'success', ON_HOLD: 'warning', COMPLETED: 'primary', ARCHIVED: 'secondary' };
        return colors[status] || 'secondary';
    };

    const msStatusColor = (status) => {
        const colors = { PENDING: 'secondary', IN_PROGRESS: 'info', COMPLETED: 'success', OVERDUE: 'danger' };
        return colors[status] || 'secondary';
    };

    const PROG_STATUSES = ['ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED'];
    const MS_STATUSES = ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE'];

    // Projects not yet in this programme
    const availableProjects = allProjects.filter(ap => !projects.find(p => p.id === ap.id));

    return (
        <div className="container-fluid p-4">
            <div className="row">
                {/* Programme list */}
                <div className="col-md-3">
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h6 className="mb-0">{t("Programmes")}</h6>
                        <button className="btn btn-sm btn-primary" onClick={handleCreateProgramme}>
                            <i className="bi bi-plus me-1"></i>{t("New")}
                        </button>
                    </div>
                    <div className="list-group">
                        {programmes.map(p => (
                            <button key={p.id}
                                className={`list-group-item list-group-item-action ${selectedProg?.id === p.id ? 'active' : ''}`}
                                onClick={() => setSelectedProg(p)}>
                                <div className="d-flex justify-content-between align-items-center">
                                    <span className="fw-semibold">{p.name}</span>
                                    <span className={`badge bg-${progStatusColor(p.status)}`}>{t(`portfolio.progStatus.${p.status}`)}</span>
                                </div>
                            </button>
                        ))}
                        {programmes.length === 0 && <p className="text-muted small p-3">{t("No programmes")}</p>}
                    </div>
                </div>

                {/* Programme detail */}
                <div className="col-md-9">
                    {selectedProg ? (
                        <div>
                            {/* Header */}
                            <div className="d-flex justify-content-between align-items-center mb-4">
                                <div className="d-flex align-items-center gap-3">
                                    <input className="form-control form-control-lg fw-bold border-0 p-0" value={selectedProg.name}
                                        onChange={e => setSelectedProg({ ...selectedProg, name: e.target.value })}
                                        onBlur={() => handleUpdateProgramme(selectedProg.id, { name: selectedProg.name })} />
                                    <select className={`form-select form-select-sm text-${progStatusColor(selectedProg.status)}`} style={{ width: 'auto' }}
                                        value={selectedProg.status}
                                        onChange={e => handleUpdateProgramme(selectedProg.id, { status: e.target.value })}>
                                        {PROG_STATUSES.map(s => <option key={s} value={s}>{t(`portfolio.progStatus.${s}`)}</option>)}
                                    </select>
                                </div>
                                <button className="btn btn-sm btn-outline-danger" onClick={async () => {
                                    await librepm.programmeDelete(selectedProg.id);
                                    setProgrammes(prev => prev.filter(p => p.id !== selectedProg.id));
                                    setSelectedProg(null);
                                }}><i className="bi bi-trash me-1"></i>{t("Delete")}</button>
                            </div>

                            {/* Overview stats */}
                            {overview && (
                                <div className="row g-3 mb-4">
                                    <div className="col-md-3">
                                        <div className="card text-center">
                                            <div className="card-body py-3">
                                                <div className="fs-3 fw-bold text-primary">{overview.projectCount}</div>
                                                <div className="text-muted small">{t("Projects")}</div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-md-3">
                                        <div className="card text-center">
                                            <div className="card-body py-3">
                                                <div className="fs-3 fw-bold text-success">{overview.milestonesCompleted}</div>
                                                <div className="text-muted small">{t("Milestones Completed")}</div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-md-3">
                                        <div className="card text-center">
                                            <div className="card-body py-3">
                                                <div className="fs-3 fw-bold">{overview.milestonesTotal}</div>
                                                <div className="text-muted small">{t("Total Milestones")}</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* Projects in programme */}
                            <div className="card mb-4">
                                <div className="card-header d-flex justify-content-between align-items-center">
                                    <h6 className="mb-0"><i className="bi bi-folder2-open me-2"></i>{t("Projects")}</h6>
                                    {availableProjects.length > 0 && (
                                        <select className="form-select form-select-sm" style={{ width: 'auto' }}
                                            value="" onChange={e => { if (e.target.value) handleAddProject(e.target.value); }}>
                                            <option value="">{t("Add project...")}</option>
                                            {availableProjects.map(ap => <option key={ap.id} value={ap.id}>{ap.name}</option>)}
                                        </select>
                                    )}
                                </div>
                                <div className="card-body">
                                    {projects.length > 0 ? (
                                        <div className="row g-3">
                                            {projects.map(p => (
                                                <div className="col-md-4" key={p.id}>
                                                    <div className="card h-100">
                                                        <div className="card-body py-2 px-3">
                                                            <div className="fw-semibold">{p.name}</div>
                                                            <div className="small text-muted">{t(`portfolio.progStatus.${p.status}`)}</div>
                                                            {p.startDate && <div className="small text-muted">{p.startDate} — {p.endDate || '...'}</div>}
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    ) : (
                                        <p className="text-muted mb-0">{t("No projects in this programme")}</p>
                                    )}
                                </div>
                            </div>

                            {/* Milestones */}
                            <div className="card">
                                <div className="card-header d-flex justify-content-between align-items-center">
                                    <h6 className="mb-0"><i className="bi bi-flag me-2"></i>{t("Milestones")}</h6>
                                    <button className="btn btn-sm btn-outline-primary" onClick={handleAddMilestone}>
                                        <i className="bi bi-plus me-1"></i>{t("Add")}
                                    </button>
                                </div>
                                <div className="card-body">
                                    {milestones.length > 0 ? (
                                        <div className="table-responsive">
                                            <table className="table table-hover align-middle mb-0">
                                                <thead>
                                                    <tr>
                                                        <th>{t("Name")}</th>
                                                        <th>{t("Target Date")}</th>
                                                        <th>{t("Status")}</th>
                                                        <th></th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {milestones.map(ms => (
                                                        <tr key={ms.id}>
                                                            <td>
                                                                <input className="form-control form-control-sm border-0" value={ms.name || ''}
                                                                    onChange={e => setMilestones(prev => prev.map(x => x.id === ms.id ? { ...x, name: e.target.value } : x))}
                                                                    onBlur={() => librepm.programmeMilestoneUpdate(selectedProg.id, ms.id, { name: ms.name })} />
                                                            </td>
                                                            <td>
                                                                <input type="date" className="form-control form-control-sm border-0" value={ms.targetDate || ''}
                                                                    onChange={e => {
                                                                        const v = e.target.value;
                                                                        librepm.programmeMilestoneUpdate(selectedProg.id, ms.id, { targetDate: v || null });
                                                                        setMilestones(prev => prev.map(x => x.id === ms.id ? { ...x, targetDate: v } : x));
                                                                    }} />
                                                            </td>
                                                            <td>
                                                                <select className={`form-select form-select-sm border-0 text-${msStatusColor(ms.status)}`}
                                                                    value={ms.status}
                                                                    onChange={e => {
                                                                        const v = e.target.value;
                                                                        librepm.programmeMilestoneUpdate(selectedProg.id, ms.id, { status: v });
                                                                        setMilestones(prev => prev.map(x => x.id === ms.id ? { ...x, status: v } : x));
                                                                    }}>
                                                                    {MS_STATUSES.map(s => <option key={s} value={s}>{t(`portfolio.msStatus.${s}`)}</option>)}
                                                                </select>
                                                            </td>
                                                            <td>
                                                                <button className="btn btn-sm btn-outline-danger" onClick={async () => {
                                                                    await librepm.programmeMilestoneDelete(selectedProg.id, ms.id);
                                                                    setMilestones(prev => prev.filter(x => x.id !== ms.id));
                                                                }}><i className="bi bi-trash"></i></button>
                                                            </td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    ) : (
                                        <p className="text-muted mb-0">{t("No milestones")}</p>
                                    )}
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center text-muted p-5">
                            <i className="bi bi-collection fs-1 d-block mb-3"></i>
                            <p>{t("Select a programme to view details")}</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
