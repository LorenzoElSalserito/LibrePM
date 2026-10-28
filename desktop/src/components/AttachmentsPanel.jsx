import { useCallback, useEffect, useState } from "react";
import { useDropzone } from "react-dropzone";
import { toast } from "react-toastify";
import { useTranslation } from "react-i18next";
import { librepm } from "@api/librepm.js";
import "./AttachmentsPanel.css";

/** Must match librepm.assets.max-file-size on the backend. */
export const MAX_ATTACHMENT_BYTES = 50 * 1024 * 1024;

function formatSize(bytes) {
    if (bytes == null) return "";
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function fileIcon(mimeType = "") {
    if (mimeType.startsWith("image/")) return "bi-file-earmark-image";
    if (mimeType.includes("pdf")) return "bi-file-earmark-pdf";
    if (mimeType.includes("sheet") || mimeType.includes("excel") || mimeType.includes("csv")) return "bi-file-earmark-spreadsheet";
    if (mimeType.includes("word") || mimeType.includes("document")) return "bi-file-earmark-richtext";
    if (mimeType.includes("zip")) return "bi-file-earmark-zip";
    if (mimeType.startsWith("text/")) return "bi-file-earmark-text";
    return "bi-file-earmark";
}

/**
 * Files shared with the members of a project, or of one of its tasks.
 * Files travel through the database, so attachments uploaded on another PC
 * are listed and can be opened here.
 */
export default function AttachmentsPanel({ projectId = null, taskId = null, title = null }) {
    const { t, i18n } = useTranslation();
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [busyId, setBusyId] = useState(null);
    const [confirmId, setConfirmId] = useState(null);
    const [loadError, setLoadError] = useState(false);
    const currentUserId = librepm.getCurrentUser();
    const enabled = Boolean(taskId || projectId);

    const load = useCallback(async () => {
        if (!enabled) {
            setItems([]);
            return;
        }
        try {
            setLoading(true);
            setLoadError(false);
            const list = taskId
                ? await librepm.attachmentsListForTask(taskId)
                : await librepm.attachmentsListForProject(projectId);
            setItems(list || []);
        } catch (e) {
            console.error("[AttachmentsPanel] load failed", e);
            setLoadError(true);
        } finally {
            setLoading(false);
        }
    }, [enabled, projectId, taskId]);

    useEffect(() => {
        load();
    }, [load]);

    const uploadFiles = async (files) => {
        if (!files.length) return;
        setUploading(true);
        let uploaded = 0;
        for (const file of files) {
            try {
                await librepm.attachmentsUpload({ projectId, taskId, file });
                uploaded++;
            } catch (e) {
                const reason = e.status === 413 ? t("File too large (max 50 MB)") : e.message;
                toast.error(`${t("Upload failed")}: ${file.name} — ${reason}`);
            }
        }
        setUploading(false);
        if (uploaded > 0) {
            toast.success(t("{{count}} file(s) shared with the project", { count: uploaded }));
            load();
        }
    };

    const { getRootProps, getInputProps, isDragActive, open } = useDropzone({
        onDrop: uploadFiles,
        onDropRejected: (rejections) => rejections.forEach(({ file }) =>
            toast.error(`${t("Upload failed")}: ${file.name} — ${t("File too large (max 50 MB)")}`)),
        maxSize: MAX_ATTACHMENT_BYTES,
        multiple: true,
        noClick: true,
        noKeyboard: true,
        disabled: !enabled || uploading,
    });

    const handleDownload = async (asset) => {
        try {
            setBusyId(asset.id);
            await librepm.attachmentsDownload(asset);
        } catch (e) {
            toast.error(`${t("Download failed")}: ${e.message}`);
        } finally {
            setBusyId(null);
        }
    };

    const handleDelete = async (asset) => {
        try {
            setBusyId(asset.id);
            await librepm.attachmentsDelete(asset.id);
            setItems((prev) => prev.filter((a) => a.id !== asset.id));
            toast.success(t("File deleted"));
        } catch (e) {
            toast.error(`${t("Error")}: ${e.message}`);
        } finally {
            setBusyId(null);
            setConfirmId(null);
        }
    };

    const formatDate = (value) => {
        if (!value) return "";
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? "" : date.toLocaleString(i18n.language, { dateStyle: "short", timeStyle: "short" });
    };

    return (
        <section className="lp-attachments" aria-label={title || t("Shared files")}>
            <div className="lp-attachments__header">
                <h6 className="lp-attachments__title">
                    <i className="bi bi-paperclip" aria-hidden="true"></i>
                    {title || t("Shared files")}
                    {items.length > 0 && <span className="badge rounded-pill lp-attachments__count">{items.length}</span>}
                </h6>
                <div className="d-flex gap-2">
                    <button type="button" className="btn btn-sm btn-outline-secondary" onClick={load}
                            disabled={!enabled || loading} title={t("Refresh")} aria-label={t("Refresh")}>
                        <i className={`bi bi-arrow-clockwise ${loading ? "lp-attachments__spin" : ""}`} aria-hidden="true"></i>
                    </button>
                    <button type="button" className="btn btn-sm btn-primary" onClick={open} disabled={!enabled || uploading}>
                        <i className="bi bi-upload me-1" aria-hidden="true"></i>
                        {uploading ? t("Uploading...") : t("Upload file")}
                    </button>
                </div>
            </div>

            {!enabled ? (
                <p className="lp-attachments__empty">{t("Select a project to view files.")}</p>
            ) : (
                <div {...getRootProps({ className: `lp-attachments__drop ${isDragActive ? "is-active" : ""}` })}>
                    <input {...getInputProps()} aria-label={t("Upload file")} data-testid="attachments-input" />
                    {loadError ? (
                        <p className="lp-attachments__empty text-danger">{t("Could not load files")}</p>
                    ) : items.length === 0 ? (
                        <p className="lp-attachments__empty">
                            <i className="bi bi-cloud-arrow-up d-block fs-3 mb-1" aria-hidden="true"></i>
                            {loading ? t("Loading...") : (isDragActive ? t("Drop files here...") : t("No files yet: drag them here"))}
                        </p>
                    ) : (
                        <ul className="lp-attachments__list" data-testid="attachments-list">
                            {items.map((asset) => {
                                const mine = asset.ownerId === currentUserId;
                                const busy = busyId === asset.id;
                                return (
                                    <li key={asset.id} className="lp-attachments__item">
                                        <i className={`bi ${fileIcon(asset.mimeType || "")} lp-attachments__icon`} aria-hidden="true"></i>
                                        <div className="lp-attachments__meta">
                                            <span className="lp-attachments__name" title={asset.fileName}>{asset.fileName}</span>
                                            <small className="lp-attachments__details">
                                                {formatSize(asset.sizeBytes)}
                                                {asset.ownerName ? ` · ${t("Uploaded by {{name}}", { name: asset.ownerName })}` : ""}
                                                {asset.createdAt ? ` · ${formatDate(asset.createdAt)}` : ""}
                                            </small>
                                        </div>
                                        {confirmId === asset.id ? (
                                            <div className="lp-attachments__actions" role="group" aria-label={t("Delete this file for all project members?")}>
                                                <small className="text-danger me-1 d-none d-sm-inline">{t("Delete for everyone?")}</small>
                                                <button type="button" className="btn btn-sm btn-danger" disabled={busy} onClick={() => handleDelete(asset)}>
                                                    {t("Delete")}
                                                </button>
                                                <button type="button" className="btn btn-sm btn-outline-secondary" disabled={busy} onClick={() => setConfirmId(null)}>
                                                    {t("Cancel")}
                                                </button>
                                            </div>
                                        ) : (
                                            <div className="lp-attachments__actions">
                                                <button type="button" className="btn btn-sm btn-outline-primary" disabled={busy}
                                                        onClick={() => handleDownload(asset)}
                                                        title={t("Download")} aria-label={`${t("Download")} ${asset.fileName}`}>
                                                    <i className={`bi ${busy ? "bi-hourglass-split" : "bi-download"}`} aria-hidden="true"></i>
                                                </button>
                                                {mine && (
                                                    <button type="button" className="btn btn-sm btn-outline-danger" disabled={busy}
                                                            onClick={() => setConfirmId(asset.id)}
                                                            title={t("Delete")} aria-label={`${t("Delete")} ${asset.fileName}`}>
                                                        <i className="bi bi-trash" aria-hidden="true"></i>
                                                    </button>
                                                )}
                                            </div>
                                        )}
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </div>
            )}
            {enabled && (
                <small className="lp-attachments__hint">
                    <i className="bi bi-people me-1" aria-hidden="true"></i>
                    {t("Files are shared with all project members, also on other PCs. Max 50 MB per file.")}
                </small>
            )}
        </section>
    );
}
