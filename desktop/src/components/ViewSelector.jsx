import { useState, useEffect, useCallback } from "react";
import { toast } from "react-toastify";
import { librepm } from "@api/librepm.js";
import { useTranslation } from "react-i18next";

/**
 * ViewSelector — Dropdown component for saving/loading view configurations
 * (PRD-01-FR-007, PRD-10-FR-003).
 *
 * Integrates into any page that supports views (Planner, Kanban, Gantt, Calendar).
 *
 * @param {string} viewType - The view type: LIST, BOARD, GANTT, CALENDAR, WORKLOAD
 * @param {string} projectId - Current project ID (optional, null for global views)
 * @param {object} currentConfig - Current filter/sort/group/columns config to save
 * @param {function} onApply - Callback when a saved view is selected: (config) => void
 */
export default function ViewSelector({ viewType, projectId, currentConfig, onApply }) {
    const { t } = useTranslation();
    const [views, setViews] = useState([]);
    const [selectedViewId, setSelectedViewId] = useState("");
    const [showSaveDialog, setShowSaveDialog] = useState(false);
    const [newViewName, setNewViewName] = useState("");
    const [isDefault, setIsDefault] = useState(false);

    const loadViews = useCallback(async () => {
        try {
            const data = projectId
                ? await librepm.savedViewsListByProject(projectId)
                : await librepm.savedViewsList();
            const filtered = (data || []).filter(v => v.viewType === viewType);
            setViews(filtered);
        } catch { /* ignore */ }
    }, [projectId, viewType]);

    useEffect(() => {
        loadViews();
    }, [loadViews]);

    const handleSelect = (viewId) => {
        setSelectedViewId(viewId);
        const view = views.find(v => v.id === viewId);
        if (view && onApply) {
            onApply({
                filters: view.filtersJson ? JSON.parse(view.filtersJson) : null,
                sort: view.sortJson ? JSON.parse(view.sortJson) : null,
                grouping: view.groupingJson ? JSON.parse(view.groupingJson) : null,
                columns: view.columnsJson ? JSON.parse(view.columnsJson) : null,
            });
        }
    };

    const handleSave = async () => {
        if (!newViewName.trim()) return;
        try {
            await librepm.savedViewsCreate({
                name: newViewName.trim(),
                viewType,
                projectId: projectId || null,
                filtersJson: currentConfig?.filters ? JSON.stringify(currentConfig.filters) : null,
                sortJson: currentConfig?.sort ? JSON.stringify(currentConfig.sort) : null,
                groupingJson: currentConfig?.grouping ? JSON.stringify(currentConfig.grouping) : null,
                columnsJson: currentConfig?.columns ? JSON.stringify(currentConfig.columns) : null,
                isDefault,
                isShared: false,
            });
            toast.success(t("Saved successfully"));
            setShowSaveDialog(false);
            setNewViewName("");
            setIsDefault(false);
            loadViews();
        } catch {
            toast.error(t("Error"));
        }
    };

    const handleDelete = async (viewId) => {
        try {
            await librepm.savedViewsDelete(viewId);
            toast.success(t("Deleted successfully"));
            if (selectedViewId === viewId) setSelectedViewId("");
            loadViews();
        } catch {
            toast.error(t("Deletion error"));
        }
    };

    return (
        <div className="d-flex align-items-center gap-2">
            {/* View dropdown */}
            <div className="dropdown">
                <button
                    className="btn btn-sm btn-outline-secondary dropdown-toggle"
                    type="button"
                    data-bs-toggle="dropdown"
                    aria-expanded="false"
                >
                    <i className="bi bi-eye me-1"></i>
                    {selectedViewId
                        ? views.find(v => v.id === selectedViewId)?.name || t("View")
                        : t("Views")}
                </button>
                <ul className="dropdown-menu">
                    {views.length === 0 ? (
                        <li>
                            <span className="dropdown-item text-muted small">
                                {t("No saved views")}
                            </span>
                        </li>
                    ) : (
                        views.map(v => (
                            <li key={v.id} className="d-flex align-items-center">
                                <button
                                    className={`dropdown-item flex-grow-1 ${selectedViewId === v.id ? "active" : ""}`}
                                    onClick={() => handleSelect(v.id)}
                                >
                                    {v.isDefault && <i className="bi bi-star-fill text-warning me-1" style={{ fontSize: "0.7rem" }}></i>}
                                    {v.name}
                                </button>
                                <button
                                    className="btn btn-sm btn-link text-danger p-0 me-2"
                                    onClick={(e) => { e.stopPropagation(); handleDelete(v.id); }}
                                    title={t("Delete")}
                                >
                                    <i className="bi bi-trash" style={{ fontSize: "0.75rem" }}></i>
                                </button>
                            </li>
                        ))
                    )}
                    <li><hr className="dropdown-divider" /></li>
                    <li>
                        <button
                            className="dropdown-item"
                            onClick={() => setShowSaveDialog(true)}
                        >
                            <i className="bi bi-plus-lg me-1"></i>
                            {t("Save current view")}
                        </button>
                    </li>
                    {selectedViewId && (
                        <li>
                            <button
                                className="dropdown-item text-muted"
                                onClick={() => { setSelectedViewId(""); if (onApply) onApply(null); }}
                            >
                                <i className="bi bi-x-lg me-1"></i>
                                {t("Clear")}
                            </button>
                        </li>
                    )}
                </ul>
            </div>

            {/* Save dialog (inline popover) */}
            {showSaveDialog && (
                <div className="d-flex align-items-center gap-1">
                    <input
                        type="text"
                        className="form-control form-control-sm"
                        placeholder={t("View name")}
                        value={newViewName}
                        onChange={(e) => setNewViewName(e.target.value)}
                        onKeyDown={(e) => { if (e.key === "Enter") handleSave(); }}
                        autoFocus
                        style={{ maxWidth: "150px" }}
                    />
                    <div className="form-check form-check-inline ms-1">
                        <input
                            className="form-check-input"
                            type="checkbox"
                            checked={isDefault}
                            onChange={(e) => setIsDefault(e.target.checked)}
                            id="viewDefault"
                        />
                        <label className="form-check-label small" htmlFor="viewDefault">
                            {t("Default")}
                        </label>
                    </div>
                    <button className="btn btn-sm btn-primary" onClick={handleSave} disabled={!newViewName.trim()}>
                        <i className="bi bi-check-lg"></i>
                    </button>
                    <button className="btn btn-sm btn-outline-secondary" onClick={() => setShowSaveDialog(false)}>
                        <i className="bi bi-x-lg"></i>
                    </button>
                </div>
            )}
        </div>
    );
}
