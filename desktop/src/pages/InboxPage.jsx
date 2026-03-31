import { useState, useEffect, useCallback } from "react";
import { toast } from "react-toastify";
import { librepm } from "@api/librepm.js";
import { useTranslation } from "react-i18next";
import { useWorkflow } from "../context/WorkflowContext.jsx";

/**
 * InboxPage — Personal inbox for quick task capture (PRD-01-FR-004).
 *
 * Tasks created here have no project assignment and can be
 * moved to a project later.
 *
 * @component
 * @author Lorenzo DM
 * @since 0.10.0
 */
export default function InboxPage({ onNavigate }) {
    const { t } = useTranslation();
    const { statuses, priorities } = useWorkflow();

    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [newTitle, setNewTitle] = useState("");
    const [projects, setProjects] = useState([]);
    const [movingTaskId, setMovingTaskId] = useState(null);
    const [selectedProjectId, setSelectedProjectId] = useState("");

    const loadTasks = useCallback(async () => {
        try {
            const data = await librepm.inboxList();
            setTasks(data || []);
        } catch (e) {
            toast.error(t("Error loading tasks"));
        } finally {
            setLoading(false);
        }
    }, [t]);

    const loadProjects = useCallback(async () => {
        try {
            const data = await librepm.projectsList();
            setProjects(data || []);
        } catch { /* ignore */ }
    }, []);

    useEffect(() => {
        loadTasks();
        loadProjects();
    }, [loadTasks, loadProjects]);

    const handleQuickCreate = async (e) => {
        e.preventDefault();
        if (!newTitle.trim()) return;

        try {
            await librepm.inboxCreate({ title: newTitle.trim() });
            setNewTitle("");
            toast.success(t("Task created!"));
            loadTasks();
        } catch (e) {
            toast.error(t("Error saving task"));
        }
    };

    const handleMoveToProject = async (taskId) => {
        if (!selectedProjectId) return;
        try {
            await librepm.inboxMoveToProject(taskId, selectedProjectId);
            toast.success(t("Task moved to") + " " + t("Project"));
            setMovingTaskId(null);
            setSelectedProjectId("");
            loadTasks();
        } catch (e) {
            toast.error(t("Error saving task"));
        }
    };

    const getStatusDisplay = (statusName, statusColor) => {
        if (!statusName) return null;
        return (
            <span className="badge" style={{
                backgroundColor: statusColor || "#6c757d",
                color: "#fff",
                fontSize: "0.75rem"
            }}>
                {t(statusName)}
            </span>
        );
    };

    const getPriorityDisplay = (priorityName, priorityColor) => {
        if (!priorityName) return null;
        return (
            <span className="badge" style={{
                backgroundColor: priorityColor || "#6c757d",
                color: "#fff",
                fontSize: "0.75rem"
            }}>
                {t(priorityName)}
            </span>
        );
    };

    return (
        <div className="container-fluid p-4">
            {/* Header */}
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h4 className="mb-1">
                        <i className="bi bi-inbox me-2"></i>
                        {t("Inbox")}
                    </h4>
                    <small className="text-muted">
                        {t("Inbox description")}
                    </small>
                </div>
                <span className="badge bg-primary fs-6">{tasks.length}</span>
            </div>

            {/* Quick Create */}
            <form onSubmit={handleQuickCreate} className="mb-4">
                <div className="input-group">
                    <span className="input-group-text">
                        <i className="bi bi-plus-lg"></i>
                    </span>
                    <input
                        type="text"
                        className="form-control"
                        placeholder={t("Quick add task...")}
                        value={newTitle}
                        onChange={(e) => setNewTitle(e.target.value)}
                        autoFocus
                    />
                    <button type="submit" className="btn btn-primary" disabled={!newTitle.trim()}>
                        {t("Add")}
                    </button>
                </div>
            </form>

            {/* Task List */}
            {loading ? (
                <div className="text-center py-5">
                    <div className="spinner-border text-primary" />
                </div>
            ) : tasks.length === 0 ? (
                <div className="text-center py-5 text-muted">
                    <i className="bi bi-inbox" style={{ fontSize: "3rem" }}></i>
                    <p className="mt-3">{t("Inbox empty")}</p>
                </div>
            ) : (
                <div className="list-group">
                    {tasks.map((task) => (
                        <div key={task.id} className="list-group-item list-group-item-action">
                            <div className="d-flex justify-content-between align-items-start">
                                <div className="flex-grow-1">
                                    <h6 className="mb-1">{task.title}</h6>
                                    {task.description && (
                                        <small className="text-muted d-block mb-1">{task.description}</small>
                                    )}
                                    <div className="d-flex gap-2 align-items-center">
                                        {getStatusDisplay(task.statusName, task.statusColor)}
                                        {getPriorityDisplay(task.priorityName, task.priorityColor)}
                                        {task.deadline && (
                                            <small className="text-muted">
                                                <i className="bi bi-calendar-event me-1"></i>
                                                {new Date(task.deadline).toLocaleDateString()}
                                            </small>
                                        )}
                                    </div>
                                </div>

                                {/* Move to Project */}
                                <div className="d-flex gap-2 ms-3">
                                    {movingTaskId === task.id ? (
                                        <div className="d-flex gap-1 align-items-center">
                                            <select
                                                className="form-select form-select-sm"
                                                value={selectedProjectId}
                                                onChange={(e) => setSelectedProjectId(e.target.value)}
                                                style={{ minWidth: "180px" }}
                                            >
                                                <option value="">{t("Select project")}</option>
                                                {projects.map((p) => (
                                                    <option key={p.id} value={p.id}>{p.name}</option>
                                                ))}
                                            </select>
                                            <button
                                                className="btn btn-sm btn-success"
                                                onClick={() => handleMoveToProject(task.id)}
                                                disabled={!selectedProjectId}
                                            >
                                                <i className="bi bi-check-lg"></i>
                                            </button>
                                            <button
                                                className="btn btn-sm btn-outline-secondary"
                                                onClick={() => { setMovingTaskId(null); setSelectedProjectId(""); }}
                                            >
                                                <i className="bi bi-x-lg"></i>
                                            </button>
                                        </div>
                                    ) : (
                                        <button
                                            className="btn btn-sm btn-outline-primary"
                                            onClick={() => setMovingTaskId(task.id)}
                                            title={t("Move to project")}
                                        >
                                            <i className="bi bi-folder-symlink me-1"></i>
                                            {t("Move")}
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
