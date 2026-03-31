import React, { useState, useEffect } from 'react';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import PortalModal from '../components/PortalModal.jsx';
import { useTranslation } from 'react-i18next';
import { useModal } from '../hooks/useModal.js';

const TeamPage = ({ shell }) => {
    const { t } = useTranslation();
    const modal = useModal();
    const [projects, setProjects] = useState([]);
    const [members, setMembers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedProject, setSelectedProject] = useState('');
    const [showModal, setShowModal] = useState(false);

    // Modal State
    const [isRealUser, setIsRealUser] = useState(false); // Checkbox state

    // Form per Utente Locale (Ghost)
    const [localName, setLocalName] = useState('');
    const [localUsername, setLocalUsername] = useState('');

    // Form per Utente Reale (Contact) o Ghost esistente
    const [friends, setFriends] = useState([]);
    const [ghosts, setGhosts] = useState([]);
    const [selectedFriendId, setSelectedFriendId] = useState('');

    // Common
    const [targetProjectId, setTargetProjectId] = useState('');

    // System roles
    const [systemRoles, setSystemRoles] = useState([]);

    // External contributors
    const [externalContributors, setExternalContributors] = useState([]);
    const [showExtModal, setShowExtModal] = useState(false);
    const [extName, setExtName] = useState('');
    const [extEmail, setExtEmail] = useState('');
    const [extOrg, setExtOrg] = useState('');
    const [extRoleId, setExtRoleId] = useState('');
    const [extScope, setExtScope] = useState('PROJECT');
    const [extScopeEntityId, setExtScopeEntityId] = useState('');

    // Edit ghost state
    const [showEditModal, setShowEditModal] = useState(false);
    const [editGhost, setEditGhost] = useState(null);
    const [editName, setEditName] = useState('');
    const [editUsername, setEditUsername] = useState('');
    const [editProjects, setEditProjects] = useState(new Set());
    const [savingEdit, setSavingEdit] = useState(false);

    useEffect(() => {
        shell?.setTitle?.(t("Team Management"));

        // Set up the side information panel
        shell?.setRightPanel?.(
            <div className="d-flex flex-column gap-3">
                <div className="fw-bold">{t("Member Management")}</div>

                <div className="p-3 border rounded-3 bg-white">
                    <div className="d-flex align-items-center gap-2 mb-2">
                        <span className="badge bg-primary-subtle text-primary border border-primary-subtle">{t("LibrePM")}</span>
                        <span className="fw-bold small">{t("Real User")}</span>
                    </div>
                    <p className="small text-muted mb-0">
                        {t("A Contact contact...")}
                    </p>
                </div>

                <div className="p-3 border rounded-3 bg-white">
                    <div className="d-flex align-items-center gap-2 mb-2">
                        <span className="badge bg-secondary-subtle text-secondary border border-secondary-subtle">{t("Local")}</span>
                        <span className="fw-bold small">{t("Virtual User")}</span>
                    </div>
                    <p className="small text-muted mb-0">
                        {t("A placeholder created only...")}
                    </p>
                </div>
            </div>
        );

        loadData();
    }, [shell, t]);

    const loadData = async () => {
        try {
            setLoading(true);

            // Use librepm.js methods instead of direct fetch calls
            const [projectsList, friendsList, ghostsList, rolesList, extList] = await Promise.all([
                librepm.projectsList({ archived: false }),
                librepm.connectionsList(),
                librepm.ghostsList(),
                librepm.rolesList().catch(() => []),
                librepm.externalContributorsList().catch(() => [])
            ]);

            setProjects(projectsList);
            setFriends(friendsList);
            setGhosts(ghostsList);
            setSystemRoles(rolesList);
            setExternalContributors(extList);

            // Load members for each project
            const allMembers = [];
            for (const p of projectsList) {
                try {
                    const projectMembers = await librepm.projectMembersList(p.id);
                    projectMembers.forEach(m => {
                        allMembers.push({
                            ...m,
                            projectName: p.name,
                            projectId: p.id
                        });
                    });
                } catch (e) {
                    console.warn(`Error loading members for project ${p.name}`, e);
                }
            }
            setMembers(allMembers);
        } catch (e) {
            console.error("Error loading team data:", e);
            toast.error(t("Error loading"));
        } finally {
            setLoading(false);
        }
    };

    const handleAddMember = async () => {
        if (!targetProjectId) {
            toast.warning(t("Select a project"));
            return;
        }

        try {
            if (isRealUser) {
                // Add real user (friend)
                if (!selectedFriendId) {
                    toast.warning(t("Select Contact *"));
                    return;
                }
                await librepm.projectMembersAdd(targetProjectId, selectedFriendId, "EDITOR");
                toast.success(t("Success"));
            } else {
                // Create and add local user (Ghost)
                if (!localName || !localUsername) {
                    toast.warning(t("Please fix form errors"));
                    return;
                }
                await librepm.projectMembersCreateGhost(targetProjectId, localUsername, localName);
                toast.success(t("Success"));
            }

            // Reset and reload
            setShowModal(false);
            setLocalName('');
            setLocalUsername('');
            setSelectedFriendId('');
            loadData();
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        }
    };

    const openEditGhost = (ghost) => {
        setEditGhost(ghost);
        setEditName(ghost.displayName || '');
        setEditUsername(ghost.username || '');
        // Find which projects this ghost is currently a member of
        const ghostProjects = new Set(
            members.filter(m => m.user.id === ghost.id).map(m => m.projectId)
        );
        setEditProjects(ghostProjects);
        setShowEditModal(true);
    };

    const handleEditGhost = async () => {
        if (!editGhost || !editName || !editUsername) return;
        try {
            setSavingEdit(true);
            // Update name/username
            await librepm.ghostsUpdate(editGhost.id, { username: editUsername, displayName: editName });

            // Sync project assignments
            const currentProjects = new Set(
                members.filter(m => m.user.id === editGhost.id).map(m => m.projectId)
            );
            // Add to new projects
            for (const pid of editProjects) {
                if (!currentProjects.has(pid)) {
                    await librepm.projectMembersAdd(pid, editGhost.id, "EDITOR");
                }
            }
            // Remove from deselected projects
            for (const pid of currentProjects) {
                if (!editProjects.has(pid)) {
                    await librepm.projectMembersRemove(pid, editGhost.id);
                }
            }

            toast.success(t("Member updated"));
            setShowEditModal(false);
            setEditGhost(null);
            loadData();
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        } finally {
            setSavingEdit(false);
        }
    };

    const toggleEditProject = (projectId) => {
        setEditProjects(prev => {
            const next = new Set(prev);
            if (next.has(projectId)) next.delete(projectId);
            else next.add(projectId);
            return next;
        });
    };

    const handleRoleChange = async (member, newRole) => {
        try {
            await librepm.projectMembersUpdateRole(member.projectId, member.user.id, newRole, member.systemRoleId || null);
            toast.success(t("Role updated"));
            loadData();
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        }
    };

    const handleSystemRoleChange = async (member, newSystemRoleId) => {
        try {
            await librepm.projectMembersUpdateRole(member.projectId, member.user.id, member.role, newSystemRoleId || null);
            toast.success(t("Role updated"));
            loadData();
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        }
    };

    const handleRemoveMember = async (member) => {
        const confirmed = await modal.confirm({ title: `${t("Remove")} ${member.user.displayName || member.user.username} ${t("from project")} ${member.projectName}?` });
        if (!confirmed) return;

        try {
            await librepm.projectMembersRemove(member.projectId, member.user.id);
            toast.success(t("Deleted successfully"));
            loadData();
        } catch (e) {
            toast.error(t("Deletion error") + ": " + e.message);
        }
    };

    const handleAddExternal = async () => {
        if (!extName.trim()) { toast.warning(t("Display Name *")); return; }
        try {
            await librepm.externalContributorsCreate({
                displayName: extName,
                email: extEmail || null,
                organization: extOrg || null,
                roleId: extRoleId || null,
                scope: extScope,
                scopeEntityId: extScopeEntityId || null,
            });
            toast.success(t("Success"));
            setShowExtModal(false);
            setExtName(''); setExtEmail(''); setExtOrg(''); setExtRoleId(''); setExtScope('PROJECT'); setExtScopeEntityId('');
            loadData();
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        }
    };

    const handleRevokeExternal = async (contributor) => {
        const confirmed = await modal.confirm({ title: `${t("Revoke access for")} ${contributor.displayName}?` });
        if (!confirmed) return;
        try {
            await librepm.externalContributorsRevoke(contributor.id);
            toast.success(t("Deleted successfully"));
            loadData();
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        }
    };

    // Filter members
    const filteredMembers = selectedProject
        ? members.filter(m => m.projectId === selectedProject)
        : members;

    if (loading) return <div className="p-5 text-center"><div className="spinner-border text-primary"></div></div>;

    return (
        <div className="container-fluid p-4 fade-in">
            {/* Toolbar */}
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div className="d-flex gap-3 align-items-center">
                    <select
                        className="form-select"
                        style={{ maxWidth: 250 }}
                        value={selectedProject}
                        onChange={(e) => setSelectedProject(e.target.value)}
                    >
                        <option value="">{t("All projects")}</option>
                        {projects.map(p => (
                            <option key={p.id} value={p.id}>{p.name}</option>
                        ))}
                    </select>
                </div>
                <button className="btn btn-primary" onClick={() => setShowModal(true)}>
                    <i className="bi bi-plus-lg me-2"></i>
                    {t("New Member")}
                </button>
            </div>

            {/* Members Table */}
            <div className="card border-0 shadow-sm">
                <div className="table-responsive">
                    <table className="table table-hover mb-0">
                        <thead className="bg-light">
                        <tr>
                            <th className="ps-4 py-3">{t("Member")}</th>
                            <th className="py-3">{t("Account Type")}</th>
                            <th className="py-3">{t("Project")}</th>
                            <th className="py-3">{t("Role")}</th>
                            <th className="py-3">{t("System Role")}</th>
                            <th className="py-3 text-end pe-4">{t("Actions")}</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredMembers.length === 0 ? (
                            <tr>
                                <td colSpan="6" className="text-center py-5 text-muted">
                                    {t("No members found.")}
                                </td>
                            </tr>
                        ) : (
                            filteredMembers.map((m, idx) => (
                                <tr key={`${m.projectId}-${m.user.id}-${idx}`}>
                                    <td className="ps-4 py-3 align-middle">
                                        <div className="d-flex align-items-center">
                                            <div className="avatar-circle bg-secondary text-white me-3 d-flex align-items-center justify-content-center" style={{width: 32, height: 32, borderRadius: '50%'}}>
                                                {(m.user.displayName || m.user.username).substring(0, 2).toUpperCase()}
                                            </div>
                                            <div>
                                                <div className="fw-bold">{m.user.displayName || m.user.username}</div>
                                                <div className="small text-muted">@{m.user.username}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        {m.user.ghost ? (
                                            <span className="badge bg-secondary-subtle text-secondary border border-secondary-subtle">{t("Local")}</span>
                                        ) : (
                                            <span className="badge bg-primary-subtle text-primary border border-primary-subtle">{t("LibrePM")}</span>
                                        )}
                                    </td>
                                    <td>
                                            <span className="badge bg-light text-dark border">
                                                {m.projectName}
                                            </span>
                                    </td>
                                    <td>
                                        {m.role === 'OWNER' ? (
                                            <span className="badge bg-warning-subtle text-warning-emphasis">{t("Owner")}</span>
                                        ) : (
                                            <select
                                                className="form-select form-select-sm"
                                                style={{ maxWidth: 130 }}
                                                value={m.role}
                                                onChange={(e) => handleRoleChange(m, e.target.value)}
                                            >
                                                <option value="ADMIN">{t("Admin")}</option>
                                                <option value="EDITOR">{t("Editor")}</option>
                                                <option value="VIEWER">{t("Viewer")}</option>
                                            </select>
                                        )}
                                    </td>
                                    <td>
                                        {m.role !== 'OWNER' ? (
                                            <select
                                                className="form-select form-select-sm"
                                                style={{ maxWidth: 170 }}
                                                value={m.systemRoleId || ''}
                                                onChange={(e) => handleSystemRoleChange(m, e.target.value)}
                                            >
                                                <option value="">{t("None")}</option>
                                                {systemRoles.map(r => (
                                                    <option key={r.id} value={r.id}>{r.name}</option>
                                                ))}
                                            </select>
                                        ) : (
                                            <span className="text-muted small">—</span>
                                        )}
                                    </td>
                                    <td className="text-end pe-4">
                                        <div className="btn-group">
                                            {m.user.ghost && (
                                                <button
                                                    className="btn btn-sm btn-outline-primary"
                                                    onClick={() => openEditGhost(m.user)}
                                                    title={t("Edit Member")}
                                                >
                                                    <i className="bi bi-pencil"></i>
                                                </button>
                                            )}
                                            {m.role !== 'OWNER' && (
                                                <button
                                                    className="btn btn-sm btn-outline-danger"
                                                    onClick={() => handleRemoveMember(m)}
                                                    title={t("Remove from project")}
                                                >
                                                    <i className="bi bi-trash"></i>
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* External Contributors Section */}
            <div className="d-flex justify-content-between align-items-center mt-5 mb-3">
                <h5 className="mb-0"><i className="bi bi-person-badge me-2"></i>{t("External Contributors")}</h5>
                <button className="btn btn-outline-primary btn-sm" onClick={() => setShowExtModal(true)}>
                    <i className="bi bi-plus-lg me-1"></i>{t("Add External")}
                </button>
            </div>
            <div className="card border-0 shadow-sm">
                <div className="table-responsive">
                    <table className="table table-hover mb-0">
                        <thead className="bg-light">
                        <tr>
                            <th className="ps-4 py-3">{t("Name")}</th>
                            <th className="py-3">{t("Email")}</th>
                            <th className="py-3">{t("Organization")}</th>
                            <th className="py-3">{t("System Role")}</th>
                            <th className="py-3">{t("Scope")}</th>
                            <th className="py-3 text-end pe-4">{t("Actions")}</th>
                        </tr>
                        </thead>
                        <tbody>
                        {externalContributors.length === 0 ? (
                            <tr>
                                <td colSpan="6" className="text-center py-4 text-muted">
                                    {t("No external contributors yet.")}
                                </td>
                            </tr>
                        ) : (
                            externalContributors.map(ec => (
                                <tr key={ec.id}>
                                    <td className="ps-4 py-3 fw-bold">{ec.displayName}</td>
                                    <td className="text-muted">{ec.email || '—'}</td>
                                    <td>{ec.organization || '—'}</td>
                                    <td>
                                        {ec.roleName ? (
                                            <span className="badge bg-secondary-subtle text-secondary">{ec.roleName}</span>
                                        ) : '—'}
                                    </td>
                                    <td><span className="badge bg-light text-dark border">{ec.scope}</span></td>
                                    <td className="text-end pe-4">
                                        <button
                                            className="btn btn-sm btn-outline-danger"
                                            onClick={() => handleRevokeExternal(ec)}
                                            title={t("Revoke Access")}
                                        >
                                            <i className="bi bi-x-circle"></i>
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Add External Contributor Modal */}
            {showExtModal && (
                <PortalModal onClick={() => setShowExtModal(false)}>
                    <div className="modal-header">
                        <h5 className="modal-title">{t("Add External Contributor")}</h5>
                        <button type="button" className="btn-close" onClick={() => setShowExtModal(false)} />
                    </div>
                    <div className="modal-body">
                        <div className="mb-3">
                            <label className="form-label fw-bold">{t("Display Name *")}</label>
                            <input type="text" className="form-control" value={extName} onChange={e => setExtName(e.target.value)} />
                        </div>
                        <div className="mb-3">
                            <label className="form-label">{t("Email")}</label>
                            <input type="email" className="form-control" value={extEmail} onChange={e => setExtEmail(e.target.value)} />
                        </div>
                        <div className="mb-3">
                            <label className="form-label">{t("Organization")}</label>
                            <input type="text" className="form-control" value={extOrg} onChange={e => setExtOrg(e.target.value)} />
                        </div>
                        <div className="mb-3">
                            <label className="form-label">{t("System Role")}</label>
                            <select className="form-select" value={extRoleId} onChange={e => setExtRoleId(e.target.value)}>
                                <option value="">{t("None")}</option>
                                {systemRoles.map(r => (
                                    <option key={r.id} value={r.id}>{r.name}</option>
                                ))}
                            </select>
                        </div>
                        <div className="row">
                            <div className="col-6 mb-3">
                                <label className="form-label">{t("Scope")}</label>
                                <select className="form-select" value={extScope} onChange={e => setExtScope(e.target.value)}>
                                    <option value="PROJECT">{t("Project")}</option>
                                    <option value="DELIVERABLE">{t("Deliverable")}</option>
                                    <option value="REVIEW">{t("Review")}</option>
                                </select>
                            </div>
                            <div className="col-6 mb-3">
                                <label className="form-label">{t("Linked Entity")}</label>
                                {extScope === 'PROJECT' ? (
                                    <select className="form-select" value={extScopeEntityId} onChange={e => setExtScopeEntityId(e.target.value)}>
                                        <option value="">{t("All")}</option>
                                        {projects.map(p => (
                                            <option key={p.id} value={p.id}>{p.name}</option>
                                        ))}
                                    </select>
                                ) : (
                                    <input type="text" className="form-control" placeholder="ID" value={extScopeEntityId} onChange={e => setExtScopeEntityId(e.target.value)} />
                                )}
                            </div>
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button className="btn btn-secondary" onClick={() => setShowExtModal(false)}>{t("Cancel")}</button>
                        <button className="btn btn-primary" onClick={handleAddExternal} disabled={!extName.trim()}>
                            {t("Add External")}
                        </button>
                    </div>
                </PortalModal>
            )}

            {/* Edit Ghost Modal */}
            {showEditModal && editGhost && (
                <PortalModal onClick={() => setShowEditModal(false)}>
                    <div className="modal-header">
                        <h5 className="modal-title">{t("Edit Member")}</h5>
                        <button type="button" className="btn-close" onClick={() => setShowEditModal(false)} />
                    </div>
                    <div className="modal-body">
                        <div className="mb-3">
                            <label className="form-label fw-bold">{t("Display Name *")}</label>
                            <input
                                type="text"
                                className="form-control"
                                value={editName}
                                onChange={(e) => setEditName(e.target.value)}
                            />
                        </div>
                        <div className="mb-3">
                            <label className="form-label fw-bold">{t("Username Identifier *")}</label>
                            <input
                                type="text"
                                className="form-control"
                                value={editUsername}
                                onChange={(e) => setEditUsername(e.target.value)}
                            />
                        </div>
                        <hr />
                        <div className="mb-3">
                            <label className="form-label fw-bold">{t("Projects")}</label>
                            {projects.length === 0 ? (
                                <p className="text-muted small">{t("No active projects")}</p>
                            ) : (
                                <div className="list-group">
                                    {projects.map(p => (
                                        <label key={p.id} className="list-group-item d-flex align-items-center gap-2" style={{ cursor: 'pointer' }}>
                                            <input
                                                type="checkbox"
                                                className="form-check-input m-0"
                                                checked={editProjects.has(p.id)}
                                                onChange={() => toggleEditProject(p.id)}
                                            />
                                            <span>{p.name}</span>
                                        </label>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button className="btn btn-secondary" onClick={() => setShowEditModal(false)}>{t("Cancel")}</button>
                        <button className="btn btn-primary" onClick={handleEditGhost} disabled={savingEdit || !editName || !editUsername}>
                            {savingEdit ? t("Loading...") : t("Save Changes")}
                        </button>
                    </div>
                </PortalModal>
            )}

            {/* Add Member Modal - CORRETTO: senza doppio wrapping */}
            {showModal && (
                <PortalModal onClick={() => setShowModal(false)}>
                    <div className="modal-header">
                        <h5 className="modal-title">{t("Add Member to Team")}</h5>
                        <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
                    </div>
                    <div className="modal-body">
                        {/* Project Selection */}
                        <div className="mb-3">
                            <label className="form-label fw-bold">{t("Project *")}</label>
                            <select
                                className="form-select"
                                value={targetProjectId}
                                onChange={(e) => setTargetProjectId(e.target.value)}
                            >
                                <option value="">{t("Select project...")}</option>
                                {projects.map(p => (
                                    <option key={p.id} value={p.id}>{p.name}</option>
                                ))}
                            </select>
                        </div>

                        <hr />

                        {/* User Type Toggle */}
                        <div className="mb-3">
                            <div className="form-check form-switch">
                                <input
                                    className="form-check-input"
                                    type="checkbox"
                                    id="userTypeSwitch"
                                    checked={isRealUser}
                                    onChange={(e) => setIsRealUser(e.target.checked)}
                                />
                                <label className="form-check-label fw-bold" htmlFor="userTypeSwitch">
                                    {isRealUser ? t("Connect LibrePM User (Friend)") : t("Create Local User (Virtual)")}
                                </label>
                            </div>
                            <div className="form-text small mt-1">
                                {isRealUser
                                    ? t("Select one of your contacts...")
                                    : t("Create a local profile...")
                                }
                            </div>
                        </div>

                        {/* Dynamic Form Fields */}
                        {isRealUser ? (
                            <div className="mb-3 fade-in">
                                <label className="form-label">{t("Select Contact *")}</label>
                                <select
                                    className="form-select"
                                    value={selectedFriendId}
                                    onChange={(e) => setSelectedFriendId(e.target.value)}
                                >
                                    <option value="">{t("-- Select contact --")}</option>
                                    {friends.length > 0 && (
                                        <optgroup label={t("Contacts")}>
                                            {friends.map(u => {
                                                const alreadyMember = targetProjectId && members.some(m => m.projectId === targetProjectId && m.user.id === u.id);
                                                return (
                                                    <option key={u.id} value={u.id} disabled={alreadyMember}>
                                                        {u.displayName || u.username} (@{u.username}){alreadyMember ? ` - ${t("Already member")}` : ''}
                                                    </option>
                                                );
                                            })}
                                        </optgroup>
                                    )}
                                    {ghosts.length > 0 && (
                                        <optgroup label={t("Virtual Members")}>
                                            {ghosts.map(u => {
                                                const alreadyMember = targetProjectId && members.some(m => m.projectId === targetProjectId && m.user.id === u.id);
                                                return (
                                                    <option key={u.id} value={u.id} disabled={alreadyMember}>
                                                        {u.displayName || u.username} (@{u.username}){alreadyMember ? ` - ${t("Already member")}` : ''}
                                                    </option>
                                                );
                                            })}
                                        </optgroup>
                                    )}
                                </select>
                                {friends.length === 0 && ghosts.length === 0 && (
                                    <div className="alert alert-warning mt-2 small">
                                        {t("No connections yet...")}
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="fade-in">
                                <div className="mb-3">
                                    <label className="form-label">{t("Display Name *")}</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder={t("Ex. John Doe (External)")}
                                        value={localName}
                                        onChange={(e) => setLocalName(e.target.value)}
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">{t("Username Identifier *")}</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder={t("Ex. john.doe.ext")}
                                        value={localUsername}
                                        onChange={(e) => setLocalUsername(e.target.value)}
                                    />
                                    <div className="form-text">{t("Must be unique in the system.")}</div>
                                </div>
                            </div>
                        )}
                    </div>
                    <div className="modal-footer">
                        <button className="btn btn-secondary" onClick={() => setShowModal(false)}>{t("Cancel")}</button>
                        <button className="btn btn-primary" onClick={handleAddMember}>
                            {isRealUser ? t("Add to Team") : t("Create Local")}
                        </button>
                    </div>
                </PortalModal>
            )}
        </div>
    );
};

export default TeamPage;
