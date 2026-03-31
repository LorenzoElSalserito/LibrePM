import React, { useState, useEffect } from 'react';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import { Tooltip } from 'react-tooltip';
import PortalModal from '../components/PortalModal.jsx';
import { useTranslation } from 'react-i18next';
import { useModal } from '../hooks/useModal.js';

// Format date as YYYY-MM-DD using local time (avoids DST/UTC shift bugs)
const toLocalDateStr = (d) => {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

const ResourcePage = ({ shell }) => {
    const { t, i18n } = useTranslation();
    const modal = useModal();
    const [projects, setProjects] = useState([]);
    const [selectedProjectId, setSelectedProjectId] = useState('');
    const [allocations, setAllocations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
    const [rangeDays, setRangeDays] = useState(14); // 2 settimane default

    // Modal State
    const [showTaskModal, setShowTaskModal] = useState(false);
    const [showMemberModal, setShowMemberModal] = useState(false);
    const [showAddMemberModal, setShowAddMemberModal] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const [selectedDate, setSelectedDate] = useState(null);
    const [userTasks, setUserTasks] = useState([]);
    const [availableMembers, setAvailableMembers] = useState([]);
    const [addingMember, setAddingMember] = useState(false);

    useEffect(() => {
        shell?.setTitle?.(t("Resources & Workload"));
        shell?.setHeaderActions?.(
            <div className="d-flex gap-2 align-items-center">
                <select
                    className="form-select form-select-sm"
                    value={selectedProjectId}
                    onChange={(e) => setSelectedProjectId(e.target.value)}
                    style={{ width: 200 }}
                >
                    {projects.map(p => (
                        <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                </select>
                <button
                    className="btn btn-sm btn-outline-secondary"
                    onClick={loadAllocations}
                    title={t("Refresh")}
                >
                    <i className="bi bi-arrow-clockwise"></i>
                </button>
            </div>
        );

        loadProjects();

        return () => {
            shell?.setHeaderActions?.(null);
        };
    }, [shell, selectedProjectId, projects, t]);

    useEffect(() => {
        if (selectedProjectId) {
            loadAllocations();
        } else {
            setAllocations([]);
        }
    }, [selectedProjectId, startDate, rangeDays]);

    const loadProjects = async () => {
        try {
            const list = await librepm.projectsList({ archived: false });
            setProjects(list);
            if (list.length > 0) {
                setSelectedProjectId(list[0].id);
            }
        } catch (e) {
            toast.error(t("Error loading"));
        } finally {
            setLoading(false);
        }
    };

    const loadAllocations = async () => {
        try {
            setLoading(true);
            const start = new Date(startDate);
            const end = new Date(start);
            end.setDate(end.getDate() + rangeDays - 1);

            const data = await librepm.resourceAllocation(selectedProjectId, startDate, toLocalDateStr(end));
            setAllocations(data.allocations || []); // Ensure allocations is array

        } catch (e) {
            console.error(e);
            toast.error(t("Error loading"));
        } finally {
            setLoading(false);
        }
    };

    const handleMemberClick = (userId, userName) => {
        setSelectedUser({ id: userId, name: userName });
        setShowMemberModal(true);
    };

    const handleCellClick = async (userId, userName, dateStr) => {
        setSelectedUser({ id: userId, name: userName });
        setSelectedDate(dateStr);
        setShowTaskModal(true);

        try {
            const tasks = await librepm.tasksList(selectedProjectId);
            const date = new Date(dateStr);
            date.setHours(0,0,0,0);

            const filtered = tasks.filter(t => {
                const assigneeId = t.assignedTo ? t.assignedTo.id : (t.owner || "unassigned"); // Fallback if assignedTo object missing
                // Check assignee ID or username if simple string
                if (t.assignedTo && t.assignedTo.id === userId) {
                     // match
                } else if (!t.assignedTo && t.owner === userName) {
                     // match owner as fallback
                } else {
                    return false;
                }

                // Check date range intersection
                if (t.plannedStart) {
                    const start = new Date(t.plannedStart);
                    const end = t.plannedFinish ? new Date(t.plannedFinish) : start;
                    start.setHours(0,0,0,0);
                    end.setHours(0,0,0,0);
                    return date >= start && date <= end;
                } else if (t.deadline) {
                    const deadline = new Date(t.deadline);
                    deadline.setHours(0,0,0,0);
                    return deadline.getTime() === date.getTime();
                }
                return false;
            });

            setUserTasks(filtered);
        } catch (e) {
            console.error(e);
            toast.error(t("Error loading"));
        }
    };

    const handleUnassignTask = async (taskId) => {
        const confirmed = await modal.confirm({ title: t("Confirm") });
        if (!confirmed) return;
        try {
            // Setting assignedToId to null or empty string to unassign
            await librepm.tasksUpdate(selectedProjectId, taskId, { assignedToId: null });
            toast.success(t("Success"));
            setShowTaskModal(false);
            loadAllocations(); // Reload grid
        } catch (e) {
            toast.error(t("Update error"));
        }
    };

    const openAddMemberModal = async () => {
        try {
            const [friends, ghosts, currentMembers] = await Promise.all([
                librepm.connectionsList(),
                librepm.ghostsList(),
                librepm.projectMembersList(selectedProjectId)
            ]);
            const memberIds = new Set(currentMembers.map(m => m.id));
            const candidates = [...friends, ...ghosts].filter(u => !memberIds.has(u.id));
            setAvailableMembers(candidates);
            setShowAddMemberModal(true);
        } catch (e) {
            toast.error(t("Error loading"));
        }
    };

    const handleAddMember = async (userId) => {
        try {
            setAddingMember(true);
            await librepm.projectMembersAdd(selectedProjectId, userId, 'MEMBER');
            toast.success(t("Success"));
            setAvailableMembers(prev => prev.filter(u => u.id !== userId));
            loadAllocations();
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        } finally {
            setAddingMember(false);
        }
    };

    const handleRemoveMember = async () => {
        if (!selectedUser || !selectedProjectId) return;
        const confirmed = await modal.confirm({ title: `${t("Remove")} ${selectedUser.name}?` });
        if (!confirmed) return;

        try {
            await librepm.projectMembersRemove(selectedProjectId, selectedUser.id);
            toast.success(t("Deleted successfully"));
            setShowMemberModal(false);
            loadAllocations();
        } catch (e) {
            toast.error(t("Deletion error") + ": " + e.message);
        }
    };

    // Generate dates for header
    const dates = [];
    for (let i = 0; i < rangeDays; i++) {
        const d = new Date(startDate);
        d.setDate(d.getDate() + i);
        dates.push(d);
    }

    const getCellColor = (minutes) => {
        if (!minutes) return 'bg-light cursor-pointer';
        const hours = minutes / 60;
        if (hours <= 4) return 'bg-success-subtle text-success-emphasis cursor-pointer'; // Optimal
        if (hours <= 8) return 'bg-warning-subtle text-warning-emphasis cursor-pointer'; // Full
        return 'bg-danger-subtle text-danger-emphasis fw-bold cursor-pointer'; // Overload
    };

    if (loading && projects.length === 0) return <div className="p-5 text-center"><div className="spinner-border text-primary"></div></div>;

    return (
        <div className="container-fluid p-4 fade-in">
            {/* Toolbar */}
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div className="d-flex gap-3 align-items-center">
                    <select
                        className="form-select"
                        style={{ maxWidth: 300 }}
                        value={selectedProjectId}
                        onChange={(e) => setSelectedProjectId(e.target.value)}
                    >
                        {projects.map(p => (
                            <option key={p.id} value={p.id}>{p.name}</option>
                        ))}
                    </select>

                    <input
                        type="date"
                        className="form-control"
                        style={{ maxWidth: 180 }}
                        value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                    />

                    <select
                        className="form-select"
                        style={{ maxWidth: 150 }}
                        value={rangeDays}
                        onChange={(e) => setRangeDays(parseInt(e.target.value))}
                    >
                        <option value="7">{t("7 days")}</option>
                        <option value="14">{t("14 days")}</option>
                        <option value="21">{t("21 days")}</option>
                        <option value="30">{t("30 days")}</option>
                    </select>
                </div>
                <button
                    className="btn btn-primary"
                    onClick={openAddMemberModal}
                    disabled={!selectedProjectId}
                >
                    <i className="bi bi-person-plus me-2"></i>
                    {t("Add Member")}
                </button>
            </div>

            {/* Resource Grid */}
            <div className="card border-0 shadow-sm">
                <div className="table-responsive" style={{ overflowX: 'auto' }}>
                    <table className="table table-bordered mb-0" style={{ minWidth: 800 }}>
                        <thead className="bg-light">
                        <tr>
                            <th style={{ width: 200, minWidth: 200 }} className="ps-4 py-3">{t("Member")}</th>
                            {dates.map(d => (
                                <th key={toLocalDateStr(d)} className="text-center small py-3" style={{ width: 100, minWidth: 100 }}>
                                    <div className="fw-bold">{d.toLocaleDateString(i18n.language, { weekday: 'short' })}</div>
                                    <div className="text-muted">{d.getDate()}</div>
                                </th>
                            ))}
                            <th className="text-center small py-3" style={{ minWidth: 90 }}>
                                <div className="fw-bold">{t("Est.")}</div>
                                <div className="text-muted">{t("Act.")}</div>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        {allocations.length === 0 ? (
                            <tr>
                                <td colSpan={dates.length + 2} className="text-center py-5 text-muted">
                                    {t("No data available...")}
                                </td>
                            </tr>
                        ) : (
                            allocations.map(user => (
                                <tr key={user.userId}>
                                    <td
                                        className="ps-4 py-3 align-middle bg-white position-sticky start-0 border-end shadow-sm"
                                        style={{ cursor: 'pointer' }}
                                        onClick={() => handleMemberClick(user.userId, user.userName)}
                                    >
                                        <div className="d-flex align-items-center">
                                            <div className="avatar-circle bg-secondary text-white me-2 d-flex align-items-center justify-content-center" style={{width: 32, height: 32, borderRadius: '50%'}}>
                                                {user.userName.substring(0, 2).toUpperCase()}
                                            </div>
                                            <div className="text-truncate fw-medium">{user.userName}</div>
                                        </div>
                                    </td>
                                    {dates.map(d => {
                                        const dateStr = toLocalDateStr(d);
                                        const minutes = user.dailyMinutes[dateStr] || 0;
                                        const hours = (minutes / 60).toFixed(1);

                                        return (
                                            <td
                                                key={dateStr}
                                                className={`text-center align-middle p-1 ${getCellColor(minutes)}`}
                                                onClick={() => handleCellClick(user.userId, user.userName, dateStr)}
                                                data-tooltip-id={`tooltip-${user.userId}-${dateStr}`}
                                            >
                                                {minutes > 0 && (
                                                    <>
                                                        <div className="rounded p-1">
                                                            {hours}h
                                                        </div>
                                                        <Tooltip id={`tooltip-${user.userId}-${dateStr}`} place="top">
                                                            {hours} {t("estimated hours")}
                                                        </Tooltip>
                                                    </>
                                                )}
                                            </td>
                                        );
                                    })}
                                    {/* Summary: estimated vs actual */}
                                    <td className="text-center align-middle p-1 bg-white border-start fw-medium small" style={{ minWidth: 90 }}>
                                        <div>{((user.totalEstimatedMinutes || 0) / 60).toFixed(1)}h</div>
                                        <div className={`small ${(user.totalActualMinutes || 0) > (user.totalEstimatedMinutes || 0) ? 'text-danger' : 'text-success'}`}>
                                            {((user.totalActualMinutes || 0) / 60).toFixed(1)}h
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            </div>

            <div className="d-flex gap-3 mt-3 small text-muted">
                <div className="d-flex align-items-center gap-1">
                    <div className="rounded bg-success-subtle border border-success-subtle" style={{width: 16, height: 16}}></div>
                    <span>{t("Optimal")}</span>
                </div>
                <div className="d-flex align-items-center gap-1">
                    <div className="rounded bg-warning-subtle border border-warning-subtle" style={{width: 16, height: 16}}></div>
                    <span>{t("Full")}</span>
                </div>
                <div className="d-flex align-items-center gap-1">
                    <div className="rounded bg-danger-subtle border border-danger-subtle" style={{width: 16, height: 16}}></div>
                    <span>{t("Overload")}</span>
                </div>
            </div>

            {/* Task Management Modal */}
            {showTaskModal && (
                 <PortalModal onClick={() => setShowTaskModal(false)} className="modal-dialog-centered">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">{t("Task")}: {selectedUser?.name}</h5>
                            <button type="button" className="btn-close" onClick={() => setShowTaskModal(false)}></button>
                        </div>
                        <div className="modal-body">
                            <p className="text-muted small mb-3">
                                {t("Day:")} <strong>{new Date(selectedDate).toLocaleDateString()}</strong>
                            </p>

                            {userTasks.length === 0 ? (
                                <div className="text-center text-muted py-3">{t("No tasks assigned...")}</div>
                            ) : (
                                <div className="list-group">
                                    {userTasks.map(task => (
                                        <div key={task.id} className="list-group-item d-flex justify-content-between align-items-center">
                                            <div>
                                                <div className="fw-bold">{task.title}</div>
                                                <small className="text-muted">
                                                    {task.estimatedMinutes ? `${task.estimatedMinutes} ${t("min")}` : t("Not set")}
                                                </small>
                                            </div>
                                            <button
                                                className="btn btn-sm btn-outline-danger"
                                                onClick={() => handleUnassignTask(task.id)}
                                                title={t("Remove assignment")}
                                            >
                                                <i className="bi bi-person-x"></i>
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-secondary" onClick={() => setShowTaskModal(false)}>{t("Close")}</button>
                        </div>
                    </div>
                </PortalModal>
            )}

            {/* Add Member Modal */}
            {showAddMemberModal && (
                <PortalModal onClick={() => setShowAddMemberModal(false)} className="modal-dialog-centered">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">{t("Add Member")}</h5>
                            <button type="button" className="btn-close" onClick={() => setShowAddMemberModal(false)}></button>
                        </div>
                        <div className="modal-body">
                            {availableMembers.length === 0 ? (
                                <div className="text-center text-muted py-3">{t("No data available...")}</div>
                            ) : (
                                <div className="list-group">
                                    {availableMembers.map(user => (
                                        <div key={user.id} className="list-group-item d-flex justify-content-between align-items-center">
                                            <div className="d-flex align-items-center gap-2">
                                                <div className="avatar-circle bg-secondary text-white d-flex align-items-center justify-content-center" style={{width: 32, height: 32, borderRadius: '50%'}}>
                                                    {(user.displayName || user.username).substring(0, 2).toUpperCase()}
                                                </div>
                                                <div>
                                                    <div className="fw-bold">{user.displayName || user.username}</div>
                                                    <small className="text-muted">@{user.username}</small>
                                                </div>
                                                {user.ghost && <span className="badge bg-secondary-subtle text-secondary">{t("Virtual")}</span>}
                                            </div>
                                            <button
                                                className="btn btn-sm btn-outline-primary"
                                                onClick={() => handleAddMember(user.id)}
                                                disabled={addingMember}
                                            >
                                                <i className="bi bi-plus-lg"></i>
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-secondary" onClick={() => setShowAddMemberModal(false)}>{t("Close")}</button>
                        </div>
                    </div>
                </PortalModal>
            )}

            {/* Member Management Modal */}
            {showMemberModal && (
                 <PortalModal onClick={() => setShowMemberModal(false)} className="modal-dialog-centered">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">{t("Member Management:")} {selectedUser?.name}</h5>
                            <button type="button" className="btn-close" onClick={() => setShowMemberModal(false)}></button>
                        </div>
                        <div className="modal-body">
                            <div className="d-flex flex-column gap-3">
                                <div className="alert alert-light border">
                                    <div className="fw-bold mb-1">{t("Details")}</div>
                                    <div className="small text-muted">{t("ID:")} {selectedUser?.id}</div>
                                </div>

                                <button
                                    className="btn btn-outline-danger w-100"
                                    onClick={handleRemoveMember}
                                >
                                    <i className="bi bi-person-dash me-2"></i>
                                    {t("Remove from Project")}
                                </button>
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-secondary" onClick={() => setShowMemberModal(false)}>{t("Close")}</button>
                        </div>
                    </div>
                </PortalModal>
            )}
        </div>
    );
};

export default ResourcePage;
