import { useTranslation } from 'react-i18next';

export default function KanbanBoard({
                                        tasks,
                                        selectedTaskId,
                                        onSelectTask,
                                        onMoveTask,
                                        onDeleteTask,
                                    }) {
    const { t } = useTranslation();
    const columns = [
        { key: "TODO", title: "TODO" },
        { key: "DOING", title: "DOING" },
        { key: "DONE", title: "DONE" },
    ];

    function onDragStart(e, taskId) {
        e.dataTransfer.setData("text/plain", taskId);
        e.dataTransfer.effectAllowed = "move";
    }

    function onDrop(e, newStatus) {
        e.preventDefault();
        const taskId = e.dataTransfer.getData("text/plain");
        if (!taskId) return;
        onMoveTask(taskId, newStatus);
    }

    function onDragOver(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = "move";
    }

    return (
        <div className="card bg-black text-light border-secondary h-100">
            <div className="card-header border-secondary d-flex justify-content-between align-items-center">
                <span>Kanban</span>
                <span className="text-secondary small">{tasks.length} {t("visible tasks")}</span>
            </div>

            <div className="card-body h-100">
                <div className="row h-100 g-3">
                    {columns.map((col) => {
                        const colTasks = tasks.filter((t) => t.status === col.key);

                        return (
                            <div className="col-4 h-100" key={col.key}>
                                <div
                                    className="h-100 p-2 rounded border border-secondary bg-dark"
                                    onDrop={(e) => onDrop(e, col.key)}
                                    onDragOver={onDragOver}
                                >
                                    <div className="d-flex justify-content-between align-items-center mb-2">
                                        <div className="fw-bold">{col.title}</div>
                                        <div className="text-secondary small">{colTasks.length}</div>
                                    </div>

                                    <div className="d-flex flex-column gap-2 overflow-auto" style={{ maxHeight: "calc(100vh - 290px)" }}>
                                        {colTasks.map((t) => {
                                            const active = t.id === selectedTaskId;

                                            return (
                                                <div
                                                    key={t.id}
                                                    className={
                                                        "p-2 rounded border " +
                                                        (active ? "border-light" : "border-secondary") +
                                                        " bg-black"
                                                    }
                                                    style={{ cursor: "pointer" }}
                                                    draggable
                                                    onDragStart={(e) => onDragStart(e, t.id)}
                                                    onClick={() => onSelectTask(t.id)}
                                                    title={t("Drag to change column")}
                                                >
                                                    <div className="d-flex justify-content-between align-items-start gap-2">
                                                        <div className="flex-grow-1">
                                                            <div className="fw-semibold text-truncate">
                                                                {t.title}
                                                            </div>
                                                            {t.description ? (
                                                                <div className="text-secondary small text-truncate">
                                                                    {t.description}
                                                                </div>
                                                            ) : (
                                                                <div className="text-secondary small">
                                                                    â€”
                                                                </div>
                                                            )}
                                                            <div className="mt-2 d-flex gap-2">
                                <span className="badge bg-secondary">
                                  {t.priority}
                                </span>
                                                            </div>
                                                        </div>

                                                        <button
                                                            className="btn btn-sm btn-outline-danger"
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                onDeleteTask(t.id);
                                                            }}
                                                            title={t("Delete task")}
                                                        >
                                                            âœ•
                                                        </button>
                                                    </div>

                                                    <div className="d-flex gap-2 mt-2">
                                                        <button
                                                            className="btn btn-sm btn-outline-light"
                                                            disabled={t.status === "TODO"}
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                const next = t.status === "DONE" ? "DOING" : "TODO";
                                                                onMoveTask(t.id, next);
                                                            }}
                                                            title={t("Move left")}
                                                        >
                                                            â†
                                                        </button>

                                                        <button
                                                            className="btn btn-sm btn-outline-light"
                                                            disabled={t.status === "DONE"}
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                const next = t.status === "TODO" ? "DOING" : "DONE";
                                                                onMoveTask(t.id, next);
                                                            }}
                                                            title={t("Move right")}
                                                        >
                                                            â†’
                                                        </button>
                                                    </div>
                                                </div>
                                            );
                                        })}

                                        {colTasks.length === 0 && (
                                            <div className="text-secondary small">
                                                {t("No tasks in")} {col.title}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>

                <div className="text-secondary small mt-2">
                    {t("Tip: to edit text/description comfortably use the List view")}
                </div>
            </div>
        </div>
    );
}
