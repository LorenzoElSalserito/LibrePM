import { useState, useEffect } from "react";
import { librepm } from "@api/librepm.js";
import { toast } from "react-toastify";
import { useTranslation } from 'react-i18next';

export default function ProjectNotesWidget({ projectId, onNavigateToNotes, showProjectSelector, projects, onProjectChange }) {
    const { t } = useTranslation();
    const [notes, setNotes] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        async function load() {
            if (!projectId) {
                setNotes([]);
                return;
            }
            try {
                setLoading(true);
                // Carica note del progetto
                const list = await librepm.notesList("PROJECT", projectId);
                setNotes(list || []);
            } catch (e) {
                console.error("Errore caricamento note progetto:", e);
            } finally {
                setLoading(false);
            }
        }
        load();
    }, [projectId]);

    const handleNewNote = async () => {
        if (!projectId) {
            toast.warn(t("Select a project first"));
            return;
        }
        try {
            const newNote = await librepm.notesCreate({
                title: t("New Project Note"),
                content: "",
                parentType: "PROJECT",
                parentId: projectId
            });
            toast.success(t("Note created"));
            // Naviga alla pagina note per editare
            onNavigateToNotes(newNote);
        } catch (e) {
            toast.error(t("Error creating note") + ": " + e.message);
        }
    };

    return (
        <div className="card h-100 shadow-sm">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <div className="d-flex align-items-center gap-2 flex-grow-1">
                    <h6 className="mb-0 text-nowrap">
                        <i className="bi bi-journal-bookmark text-info me-2"></i>
                        {t("Notes")}
                    </h6>
                    {showProjectSelector && projects && (
                        <select 
                            className="form-select form-select-sm border-0 bg-light" 
                            value={projectId || ""}
                            onChange={(e) => onProjectChange(e.target.value)}
                            style={{ maxWidth: '200px' }}
                        >
                            <option value="">{t("Select project...")}</option>
                            {projects.map(p => (
                                <option key={p.id} value={p.id}>{p.name}</option>
                            ))}
                        </select>
                    )}
                </div>
                <button className="btn btn-sm btn-link" onClick={handleNewNote} disabled={!projectId}>
                    <i className="bi bi-plus-lg"></i>
                </button>
            </div>
            <div className="list-group list-group-flush">
                {!projectId ? (
                    <div className="list-group-item text-center text-muted py-4 small">
                        {t("Select a project to view notes.")}
                    </div>
                ) : loading ? (
                    <div className="list-group-item text-center py-3">
                        <div className="spinner-border spinner-border-sm text-secondary"></div>
                    </div>
                ) : notes.length === 0 ? (
                    <div className="list-group-item text-center text-muted py-4 small">
                        {t("No notes for this project.")}
                    </div>
                ) : (
                    notes.slice(0, 5).map(note => (
                        <div 
                            key={note.id} 
                            className="list-group-item list-group-item-action"
                            onClick={() => onNavigateToNotes(note)}
                            style={{ cursor: 'pointer' }}
                        >
                            <div className="d-flex w-100 justify-content-between">
                                <h6 className="mb-1 text-truncate" style={{ maxWidth: "80%" }}>{note.title || t("Untitled")}</h6>
                                <small className="text-muted">{new Date(note.updatedAt).toLocaleDateString()}</small>
                            </div>
                            <p className="mb-1 small text-muted text-truncate">
                                {note.content ? note.content.replace(/[#*`]/g, '') : t("No content")}
                            </p>
                        </div>
                    ))
                )}
            </div>
            {projectId && notes.length > 5 && (
                <div className="card-footer bg-white text-center p-1">
                    <button className="btn btn-sm btn-link text-muted" onClick={() => onNavigateToNotes(null)}>
                        {t("See all")} ({notes.length})
                    </button>
                </div>
            )}
        </div>
    );
}
