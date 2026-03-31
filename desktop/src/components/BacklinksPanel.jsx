import { useState, useEffect } from "react";
import { librepm } from "@api/librepm.js";
import { useTranslation } from "react-i18next";

/**
 * BacklinksPanel - Shows notes linked to a given entity (PRD-02-FR-005).
 * Displays a compact list of backlinked notes with type badges.
 *
 * @param {string} entityType - TASK, PROJECT, DELIVERABLE, etc.
 * @param {string} entityId - The entity ID
 * @param {function} onNavigate - Callback when clicking a note (receives noteId)
 */
export default function BacklinksPanel({ entityType, entityId, onNavigate }) {
    const { t } = useTranslation();
    const [notes, setNotes] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!entityType || !entityId) return;
        setLoading(true);
        librepm.notesBacklinks(entityType, entityId)
            .then(data => setNotes(data || []))
            .catch(err => console.error("Backlinks error:", err))
            .finally(() => setLoading(false));
    }, [entityType, entityId]);

    if (loading) {
        return (
            <div className="text-center py-2">
                <div className="spinner-border spinner-border-sm text-primary"></div>
            </div>
        );
    }

    if (notes.length === 0) {
        return (
            <div className="text-muted small text-center py-2">
                <i className="bi bi-link-45deg me-1"></i>
                {t("No linked notes")}
            </div>
        );
    }

    return (
        <div className="backlinks-panel">
            <div className="small fw-bold text-muted mb-2">
                <i className="bi bi-link-45deg me-1"></i>
                {t("Linked Notes")} ({notes.length})
            </div>
            {notes.map(note => (
                <div
                    key={note.id}
                    className="d-flex align-items-center gap-2 p-2 rounded mb-1"
                    style={{ cursor: "pointer", background: "rgba(0,0,0,0.03)" }}
                    onClick={() => onNavigate?.(note.id)}
                    title={note.title}
                >
                    <i className="bi bi-journal-text text-primary"></i>
                    <div className="flex-grow-1 text-truncate">
                        <span className="small">{note.title || t("Untitled")}</span>
                    </div>
                    <div className="d-flex gap-1">
                        {note.noteType && note.noteType !== "MEMO" && (
                            <span className="badge bg-primary bg-opacity-10 text-primary" style={{ fontSize: "0.6rem" }}>
                                {t(`noteType.${note.noteType}`)}
                            </span>
                        )}
                        {note.evidence && (
                            <span className="badge bg-warning text-dark" style={{ fontSize: "0.6rem" }}>
                                <i className="bi bi-shield-check"></i>
                            </span>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
}
