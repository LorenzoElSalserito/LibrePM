import React, { useState, useEffect, useCallback, useMemo } from "react";
import { librepm } from "@api/librepm.js";
import { toast } from "react-toastify";
import MDEditor from "@uiw/react-md-editor";
import { useTranslation } from 'react-i18next';
import { useModal } from "../hooks/useModal.js";

/**
 * NotesPage - Pagina per gestione note markdown (Feed/Inbox)
 * 
 * Features:
 * - Feed note (Inbox, Sent, All)
 * - Filtri contestuali (Progetto/Task)
 * - Editor Markdown completo
 * - Preview in tempo reale
 * - Creazione/Modifica/Eliminazione note
 * - Ricerca full-text
 * - Deep Linking (apertura nota specifica da contesto)
 * 
 * @author Lorenzo DM
 * @since 0.2.0
 * @updated 0.8.1 - Fix creazione note contestuali
 */

// ========================================
// NoteCard Component
// ========================================

function NoteCard({ note, isActive, onClick, onDelete, currentUserId, t }) {
    const isOwner = note.owner?.id === currentUserId;
    
    const updatedAt = note.updatedAt 
        ? new Date(note.updatedAt).toLocaleDateString("it-IT", {
            day: "2-digit",
            month: "short",
            hour: "2-digit",
            minute: "2-digit",
        })
        : null;

    const preview = useMemo(() => {
        if (!note.content) return t("No content");
        const text = note.content
            .replace(/#{1,6}\s/g, "")
            .replace(/\*\*/g, "")
            .replace(/\*/g, "")
            .replace(/`/g, "")
            .replace(/\[([^\]]+)\]\([^)]+\)/g, "$1")
            .trim();
        return text.length > 100 ? text.substring(0, 100) + "..." : text;
    }, [note.content, t]);

    return (
        <div
            className={`note-card card mb-2 ${isActive ? "border-primary" : ""}`}
            onClick={onClick}
            style={{
                cursor: "pointer",
                borderLeftWidth: 4,
                borderLeftColor: isActive ? "#0d6efd" : (isOwner ? "#198754" : "#6c757d"),
            }}
        >
            <div className="card-body p-3">
                <div className="d-flex justify-content-between align-items-start">
                    <h6 className="card-title mb-1 text-truncate" style={{maxWidth: "75%"}}>
                        {note.title || t("Untitled")}
                    </h6>
                    <div className="d-flex gap-1 align-items-center">
                        {note.evidence && (
                            <span className="badge bg-warning text-dark" style={{fontSize: '0.6rem'}} title={t("Evidence")}>
                                <i className="bi bi-shield-check"></i>
                            </span>
                        )}
                        {note.frozen && (
                            <span className="badge bg-info text-white" style={{fontSize: '0.6rem'}} title={t("Frozen")}>
                                <i className="bi bi-lock-fill"></i>
                            </span>
                        )}
                        {isOwner && (
                            <button
                                className="btn btn-sm btn-link text-danger p-0"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onDelete(note);
                                }}
                                title={t("Delete")}
                            >
                                <i className="bi bi-trash"></i>
                            </button>
                        )}
                    </div>
                </div>
                
                {!isOwner && (
                    <div className="small text-primary mb-1">
                        <i className="bi bi-person-circle me-1"></i>
                        {note.owner?.displayName || note.owner?.username || t("Unknown")}
                    </div>
                )}
                
                <p className="card-text text-muted small mb-1" style={{ 
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    display: "-webkit-box",
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: "vertical",
                }}>
                    {preview}
                </p>

                <div className="d-flex justify-content-between align-items-center mt-2">
                    {updatedAt && (
                        <small className="text-muted" style={{fontSize: '0.7rem'}}>
                            {updatedAt}
                        </small>
                    )}
                    
                    <div className="d-flex gap-1">
                        {note.noteType && note.noteType !== "MEMO" && (
                            <span className="badge bg-primary bg-opacity-10 text-primary border" style={{fontSize: '0.6rem'}}>
                                {t(`noteType.${note.noteType}`)}
                            </span>
                        )}
                        <span className="badge bg-light text-dark border" style={{fontSize: '0.65rem'}}>
                            {note.parentType === "TASK" ? "TASK" : "PROJ"}
                        </span>
                        {note.parentTitle && (
                            <span className="badge bg-light text-secondary border text-truncate" style={{fontSize: '0.65rem', maxWidth: '80px'}} title={note.parentTitle}>
                                {note.parentTitle}
                            </span>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

// ========================================
// NotesPage Main Component
// ========================================

export default function NotesPage({ shell }) {
    const { t } = useTranslation();
    const modal = useModal();
    const [notes, setNotes] = useState([]);
    const [activeNote, setActiveNote] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [searchTerm, setSearchTerm] = useState("");
    
    // Filtri
    const [scope, setScope] = useState("ALL"); // ALL, INBOX, SENT
    const [contextFilter, setContextFilter] = useState("ALL"); // ALL, PROJECT, TASK
    const [contextId, setContextId] = useState(""); // ID specifico se filtrato
    
    // Liste per filtri
    const [availableTasks, setAvailableTasks] = useState([]);
    
    const context = shell?.navContext;
    const currentUserId = librepm.getCurrentUser();

    const [editTitle, setEditTitle] = useState("");
    const [editContent, setEditContent] = useState("");
    const [editorMode, setEditorMode] = useState("edit");
    const [hasChanges, setHasChanges] = useState(false);
    const [showRevisions, setShowRevisions] = useState(false);
    const [revisions, setRevisions] = useState([]);
    const [loadingRevisions, setLoadingRevisions] = useState(false);

    // Note type filter
    const [typeFilter, setTypeFilter] = useState("ALL");
    const [evidenceOnly, setEvidenceOnly] = useState(false);

    const NOTE_TYPES = ["MEMO", "RATIONALE", "MEETING_NOTE", "RISK_NOTE", "GRANT_NOTE", "SPONSOR_NOTE", "AUDIT_NOTE", "DECISION", "BRIEF"];

    // State for the creation modal
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [newNoteTitle, setNewNoteTitle] = useState("");
    const [newNoteParentType, setNewNoteParentType] = useState("PROJECT");
    const [newNoteParentId, setNewNoteParentId] = useState("");
    const [newNoteType, setNewNoteType] = useState("MEMO");

    // Initialization from navigation context
    useEffect(() => {
        if (context?.noteId) {
            // If there's a noteId, we'll load the specific note later
            // But first we load the feed for context
        }
        
        if (context?.taskId) {
            setContextFilter("TASK");
            setContextId(context.taskId);
        } else if (context?.projectId) {
            setContextFilter("PROJECT");
            setContextId(context.projectId);
        }
    }, [context]);

    // Load tasks for filters and creation
    useEffect(() => {
        if (shell?.currentProject) {
            librepm.tasksList(shell.currentProject.id)
                .then(tasks => setAvailableTasks(tasks))
                .catch(err => console.error("Error loading tasks", err));
        }
    }, [shell?.currentProject]);

    // Helper to open the modal with the correct data
    const openCreateModal = useCallback(() => {
        if (!shell?.currentProject?.id) {
            toast.warn(t("notes.no_project_selected_select_first"));
            return;
        }

        setNewNoteTitle("");
        setNewNoteType("MEMO");

        // Smart pre-fill based on current filters
        if (contextFilter === "TASK" && contextId) {
            setNewNoteParentType("TASK");
            setNewNoteParentId(contextId);
        } else if (contextFilter === "PROJECT" && contextId) {
            setNewNoteParentType("PROJECT");
            setNewNoteParentId(contextId);
        } else {
            // Default to the current project
            setNewNoteParentType("PROJECT");
            setNewNoteParentId(shell.currentProject.id);
        }
        
        setShowCreateModal(true);
    }, [contextFilter, contextId, shell?.currentProject, t]);

    useEffect(() => {
        shell?.setTitle?.(t("Notes & Feed"));
        
        const headerActions = (
            <div className="d-flex gap-2 align-items-center">
                <div className="input-group input-group-sm" style={{ width: 250 }}>
                    <span className="input-group-text bg-transparent border-end-0">
                        <i className="bi bi-search"></i>
                    </span>
                    <input
                        type="text"
                        className="form-control border-start-0 ps-0"
                        placeholder={t("Search notes...")}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                
                <button
                    className="btn btn-sm btn-primary"
                    onClick={openCreateModal}
                >
                    <i className="bi bi-plus-lg me-1"></i>
                    {t("New Note")}
                </button>
            </div>
        );

        shell?.setHeaderActions?.(headerActions);
        shell?.setRightPanel?.(null);

        return () => {
            shell?.setHeaderActions?.(null);
        };
    }, [shell, searchTerm, contextFilter, contextId, t, openCreateModal]);

    const loadNotes = useCallback(async () => {
        if (!shell?.currentProject) {
            setNotes([]);
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            let fetchedNotes = [];

            // Loading logic based on filters
            if (contextFilter === "TASK" && contextId) {
                fetchedNotes = await librepm.notesListTask(contextId);
            } else if (contextFilter === "PROJECT" && contextId) {
                fetchedNotes = await librepm.notesListProject(contextId);
            } else {
                // General feed (filtered by scope)
                fetchedNotes = await librepm.notesFeed(scope);
            }
            
            // Client-side filter for fast search
            if (searchTerm) {
                const lower = searchTerm.toLowerCase();
                fetchedNotes = fetchedNotes.filter(n =>
                    (n.title && n.title.toLowerCase().includes(lower)) ||
                    (n.content && n.content.toLowerCase().includes(lower))
                );
            }

            // Type filter
            if (typeFilter !== "ALL") {
                fetchedNotes = fetchedNotes.filter(n => n.noteType === typeFilter);
            }

            // Evidence filter
            if (evidenceOnly) {
                fetchedNotes = fetchedNotes.filter(n => n.evidence);
            }

            setNotes(fetchedNotes || []);
            
            // Deep Linking: If there's a noteId in context, find or load it
            if (context?.noteId) {
                const target = fetchedNotes.find(n => n.id === context.noteId);
                if (target) {
                    setActiveNote(target);
                } else {
                    // If not in the list (e.g. filtered list), try loading it individually
                    try {
                        const singleNote = await librepm.notesGet(context.noteId);
                        setActiveNote(singleNote);
                        // Optional: add it to the list if consistent
                        setNotes(prev => [singleNote, ...prev]);
                    } catch (e) {
                        console.warn("Deep-link note not found", e);
                    }
                }
            } else if (activeNote && !fetchedNotes.find(n => n.id === activeNote.id)) {
                // If filter changes and the active note disappears, deselect
                setActiveNote(null);
            }
        } catch (e) {
            toast.error(t("Error loading notes") + ": " + e.message);
        } finally {
            setLoading(false);
        }
    }, [shell?.currentProject, contextFilter, contextId, scope, searchTerm, context, typeFilter, evidenceOnly]);

    useEffect(() => {
        loadNotes();
    }, [loadNotes]);

    useEffect(() => {
        if (activeNote) {
            setEditTitle(activeNote.title || "");
            setEditContent(activeNote.content || "");
            setHasChanges(false);
            setShowRevisions(false);
            setRevisions([]);
        } else {
            setEditTitle("");
            setEditContent("");
            setHasChanges(false);
            setShowRevisions(false);
            setRevisions([]);
        }
    }, [activeNote]);

    const loadRevisions = useCallback(async () => {
        if (!activeNote) return;
        try {
            setLoadingRevisions(true);
            const revs = await librepm.notesRevisions(activeNote.id);
            setRevisions(revs || []);
        } catch (e) {
            console.error("Error loading revisions", e);
        } finally {
            setLoadingRevisions(false);
        }
    }, [activeNote]);

    const handleCreateNote = async () => {
        if (!newNoteTitle.trim()) {
            toast.warn(t("Please enter a title"));
            return;
        }
        
        if (!newNoteParentId) {
            if (newNoteParentType === "PROJECT") {
                toast.warn(t("notes.no_project_selected_select_first"));
            } else {
                toast.warn(t("notes.please_select_a_task"));
            }
            return;
        }

        try {
            let newNote;
            const noteData = {
                title: newNoteTitle,
                content: "",
                noteType: newNoteType
            };

            if (newNoteParentType === "TASK") {
                newNote = await librepm.notesCreateTask(newNoteParentId, noteData);
            } else {
                newNote = await librepm.notesCreateProject(newNoteParentId, noteData);
            }

            // Update the list ONLY if the new note is consistent with the current filters
            const shouldAddToList = 
                contextFilter === "ALL" || 
                (contextFilter === "PROJECT" && newNoteParentType === "PROJECT") ||
                (contextFilter === "TASK" && newNoteParentType === "TASK" && newNoteParentId === contextId);

            if (shouldAddToList) {
                setNotes((prev) => [newNote, ...prev]);
            }
            
            setActiveNote(newNote);
            setShowCreateModal(false);
            toast.success(t("Note created"));
        } catch (e) {
            toast.error(t("Error creating note") + ": " + e.message);
        }
    };

    const handleSave = async () => {
        if (!activeNote) return;
        try {
            setSaving(true);
            const updatedNote = await librepm.notesUpdate(activeNote.id, {
                title: editTitle,
                content: editContent,
            });
            setActiveNote(updatedNote);
            setNotes((prev) => prev.map((n) => n.id === activeNote.id ? updatedNote : n));
            setHasChanges(false);
            toast.success(t("Note saved"));
        } catch (e) {
            toast.error(t("Error saving note") + ": " + e.message);
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (note) => {
        const confirmed = await modal.confirm({ title: t("Are you sure you want to delete") + ` "${note.title}"?` });
        if (!confirmed) return;
        try {
            await librepm.notesDelete(note.id);
            setNotes((prev) => prev.filter((n) => n.id !== note.id));
            if (activeNote?.id === note.id) setActiveNote(null);
            toast.success(t("Note deleted"));
        } catch (e) {
            toast.error(t("Error deleting note") + ": " + e.message);
        }
    };

    const handleTitleChange = (value) => {
        setEditTitle(value);
        setHasChanges(true);
    };

    const handleContentChange = (value) => {
        setEditContent(value || "");
        setHasChanges(true);
    };

    const isOwner = activeNote?.owner?.id === currentUserId;

    if (loading && notes.length === 0) return <div className="d-flex justify-content-center align-items-center py-5"><div className="spinner-border text-primary"></div></div>;
    if (!shell?.currentProject) return <div className="text-center py-5"><i className="bi bi-folder2-open fs-1 text-muted d-block mb-3"></i><h5 className="text-muted">{t("notes.no_project_selected")}</h5><p className="text-muted small">{t("Select or create a project to manage notes.")}</p></div>;

    return (
        <div className="notes-page d-flex h-100" style={{ minHeight: "calc(100vh - 150px)" }}>
            <div className="notes-sidebar border-end bg-light d-flex flex-column" style={{ width: 320, minWidth: 280 }}>
                {/* Filtri Scope (Inbox/Sent/All) */}
                <div className="p-2 border-bottom bg-white">
                    <div className="d-flex gap-1 justify-content-center mb-2">
                        <button 
                            className={`btn btn-sm flex-grow-1 ${scope === 'ALL' ? 'btn-primary' : 'btn-outline-secondary'}`}
                            onClick={() => { setScope('ALL'); setContextFilter('ALL'); }}
                        >
                            {t("All")}
                        </button>
                        <button 
                            className={`btn btn-sm flex-grow-1 ${scope === 'INBOX' ? 'btn-primary' : 'btn-outline-secondary'}`}
                            onClick={() => { setScope('INBOX'); setContextFilter('ALL'); }}
                        >
                            {t("Inbox")}
                        </button>
                        <button 
                            className={`btn btn-sm flex-grow-1 ${scope === 'SENT' ? 'btn-primary' : 'btn-outline-secondary'}`}
                            onClick={() => { setScope('SENT'); setContextFilter('ALL'); }}
                        >
                            {t("Sent")}
                        </button>
                    </div>
                    
                    {/* Filtri Contestuali */}
                    <div className="d-flex gap-2">
                        <select
                            className="form-select form-select-sm"
                            value={contextFilter}
                            onChange={(e) => {
                                setContextFilter(e.target.value);
                                if (e.target.value === "PROJECT") setContextId(shell.currentProject.id);
                                if (e.target.value === "ALL") setContextId("");
                                if (e.target.value === "TASK") setContextId(""); // Reset to force selection
                            }}
                        >
                            <option value="ALL">{t("All contexts")}</option>
                            <option value="PROJECT">{t("Project")}</option>
                            <option value="TASK">{t("Task")}</option>
                        </select>

                        {contextFilter === "TASK" && (
                            <select
                                className="form-select form-select-sm"
                                value={contextId}
                                onChange={(e) => setContextId(e.target.value)}
                            >
                                <option value="">{t("Select Task...")}</option>
                                {availableTasks.map(t => (
                                    <option key={t.id} value={t.id}>{t.title.substring(0, 20)}...</option>
                                ))}
                            </select>
                        )}
                    </div>

                    {/* Type & Evidence Filters */}
                    <div className="d-flex gap-2 mt-2">
                        <select
                            className="form-select form-select-sm"
                            value={typeFilter}
                            onChange={(e) => setTypeFilter(e.target.value)}
                        >
                            <option value="ALL">{t("All Types")}</option>
                            {NOTE_TYPES.map(nt => (
                                <option key={nt} value={nt}>{t(`noteType.${nt}`)}</option>
                            ))}
                        </select>
                        <button
                            className={`btn btn-sm ${evidenceOnly ? 'btn-warning' : 'btn-outline-secondary'}`}
                            onClick={() => setEvidenceOnly(!evidenceOnly)}
                            title={t("Evidence Only")}
                        >
                            <i className="bi bi-shield-check"></i>
                        </button>
                    </div>
                </div>

                {/* Lista Note */}
                <div className="flex-grow-1 overflow-auto p-2">
                    {notes.length === 0 ? (
                        <div className="text-center text-muted py-4">
                            <i className="bi bi-journal-text fs-1 d-block mb-2"></i>
                            <small>{t("No notes found")}</small>
                            <br />
                            <button className="btn btn-sm btn-primary mt-2" onClick={openCreateModal}>{t("Create the first note")}</button>
                        </div>
                    ) : (
                        notes.map((note) => (
                            <NoteCard
                                key={note.id}
                                note={note}
                                isActive={activeNote?.id === note.id}
                                onClick={() => setActiveNote(note)}
                                onDelete={handleDelete}
                                currentUserId={currentUserId}
                                t={t}
                            />
                        ))
                    )}
                </div>
            </div>

            <div className="notes-editor flex-grow-1 d-flex flex-column">
                {activeNote ? (
                    <>
                        <div className="editor-toolbar border-bottom p-2 bg-white d-flex justify-content-between align-items-center">
                            <div className="d-flex align-items-center gap-2 flex-grow-1">
                                {!isOwner && (
                                    <span className="badge bg-secondary me-2">
                                        <i className="bi bi-lock-fill me-1"></i>
                                        {t("Read Only")}
                                    </span>
                                )}
                                {activeNote?.frozen && (
                                    <span className="badge bg-info me-1">
                                        <i className="bi bi-lock-fill me-1"></i>
                                        {t("Frozen")}
                                    </span>
                                )}
                                {activeNote?.evidence && (
                                    <span className="badge bg-warning text-dark me-1">
                                        <i className="bi bi-shield-check me-1"></i>
                                        {t("Evidence")}
                                    </span>
                                )}
                                <input
                                    type="text"
                                    className="form-control form-control-sm border-0 fs-5 fw-bold"
                                    value={editTitle}
                                    onChange={(e) => handleTitleChange(e.target.value)}
                                    placeholder={t("Title")}
                                    style={{ maxWidth: 300 }}
                                    disabled={!isOwner || activeNote?.frozen}
                                />
                                {isOwner && (
                                    <select
                                        className="form-select form-select-sm"
                                        style={{ width: 140 }}
                                        value={activeNote?.noteType || "MEMO"}
                                        onChange={async (e) => {
                                            if (activeNote?.frozen) { toast.warn(t("Cannot edit frozen note")); return; }
                                            try {
                                                const updated = await librepm.notesUpdate(activeNote.id, { noteType: e.target.value });
                                                setActiveNote(updated);
                                                setNotes(prev => prev.map(n => n.id === activeNote.id ? updated : n));
                                            } catch (err) { toast.error(err.message); }
                                        }}
                                    >
                                        {NOTE_TYPES.map(nt => (
                                            <option key={nt} value={nt}>{t(`noteType.${nt}`)}</option>
                                        ))}
                                    </select>
                                )}
                            </div>
                            <div className="d-flex gap-2 align-items-center">
                                <div className="btn-group btn-group-sm">
                                    <button className={`btn ${editorMode === "edit" ? "btn-primary" : "btn-outline-secondary"}`} onClick={() => setEditorMode("edit")} title={t("Editor")}><i className="bi bi-pencil"></i></button>
                                    <button className={`btn ${editorMode === "live" ? "btn-primary" : "btn-outline-secondary"}`} onClick={() => setEditorMode("live")} title={t("Editor + Preview")}><i className="bi bi-layout-split"></i></button>
                                    <button className={`btn ${editorMode === "preview" ? "btn-primary" : "btn-outline-secondary"}`} onClick={() => setEditorMode("preview")} title={t("Preview")}><i className="bi bi-eye"></i></button>
                                </div>
                                <button
                                    className={`btn btn-sm ${showRevisions ? "btn-secondary" : "btn-outline-secondary"}`}
                                    onClick={() => {
                                        const next = !showRevisions;
                                        setShowRevisions(next);
                                        if (next) loadRevisions();
                                    }}
                                    title={t("Revisions")}
                                >
                                    <i className="bi bi-clock-history"></i>
                                </button>
                                {isOwner && (
                                    <>
                                        <button
                                            className={`btn btn-sm ${activeNote?.evidence ? "btn-warning" : "btn-outline-warning"}`}
                                            title={activeNote?.evidence ? t("Remove Evidence") : t("Mark as Evidence")}
                                            onClick={async () => {
                                                if (activeNote?.frozen) { toast.warn(t("Cannot edit frozen note")); return; }
                                                try {
                                                    const updated = await librepm.notesUpdate(activeNote.id, { evidence: !activeNote.evidence });
                                                    setActiveNote(updated);
                                                    setNotes(prev => prev.map(n => n.id === activeNote.id ? updated : n));
                                                } catch (err) { toast.error(err.message); }
                                            }}
                                        >
                                            <i className="bi bi-shield-check"></i>
                                        </button>
                                        <button
                                            className={`btn btn-sm ${activeNote?.frozen ? "btn-info" : "btn-outline-info"}`}
                                            title={activeNote?.frozen ? t("Unfreeze Note") : t("Freeze Note")}
                                            onClick={async () => {
                                                try {
                                                    const updated = await librepm.notesUpdate(activeNote.id, { frozen: !activeNote.frozen });
                                                    setActiveNote(updated);
                                                    setNotes(prev => prev.map(n => n.id === activeNote.id ? updated : n));
                                                    toast.success(activeNote?.frozen ? t("Note unfrozen") : t("Note frozen"));
                                                } catch (err) { toast.error(err.message); }
                                            }}
                                        >
                                            <i className={`bi ${activeNote?.frozen ? "bi-unlock" : "bi-lock"}`}></i>
                                        </button>
                                        <button
                                            className={`btn btn-sm ${hasChanges ? "btn-success" : "btn-outline-secondary"}`}
                                            onClick={handleSave}
                                            disabled={!hasChanges || saving || activeNote?.frozen}
                                        >
                                            {saving ? <span className="spinner-border spinner-border-sm me-1"></span> : <i className="bi bi-check-lg me-1"></i>}
                                            {hasChanges ? t("Save") : t("Saved")}
                                        </button>
                                    </>
                                )}
                            </div>
                        </div>
                        <div className="d-flex flex-grow-1 overflow-hidden">
                            <div className="editor-content flex-grow-1" data-color-mode={shell.currentTheme === 'dark' ? 'dark' : 'light'}>
                                <MDEditor
                                    value={editContent}
                                    onChange={handleContentChange}
                                    preview={(!isOwner || activeNote?.frozen) ? "preview" : editorMode}
                                    height="100%"
                                    style={{ height: "100%" }}
                                    hideToolbar={editorMode === "preview" || !isOwner || activeNote?.frozen}
                                    visibleDragbar={false}
                                    textareaProps={{
                                        disabled: !isOwner || activeNote?.frozen
                                    }}
                                />
                            </div>
                            {showRevisions && (
                                <div className="revisions-panel border-start bg-light overflow-auto" style={{ width: 280, minWidth: 240 }}>
                                    <div className="p-2 border-bottom bg-white">
                                        <h6 className="mb-0"><i className="bi bi-clock-history me-1"></i>{t("Revisions")}</h6>
                                    </div>
                                    <div className="p-2">
                                        {loadingRevisions ? (
                                            <div className="text-center py-3"><div className="spinner-border spinner-border-sm text-primary"></div></div>
                                        ) : revisions.length === 0 ? (
                                            <p className="text-muted small text-center py-3">{t("No revisions yet")}</p>
                                        ) : (
                                            revisions.map((rev) => (
                                                <div
                                                    key={rev.id}
                                                    className="card mb-2 cursor-pointer"
                                                    style={{ cursor: "pointer" }}
                                                    onClick={() => {
                                                        setEditTitle(rev.title || "");
                                                        setEditContent(rev.content || "");
                                                        setHasChanges(true);
                                                    }}
                                                    title={t("Click to restore this revision")}
                                                >
                                                    <div className="card-body p-2">
                                                        <div className="d-flex justify-content-between">
                                                            <small className="fw-bold">#{rev.revisionNumber}</small>
                                                            <small className="text-muted" style={{fontSize: '0.7rem'}}>
                                                                {rev.createdAt ? new Date(rev.createdAt).toLocaleDateString("it-IT", {
                                                                    day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit"
                                                                }) : ""}
                                                            </small>
                                                        </div>
                                                        <small className="text-truncate d-block text-muted">{rev.title || t("Untitled")}</small>
                                                    </div>
                                                </div>
                                            ))
                                        )}
                                    </div>
                                </div>
                            )}
                        </div>
                    </>
                ) : (
                    <div className="d-flex flex-column align-items-center justify-content-center h-100 text-muted">
                        <i className="bi bi-journal-richtext fs-1 mb-3"></i>
                        <h5>{t("Select a note")}</h5>
                        <p className="small">
                            {t("Choose a note from the list or")} <button className="btn btn-link btn-sm p-0 ms-1" onClick={openCreateModal}>{t("create a new one")}</button>
                        </p>
                    </div>
                )}
            </div>

            {/* Modal Creazione Nota */}
            {showCreateModal && (
                <div className="modal show d-block" style={{ backgroundColor: "rgba(0,0,0,0.5)", zIndex: 1050 }}>
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">{t("New Note")}</h5>
                                <button type="button" className="btn-close" onClick={() => setShowCreateModal(false)}></button>
                            </div>
                            <div className="modal-body">
                                <div className="mb-3">
                                    <label className="form-label">{t("Title")}</label>
                                    <input 
                                        type="text" 
                                        className="form-control" 
                                        value={newNoteTitle}
                                        onChange={(e) => setNewNoteTitle(e.target.value)}
                                        placeholder={t("Enter title...")}
                                        autoFocus
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">{t("Note Type")}</label>
                                    <select
                                        className="form-select"
                                        value={newNoteType}
                                        onChange={(e) => setNewNoteType(e.target.value)}
                                    >
                                        {NOTE_TYPES.map(nt => (
                                            <option key={nt} value={nt}>{t(`noteType.${nt}`)}</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">{t("Associate to")}</label>
                                    <select 
                                        className="form-select mb-2"
                                        value={newNoteParentType}
                                        onChange={(e) => {
                                            const newType = e.target.value;
                                            setNewNoteParentType(newType);
                                            if (newType === "PROJECT") {
                                                setNewNoteParentId(shell.currentProject?.id || "");
                                            } else {
                                                setNewNoteParentId(availableTasks.length > 0 ? availableTasks[0].id : "");
                                            }
                                        }}
                                    >
                                        <option value="PROJECT">{t("Current Project")}</option>
                                        <option value="TASK">{t("Specific Task")}</option>
                                    </select>

                                    {newNoteParentType === "TASK" && (
                                        availableTasks.length > 0 ? (
                                            <select 
                                                className="form-select"
                                                value={newNoteParentId}
                                                onChange={(e) => setNewNoteParentId(e.target.value)}
                                            >
                                                {availableTasks.map(task => (
                                                    <option key={task.id} value={task.id}>{task.title}</option>
                                                ))}
                                            </select>
                                        ) : (
                                            <div className="alert alert-warning small py-2">
                                                <i className="bi bi-exclamation-triangle me-2"></i>
                                                {t("No tasks found. Click on '+ Task' to create one.")}
                                            </div>
                                        )
                                    )}
                                    
                                    {newNoteParentType === "PROJECT" && !shell?.currentProject?.id && (
                                        <div className="alert alert-warning small py-2 mb-2">
                                            <i className="bi bi-exclamation-triangle me-2"></i>
                                            {t("notes.no_project_selected_select_first")}
                                        </div>
                                    )}
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>{t("Cancel")}</button>
                                <button 
                                    className="btn btn-primary" 
                                    onClick={handleCreateNote}
                                    disabled={!newNoteTitle.trim() || !newNoteParentId}
                                >
                                    {t("Create Note")}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <style>{`
                .notes-page .wmde-markdown-var { --color-canvas-default: white; }
                [data-theme="dark"] .notes-page .wmde-markdown-var { --color-canvas-default: #1e1e1e; }
                .note-card:hover { background-color: #f8f9fa; }
                [data-theme="dark"] .note-card:hover { background-color: rgba(255,255,255,0.05); }
                .note-card.border-primary { background-color: rgba(13, 110, 253, 0.05); }
                .editor-content .w-md-editor { height: 100% !important; }
                .editor-content .w-md-editor-content { height: calc(100% - 29px) !important; }
                
                /* Dark mode overrides for sidebar */
                [data-theme="dark"] .notes-sidebar {
                    background-color: var(--jl-bg-secondary) !important;
                    border-color: var(--jl-border-color) !important;
                }
                [data-theme="dark"] .notes-sidebar .bg-white {
                    background-color: var(--jl-bg-secondary) !important;
                    color: var(--jl-text-primary);
                }
                [data-theme="dark"] .notes-sidebar .bg-light {
                    background-color: var(--jl-bg-secondary) !important;
                }
                [data-theme="dark"] .editor-toolbar {
                    background-color: var(--jl-bg-secondary) !important;
                    border-color: var(--jl-border-color) !important;
                }
                [data-theme="dark"] .editor-toolbar input {
                    background-color: transparent;
                    color: var(--jl-text-primary);
                }
            `}</style>
        </div>
    );
}
