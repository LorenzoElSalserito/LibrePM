import { useState } from "react";
import { useTranslation } from 'react-i18next';

export default function Sidebar({
                                    projects,
                                    selectedProjectId,
                                    onSelectProject,
                                    onCreateProject,
                                    onDeleteProject,
                                }) {
    const { t } = useTranslation();
    const [newProjectName, setNewProjectName] = useState("");

    async function handleCreate() {
        const name = newProjectName.trim();
        if (!name) return;

        await onCreateProject(name);
        setNewProjectName("");
    }

    return (
        <div
            style={{ width: 280 }}
            className="border-end border-secondary p-3 bg-black text-light"
        >
            <div className="mb-2">
                <h5 className="m-0">{t("Projects")}</h5>
                <span className="badge bg-info text-dark mt-1" style={{ fontSize: '0.65rem' }}>
                    {t("New Features Incoming")}
                </span>
            </div>

            {/* input nuovo progetto */}
            <div className="d-flex gap-2 mb-3">
                <input
                    className="form-control form-control-sm bg-dark text-light border-secondary"
                    placeholder={t("New project...")}
                    value={newProjectName}
                    onChange={(e) => setNewProjectName(e.target.value)}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") handleCreate();
                    }}
                />

                <button
                    className="btn btn-sm btn-outline-light"
                    onClick={handleCreate}
                    title={t("Create Project")}
                >
                    +
                </button>
            </div>

            {/* lista progetti */}
            {projects.length === 0 && (
                <div className="text-secondary small">
                    {t("No projects. Create one above.")}
                </div>
            )}

            <div className="list-group list-group-flush">
                {projects.map((p) => {
                    const active = p.id === selectedProjectId;

                    return (
                        <div
                            key={p.id}
                            className={
                                "list-group-item list-group-item-action d-flex justify-content-between align-items-center " +
                                (active ? "active" : "bg-black text-light border-secondary")
                            }
                            style={{ cursor: "pointer" }}
                            onClick={() => onSelectProject(p.id)}
                        >
                            <span className="text-truncate">{p.name}</span>

                            <button
                                className={
                                    "btn btn-sm " + (active ? "btn-outline-light" : "btn-outline-secondary")
                                }
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onDeleteProject(p.id);
                                }}
                                title={t("Delete Project")}
                            >
                                âœ•
                            </button>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
