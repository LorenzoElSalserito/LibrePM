import { useState, useEffect, useCallback, useRef } from "react";
import { librepm } from "@api/librepm.js";
import { toast } from "react-toastify";
import EstimatesWidget from "../components/EstimatesWidget.jsx";
import FocusHeatmapWidget from "../components/FocusHeatmapWidget.jsx";
import ProjectNotesWidget from "../components/ProjectNotesWidget.jsx";
import EffortVarianceWidget from "../components/EffortVarianceWidget.jsx";
import PendingApprovalsWidget from "../components/PendingApprovalsWidget.jsx";
import Modal from "../components/modal/Modal.jsx";
import { useTranslation } from 'react-i18next';
import { useModal } from "../hooks/useModal.js";

/**
 * Dashboard - Pagina principale con widget e statistiche
 *
 * Features:
 * - Classifica Progetti per salute (PRD-05)
 * - Widget "Task in Ritardo" (PRD-05)
 * - Statistiche generali
 * - Sezione Progetti Completati (v0.4.3)
 * - Widget Analytics Stime (PRD-08)
 * - Widget Focus Heatmap (PRD-10)
 * - Widget Note Progetto (v0.5.1)
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @updated 0.5.1 - Aggiunto widget note progetto
 */

export default function Dashboard({ shell }) {
    const { t } = useTranslation();
    const modal = useModal();
    const [stats, setStats] = useState({
        totalProjects: 0,
        totalTasks: 0,
        overdueTasks: 0,
        completedTasks: 0,
    });
    const [rankedProjects, setRankedProjects] = useState([]);
    const [completedProjects, setCompletedProjects] = useState([]);
    const [overdueTasksList, setOverdueTasksList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedProjectId, setSelectedProjectId] = useState(shell?.currentProject?.id || "");
    const [showNoProjectsModal, setShowNoProjectsModal] = useState(false);
    const welcomeShown = useRef(false);

    // Setup shell
    useEffect(() => {
        shell?.setTitle?.(t("Dashboard"));
        shell?.setHeaderActions?.(
            <div className="d-flex gap-2 align-items-center">
                <button
                    className="btn btn-sm btn-outline-secondary"
                    onClick={loadData}
                    title={t("Refresh")}
                >
                    <i className="bi bi-arrow-clockwise"></i>
                </button>
            </div>
        );
        shell?.setRightPanel?.(null);

        return () => {
            shell?.setHeaderActions?.(null);
        };
    }, [shell, t]);

    // Sync selected project with shell
    useEffect(() => {
        if (shell?.currentProject?.id) {
            setSelectedProjectId(shell.currentProject.id);
        }
    }, [shell?.currentProject]);

    // Load Data
    const loadData = useCallback(async () => {
        try {
            setLoading(true);
            
            // 1. Carica progetti attivi e archiviati
            const activeProjects = await librepm.projectsList({ archived: false });
            const archivedProjects = await librepm.projectsList({ archived: true });
            
            // 2. Per ogni progetto attivo, carica i task per calcolare statistiche
            let allTasks = [];
            let projectStats = [];

            for (const p of activeProjects) {
                const pTasks = await librepm.tasksList(p.id);
                const pOverdue = pTasks.filter(t => {
                    if (!t.deadline || t.status === 'DONE') return false;
                    return new Date(t.deadline) < new Date();
                });

                // Calcola salute progetto (PRD-05)
                let health = "OK"; // Verde
                if (pOverdue.length > 3) health = "CRITICAL"; // Rosso
                else if (pOverdue.length > 0) health = "WARNING"; // Giallo

                projectStats.push({
                    ...p,
                    taskCount: pTasks.length,
                    overdueCount: pOverdue.length,
                    health
                });

                allTasks = [...allTasks, ...pTasks.map(t => ({ ...t, projectName: p.name, projectId: p.id }))];
            }

            // 3. Calcola totali
            const overdue = allTasks.filter(t => {
                if (!t.deadline || t.status === 'DONE') return false;
                return new Date(t.deadline) < new Date();
            });

            setStats({
                totalProjects: activeProjects.length,
                totalTasks: allTasks.length,
                overdueTasks: overdue.length,
                completedTasks: allTasks.filter(t => t.status === 'DONE').length
            });

            // Show "no projects" modal if empty
            if (activeProjects.length === 0 && archivedProjects.length === 0) {
                setShowNoProjectsModal(true);
            }

            // 4. Classifica Progetti (Verdi -> Gialli -> Rossi)
            const ranked = projectStats.sort((a, b) => {
                const healthScore = { "OK": 0, "WARNING": 1, "CRITICAL": 2 };
                if (healthScore[a.health] !== healthScore[b.health]) {
                    return healthScore[a.health] - healthScore[b.health];
                }
                return a.overdueCount - b.overdueCount;
            });
            setRankedProjects(ranked);

            // 5. Task in ritardo (Top 10)
            setOverdueTasksList(overdue.sort((a, b) => new Date(a.deadline) - new Date(b.deadline)).slice(0, 10));

            // 6. Progetti Completati (Archiviati)
            setCompletedProjects(archivedProjects);

        } catch (e) {
            console.error("[Dashboard] Errore caricamento:", e);
            toast.error(t("Error loading dashboard"));
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadData().then(() => {
            // Welcome toast on first login (one-shot)
            if (!welcomeShown.current) {
                welcomeShown.current = true;
                const key = "librepm_welcome_shown";
                if (!localStorage.getItem(key)) {
                    localStorage.setItem(key, "1");
                    toast.info(t("welcome_first_login"), { autoClose: 6000 });
                }
            }
        });
    }, [loadData]);

    const handleRestoreProject = async (project) => {
        const confirmed = await modal.confirm({ title: t("Reopen project") + ` "${project.name}"?` });
        if (!confirmed) return;
        try {
            await librepm.projectsSetArchived(project.id, false);
            toast.success(t("Project reopened"));
            loadData();
        } catch (e) {
            toast.error(t("Error reopening project") + ": " + e.message);
        }
    };

    const handleProjectChange = (e) => {
        const projectId = e.target.value;
        setSelectedProjectId(projectId);
        const project = rankedProjects.find(p => p.id === projectId);
        if (project) {
            shell.setCurrentProject(project);
        }
    };

    // Render loading
    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center py-5">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">{t("Loading")}</span>
                </div>
            </div>
        );
    }

    return (
        <div className="dashboard-page container-fluid py-4">
            {/* Stat Cards */}
            <div className="row g-3 mb-4">
                <div className="col-md-3">
                    <div className="card h-100 border-primary border-start-4">
                        <div className="card-body">
                            <h6 className="card-subtitle mb-2 text-muted">{t("Active Projects")}</h6>
                            <h2 className="card-title mb-0">{stats.totalProjects}</h2>
                        </div>
                    </div>
                </div>
                <div className="col-md-3">
                    <div className="card h-100 border-success border-start-4">
                        <div className="card-body">
                            <h6 className="card-subtitle mb-2 text-muted">{t("Completed Tasks")}</h6>
                            <h2 className="card-title mb-0">{stats.completedTasks}</h2>
                        </div>
                    </div>
                </div>
                <div className="col-md-3">
                    <div className="card h-100 border-warning border-start-4">
                        <div className="card-body">
                            <h6 className="card-subtitle mb-2 text-muted">{t("Total Tasks")}</h6>
                            <h2 className="card-title mb-0">{stats.totalTasks}</h2>
                        </div>
                    </div>
                </div>
                <div className="col-md-3">
                    <div className="card h-100 border-danger border-start-4">
                        <div className="card-body">
                            <h6 className="card-subtitle mb-2 text-muted">{t("Overdue")}</h6>
                            <h2 className="card-title mb-0 text-danger">{stats.overdueTasks}</h2>
                        </div>
                    </div>
                </div>
            </div>

            {/* Analytics Row */}
            <div className="row g-4 mb-4">
                <div className="col-md-8">
                    <FocusHeatmapWidget />
                </div>
                <div className="col-md-4">
                    <EstimatesWidget />
                </div>
            </div>

            {/* Effort Variance Row */}
            <div className="row g-4 mb-4">
                <div className="col-12">
                    <EffortVarianceWidget projectId={selectedProjectId} />
                </div>
            </div>

            {/* Pending Approvals */}
            <div className="row g-4 mb-4">
                <div className="col-12">
                    <div className="card shadow-sm border-0">
                        <div className="card-header bg-white">
                            <h6 className="mb-0">
                                <i className="bi bi-clipboard-check text-warning me-2"></i>
                                {t("Pending Approvals")}
                            </h6>
                        </div>
                        <div className="card-body">
                            <PendingApprovalsWidget />
                        </div>
                    </div>
                </div>
            </div>

            {/* Project Notes Row */}
            <div className="row g-4 mb-4">
                <div className="col-12">
                    <div className="card shadow-sm">
                        <div className="card-header bg-white d-flex justify-content-between align-items-center">
                            <h6 className="mb-0">
                                <i className="bi bi-journal-text text-info me-2"></i>
                                {t("Project Notes")}
                            </h6>
                            <select 
                                className="form-select form-select-sm" 
                                style={{ width: 200 }}
                                value={selectedProjectId}
                                onChange={handleProjectChange}
                            >
                                <option value="">{t("Select a project")}...</option>
                                {rankedProjects.map(p => (
                                    <option key={p.id} value={p.id}>{p.name}</option>
                                ))}
                            </select>
                        </div>
                        <div className="card-body p-0">
                            <ProjectNotesWidget 
                                projectId={selectedProjectId} 
                                onNavigateToNotes={(note) => shell.navigate("notes", { projectId: selectedProjectId, noteId: note?.id })}
                            />
                        </div>
                    </div>
                </div>
            </div>

            <div className="row g-4 mb-4">
                {/* Classifica Progetti Widget */}
                <div className="col-md-6">
                    <div className="card h-100 shadow-sm">
                        <div className="card-header bg-white d-flex justify-content-between align-items-center">
                            <h5 className="mb-0">
                                <i className="bi bi-trophy text-primary me-2"></i>
                                {t("Project Health")}
                            </h5>
                            <small className="text-muted">{t("Sorted by health")}</small>
                        </div>
                        <div className="list-group list-group-flush">
                            {rankedProjects.length === 0 ? (
                                <div className="list-group-item text-center text-muted py-4">
                                    <i className="bi bi-folder2-open fs-1 d-block mb-2"></i>
                                    {t("No active projects.")}
                                </div>
                            ) : (
                                rankedProjects.map((p, index) => (
                                    <div key={p.id} className="list-group-item d-flex justify-content-between align-items-center">
                                        <div className="d-flex align-items-center gap-3">
                                            <span className="fw-bold text-muted" style={{ width: 20 }}>#{index + 1}</span>
                                            <div>
                                                <div className="fw-bold">{p.name}</div>
                                                <small className="text-muted">{p.taskCount} {t("Total Tasks").toLowerCase()}</small>
                                            </div>
                                        </div>
                                        <div className="text-end">
                                            {p.health === 'OK' && <span className="badge bg-success mb-1">{t("On time")}</span>}
                                            {p.health === 'WARNING' && <span className="badge bg-warning text-dark mb-1">{t("Warning")}</span>}
                                            {p.health === 'CRITICAL' && <span className="badge bg-danger mb-1">{t("Overdue")}</span>}
                                            
                                            {p.overdueCount > 0 && (
                                                <div className="small text-danger">
                                                    {p.overdueCount} {t("overdue tasks")}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>

                {/* Overdue Tasks Widget */}
                <div className="col-md-6">
                    <div className="card h-100 shadow-sm">
                        <div className="card-header bg-white d-flex justify-content-between align-items-center">
                            <h5 className="mb-0">
                                <i className="bi bi-alarm text-danger me-2"></i>
                                {t("Overdue Tasks (Top 10)")}
                            </h5>
                            <button className="btn btn-sm btn-link" onClick={() => shell.navigate('planner')}>
                                {t("See all")}
                            </button>
                        </div>
                        <div className="list-group list-group-flush">
                            {overdueTasksList.length === 0 ? (
                                <div className="list-group-item text-center text-muted py-4">
                                    <i className="bi bi-emoji-smile fs-1 text-success d-block mb-2"></i>
                                    {t("No overdue tasks. Great job!")}
                                </div>
                            ) : (
                                overdueTasksList.map(t => (
                                    <div key={t.id} className="list-group-item">
                                        <div className="d-flex w-100 justify-content-between">
                                            <h6 className="mb-1 text-truncate" style={{ maxWidth: "70%" }}>{t.title}</h6>
                                            <small className="text-danger fw-bold">
                                                {new Date(t.deadline).toLocaleDateString()}
                                            </small>
                                        </div>
                                        <div className="d-flex justify-content-between align-items-center mt-1">
                                            <small className="text-muted">
                                                <i className="bi bi-folder2 me-1"></i>
                                                {t.projectName}
                                            </small>
                                            <span className={`badge ${
                                                t.priority === 'HIGH' ? 'bg-danger' : 
                                                t.priority === 'MED' ? 'bg-warning text-dark' : 'bg-secondary'
                                            } scale-75`}>
                                                {t.priority}
                                            </span>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Completed Projects Section */}
            {completedProjects.length > 0 && (
                <div className="row">
                    <div className="col-12">
                        <div className="card border-0 shadow-sm">
                            <div className="card-header bg-light border-0">
                                <h6 className="mb-0 text-muted">
                                    <i className="bi bi-archive me-2"></i>
                                    {t("Completed / Archived Projects")}
                                </h6>
                            </div>
                            <div className="list-group list-group-flush">
                                {completedProjects.map(p => (
                                    <div key={p.id} className="list-group-item d-flex justify-content-between align-items-center bg-light">
                                        <div>
                                            <span className="text-decoration-line-through text-muted">{p.name}</span>
                                            <small className="text-muted d-block">{t("Archived on")} {new Date(p.updatedAt).toLocaleDateString()}</small>
                                        </div>
                                        <button
                                            className="btn btn-sm btn-outline-secondary"
                                            onClick={() => handleRestoreProject(p)}
                                            title={t("Reopen")}
                                        >
                                            <i className="bi bi-arrow-counterclockwise"></i>
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* No Projects Modal */}
            {showNoProjectsModal && (
                <Modal title={t("no_projects_title")} onClose={() => setShowNoProjectsModal(false)}>
                    <div className="modal-body">
                        <div className="text-center mb-3">
                            <i className="bi bi-folder-plus" style={{ fontSize: "3rem", color: "var(--bs-primary)" }}></i>
                        </div>
                        <p>{t("no_projects_message")}</p>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" onClick={() => setShowNoProjectsModal(false)}>
                            {t("Close")}
                        </button>
                        <button
                            type="button"
                            className="btn btn-primary"
                            autoFocus
                            onClick={() => {
                                setShowNoProjectsModal(false);
                                shell.navigate("menu");
                            }}
                        >
                            <i className="bi bi-folder2-open me-2"></i>
                            {t("Go to Projects")}
                        </button>
                    </div>
                </Modal>
            )}
        </div>
    );
}