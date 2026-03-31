import { useState } from "react";
import { librepm } from "@api/librepm.js";
import { useTranslation } from 'react-i18next';

export default function DataActions({ selectedProjectId, onImported }) {
    const { t } = useTranslation();
    const [busy, setBusy] = useState(false);
    const [message, setMessage] = useState("");

    async function doExportJson() {
        setBusy(true);
        setMessage("");
        try {
            librepm.log("[UI] Export JSON dialog");
            const res = await librepm.exportJsonDialog();
            if (res?.ok) setMessage(`${t("JSON exported:")} ${res.filePath}`);
            else if (res?.canceled) setMessage(t("Cancel"));
            else setMessage(t("Error"));
        } catch (e) {
            librepm.error("Export JSON error", e);
            setMessage(t("Error"));
        } finally {
            setBusy(false);
        }
    }

    async function doExportCsv() {
        setBusy(true);
        setMessage("");
        try {
            if (!selectedProjectId) {
                setMessage(t("Select a project to export CSV"));
                return;
            }
            librepm.log("[UI] Export CSV dialog projectId=", selectedProjectId);
            const res = await librepm.exportCsvDialog(selectedProjectId);
            if (res?.ok) setMessage(`${t("Success")}: ${res.filePath}`);
            else if (res?.canceled) setMessage(t("Cancel"));
            else setMessage(res?.error ?? t("Error"));
        } catch (e) {
            librepm.error("Export CSV error", e);
            setMessage(t("Error"));
        } finally {
            setBusy(false);
        }
    }

    async function doImportJson() {
        setBusy(true);
        setMessage("");
        try {
            librepm.log("[UI] Import JSON dialog");
            const res = await librepm.importJsonDialog();
            if (res?.ok) {
                setMessage(
                    `${t("Success")}: ${t("Projects")} = ${res.stats.projects}, ${t("Task")} = ${res.stats.tasks}, ${t("Sessions")} = ${res.stats.focusSessions}`
                );
                if (onImported) await onImported();
            } else if (res?.canceled) {
                setMessage(t("Cancel"));
            } else {
                setMessage(t("Error"));
            }
        } catch (e) {
            librepm.error("Import JSON error", e);
            setMessage(t("Error"));
        } finally {
            setBusy(false);
        }
    }

    return (
        <div className="d-flex flex-column gap-2 align-items-end">
            <div className="d-flex gap-2 flex-wrap justify-content-end">
                <button className="btn btn-outline-light" disabled={busy} onClick={doExportJson}>
                    {t("Export JSON")}
                </button>
                <button
                    className="btn btn-outline-light"
                    disabled={busy || !selectedProjectId}
                    onClick={doExportCsv}
                    title={!selectedProjectId ? t("Select a project") : ""}
                >
                    {t("Export CSV (Project)")}
                </button>
                <button className="btn btn-outline-warning" disabled={busy} onClick={doImportJson}>
                    {t("Import JSON")}
                </button>
            </div>

            {message && <div className="text-secondary small text-end">{message}</div>}
        </div>
    );
}
