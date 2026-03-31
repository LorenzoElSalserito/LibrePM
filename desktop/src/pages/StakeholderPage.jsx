import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

/**
 * StakeholderPage — Manages stakeholder map, sponsors, donors, and partners.
 * Tabs: Stakeholder Map, Sponsors, Donors, Partners.
 */
export default function StakeholderPage({ shell }) {
    const { t } = useTranslation();
    const projectId = shell?.currentProject?.id;
    const [tab, setTab] = useState('map');

    // Stakeholders
    const [stakeholders, setStakeholders] = useState([]);

    // Sponsors & commitments
    const [sponsors, setSponsors] = useState([]);
    const [commitments, setCommitments] = useState([]);

    // Donors & donations
    const [donors, setDonors] = useState([]);
    const [donations, setDonations] = useState([]);

    // Partners
    const [partners, setPartners] = useState([]);

    useEffect(() => { shell?.setTitle?.(t("Stakeholders")); }, []);

    // =========== LOADERS ===========

    const loadStakeholders = useCallback(async () => {
        if (!projectId) return;
        try { setStakeholders(await librepm.stakeholdersList(projectId) || []); } catch (e) { console.error(e); }
    }, [projectId]);

    const loadSponsors = useCallback(async () => {
        if (!projectId) return;
        try {
            setSponsors(await librepm.sponsorsList(projectId) || []);
            setCommitments(await librepm.sponsorCommitmentsList(projectId) || []);
        } catch (e) { console.error(e); }
    }, [projectId]);

    const loadDonors = useCallback(async () => {
        if (!projectId) return;
        try {
            setDonors(await librepm.donorsList(projectId) || []);
            setDonations(await librepm.donationsList(projectId) || []);
        } catch (e) { console.error(e); }
    }, [projectId]);

    const loadPartners = useCallback(async () => {
        if (!projectId) return;
        try { setPartners(await librepm.partnersList(projectId) || []); } catch (e) { console.error(e); }
    }, [projectId]);

    useEffect(() => { loadStakeholders(); loadSponsors(); loadDonors(); loadPartners(); },
        [loadStakeholders, loadSponsors, loadDonors, loadPartners]);

    // =========== HANDLERS ===========

    const handleAddStakeholder = async () => {
        try {
            const s = await librepm.stakeholderCreate(projectId, { name: t("New stakeholder") });
            setStakeholders(prev => [...prev, s]);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleUpdateStakeholder = async (id, data) => {
        try {
            const updated = await librepm.stakeholderUpdate(projectId, id, data);
            setStakeholders(prev => prev.map(s => s.id === id ? updated : s));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleDeleteStakeholder = async (id) => {
        try {
            await librepm.stakeholderDelete(projectId, id);
            setStakeholders(prev => prev.filter(s => s.id !== id));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleAddSponsor = async () => {
        try {
            const s = await librepm.sponsorCreate(projectId, { name: t("New sponsor") });
            setSponsors(prev => [...prev, s]);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleAddCommitment = async (sponsorId) => {
        try {
            const c = await librepm.sponsorCommitmentCreate(projectId, { sponsorId, committedAmount: 0 });
            setCommitments(prev => [...prev, c]);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleAddDonor = async () => {
        try {
            const d = await librepm.donorCreate(projectId, { name: t("New donor") });
            setDonors(prev => [...prev, d]);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleAddDonation = async (donorId) => {
        try {
            const d = await librepm.donationCreate(projectId, { donorId, amount: 0 });
            setDonations(prev => [...prev, d]);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleUpdateSponsor = async (id, data) => {
        try {
            const updated = await librepm.sponsorUpdate(projectId, id, data);
            setSponsors(prev => prev.map(s => s.id === id ? updated : s));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleUpdateCommitment = async (id, data) => {
        try {
            const updated = await librepm.sponsorCommitmentUpdate(projectId, id, data);
            setCommitments(prev => prev.map(c => c.id === id ? updated : c));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleDeleteCommitment = async (id) => {
        try {
            await librepm.sponsorCommitmentDelete(projectId, id);
            setCommitments(prev => prev.filter(c => c.id !== id));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleUpdateDonor = async (id, data) => {
        try {
            const updated = await librepm.donorUpdate(projectId, id, data);
            setDonors(prev => prev.map(d => d.id === id ? updated : d));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleDeleteDonor = async (id) => {
        try {
            await librepm.donorDelete(projectId, id);
            setDonors(prev => prev.filter(d => d.id !== id));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleUpdateDonation = async (id, data) => {
        try {
            const updated = await librepm.donationUpdate(projectId, id, data);
            setDonations(prev => prev.map(d => d.id === id ? updated : d));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleDeleteDonation = async (id) => {
        try {
            await librepm.donationDelete(projectId, id);
            setDonations(prev => prev.filter(d => d.id !== id));
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    const handleAddPartner = async () => {
        try {
            const p = await librepm.partnerCreate(projectId, { name: t("New partner") });
            setPartners(prev => [...prev, p]);
        } catch (e) { toast.error(t("Error") + ': ' + e.message); }
    };

    // =========== INFLUENCE/INTEREST MATRIX ===========

    const LEVELS = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    const INTEREST_LEVELS = ['LOW', 'MEDIUM', 'HIGH'];

    const levelToNum = (level) => {
        const map = { LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 };
        return map[level] || 1;
    };

    const influenceColor = (level) => {
        const colors = { LOW: 'success', MEDIUM: 'info', HIGH: 'warning', CRITICAL: 'danger' };
        return colors[level] || 'secondary';
    };

    const commitStatusColor = (status) => {
        const colors = { PROPOSED: 'secondary', AGREED: 'info', ACTIVE: 'success', COMPLETED: 'primary', WITHDRAWN: 'danger' };
        return colors[status] || 'secondary';
    };

    const PARTNER_ROLES = ['COORDINATOR', 'PARTNER', 'ASSOCIATE', 'SUBCONTRACTOR'];
    const COMMIT_STATUSES = ['PROPOSED', 'AGREED', 'ACTIVE', 'COMPLETED', 'WITHDRAWN'];
    const CHANNELS = ['EMAIL', 'PHONE', 'MEETING', 'PORTAL'];

    // =========== RENDER ===========

    if (!projectId) {
        return <div className="container-fluid p-4 text-center text-muted">
            <i className="bi bi-folder2-open fs-1 d-block mb-3"></i>
            <p>{t("Select a project")}</p>
        </div>;
    }

    return (
        <div className="container-fluid p-4">
            {/* Tabs */}
            <ul className="nav nav-tabs mb-4">
                {[
                    { key: 'map', icon: 'bi-diagram-3', label: t("Stakeholder Map") },
                    { key: 'sponsors', icon: 'bi-building', label: t("Sponsors") },
                    { key: 'donors', icon: 'bi-heart', label: t("Donors") },
                    { key: 'partners', icon: 'bi-people', label: t("Partners") },
                ].map(({ key, icon, label }) => (
                    <li className="nav-item" key={key}>
                        <button className={`nav-link ${tab === key ? 'active' : ''}`} onClick={() => setTab(key)}>
                            <i className={`bi ${icon} me-1`}></i>{label}
                        </button>
                    </li>
                ))}
            </ul>

            {/* STAKEHOLDER MAP TAB */}
            {tab === 'map' && (
                <div>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h6 className="mb-0">{t("Stakeholder Register")}</h6>
                        <button className="btn btn-sm btn-primary" onClick={handleAddStakeholder}>
                            <i className="bi bi-plus me-1"></i>{t("Add")}
                        </button>
                    </div>

                    {/* Visual matrix */}
                    <div className="card mb-4">
                        <div className="card-header"><h6 className="mb-0">{t("Influence / Interest Matrix")}</h6></div>
                        <div className="card-body">
                            <div className="d-flex align-items-stretch" style={{ minHeight: 340 }}>
                                {/* Y-axis label */}
                                <div className="d-flex align-items-center me-1" style={{ writingMode: 'vertical-rl', transform: 'rotate(180deg)' }}>
                                    <span className="text-muted fw-semibold small text-uppercase letter-spacing-1">{t("Influence")}</span>
                                </div>
                                {/* Y-axis ticks */}
                                <div className="d-flex flex-column justify-content-between py-1 me-1 text-end" style={{ width: 60, fontSize: '0.7rem' }}>
                                    {['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map(l => (
                                        <span key={l} className="text-muted">{t(`stakeholder.level.${l}`)}</span>
                                    ))}
                                </div>
                                {/* Matrix area */}
                                <div className="flex-grow-1 d-flex flex-column">
                                    <div className="position-relative flex-grow-1" style={{ border: '1px solid var(--bs-border-color)', borderRadius: 8, overflow: 'hidden', background: 'var(--bs-body-bg)' }}>
                                        {/* Quadrant backgrounds */}
                                        <div className="position-absolute" style={{ top: 0, left: 0, width: '50%', height: '50%', background: 'rgba(var(--bs-warning-rgb), 0.04)' }}></div>
                                        <div className="position-absolute" style={{ top: 0, right: 0, width: '50%', height: '50%', background: 'rgba(var(--bs-danger-rgb), 0.04)' }}></div>
                                        <div className="position-absolute" style={{ bottom: 0, left: 0, width: '50%', height: '50%', background: 'rgba(var(--bs-success-rgb), 0.04)' }}></div>
                                        <div className="position-absolute" style={{ bottom: 0, right: 0, width: '50%', height: '50%', background: 'rgba(var(--bs-info-rgb), 0.04)' }}></div>
                                        {/* Quadrant labels */}
                                        <div className="position-absolute text-muted fw-semibold" style={{ top: 8, left: 10, opacity: 0.35, fontSize: '0.75rem' }}>{t("Keep Satisfied")}</div>
                                        <div className="position-absolute text-muted fw-semibold" style={{ top: 8, right: 10, opacity: 0.35, fontSize: '0.75rem' }}>{t("Manage Closely")}</div>
                                        <div className="position-absolute text-muted fw-semibold" style={{ bottom: 8, left: 10, opacity: 0.35, fontSize: '0.75rem' }}>{t("Monitor")}</div>
                                        <div className="position-absolute text-muted fw-semibold" style={{ bottom: 8, right: 10, opacity: 0.35, fontSize: '0.75rem' }}>{t("Keep Informed")}</div>
                                        {/* Grid lines */}
                                        <div className="position-absolute" style={{ top: '50%', left: 0, right: 0, borderTop: '1px dashed var(--bs-border-color)' }}></div>
                                        <div className="position-absolute" style={{ left: '50%', top: 0, bottom: 0, borderLeft: '1px dashed var(--bs-border-color)' }}></div>
                                        <div className="position-absolute" style={{ top: '25%', left: 0, right: 0, borderTop: '1px dotted var(--bs-border-color)', opacity: 0.4 }}></div>
                                        <div className="position-absolute" style={{ top: '75%', left: 0, right: 0, borderTop: '1px dotted var(--bs-border-color)', opacity: 0.4 }}></div>
                                        <div className="position-absolute" style={{ left: '33.3%', top: 0, bottom: 0, borderLeft: '1px dotted var(--bs-border-color)', opacity: 0.4 }}></div>
                                        <div className="position-absolute" style={{ left: '66.6%', top: 0, bottom: 0, borderLeft: '1px dotted var(--bs-border-color)', opacity: 0.4 }}></div>
                                        {/* Stakeholder dots */}
                                        {stakeholders.map(s => {
                                            const interestNum = { LOW: 1, MEDIUM: 2, HIGH: 3 };
                                            const influenceNum = { LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 };
                                            const x = ((interestNum[s.interestLevel] || 1) - 0.5) / 3 * 100;
                                            const y = 100 - ((influenceNum[s.influenceLevel] || 1) - 0.5) / 4 * 100;
                                            return (
                                                <div key={s.id} className="position-absolute" style={{ left: `${x}%`, top: `${y}%`, transform: 'translate(-50%, -50%)', zIndex: 10 }}
                                                    title={`${s.name}\n${t("Influence")}: ${s.influenceLevel || 'MEDIUM'}\n${t("Interest")}: ${s.interestLevel || 'MEDIUM'}`}>
                                                    <div className={`rounded-circle bg-${influenceColor(s.influenceLevel)} d-flex align-items-center justify-content-center text-white fw-bold shadow-sm`}
                                                        style={{ width: 40, height: 40, fontSize: 11, cursor: 'pointer', border: '2px solid rgba(255,255,255,0.3)', transition: 'transform 0.15s' }}
                                                        onMouseEnter={e => e.currentTarget.style.transform = 'scale(1.2)'}
                                                        onMouseLeave={e => e.currentTarget.style.transform = 'scale(1)'}>
                                                        {s.name?.substring(0, 2).toUpperCase()}
                                                    </div>
                                                </div>
                                            );
                                        })}
                                        {stakeholders.length === 0 && (
                                            <div className="d-flex align-items-center justify-content-center h-100 text-muted small">
                                                {t("Add stakeholders to see them on the matrix")}
                                            </div>
                                        )}
                                    </div>
                                    {/* X-axis ticks */}
                                    <div className="d-flex justify-content-around mt-1" style={{ fontSize: '0.7rem' }}>
                                        {['LOW', 'MEDIUM', 'HIGH'].map(l => (
                                            <span key={l} className="text-muted">{t(`stakeholder.interestLevel.${l}`)}</span>
                                        ))}
                                    </div>
                                    {/* X-axis label */}
                                    <div className="text-center mt-1">
                                        <span className="text-muted fw-semibold small text-uppercase">{t("Interest")}</span>
                                    </div>
                                </div>
                            </div>
                            {/* Legend */}
                            {stakeholders.length > 0 && (
                                <div className="d-flex flex-wrap gap-2 mt-3 pt-2 border-top">
                                    {stakeholders.map(s => (
                                        <span key={s.id} className="d-inline-flex align-items-center gap-1" style={{ fontSize: '0.75rem' }}>
                                            <span className={`rounded-circle bg-${influenceColor(s.influenceLevel)} d-inline-block`} style={{ width: 10, height: 10 }}></span>
                                            {s.name}
                                        </span>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Stakeholder table */}
                    {stakeholders.length > 0 ? (
                        <div className="table-responsive">
                            <table className="table table-hover align-middle">
                                <thead>
                                    <tr>
                                        <th>{t("Name")}</th>
                                        <th>{t("Organization")}</th>
                                        <th>{t("Role")}</th>
                                        <th>{t("Influence")}</th>
                                        <th>{t("Interest")}</th>
                                        <th>{t("Channel")}</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {stakeholders.map(s => (
                                        <tr key={s.id}>
                                            <td>
                                                <input className="form-control form-control-sm border-0" value={s.name || ''}
                                                    onChange={e => setStakeholders(prev => prev.map(x => x.id === s.id ? { ...x, name: e.target.value } : x))}
                                                    onBlur={() => handleUpdateStakeholder(s.id, { name: s.name })} />
                                            </td>
                                            <td>
                                                <input className="form-control form-control-sm border-0" value={s.organization || ''}
                                                    onChange={e => setStakeholders(prev => prev.map(x => x.id === s.id ? { ...x, organization: e.target.value } : x))}
                                                    onBlur={() => handleUpdateStakeholder(s.id, { organization: s.organization })} />
                                            </td>
                                            <td>
                                                <input className="form-control form-control-sm border-0" value={s.roleDescription || ''}
                                                    onChange={e => setStakeholders(prev => prev.map(x => x.id === s.id ? { ...x, roleDescription: e.target.value } : x))}
                                                    onBlur={() => handleUpdateStakeholder(s.id, { roleDescription: s.roleDescription })} />
                                            </td>
                                            <td>
                                                <select className="form-select form-select-sm border-0" value={s.influenceLevel || 'MEDIUM'}
                                                    onChange={e => handleUpdateStakeholder(s.id, { influenceLevel: e.target.value })}>
                                                    {LEVELS.map(l => <option key={l} value={l}>{t(`stakeholder.level.${l}`)}</option>)}
                                                </select>
                                            </td>
                                            <td>
                                                <select className="form-select form-select-sm border-0" value={s.interestLevel || 'MEDIUM'}
                                                    onChange={e => handleUpdateStakeholder(s.id, { interestLevel: e.target.value })}>
                                                    {INTEREST_LEVELS.map(l => <option key={l} value={l}>{t(`stakeholder.interestLevel.${l}`)}</option>)}
                                                </select>
                                            </td>
                                            <td>
                                                <select className="form-select form-select-sm border-0" value={s.channel || ''}
                                                    onChange={e => handleUpdateStakeholder(s.id, { channel: e.target.value })}>
                                                    <option value="">-</option>
                                                    {CHANNELS.map(c => <option key={c} value={c}>{t(`stakeholder.channel.${c}`)}</option>)}
                                                </select>
                                            </td>
                                            <td>
                                                <button className="btn btn-sm btn-outline-danger" onClick={() => handleDeleteStakeholder(s.id)}>
                                                    <i className="bi bi-trash"></i>
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p className="text-muted">{t("No stakeholders")}</p>
                    )}
                </div>
            )}

            {/* SPONSORS TAB */}
            {tab === 'sponsors' && (
                <div>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h6 className="mb-0">{t("Sponsors")}</h6>
                        <button className="btn btn-sm btn-primary" onClick={handleAddSponsor}>
                            <i className="bi bi-plus me-1"></i>{t("Add")}
                        </button>
                    </div>
                    {sponsors.length > 0 ? (
                        <div className="table-responsive">
                            <table className="table table-hover align-middle">
                                <thead>
                                    <tr>
                                        <th>{t("Name")}</th>
                                        <th>{t("Organization")}</th>
                                        <th>{t("Email")}</th>
                                        <th>{t("Contact")}</th>
                                        <th>{t("Type")}</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {sponsors.map(sp => {
                                        const spCommits = commitments.filter(c => c.sponsorId === sp.id);
                                        return (
                                            <React.Fragment key={sp.id}>
                                                <tr>
                                                    <td>
                                                        <input className="form-control form-control-sm border-0" value={sp.name || ''}
                                                            onChange={e => setSponsors(prev => prev.map(x => x.id === sp.id ? { ...x, name: e.target.value } : x))}
                                                            onBlur={() => handleUpdateSponsor(sp.id, { name: sp.name })} />
                                                    </td>
                                                    <td>
                                                        <input className="form-control form-control-sm border-0" value={sp.organization || ''}
                                                            onChange={e => setSponsors(prev => prev.map(x => x.id === sp.id ? { ...x, organization: e.target.value } : x))}
                                                            onBlur={() => handleUpdateSponsor(sp.id, { organization: sp.organization })} />
                                                    </td>
                                                    <td>
                                                        <input className="form-control form-control-sm border-0" value={sp.email || ''}
                                                            onChange={e => setSponsors(prev => prev.map(x => x.id === sp.id ? { ...x, email: e.target.value } : x))}
                                                            onBlur={() => handleUpdateSponsor(sp.id, { email: sp.email })} />
                                                    </td>
                                                    <td>
                                                        <input className="form-control form-control-sm border-0" value={sp.contactPerson || ''}
                                                            onChange={e => setSponsors(prev => prev.map(x => x.id === sp.id ? { ...x, contactPerson: e.target.value } : x))}
                                                            onBlur={() => handleUpdateSponsor(sp.id, { contactPerson: sp.contactPerson })} />
                                                    </td>
                                                    <td>
                                                        <select className="form-select form-select-sm border-0" value={sp.type || 'OTHER'}
                                                            onChange={e => { setSponsors(prev => prev.map(x => x.id === sp.id ? { ...x, type: e.target.value } : x)); handleUpdateSponsor(sp.id, { type: e.target.value }); }}>
                                                            {['CORPORATE', 'INDIVIDUAL', 'FOUNDATION', 'GOVERNMENT', 'OTHER'].map(t2 => <option key={t2} value={t2}>{t(`stakeholder.sponsorType.${t2}`)}</option>)}
                                                        </select>
                                                    </td>
                                                    <td>
                                                        <button className="btn btn-sm btn-outline-danger" onClick={async () => {
                                                            await librepm.sponsorDelete(projectId, sp.id);
                                                            setSponsors(prev => prev.filter(x => x.id !== sp.id));
                                                        }}><i className="bi bi-trash"></i></button>
                                                    </td>
                                                </tr>
                                                {/* Commitments sub-table */}
                                                <tr>
                                                    <td colSpan={6} className="p-0 ps-4 pb-2">
                                                        <div className="d-flex justify-content-between align-items-center mb-1 mt-1">
                                                            <small className="fw-semibold text-muted">{t("Commitments")}</small>
                                                            <button className="btn btn-sm btn-outline-primary" style={{ fontSize: '0.7rem', padding: '0.15rem 0.4rem' }}
                                                                onClick={() => handleAddCommitment(sp.id)}>
                                                                <i className="bi bi-plus me-1"></i>{t("Add")}
                                                            </button>
                                                        </div>
                                                        {spCommits.length > 0 && (
                                                            <table className="table table-sm table-borderless mb-0">
                                                                <thead>
                                                                    <tr className="small text-muted">
                                                                        <th>{t("Description")}</th>
                                                                        <th style={{ width: 110 }}>{t("Amount")}</th>
                                                                        <th style={{ width: 70 }}>{t("Currency")}</th>
                                                                        <th style={{ width: 130 }}>{t("Status")}</th>
                                                                        <th style={{ width: 130 }}>{t("Agreement Date")}</th>
                                                                        <th style={{ width: 40 }}></th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    {spCommits.map(c => (
                                                                        <tr key={c.id} className="small">
                                                                            <td>
                                                                                <input className="form-control form-control-sm border-0 p-0" value={c.description || ''}
                                                                                    onChange={e => setCommitments(prev => prev.map(x => x.id === c.id ? { ...x, description: e.target.value } : x))}
                                                                                    onBlur={() => handleUpdateCommitment(c.id, { description: c.description })} />
                                                                            </td>
                                                                            <td>
                                                                                <input type="number" className="form-control form-control-sm border-0 p-0 text-end" value={c.committedAmount || 0}
                                                                                    onChange={e => setCommitments(prev => prev.map(x => x.id === c.id ? { ...x, committedAmount: parseFloat(e.target.value) || 0 } : x))}
                                                                                    onBlur={() => handleUpdateCommitment(c.id, { committedAmount: c.committedAmount })} />
                                                                            </td>
                                                                            <td>
                                                                                <input className="form-control form-control-sm border-0 p-0" value={c.currency || ''} style={{ width: 60 }}
                                                                                    onChange={e => setCommitments(prev => prev.map(x => x.id === c.id ? { ...x, currency: e.target.value } : x))}
                                                                                    onBlur={() => handleUpdateCommitment(c.id, { currency: c.currency })} />
                                                                            </td>
                                                                            <td>
                                                                                <select className="form-select form-select-sm border-0 p-0" value={c.status || 'PROPOSED'}
                                                                                    onChange={e => { setCommitments(prev => prev.map(x => x.id === c.id ? { ...x, status: e.target.value } : x)); handleUpdateCommitment(c.id, { status: e.target.value }); }}>
                                                                                    {COMMIT_STATUSES.map(s => <option key={s} value={s}>{t(`stakeholder.commitStatus.${s}`)}</option>)}
                                                                                </select>
                                                                            </td>
                                                                            <td>
                                                                                <input type="date" className="form-control form-control-sm border-0 p-0" value={c.agreementDate || ''}
                                                                                    onChange={e => { setCommitments(prev => prev.map(x => x.id === c.id ? { ...x, agreementDate: e.target.value } : x)); handleUpdateCommitment(c.id, { agreementDate: e.target.value }); }} />
                                                                            </td>
                                                                            <td>
                                                                                <button className="btn btn-sm btn-link text-danger p-0" onClick={() => handleDeleteCommitment(c.id)}>
                                                                                    <i className="bi bi-trash"></i>
                                                                                </button>
                                                                            </td>
                                                                        </tr>
                                                                    ))}
                                                                </tbody>
                                                            </table>
                                                        )}
                                                    </td>
                                                </tr>
                                            </React.Fragment>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p className="text-muted">{t("No sponsors")}</p>
                    )}
                </div>
            )}

            {/* DONORS TAB */}
            {tab === 'donors' && (
                <div>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h6 className="mb-0">{t("Donors")}</h6>
                        <button className="btn btn-sm btn-primary" onClick={handleAddDonor}>
                            <i className="bi bi-plus me-1"></i>{t("Add")}
                        </button>
                    </div>
                    {donors.length > 0 ? (
                        <div className="table-responsive">
                            <table className="table table-hover align-middle">
                                <thead>
                                    <tr>
                                        <th>{t("Name")}</th>
                                        <th>{t("Organization")}</th>
                                        <th>{t("Email")}</th>
                                        <th>{t("Phone")}</th>
                                        <th>{t("Type")}</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {donors.map(d => {
                                        const dDonations = donations.filter(don => don.donorId === d.id);
                                        return (
                                            <React.Fragment key={d.id}>
                                                <tr>
                                                    <td>
                                                        <input className="form-control form-control-sm border-0" value={d.name || ''}
                                                            onChange={e => setDonors(prev => prev.map(x => x.id === d.id ? { ...x, name: e.target.value } : x))}
                                                            onBlur={() => handleUpdateDonor(d.id, { name: d.name })} />
                                                    </td>
                                                    <td>
                                                        <input className="form-control form-control-sm border-0" value={d.organization || ''}
                                                            onChange={e => setDonors(prev => prev.map(x => x.id === d.id ? { ...x, organization: e.target.value } : x))}
                                                            onBlur={() => handleUpdateDonor(d.id, { organization: d.organization })} />
                                                    </td>
                                                    <td>
                                                        <input className="form-control form-control-sm border-0" value={d.email || ''}
                                                            onChange={e => setDonors(prev => prev.map(x => x.id === d.id ? { ...x, email: e.target.value } : x))}
                                                            onBlur={() => handleUpdateDonor(d.id, { email: d.email })} />
                                                    </td>
                                                    <td>
                                                        <input className="form-control form-control-sm border-0" value={d.phone || ''}
                                                            onChange={e => setDonors(prev => prev.map(x => x.id === d.id ? { ...x, phone: e.target.value } : x))}
                                                            onBlur={() => handleUpdateDonor(d.id, { phone: d.phone })} />
                                                    </td>
                                                    <td>
                                                        <select className="form-select form-select-sm border-0" value={d.type || 'OTHER'}
                                                            onChange={e => { setDonors(prev => prev.map(x => x.id === d.id ? { ...x, type: e.target.value } : x)); handleUpdateDonor(d.id, { type: e.target.value }); }}>
                                                            {['INDIVIDUAL', 'FOUNDATION', 'CORPORATE', 'GOVERNMENT', 'OTHER'].map(t2 => <option key={t2} value={t2}>{t(`stakeholder.donorType.${t2}`)}</option>)}
                                                        </select>
                                                    </td>
                                                    <td>
                                                        <button className="btn btn-sm btn-outline-danger" onClick={() => handleDeleteDonor(d.id)}>
                                                            <i className="bi bi-trash"></i>
                                                        </button>
                                                    </td>
                                                </tr>
                                                {/* Donations sub-table */}
                                                <tr>
                                                    <td colSpan={6} className="p-0 ps-4 pb-2">
                                                        <div className="d-flex justify-content-between align-items-center mb-1 mt-1">
                                                            <small className="fw-semibold text-muted">{t("Donations")}</small>
                                                            <button className="btn btn-sm btn-outline-primary" style={{ fontSize: '0.7rem', padding: '0.15rem 0.4rem' }}
                                                                onClick={() => handleAddDonation(d.id)}>
                                                                <i className="bi bi-plus me-1"></i>{t("Add")}
                                                            </button>
                                                        </div>
                                                        {dDonations.length > 0 && (
                                                            <table className="table table-sm table-borderless mb-0">
                                                                <thead>
                                                                    <tr className="small text-muted">
                                                                        <th style={{ width: 110 }}>{t("Amount")}</th>
                                                                        <th style={{ width: 70 }}>{t("Currency")}</th>
                                                                        <th style={{ width: 130 }}>{t("Date")}</th>
                                                                        <th>{t("Notes")}</th>
                                                                        <th style={{ width: 80 }}>{t("Restricted")}</th>
                                                                        <th style={{ width: 40 }}></th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    {dDonations.map(don => (
                                                                        <tr key={don.id} className="small">
                                                                            <td>
                                                                                <input type="number" className="form-control form-control-sm border-0 p-0 text-end" value={don.amount || 0}
                                                                                    onChange={e => setDonations(prev => prev.map(x => x.id === don.id ? { ...x, amount: parseFloat(e.target.value) || 0 } : x))}
                                                                                    onBlur={() => handleUpdateDonation(don.id, { amount: don.amount })} />
                                                                            </td>
                                                                            <td>
                                                                                <input className="form-control form-control-sm border-0 p-0" value={don.currency || ''} style={{ width: 60 }}
                                                                                    onChange={e => setDonations(prev => prev.map(x => x.id === don.id ? { ...x, currency: e.target.value } : x))}
                                                                                    onBlur={() => handleUpdateDonation(don.id, { currency: don.currency })} />
                                                                            </td>
                                                                            <td>
                                                                                <input type="date" className="form-control form-control-sm border-0 p-0" value={don.donationDate || ''}
                                                                                    onChange={e => { setDonations(prev => prev.map(x => x.id === don.id ? { ...x, donationDate: e.target.value } : x)); handleUpdateDonation(don.id, { donationDate: e.target.value }); }} />
                                                                            </td>
                                                                            <td>
                                                                                <input className="form-control form-control-sm border-0 p-0" value={don.notes || ''}
                                                                                    onChange={e => setDonations(prev => prev.map(x => x.id === don.id ? { ...x, notes: e.target.value } : x))}
                                                                                    onBlur={() => handleUpdateDonation(don.id, { notes: don.notes })} />
                                                                            </td>
                                                                            <td className="text-center">
                                                                                <input type="checkbox" className="form-check-input" checked={don.isRestricted || false}
                                                                                    onChange={e => { setDonations(prev => prev.map(x => x.id === don.id ? { ...x, isRestricted: e.target.checked } : x)); handleUpdateDonation(don.id, { isRestricted: e.target.checked }); }} />
                                                                            </td>
                                                                            <td>
                                                                                <button className="btn btn-sm btn-link text-danger p-0" onClick={() => handleDeleteDonation(don.id)}>
                                                                                    <i className="bi bi-trash"></i>
                                                                                </button>
                                                                            </td>
                                                                        </tr>
                                                                    ))}
                                                                </tbody>
                                                            </table>
                                                        )}
                                                    </td>
                                                </tr>
                                            </React.Fragment>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p className="text-muted">{t("No donors")}</p>
                    )}
                </div>
            )}

            {/* PARTNERS TAB */}
            {tab === 'partners' && (
                <div>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h6 className="mb-0">{t("Partner Organisations")}</h6>
                        <button className="btn btn-sm btn-primary" onClick={handleAddPartner}>
                            <i className="bi bi-plus me-1"></i>{t("Add")}
                        </button>
                    </div>
                    {partners.length > 0 ? (
                        <div className="table-responsive">
                            <table className="table table-hover align-middle">
                                <thead>
                                    <tr>
                                        <th>{t("Name")}</th>
                                        <th>{t("Country")}</th>
                                        <th>{t("Role")}</th>
                                        <th>{t("Contact")}</th>
                                        <th className="text-end">{t("Budget Share")}</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {partners.map(p => (
                                        <tr key={p.id}>
                                            <td>
                                                <input className="form-control form-control-sm border-0" value={p.name || ''}
                                                    onChange={e => setPartners(prev => prev.map(x => x.id === p.id ? { ...x, name: e.target.value } : x))}
                                                    onBlur={() => librepm.partnerUpdate(projectId, p.id, { name: p.name })} />
                                            </td>
                                            <td>
                                                <input className="form-control form-control-sm border-0" value={p.country || ''} placeholder="-"
                                                    onChange={e => setPartners(prev => prev.map(x => x.id === p.id ? { ...x, country: e.target.value } : x))}
                                                    onBlur={() => librepm.partnerUpdate(projectId, p.id, { country: p.country })} />
                                            </td>
                                            <td>
                                                <select className="form-select form-select-sm border-0" value={p.roleInProject || 'PARTNER'}
                                                    onChange={e => librepm.partnerUpdate(projectId, p.id, { roleInProject: e.target.value }).then(() =>
                                                        setPartners(prev => prev.map(x => x.id === p.id ? { ...x, roleInProject: e.target.value } : x)))}>
                                                    {PARTNER_ROLES.map(r => <option key={r} value={r}>{t(`stakeholder.partnerRole.${r}`)}</option>)}
                                                </select>
                                            </td>
                                            <td>
                                                <input className="form-control form-control-sm border-0" value={p.contactPerson || ''} placeholder={t("Contact")}
                                                    onChange={e => setPartners(prev => prev.map(x => x.id === p.id ? { ...x, contactPerson: e.target.value } : x))}
                                                    onBlur={() => librepm.partnerUpdate(projectId, p.id, { contactPerson: p.contactPerson })} />
                                            </td>
                                            <td className="text-end">
                                                <input type="number" className="form-control form-control-sm border-0 text-end" value={p.budgetShare || ''}
                                                    onChange={e => setPartners(prev => prev.map(x => x.id === p.id ? { ...x, budgetShare: e.target.value } : x))}
                                                    onBlur={() => librepm.partnerUpdate(projectId, p.id, { budgetShare: p.budgetShare ? Number(p.budgetShare) : null })} />
                                            </td>
                                            <td>
                                                <button className="btn btn-sm btn-outline-danger" onClick={async () => {
                                                    await librepm.partnerDelete(projectId, p.id);
                                                    setPartners(prev => prev.filter(x => x.id !== p.id));
                                                }}><i className="bi bi-trash"></i></button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p className="text-muted">{t("No partners")}</p>
                    )}
                </div>
            )}
        </div>
    );
}
