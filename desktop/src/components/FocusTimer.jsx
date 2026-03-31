import { useEffect, useRef, useState } from "react";
import { librepm } from "@api/librepm.js";
import { toast } from "react-toastify";
import { useTranslation } from 'react-i18next';

function formatMs(ms) {
    const total = Math.floor(ms / 1000);
    const m = String(Math.floor(total / 60)).padStart(2, "0");
    const s = String(total % 60).padStart(2, "0");
    return `${m}:${s}`;
}

export default function FocusTimer({ selectedTask }) {
    const { t } = useTranslation();
    const [running, setRunning] = useState(null);
    const [elapsed, setElapsed] = useState(0);

    const tickRef = useRef(null);

    async function loadRunning() {
        const r = await librepm.focusRunning();
        setRunning(r);

        if (r) setElapsed(Date.now() - r.startedAt);
        else setElapsed(0);
    }

    useEffect(() => {
        loadRunning();
    }, []);

    useEffect(() => {
        if (tickRef.current) clearInterval(tickRef.current);

        if (running) {
            tickRef.current = setInterval(() => {
                setElapsed(Date.now() - running.startedAt);
            }, 500);
        }

        return () => {
            if (tickRef.current) clearInterval(tickRef.current);
        };
    }, [running]);

    async function start() {
        if (!selectedTask) return;
        const res = await librepm.focusStart(selectedTask.id);
        if (res.ok) await loadRunning();
        else toast.error(res.error);
    }

    async function stop() {
        const res = await librepm.focusStop();
        if (res.ok) await loadRunning();
        else toast.error(res.error);
    }

    const isThisTaskRunning =
        running && selectedTask && running.taskId === selectedTask.id;

    return (
        <div className="card bg-black text-light border-secondary">
            <div className="card-header border-secondary">{t("Focus Timer")}</div>

            <div className="card-body d-flex flex-column gap-3">
                <div>
                    <div className="text-secondary small mb-1">{t("Selected task")}</div>
                    <div className="fw-bold">{selectedTask ? selectedTask.title : "â€”"}</div>
                </div>

                <div className="display-6">{formatMs(elapsed)}</div>

                <div className="d-flex gap-2">
                    <button className="btn btn-success" disabled={!selectedTask || running} onClick={start}>
                        {t("Start")}
                    </button>

                    <button className="btn btn-danger" disabled={!running} onClick={stop}>
                        {t("Stop")}
                    </button>
                </div>

                {running && (
                    <div className="text-secondary small">
                        {t("Timer active on:")}{" "}
                        <span className={isThisTaskRunning ? "text-light" : "text-warning"}>
              {running.taskId}
            </span>
                    </div>
                )}

                <div className="text-secondary small">{t("MVP: simple timer + session saving")}</div>
            </div>
        </div>
    );
}
