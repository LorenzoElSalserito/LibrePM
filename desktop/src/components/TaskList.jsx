import { useTranslation } from 'react-i18next';

function statusBadge(status) {
    if (status === "DONE") return "success";
    if (status === "DOING") return "warning";
    return "secondary";
}

function priorityBadge(priority) {
    if (priority === "HIGH") return "danger";
    if (priority === "LOW") return "info";
    return "secondary";
}

export default function TaskList({
                                     tasks,
                                     selectedTaskId,
                                     onSelectTask,
                                     onUpdateTask,
                                     onDeleteTask,
                                 }) {
    const { t } = useTranslation();
    return (
        <div className="card bg-black text-light border-secondary h-100">
            <div className="card-header border-secondary d-flex justify-content-between">
                <span>{t("Task")}</span>
                <span className="text-secondary small">{tasks.length} {t("total")}</span>
            </div>

            <div className="card-body overflow-auto">
                {tasks.length === 0 && (
                    <div className="text-secondary small">
                        {t("No tasks. Press “New Task”.")}
                    </div>
                )}

                {tasks.map((t) => {
                    const active = t.id === selectedTaskId;

                    return (
                        <div
                            key={t.id}
                            className={
                                "p-3 mb-2 rounded border " +
                                (active ? "border-light" : "border-secondary")
                            }
                            style={{ cursor: "pointer" }}
                            onClick={() => onSelectTask(t.id)}
                        >
                            <div className="d-flex justify-content-between align-items-start gap-3">
                                <div className="flex-grow-1">
                                    <div className="d-flex align-items-center gap-2 mb-2">
                    <span className={"badge bg-" + statusBadge(t.status)}>
                      {t.status}
                    </span>
                                        <span className={"badge bg-" + priorityBadge(t.priority)}>
                      {t.priority}
                    </span>
                                    </div>

                                    <input
                                        className="form-control form-control-sm bg-dark text-light border-secondary"
                                        value={t.title}
                                        onChange={(e) => onUpdateTask(t.id, { title: e.target.value })}
                                        onClick={(e) => e.stopPropagation()}
                                    />

                                    <textarea
                                        className="form-control form-control-sm bg-dark text-light border-secondary mt-2"
                                        rows={2}
                                        placeholder={t("Description...")}
                                        value={t.description}
                                        onChange={(e) =>
                                            onUpdateTask(t.id, { description: e.target.value })
                                        }
                                        onClick={(e) => e.stopPropagation()}
                                    />
                                </div>

                                <div className="d-flex flex-column gap-2">
                                    <select
                                        className="form-select form-select-sm bg-dark text-light border-secondary"
                                        value={t.status}
                                        onChange={(e) =>
                                            onUpdateTask(t.id, { status: e.target.value })
                                        }
                                        onClick={(e) => e.stopPropagation()}
                                    >
                                        <option value="TODO">TODO</option>
                                        <option value="DOING">DOING</option>
                                        <option value="DONE">DONE</option>
                                    </select>

                                    <select
                                        className="form-select form-select-sm bg-dark text-light border-secondary"
                                        value={t.priority}
                                        onChange={(e) =>
                                            onUpdateTask(t.id, { priority: e.target.value })
                                        }
                                        onClick={(e) => e.stopPropagation()}
                                    >
                                        <option value="LOW">LOW</option>
                                        <option value="MED">MED</option>
                                        <option value="HIGH">HIGH</option>
                                    </select>

                                    <button
                                        className="btn btn-sm btn-outline-danger"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            onDeleteTask(t.id);
                                        }}
                                    >
                                        {t("Delete")}
                                    </button>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
