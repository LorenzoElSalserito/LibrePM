import React, { useState, useEffect } from 'react';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';
import PortalModal from '../components/PortalModal';
import { useModal } from '../hooks/useModal.js';

export default function ConnectionsPage({ shell }) {
    const { t } = useTranslation();
    const modal = useModal();
    const [friends, setFriends] = useState([]);
    const [ghosts, setGhosts] = useState([]);
    const [pendingIncoming, setPendingIncoming] = useState([]);
    const [pendingOutgoing, setPendingOutgoing] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(true);
    const [ghostName, setGhostName] = useState('');
    const [ghostUsername, setGhostUsername] = useState('');
    const [creatingGhost, setCreatingGhost] = useState(false);
    const [projects, setProjects] = useState([]);
    const [showAssignModal, setShowAssignModal] = useState(false);
    const [newGhostId, setNewGhostId] = useState(null);
    const [newGhostName, setNewGhostName] = useState('');
    const [selectedProjects, setSelectedProjects] = useState(new Set());
    const [assigning, setAssigning] = useState(false);

    // Edit ghost state
    const [showEditModal, setShowEditModal] = useState(false);
    const [editGhost, setEditGhost] = useState(null);
    const [editName, setEditName] = useState('');
    const [editUsername, setEditUsername] = useState('');
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        shell?.setTitle?.(t("Contacts"));
        shell?.setRightPanel?.(
            <div className="d-flex flex-column gap-3">
                <div className="fw-bold">{t("Your Connections")}</div>
                <p className="small text-muted">
                    {t("Manage your LibrePM network here...")}
                </p>
                <div className="alert alert-info small">
                    <i className="bi bi-lightbulb me-2"></i>
                    {t("Only connected Contacts can be added...")}
                </div>
            </div>
        );
        shell?.setHeaderActions?.(null);

        loadData();

        return () => {
            shell?.setHeaderActions?.(null);
            shell?.setRightPanel?.(null);
        };
    }, [shell, t]);

    const loadData = async () => {
        try {
            setLoading(true);

            const [friendsList, ghostsList, incoming, outgoing, projectsList] = await Promise.all([
                librepm.connectionsList(),
                librepm.ghostsList(),
                librepm.connectionsPendingIncoming(),
                librepm.connectionsPendingOutgoing(),
                librepm.projectsList().catch(() => [])
            ]);

            setFriends(friendsList);
            setGhosts(ghostsList);
            setPendingIncoming(incoming);
            setPendingOutgoing(outgoing);
            setProjects(projectsList);
        } catch (e) {
            console.error("Errore caricamento connessioni:", e);
            toast.error(t("Error loading"));
        } finally {
            setLoading(false);
        }
    };

    const handleCreateGhost = async () => {
        if (!ghostUsername || !ghostName) {
            toast.warning(t("Please fix form errors"));
            return;
        }
        try {
            setCreatingGhost(true);
            const created = await librepm.ghostsCreate(ghostUsername, ghostName);
            toast.success(t("Success"));
            const createdName = ghostName;
            setGhostName('');
            setGhostUsername('');
            loadData();

            // Open project assignment modal if there are projects
            if (projects.length > 0 && created?.id) {
                setNewGhostId(created.id);
                setNewGhostName(createdName);
                setSelectedProjects(new Set());
                setShowAssignModal(true);
            }
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        } finally {
            setCreatingGhost(false);
        }
    };

    const handleAssignToProjects = async () => {
        if (selectedProjects.size === 0 || !newGhostId) {
            setShowAssignModal(false);
            return;
        }
        try {
            setAssigning(true);
            for (const projectId of selectedProjects) {
                await librepm.projectMembersAdd(projectId, newGhostId, "EDITOR");
            }
            toast.success(t("Success"));
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        } finally {
            setAssigning(false);
            setShowAssignModal(false);
            setNewGhostId(null);
            setNewGhostName('');
            setSelectedProjects(new Set());
        }
    };

    const toggleProject = (projectId) => {
        setSelectedProjects(prev => {
            const next = new Set(prev);
            if (next.has(projectId)) next.delete(projectId);
            else next.add(projectId);
            return next;
        });
    };

    const handleSearch = async () => {
        if (!searchQuery || searchQuery.length < 2) return;
        try {
            const results = await librepm.usersSearch(searchQuery);
            // Filter out self and those who are already friends/pending
            const currentUserId = librepm.getCurrentUser();
            const friendIds = new Set(friends.map(f => f.id));
            const pendingIds = new Set([
                ...pendingIncoming.map(p => p.user.id),
                ...pendingOutgoing.map(p => p.user.id)
            ]);
            
            setSearchResults(results.filter(u => 
                u.id !== currentUserId && 
                !friendIds.has(u.id) && 
                !pendingIds.has(u.id)
            ));
        } catch (e) {
            toast.error(t("Error"));
        }
    };

    const sendRequest = async (targetId) => {
        try {
            await librepm.connectionsRequest(targetId);
            toast.success(t("Success"));
            setSearchResults(prev => prev.filter(u => u.id !== targetId));
            loadData();
        } catch (e) {
            toast.error(t("Error"));
        }
    };

    const acceptRequest = async (connectionId) => {
        try {
            await librepm.connectionsAccept(connectionId);
            toast.success(t("Success"));
            loadData();
        } catch (e) {
            toast.error(t("Error"));
        }
    };

    const rejectRequest = async (connectionId) => {
        try {
            await librepm.connectionsReject(connectionId);
            toast.info(t("Rejected"));
            loadData();
        } catch (e) {
            toast.error(t("Error"));
        }
    };

    const removeFriend = async (friendId) => {
        const confirmed = await modal.confirm({ title: t("Are you sure you want to delete") });
        if (!confirmed) return;
        try {
            await librepm.connectionsRemove(friendId);
            toast.success(t("Deleted successfully"));
            loadData();
        } catch (e) {
            toast.error(t("Deletion error"));
        }
    };

    const openEditGhost = (ghost) => {
        setEditGhost(ghost);
        setEditName(ghost.displayName || '');
        setEditUsername(ghost.username || '');
        setShowEditModal(true);
    };

    const handleEditGhost = async () => {
        if (!editGhost || !editName || !editUsername) return;
        try {
            setSaving(true);
            await librepm.ghostsUpdate(editGhost.id, { username: editUsername, displayName: editName });
            toast.success(t("Member updated"));
            setShowEditModal(false);
            setEditGhost(null);
            loadData();
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        } finally {
            setSaving(false);
        }
    };

    const deleteGhost = async (ghostId) => {
        const confirmed = await modal.confirm({ title: t("Are you sure you want to delete") });
        if (!confirmed) return;
        try {
            await librepm.ghostsDelete(ghostId);
            toast.success(t("Deleted successfully"));
            loadData();
        } catch (e) {
            toast.error(t("Deletion error") + ": " + e.message);
        }
    };

    if (loading) return <div className="p-5 text-center"><div className="spinner-border text-primary"></div></div>;

    return (
        <div className="container-fluid p-4 fade-in">
            <div className="row g-4">
                {/* Colonna Sinistra: Lista Amici e Richieste */}
                <div className="col-md-7">
                    {/* Richieste in Arrivo */}
                    {pendingIncoming.length > 0 && (
                        <div className="card mb-4 border-primary">
                            <div className="card-header bg-primary-subtle text-primary fw-bold">
                                {t("Incoming Requests")} ({pendingIncoming.length})
                            </div>
                            <ul className="list-group list-group-flush">
                                {pendingIncoming.map(req => (
                                    <li key={req.id} className="list-group-item d-flex justify-content-between align-items-center">
                                        <div>
                                            <div className="fw-bold">{req.user.displayName || req.user.username}</div>
                                            <small className="text-muted">@{req.user.username}</small>
                                        </div>
                                        <div className="btn-group">
                                            <button className="btn btn-sm btn-success" onClick={() => acceptRequest(req.id)}>{t("Accept")}</button>
                                            <button className="btn btn-sm btn-outline-danger" onClick={() => rejectRequest(req.id)}>{t("Reject")}</button>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {/* Lista Amici */}
                    <div className="card shadow-sm">
                        <div className="card-header bg-white fw-bold">
                            {t("Your Contacts")} ({friends.length})
                        </div>
                        {friends.length === 0 ? (
                            <div className="p-4 text-center text-muted">
                                {t("No connections yet...")}
                            </div>
                        ) : (
                            <ul className="list-group list-group-flush">
                                {friends.map(friend => (
                                    <li key={friend.id} className="list-group-item d-flex justify-content-between align-items-center">
                                        <div className="d-flex align-items-center gap-3">
                                            <div className="avatar-circle bg-secondary text-white d-flex align-items-center justify-content-center" style={{width: 40, height: 40, borderRadius: '50%'}}>
                                                {(friend.displayName || friend.username).substring(0, 2).toUpperCase()}
                                            </div>
                                            <div>
                                                <div className="fw-bold">{friend.displayName || friend.username}</div>
                                                <small className="text-muted">@{friend.username}</small>
                                            </div>
                                        </div>
                                        <button className="btn btn-sm btn-outline-danger" onClick={() => removeFriend(friend.id)} title={t("Remove")}>
                                            <i className="bi bi-person-x"></i>
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    {/* Virtual Members (Ghost Users) */}
                    <div className="card shadow-sm mt-4">
                        <div className="card-header bg-white fw-bold d-flex justify-content-between align-items-center">
                            <span>{t("Virtual Members")} ({ghosts.length})</span>
                        </div>
                        {ghosts.length === 0 ? (
                            <div className="p-4 text-center text-muted">
                                {t("No virtual members yet.")}
                            </div>
                        ) : (
                            <ul className="list-group list-group-flush">
                                {ghosts.map(ghost => (
                                    <li key={ghost.id} className="list-group-item d-flex justify-content-between align-items-center">
                                        <div className="d-flex align-items-center gap-3">
                                            <div className="avatar-circle bg-secondary-subtle text-secondary d-flex align-items-center justify-content-center" style={{width: 40, height: 40, borderRadius: '50%'}}>
                                                {(ghost.displayName || ghost.username).substring(0, 2).toUpperCase()}
                                            </div>
                                            <div>
                                                <div className="fw-bold">{ghost.displayName || ghost.username}</div>
                                                <small className="text-muted">@{ghost.username}</small>
                                            </div>
                                            <span className="badge bg-secondary-subtle text-secondary border border-secondary-subtle">{t("Virtual")}</span>
                                        </div>
                                        <div className="btn-group">
                                            <button className="btn btn-sm btn-outline-primary" onClick={() => openEditGhost(ghost)} title={t("Edit")}>
                                                <i className="bi bi-pencil"></i>
                                            </button>
                                            <button className="btn btn-sm btn-outline-danger" onClick={() => deleteGhost(ghost.id)} title={t("Remove")}>
                                                <i className="bi bi-trash"></i>
                                            </button>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        )}
                        {/* Inline create ghost form */}
                        <div className="card-footer bg-white">
                            <div className="row g-2 align-items-end">
                                <div className="col">
                                    <input
                                        type="text"
                                        className="form-control form-control-sm"
                                        placeholder={t("Display Name")}
                                        value={ghostName}
                                        onChange={(e) => setGhostName(e.target.value)}
                                    />
                                </div>
                                <div className="col">
                                    <input
                                        type="text"
                                        className="form-control form-control-sm"
                                        placeholder={t("Username")}
                                        value={ghostUsername}
                                        onChange={(e) => setGhostUsername(e.target.value)}
                                        onKeyDown={(e) => e.key === 'Enter' && handleCreateGhost()}
                                    />
                                </div>
                                <div className="col-auto">
                                    <button
                                        className="btn btn-sm btn-outline-secondary"
                                        onClick={handleCreateGhost}
                                        disabled={creatingGhost}
                                    >
                                        <i className="bi bi-plus-lg me-1"></i>
                                        {t("Create")}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Colonna Destra: Cerca e Aggiungi */}
                <div className="col-md-5">
                    <div className="card shadow-sm">
                        <div className="card-header bg-white fw-bold">
                            {t("Search Contacts")}
                        </div>
                        <div className="card-body">
                            <div className="input-group mb-3">
                                <input 
                                    type="text" 
                                    className="form-control" 
                                    placeholder={t("Username or email...")}
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                                />
                                <button className="btn btn-primary" onClick={handleSearch}>{t("Search")}</button>
                            </div>

                            {searchResults.length > 0 && (
                                <div className="list-group">
                                    {searchResults.map(user => (
                                        <div key={user.id} className="list-group-item d-flex justify-content-between align-items-center">
                                            <div>
                                                <div className="fw-bold">{user.displayName || user.username}</div>
                                                <small className="text-muted">@{user.username}</small>
                                            </div>
                                            <button className="btn btn-sm btn-outline-primary" onClick={() => sendRequest(user.id)}>
                                                <i className="bi bi-person-plus me-1"></i>
                                                {t("Connect")}
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                            
                            {pendingOutgoing.length > 0 && (
                                <div className="mt-4">
                                    <h6 className="small text-muted text-uppercase fw-bold mb-2">{t("Sent Requests")}</h6>
                                    <ul className="list-group list-group-flush small">
                                        {pendingOutgoing.map(req => (
                                            <li key={req.id} className="list-group-item d-flex justify-content-between align-items-center px-0">
                                                <span className="text-muted">{t("To:")} {req.user.displayName || req.user.username}</span>
                                                <span className="badge bg-light text-dark border">{t("Pending")}</span>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Modal: Edit ghost */}
            {showEditModal && editGhost && (
                <PortalModal onClick={() => setShowEditModal(false)}>
                    <div className="modal-header">
                        <h5 className="modal-title">{t("Edit Member")}</h5>
                        <button type="button" className="btn-close" onClick={() => setShowEditModal(false)} />
                    </div>
                    <div className="modal-body">
                        <div className="mb-3">
                            <label className="form-label fw-bold">{t("Display Name")}</label>
                            <input
                                type="text"
                                className="form-control"
                                value={editName}
                                onChange={(e) => setEditName(e.target.value)}
                            />
                        </div>
                        <div className="mb-3">
                            <label className="form-label fw-bold">{t("Username")}</label>
                            <input
                                type="text"
                                className="form-control"
                                value={editUsername}
                                onChange={(e) => setEditUsername(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && handleEditGhost()}
                            />
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button className="btn btn-secondary" onClick={() => setShowEditModal(false)}>{t("Cancel")}</button>
                        <button className="btn btn-primary" onClick={handleEditGhost} disabled={saving || !editName || !editUsername}>
                            {saving ? t("Loading...") : t("Save Changes")}
                        </button>
                    </div>
                </PortalModal>
            )}

            {/* Modal: Assign ghost to projects */}
            {showAssignModal && (
                <PortalModal onClick={() => setShowAssignModal(false)}>
                    <div className="modal-header">
                        <h5 className="modal-title">{t("Assign to Projects")}</h5>
                        <button type="button" className="btn-close" onClick={() => setShowAssignModal(false)} />
                    </div>
                    <div className="modal-body">
                        <p className="text-muted small mb-3">
                            {t("Select projects for this member")}: <strong>{newGhostName}</strong>
                        </p>
                        {projects.length === 0 ? (
                            <p className="text-muted">{t("No active projects")}</p>
                        ) : (
                            <div className="list-group">
                                {projects.map(p => (
                                    <label key={p.id} className="list-group-item d-flex align-items-center gap-2" style={{ cursor: 'pointer' }}>
                                        <input
                                            type="checkbox"
                                            className="form-check-input m-0"
                                            checked={selectedProjects.has(p.id)}
                                            onChange={() => toggleProject(p.id)}
                                        />
                                        <span>{p.name}</span>
                                    </label>
                                ))}
                            </div>
                        )}
                    </div>
                    <div className="modal-footer">
                        <button className="btn btn-outline-secondary" onClick={() => setShowAssignModal(false)}>
                            {t("Skip")}
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleAssignToProjects}
                            disabled={assigning || selectedProjects.size === 0}
                        >
                            {assigning ? t("Loading...") : t("Assign")}
                        </button>
                    </div>
                </PortalModal>
            )}
        </div>
    );
}
