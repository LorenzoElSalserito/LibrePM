import { useState, useEffect } from "react";
import { librepm } from "@api/librepm.js";
import { toast } from "react-toastify";
import PortalModal from "./PortalModal.jsx";
import { useTranslation } from 'react-i18next';
import { useModal } from "../hooks/useModal.js";

export default function ProjectTeamModal({ project, onClose }) {
    const { t } = useTranslation();
    const [members, setMembers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState("list"); // list, add
    const modal = useModal();

    // Add Member State
    const [isRealUser, setIsRealUser] = useState(false);
    const [selectedRole, setSelectedRole] = useState("EDITOR");

    // Search Real User State
    const [searchQuery, setSearchQuery] = useState("");
    const [searchResults, setSearchResults] = useState([]);
    const [selectedRealUser, setSelectedRealUser] = useState(null);
    const [isSearching, setIsSearching] = useState(false);

    // Create Local User State
    const [localName, setLocalName] = useState("");
    const [localUsername, setLocalUsername] = useState("");

    useEffect(() => {
        loadMembers();
    }, [project.id]);

    // Debounce search
    useEffect(() => {
        if (!isRealUser || !searchQuery || searchQuery.length < 2) {
            setSearchResults([]);
            return;
        }

        const timer = setTimeout(async () => {
            setIsSearching(true);
            try {
                const results = await librepm.usersSearch(searchQuery);
                // Filter out users who are already members
                const memberIds = new Set(members.map(m => m.user.id));
                setSearchResults(results.filter(u => !memberIds.has(u.id)));
            } catch (e) {
                console.error("Search error:", e);
            } finally {
                setIsSearching(false);
            }
        }, 300);

        return () => clearTimeout(timer);
    }, [searchQuery, isRealUser, members]);

    const loadMembers = async () => {
        try {
            setLoading(true);
            const list = await librepm.projectMembersList(project.id);
            setMembers(list || []);
        } catch (e) {
            toast.error(t("Error loading") + ": " + e.message);
        } finally {
            setLoading(false);
        }
    };

    const handleAddMember = async () => {
        try {
            if (isRealUser) {
                if (!selectedRealUser) return;
                await librepm.projectMembersAdd(project.id, selectedRealUser.id, selectedRole);
                toast.success(t("Success"));
            } else {
                if (!localName || !localUsername) return;
                await librepm.projectMembersCreateGhost(project.id, localUsername, localName);
                toast.success(t("Success"));
            }
            setActiveTab("list");
            loadMembers();
            // Reset form
            setSearchQuery("");
            setSelectedRealUser(null);
            setLocalName("");
            setLocalUsername("");
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        }
    };

    const handleRemoveMember = async (userId) => {
        const confirmed = await modal.confirm({
            title: t("Are you sure you want to delete"),
            message: t("This action is irreversible."),
        });
        if (!confirmed) return;
        try {
            await librepm.projectMembersRemove(project.id, userId);
            toast.success(t("Deleted successfully"));
            loadMembers();
        } catch (e) {
            toast.error(t("Deletion error") + ": " + e.message);
        }
    };

    return (
        <PortalModal onClick={onClose}>
            {/* NOTE: PortalModal already provides modal-dialog and modal-content,
                so here we only pass the inner content */}
            <div className="modal-header">
                <h5 className="modal-title">{t("Team Management:")} {project.name}</h5>
                <button type="button" className="btn-close" onClick={onClose}></button>
            </div>
            <div className="modal-body">
                <ul className="nav nav-tabs mb-3">
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === "list" ? "active" : ""}`} onClick={() => setActiveTab("list")}>{t("Members")}</button>
                    </li>
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === "add" ? "active" : ""}`} onClick={() => setActiveTab("add")}>{t("Add")}</button>
                    </li>
                </ul>

                {activeTab === "list" && (
                    <div className="list-group list-group-flush">
                        {loading ? (
                            <div className="text-center py-3"><div className="spinner-border spinner-border-sm"></div></div>
                        ) : members.length === 0 ? (
                            <div className="text-center text-muted py-3">{t("No members")}</div>
                        ) : (
                            members.map(m => (
                                <div key={m.user.id} className="list-group-item d-flex justify-content-between align-items-center">
                                    <div>
                                        <div className="fw-bold d-flex align-items-center gap-2">
                                            {m.user.displayName || m.user.username}
                                            {m.user.isGhost ? (
                                                <span className="badge bg-secondary-subtle text-secondary border border-secondary-subtle" style={{fontSize: '0.6rem'}}>{t("LOCAL")}</span>
                                            ) : (
                                                <span className="badge bg-primary-subtle text-primary border border-primary-subtle" style={{fontSize: '0.6rem'}}>{t("LIBREPM")}</span>
                                            )}
                                        </div>
                                        <small className="text-muted">{m.role}</small>
                                    </div>
                                    {m.role !== "OWNER" && (
                                        <button className="btn btn-sm btn-outline-danger" onClick={() => handleRemoveMember(m.user.id)}>
                                            <i className="bi bi-trash"></i>
                                        </button>
                                    )}
                                </div>
                            ))
                        )}
                    </div>
                )}

                {activeTab === "add" && (
                    <div className="d-flex flex-column gap-3">
                        <div className="form-check form-switch mb-2">
                            <input
                                className="form-check-input"
                                type="checkbox"
                                id="modalUserTypeSwitch"
                                checked={isRealUser}
                                onChange={(e) => setIsRealUser(e.target.checked)}
                            />
                            <label className="form-check-label fw-bold" htmlFor="modalUserTypeSwitch">
                                {isRealUser ? t("Connect LibrePM User") : t("Create Local User")}
                            </label>
                        </div>

                        {isRealUser ? (
                            <>
                                <div className="position-relative">
                                    <label className="form-label">{t("Search User")}</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder={t("Name, username or email...")}
                                        value={searchQuery}
                                        onChange={(e) => {
                                            setSearchQuery(e.target.value);
                                            setSelectedRealUser(null);
                                        }}
                                    />
                                    {isSearching && (
                                        <div className="position-absolute top-50 end-0 translate-middle-y me-2" style={{marginTop: 12}}>
                                            <div className="spinner-border spinner-border-sm text-secondary"></div>
                                        </div>
                                    )}

                                    {/* Search Results Dropdown */}
                                    {searchResults.length > 0 && !selectedRealUser && (
                                        <div className="list-group position-absolute w-100 mt-1 shadow-sm" style={{ zIndex: 1060, maxHeight: 200, overflowY: 'auto' }}>
                                            {searchResults.map(u => (
                                                <button
                                                    key={u.id}
                                                    className="list-group-item list-group-item-action"
                                                    onClick={() => {
                                                        setSelectedRealUser(u);
                                                        setSearchQuery(u.displayName || u.username);
                                                        setSearchResults([]);
                                                    }}
                                                >
                                                    <div className="fw-bold">{u.displayName || u.username}</div>
                                                    <small className="text-muted">@{u.username}</small>
                                                </button>
                                            ))}
                                        </div>
                                    )}
                                </div>
                                {selectedRealUser && (
                                    <div className="text-success small">
                                        <i className="bi bi-check-circle me-1"></i>
                                        {t("Selected:")} <strong>{selectedRealUser.displayName || selectedRealUser.username}</strong>
                                    </div>
                                )}
                                <div>
                                    <label className="form-label">{t("Role")}</label>
                                    <select className="form-select" value={selectedRole} onChange={(e) => setSelectedRole(e.target.value)}>
                                        <option value="EDITOR">{t("Editor")}</option>
                                        <option value="VIEWER">{t("Viewer")}</option>
                                    </select>
                                </div>
                            </>
                        ) : (
                            <>
                                <div>
                                    <label className="form-label">{t("Display Name")}</label>
                                    <input type="text" className="form-control" value={localName} onChange={(e) => setLocalName(e.target.value)} placeholder={t("Ex. John Doe (External)")} />
                                </div>
                                <div>
                                    <label className="form-label">{t("Username (unique)")}</label>
                                    <input type="text" className="form-control" value={localUsername} onChange={(e) => setLocalUsername(e.target.value)} placeholder={t("john.doe.local")} />
                                </div>
                            </>
                        )}

                        <button
                            className="btn btn-primary mt-2"
                            onClick={handleAddMember}
                            disabled={isRealUser ? !selectedRealUser : (!localName || !localUsername)}
                        >
                            {isRealUser ? t("Add to Team") : t("Create and Add")}
                        </button>
                    </div>
                )}
            </div>
        </PortalModal>
    );
}
