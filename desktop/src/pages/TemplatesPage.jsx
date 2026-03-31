import React, { useState, useEffect, useCallback } from "react";
import { librepm } from "@api/librepm.js";
import { toast } from "react-toastify";
import PortalModal from "../components/PortalModal.jsx";
import { useTranslation } from 'react-i18next';

/**
 * TemplatesPage - Gallery of Project Templates
 * 
 * Allows users to browse templates and create new projects based on them.
 */
export default function TemplatesPage({ shell }) {
    const { t } = useTranslation();
    const [templates, setTemplates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedTemplate, setSelectedTemplate] = useState(null);
    const [showPreviewModal, setShowPreviewModal] = useState(false);
    const [filterCategory, setFilterCategory] = useState("ALL");
    const [filterComplexity, setFilterComplexity] = useState("ALL");
    
    // Instantiation State
    const [projectName, setProjectName] = useState("");
    const [projectDescription, setProjectDescription] = useState("");
    const [isCreating, setIsCreating] = useState(false);

    useEffect(() => {
        shell?.setTitle?.(t("Template Gallery"));
        shell?.setHeaderActions?.(
            <div className="d-flex gap-2 align-items-center">
                <button
                    className="btn btn-sm btn-outline-secondary"
                    onClick={loadTemplates}
                    title={t("Refresh")}
                >
                    <i className="bi bi-arrow-clockwise"></i>
                </button>
            </div>
        );
        loadTemplates();

        return () => {
            shell?.setHeaderActions?.(null);
        };
    }, [shell, t]);

    const loadTemplates = useCallback(async () => {
        try {
            setLoading(true);
            const list = await librepm.templatesList();
            setTemplates(list || []);
        } catch (e) {
            toast.error(t("Error loading templates"));
            console.error(e);
        } finally {
            setLoading(false);
        }
    }, [t]);

    const handleSelectTemplate = (template) => {
        setSelectedTemplate(template);
        setProjectName(`${t("New Project")} - ${template.name}`);
        setProjectDescription(template.description || "");
        setShowPreviewModal(true);
    };

    const handleInstantiate = async () => {
        if (!selectedTemplate || !projectName.trim()) return;

        setIsCreating(true);
        try {
            // Instantiate the project
            // Assuming the endpoint returns the created project
            const newProject = await librepm.templatesInstantiate(selectedTemplate.id, {
                projectName: projectName,
                description: projectDescription
            });

            toast.success(t("Project created successfully"));
            setShowPreviewModal(false);
            
            // Refresh shell projects list and navigate to the new project
            await shell.refreshProjects();
            shell.setCurrentProject(newProject);
            shell.navigate('dashboard'); // Or 'charter' or 'gantt' depending on template type? Let's go to Dashboard.

        } catch (e) {
            toast.error(t("Error creating project") + ": " + e.message);
        } finally {
            setIsCreating(false);
        }
    };

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center h-100">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">{t("Loading...")}</span>
                </div>
            </div>
        );
    }

    // Compute unique categories from templates
    const categories = [...new Set(templates.map(t => t.category).filter(Boolean))];

    // Filter templates
    const filteredTemplates = templates.filter(tpl => {
        if (filterCategory !== "ALL" && tpl.category !== filterCategory) return false;
        if (filterComplexity !== "ALL" && tpl.complexityLevel !== filterComplexity) return false;
        return true;
    });

    const complexityColor = (level) => {
        switch (level) {
            case 'SIMPLE': return 'success';
            case 'MODERATE': return 'warning';
            case 'ADVANCED': return 'danger';
            default: return 'secondary';
        }
    };

    const categoryIcon = (cat) => {
        const icons = {
            'PLANNING': 'bi-calendar3',
            'PROJECT_MANAGEMENT': 'bi-kanban',
            'NONPROFIT': 'bi-heart',
            'GRANTS': 'bi-file-earmark-text',
            'STAKEHOLDER': 'bi-people',
            'R_AND_D': 'bi-lightbulb',
            'CONSULTING': 'bi-briefcase',
            'OPERATIONS': 'bi-gear-wide-connected',
            'PORTFOLIO': 'bi-grid-3x3-gap',
        };
        return icons[cat] || 'bi-layout-text-window-reverse';
    };

    return (
        <div className="container-fluid p-4">
            <div className="row mb-4">
                <div className="col">
                    <h4>{t("Project Templates")}</h4>
                    <p className="text-muted">{t("Start your project with a pre-configured structure.")}</p>
                </div>
            </div>

            {/* Filters */}
            <div className="row mb-4">
                <div className="col-auto">
                    <select className="form-select form-select-sm" value={filterCategory} onChange={e => setFilterCategory(e.target.value)}>
                        <option value="ALL">{t("All Categories")}</option>
                        {categories.map(cat => (
                            <option key={cat} value={cat}>{cat.replace(/_/g, ' ')}</option>
                        ))}
                    </select>
                </div>
                <div className="col-auto">
                    <select className="form-select form-select-sm" value={filterComplexity} onChange={e => setFilterComplexity(e.target.value)}>
                        <option value="ALL">{t("All Complexity")}</option>
                        <option value="SIMPLE">{t("Simple")}</option>
                        <option value="MODERATE">{t("Moderate")}</option>
                        <option value="ADVANCED">{t("Advanced")}</option>
                    </select>
                </div>
                <div className="col-auto text-muted small d-flex align-items-center">
                    {filteredTemplates.length} {t("templates")}
                </div>
            </div>

            <div className="row g-4">
                {filteredTemplates.map(tpl => {
                    let capModules = [];
                    try {
                        const cap = typeof tpl.capabilityProfile === 'string' ? JSON.parse(tpl.capabilityProfile) : (tpl.capabilityProfile || {});
                        capModules = Object.entries(cap).filter(([, v]) => v === true).map(([k]) => k);
                    } catch { /* ignore */ }

                    return (
                        <div key={tpl.id} className="col-md-6 col-lg-4 col-xl-3">
                            <div className="card h-100 shadow-sm template-card cursor-pointer" onClick={() => handleSelectTemplate(tpl)}>
                                <div className="card-body d-flex flex-column">
                                    <div className="d-flex justify-content-between align-items-start mb-3">
                                        <div className="rounded p-2 bg-primary bg-opacity-10 text-primary">
                                            <i className={`bi ${categoryIcon(tpl.category)} fs-4`}></i>
                                        </div>
                                        <div className="d-flex gap-1">
                                            {tpl.complexityLevel && (
                                                <span className={`badge bg-${complexityColor(tpl.complexityLevel)}-subtle text-${complexityColor(tpl.complexityLevel)}`} style={{ fontSize: '0.65rem' }}>
                                                    {tpl.complexityLevel}
                                                </span>
                                            )}
                                            {tpl.templateScope === 'SYSTEM' && <span className="badge bg-secondary" style={{ fontSize: '0.65rem' }}>{t("System")}</span>}
                                        </div>
                                    </div>
                                    <h6 className="card-title mb-1">{tpl.name}</h6>
                                    <p className="card-text text-muted small flex-grow-1" style={{ display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                                        {tpl.description}
                                    </p>
                                    {capModules.length > 0 && (
                                        <div className="d-flex flex-wrap gap-1 mt-2">
                                            {capModules.map(mod => (
                                                <span key={mod} className="badge bg-primary-subtle text-primary" style={{ fontSize: '0.6rem' }}>{mod}</span>
                                            ))}
                                        </div>
                                    )}
                                    {tpl.version && (
                                        <div className="text-muted mt-2" style={{ fontSize: '0.7rem' }}>v{tpl.version}</div>
                                    )}
                                </div>
                                <div className="card-footer bg-transparent border-top-0 pb-3 pt-0">
                                    <button className="btn btn-outline-primary w-100 btn-sm">
                                        {t("Preview & Use")}
                                    </button>
                                </div>
                            </div>
                        </div>
                    );
                })}

                {filteredTemplates.length === 0 && (
                    <div className="col-12 text-center py-5 text-muted">
                        <i className="bi bi-inbox fs-1 mb-3 d-block"></i>
                        <p>{t("No templates available.")}</p>
                    </div>
                )}
            </div>

            {/* Template Preview & Use Modal */}
            {showPreviewModal && selectedTemplate && (
                <PortalModal onClick={() => setShowPreviewModal(false)} className="modal-lg">
                    <div className="modal-header">
                        <h5 className="modal-title">{t("Use Template")}: {selectedTemplate.name}</h5>
                        <button type="button" className="btn-close" onClick={() => setShowPreviewModal(false)}></button>
                    </div>
                    <div className="modal-body">
                        <div className="row">
                            <div className="col-md-6 border-end">
                                <h6 className="mb-3">{t("Template Details")}</h6>
                                <p className="text-muted small">{selectedTemplate.description}</p>

                                {selectedTemplate.useCases && (
                                    <div className="mb-3">
                                        <strong className="small">{t("Use Cases")}:</strong>
                                        <p className="text-muted small mb-0">{selectedTemplate.useCases}</p>
                                    </div>
                                )}

                                {selectedTemplate.prerequisites && (
                                    <div className="mb-3">
                                        <strong className="small">{t("Prerequisites")}:</strong>
                                        <p className="text-muted small mb-0">{selectedTemplate.prerequisites}</p>
                                    </div>
                                )}

                                <div className="mb-3">
                                    <strong className="small">{t("What will be created")}:</strong>
                                    <ul className="small text-muted ps-3 mt-1">
                                        {(() => {
                                            try {
                                                const s = typeof selectedTemplate.structureJson === 'string'
                                                    ? JSON.parse(selectedTemplate.structureJson) : (selectedTemplate.structureJson || {});
                                                return (
                                                    <>
                                                        {s.phases && <li>{s.phases.length} {t("phases")}</li>}
                                                        {s.tasks && <li>{s.tasks.length} {t("tasks")}</li>}
                                                        {s.statuses && <li>{s.statuses.length} {t("statuses")}</li>}
                                                        {s.deliverables && <li>{s.deliverables.length} {t("deliverables")}</li>}
                                                        {s.dependencies && <li>{s.dependencies.length} {t("dependencies")}</li>}
                                                        {s.metrics && <li>{s.metrics.length} {t("metrics")}</li>}
                                                    </>
                                                );
                                            } catch {
                                                return <li>{t("Standard Tasks & Phases")}</li>;
                                            }
                                        })()}
                                    </ul>
                                </div>

                                <div className="d-flex gap-2 flex-wrap">
                                    {selectedTemplate.complexityLevel && (
                                        <span className={`badge bg-${complexityColor(selectedTemplate.complexityLevel)}-subtle text-${complexityColor(selectedTemplate.complexityLevel)}`}>
                                            {selectedTemplate.complexityLevel}
                                        </span>
                                    )}
                                    <span className="badge bg-secondary-subtle text-secondary">v{selectedTemplate.version}</span>
                                </div>
                            </div>
                            <div className="col-md-6 ps-4">
                                <h6 className="mb-3">{t("Project Configuration")}</h6>
                                <div className="mb-3">
                                    <label className="form-label">{t("Project Name")} <span className="text-danger">*</span></label>
                                    <input 
                                        type="text" 
                                        className="form-control" 
                                        value={projectName} 
                                        onChange={e => setProjectName(e.target.value)} 
                                        autoFocus
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">{t("Description")}</label>
                                    <textarea 
                                        className="form-control" 
                                        rows="3" 
                                        value={projectDescription} 
                                        onChange={e => setProjectDescription(e.target.value)} 
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button className="btn btn-secondary" onClick={() => setShowPreviewModal(false)}>{t("Cancel")}</button>
                        <button 
                            className="btn btn-primary" 
                            onClick={handleInstantiate} 
                            disabled={isCreating || !projectName.trim()}
                        >
                            {isCreating ? (
                                <>
                                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                    {t("Creating...")}
                                </>
                            ) : (
                                <>
                                    <i className="bi bi-rocket-takeoff me-2"></i>
                                    {t("Create Project")}
                                </>
                            )}
                        </button>
                    </div>
                </PortalModal>
            )}
        </div>
    );
}
