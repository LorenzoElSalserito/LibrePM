import React, { useState, useEffect, useCallback, useRef } from "react";
import { librepm } from "@api/librepm.js";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";

/**
 * TimeTrackingPage - Time tracking with manual entry and local timer (PRD-03-FR-001).
 */
export default function TimeTrackingPage({ shell }) {
    const { t } = useTranslation();
    const [entries, setEntries] = useState([]);
    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);

    // Form state
    const [selectedTaskId, setSelectedTaskId] = useState("");
    const [duration, setDuration] = useState("");
    const [description, setDescription] = useState("");
    const [entryDate, setEntryDate] = useState(new Date().toISOString().split("T")[0]);

    // Timer state
    const [timerRunning, setTimerRunning] = useState(false);
    const [timerTaskId, setTimerTaskId] = useState("");
    const [timerSeconds, setTimerSeconds] = useState(0);
    const timerRef = useRef(null);

    const projectId = shell?.currentProject?.id;
    const currentUserId = librepm.getCurrentUser();

    useEffect(() => {
        shell?.setTitle?.(t("Time Tracking"));
        shell?.setHeaderActions?.(null);
        shell?.setRightPanel?.(null);
    }, [shell, t]);

    // Load entries and tasks
    const loadData = useCallback(async () => {
        if (!projectId) return;
        try {
            setLoading(true);
            const [entriesData, tasksData] = await Promise.all([
                librepm.timeEntriesByProject(projectId),
                librepm.tasksList(projectId)
            ]);
            setEntries(entriesData || []);
            setTasks(tasksData || []);
        } catch (e) {
            toast.error(t("Error loading data") + ": " + e.message);
        } finally {
            setLoading(false);
        }
    }, [projectId, t]);

    useEffect(() => { loadData(); }, [loadData]);

    // Timer logic
    useEffect(() => {
        if (timerRunning) {
            timerRef.current = setInterval(() => {
                setTimerSeconds(s => s + 1);
            }, 1000);
        } else {
            if (timerRef.current) clearInterval(timerRef.current);
        }
        return () => { if (timerRef.current) clearInterval(timerRef.current); };
    }, [timerRunning]);

    const formatTimer = (seconds) => {
        const h = Math.floor(seconds / 3600);
        const m = Math.floor((seconds % 3600) / 60);
        const s = seconds % 60;
        return `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
    };

    const startTimer = () => {
        if (!timerTaskId) { toast.warn(t("Select a task first")); return; }
        setTimerSeconds(0);
        setTimerRunning(true);
    };

    const stopTimer = async () => {
        setTimerRunning(false);
        const minutes = Math.max(1, Math.round(timerSeconds / 60));
        try {
            await librepm.timeEntriesCreate({
                taskId: timerTaskId,
                entryDate: new Date().toISOString().split("T")[0],
                durationMinutes: minutes,
                type: "TIMER",
                description: t("Timer session")
            });
            toast.success(t("Time entry saved") + ` (${minutes} min)`);
            setTimerSeconds(0);
            loadData();
        } catch (e) {
            toast.error(e.message);
        }
    };

    const handleManualEntry = async () => {
        if (!selectedTaskId) { toast.warn(t("Select a task first")); return; }
        const mins = parseInt(duration);
        if (!mins || mins < 1) { toast.warn(t("Enter a valid duration")); return; }
        try {
            await librepm.timeEntriesCreate({
                taskId: selectedTaskId,
                entryDate: entryDate,
                durationMinutes: mins,
                type: "MANUAL",
                description: description || null
            });
            toast.success(t("Time entry saved"));
            setDuration("");
            setDescription("");
            loadData();
        } catch (e) {
            toast.error(e.message);
        }
    };

    const handleDelete = async (id) => {
        try {
            await librepm.timeEntriesDelete(id);
            setEntries(prev => prev.filter(e => e.id !== id));
            toast.success(t("Entry deleted"));
        } catch (e) {
            toast.error(e.message);
        }
    };

    // Group entries by date
    const groupedEntries = entries.reduce((acc, entry) => {
        const date = entry.entryDate || "Unknown";
        if (!acc[date]) acc[date] = [];
        acc[date].push(entry);
        return acc;
    }, {});

    const sortedDates = Object.keys(groupedEntries).sort((a, b) => b.localeCompare(a));

    // Totals
    const totalMinutes = entries.reduce((sum, e) => sum + (e.durationMinutes || 0), 0);
    const totalHours = (totalMinutes / 60).toFixed(1);

    if (!projectId) {
        return (
            <div className="text-center py-5">
                <i className="bi bi-clock fs-1 text-muted d-block mb-3"></i>
                <h5 className="text-muted">{t("Select a project to track time")}</h5>
            </div>
        );
    }

    if (loading) return <div className="d-flex justify-content-center py-5"><div className="spinner-border text-primary"></div></div>;

    return (
        <div className="container-fluid py-3">
            <div className="row g-3">
                {/* Timer Section */}
                <div className="col-md-6">
                    <div className="card border-0 shadow-sm">
                        <div className="card-body">
                            <h6 className="card-title mb-3">
                                <i className="bi bi-stopwatch me-2"></i>
                                {t("Timer")}
                            </h6>
                            <select
                                className="form-select form-select-sm mb-3"
                                value={timerTaskId}
                                onChange={(e) => setTimerTaskId(e.target.value)}
                                disabled={timerRunning}
                            >
                                <option value="">{t("Select Task...")}</option>
                                {tasks.map(task => (
                                    <option key={task.id} value={task.id}>{task.title}</option>
                                ))}
                            </select>
                            <div className="text-center mb-3">
                                <div className="display-4 font-monospace fw-bold" style={{ color: timerRunning ? "#198754" : "inherit" }}>
                                    {formatTimer(timerSeconds)}
                                </div>
                            </div>
                            <div className="d-flex gap-2 justify-content-center">
                                {!timerRunning ? (
                                    <button className="btn btn-success" onClick={startTimer}>
                                        <i className="bi bi-play-fill me-1"></i>{t("Start")}
                                    </button>
                                ) : (
                                    <button className="btn btn-danger" onClick={stopTimer}>
                                        <i className="bi bi-stop-fill me-1"></i>{t("Stop & Save")}
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Manual Entry */}
                <div className="col-md-6">
                    <div className="card border-0 shadow-sm">
                        <div className="card-body">
                            <h6 className="card-title mb-3">
                                <i className="bi bi-pencil-square me-2"></i>
                                {t("Manual Entry")}
                            </h6>
                            <select
                                className="form-select form-select-sm mb-2"
                                value={selectedTaskId}
                                onChange={(e) => setSelectedTaskId(e.target.value)}
                            >
                                <option value="">{t("Select Task...")}</option>
                                {tasks.map(task => (
                                    <option key={task.id} value={task.id}>{task.title}</option>
                                ))}
                            </select>
                            <div className="row g-2 mb-2">
                                <div className="col-6">
                                    <input
                                        type="date"
                                        className="form-control form-control-sm"
                                        value={entryDate}
                                        onChange={(e) => setEntryDate(e.target.value)}
                                    />
                                </div>
                                <div className="col-6">
                                    <div className="input-group input-group-sm">
                                        <input
                                            type="number"
                                            className="form-control"
                                            placeholder={t("Minutes")}
                                            value={duration}
                                            onChange={(e) => setDuration(e.target.value)}
                                            min="1"
                                        />
                                        <span className="input-group-text">min</span>
                                    </div>
                                </div>
                            </div>
                            <input
                                type="text"
                                className="form-control form-control-sm mb-2"
                                placeholder={t("Description (optional)")}
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                            />
                            <button className="btn btn-primary btn-sm w-100" onClick={handleManualEntry}>
                                <i className="bi bi-plus-lg me-1"></i>{t("Add Entry")}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Summary */}
            <div className="row mt-3">
                <div className="col-12">
                    <div className="d-flex gap-3 mb-3">
                        <div className="badge bg-primary fs-6 p-2">
                            <i className="bi bi-clock me-1"></i>
                            {t("Total")}: {totalHours}h ({totalMinutes} min)
                        </div>
                        <div className="badge bg-secondary fs-6 p-2">
                            <i className="bi bi-list-task me-1"></i>
                            {entries.length} {t("entries")}
                        </div>
                    </div>
                </div>
            </div>

            {/* Entries List grouped by date */}
            {sortedDates.length === 0 ? (
                <div className="text-center text-muted py-4">
                    <i className="bi bi-clock-history fs-1 d-block mb-2 opacity-25"></i>
                    <p>{t("No time entries yet")}</p>
                </div>
            ) : (
                sortedDates.map(date => (
                    <div key={date} className="mb-3">
                        <h6 className="text-muted border-bottom pb-2 mb-2">
                            <i className="bi bi-calendar-date me-2"></i>
                            {new Date(date + "T00:00:00").toLocaleDateString(undefined, { weekday: "long", year: "numeric", month: "long", day: "numeric" })}
                            <span className="badge bg-light text-dark ms-2">
                                {groupedEntries[date].reduce((sum, e) => sum + (e.durationMinutes || 0), 0)} min
                            </span>
                        </h6>
                        <div className="list-group list-group-flush">
                            {groupedEntries[date].map(entry => (
                                <div key={entry.id} className="list-group-item d-flex align-items-center px-0">
                                    <span className={`badge me-2 ${entry.type === "TIMER" ? "bg-success" : "bg-info"}`} style={{ fontSize: "0.7rem" }}>
                                        {entry.type === "TIMER" ? <i className="bi bi-stopwatch"></i> : <i className="bi bi-pencil"></i>}
                                    </span>
                                    <div className="flex-grow-1">
                                        <div className="fw-semibold small">{entry.taskTitle || t("Unknown Task")}</div>
                                        {entry.description && <small className="text-muted">{entry.description}</small>}
                                    </div>
                                    <div className="d-flex align-items-center gap-2">
                                        <span className="badge bg-light text-dark border">
                                            {entry.durationMinutes} min
                                        </span>
                                        <button
                                            className="btn btn-sm btn-link text-danger p-0"
                                            onClick={() => handleDelete(entry.id)}
                                            title={t("Delete")}
                                        >
                                            <i className="bi bi-trash"></i>
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                ))
            )}
        </div>
    );
}
