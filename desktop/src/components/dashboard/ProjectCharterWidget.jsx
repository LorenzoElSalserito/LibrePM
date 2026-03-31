import React, { useState, useEffect } from 'react';
import librepm from '@api/librepm.js';
import MDEditor from "@uiw/react-md-editor";
import { toast } from 'react-toastify';

/**
 * Charter Section Component (Markdown editor)
 */
const CharterSection = ({ title, content, onSave }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [localContent, setLocalContent] = useState(content || '');

    useEffect(() => setLocalContent(content || ''), [content]);

    return (
        <div className="card shadow-sm border mb-3">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <h6 className="mb-0 fw-bold">{title}</h6>
                <button
                    className="btn btn-sm btn-link text-primary text-decoration-none"
                    onClick={() => {
                        if (isEditing) onSave(localContent);
                        setIsEditing(!isEditing);
                    }}
                >
                    {isEditing ? 'Salva' : 'Modifica'}
                </button>
            </div>
            <div className="card-body">
                {isEditing ? (
                    <div data-color-mode="light">
                        <MDEditor
                            value={localContent}
                            onChange={setLocalContent}
                            preview="edit"
                            height={200}
                            visibleDragbar={false}
                        />
                    </div>
                ) : (
                    <div className="markdown-body" style={{ fontSize: '0.9rem' }}>
                        {content ? (
                            <MDEditor.Markdown source={content} style={{ whiteSpace: 'pre-wrap' }} />
                        ) : (
                            <span className="text-muted fst-italic">Nessun contenuto. Clicca Modifica per aggiungere.</span>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

/**
 * Widget for displaying and editing Project Charter details.
 */
const ProjectCharterWidget = ({ projectId, charter, onUpdate }) => {
    const handleSaveSection = (field, value) => {
        onUpdate({ [field]: value });
    };

    if (!charter) return null;

    return (
        <div className="project-charter-widget">
            <CharterSection
                title="Obiettivi / Problem Statement"
                content={charter.problemStatement}
                onSave={(val) => handleSaveSection('problemStatement', val)}
            />
            <CharterSection
                title="Business Case"
                content={charter.businessCase}
                onSave={(val) => handleSaveSection('businessCase', val)}
            />
        </div>
    );
};

export default ProjectCharterWidget;
