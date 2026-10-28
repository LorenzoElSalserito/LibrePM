import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import { useModal } from '../hooks/useModal.js';
import { useDataTransfer } from '../hooks/useDataTransfer.js';
import { TransferStatus, TransferLabel } from '../components/DataTransferStatus.jsx';

/**
 * IntegrationsPage - Manage Import/Export and external connections like ICS.
 */
export default function IntegrationsPage({ shell }) {
    const { t } = useTranslation();
    const modal = useModal();
    const transfer = useDataTransfer();
    const currentProject = shell?.currentProject || null;
    const [icsToken, setIcsToken] = useState('');
    const [loadingToken, setLoadingToken] = useState(true);
    const [mappingProfiles, setMappingProfiles] = useState([]);

    const loadMappingProfiles = useCallback(async () => {
        try {
            const profiles = await librepm.importMappingProfilesList();
            setMappingProfiles(profiles || []);
        } catch (e) {
            console.error("Error loading mapping profiles", e);
        }
    }, []);

    useEffect(() => {
        shell?.setTitle?.(t("Integrations"));
        shell?.setHeaderActions?.(
            <div className="d-flex gap-2 align-items-center">
                <button
                    className="btn btn-sm btn-outline-secondary"
                    onClick={loadIcsToken}
                    title={t("Refresh")}
                >
                    <i className="bi bi-arrow-clockwise"></i>
                </button>
            </div>
        );
        loadIcsToken();
        loadMappingProfiles();

        return () => {
            shell?.setHeaderActions?.(null);
        };
    }, [shell, t, loadMappingProfiles]);

    const loadIcsToken = async () => {
        try {
            setLoadingToken(true);
            // This is a presumed endpoint, let's add it to librepm.js if not present
            const response = await librepm.getCalendarToken();
            setIcsToken(response.token);
        } catch (e) {
            console.error("Error loading ICS token", e);
            // Don't show error toast on initial load if it's just not found
        } finally {
            setLoadingToken(false);
        }
    };

    const handleRegenerateToken = async () => {
        const confirmed = await modal.confirm({ title: t("Are you sure you want to regenerate the token?") });
        if (!confirmed) return;
        try {
            const response = await librepm.regenerateCalendarToken();
            setIcsToken(response.token);
            toast.success(t("Token regenerated successfully"));
        } catch (e) {
            toast.error(t("Error regenerating token"));
        }
    };

    const icsUrl = `${librepm.getApiUrl()}/public/calendar/${icsToken}/feed.ics`;

    return (
        <div className="container-fluid p-4">
            <h4 className="mb-4">{t("Integrations & Data")}</h4>

            {/* ICS Calendar Feed */}
            <div className="card shadow-sm mb-4">
                <div className="card-header">
                    <h5 className="mb-0">{t("External Calendar (iCal)")}</h5>
                </div>
                <div className="card-body">
                    <p className="text-muted">{t("Use this link to sync your tasks with Google Calendar, Outlook or Apple Calendar.")}</p>
                    {loadingToken ? (
                        <div className="spinner-border spinner-border-sm"></div>
                    ) : (
                        <div className="input-group">
                            <input type="text" className="form-control" value={icsUrl} readOnly />
                            <button className="btn btn-outline-secondary" onClick={() => { navigator.clipboard.writeText(icsUrl); toast.success(t("Copied to clipboard")); }}>
                                <i className="bi bi-clipboard"></i>
                            </button>
                        </div>
                    )}
                </div>
                <div className="card-footer">
                    <button className="btn btn-sm btn-outline-secondary" onClick={handleRegenerateToken}>
                        {t("Regenerate Token")}
                    </button>
                </div>
            </div>

            {/* Import / Export */}
            <div className="card shadow-sm mb-4">
                <div className="card-header">
                    <h5 className="mb-0">{t("Import / Export Data")}</h5>
                </div>
                <div className="card-body">
                    <TransferStatus busy={transfer.busy} />
                    <div className="row g-4">
                        <div className="col-md-6">
                            <h6>{t("Workspace (JSON)")}</h6>
                            <p className="small text-muted">
                                {t("All projects of the current profile with tasks, sub-tasks, tags and checklists. Works with both the local and the shared database.")}
                            </p>
                            <div className="d-flex gap-2 flex-wrap">
                                <button type="button" className="btn btn-primary" onClick={transfer.exportJson}
                                        disabled={!!transfer.busy} data-testid="integrations-export-json">
                                    <TransferLabel active={transfer.busy === 'exportJson'} icon="bi-box-arrow-down">{t("Export JSON")}</TransferLabel>
                                </button>
                                <button type="button" className="btn btn-outline-primary" onClick={transfer.importJson}
                                        disabled={!!transfer.busy} data-testid="integrations-import-json">
                                    <TransferLabel active={transfer.busy === 'importJson'} icon="bi-box-arrow-in-up">{t("Import JSON")}</TransferLabel>
                                </button>
                            </div>
                            <p className="small text-muted mt-2 mb-0">
                                <i className="bi bi-info-circle me-1" aria-hidden="true"></i>
                                {t("Import adds the projects in the file as new projects: existing data is not changed.")}
                            </p>
                        </div>
                        <div className="col-md-6">
                            <h6>{t("Project tasks (CSV)")}</h6>
                            <p className="small text-muted">
                                {currentProject
                                    ? t("Tasks of the selected project: {{name}}", { name: currentProject.name })
                                    : t("Select a project to export or import its tasks as CSV.")}
                            </p>
                            <div className="d-flex gap-2 flex-wrap">
                                <button type="button" className="btn btn-secondary" onClick={() => transfer.exportCsv(currentProject.id)}
                                        disabled={!!transfer.busy || !currentProject} data-testid="integrations-export-csv">
                                    <TransferLabel active={transfer.busy === 'exportCsv'} icon="bi-file-earmark-spreadsheet">{t("Export CSV (Project)")}</TransferLabel>
                                </button>
                                <button type="button" className="btn btn-outline-secondary" onClick={() => transfer.importCsv(currentProject.id)}
                                        disabled={!!transfer.busy || !currentProject} data-testid="integrations-import-csv">
                                    <TransferLabel active={transfer.busy === 'importCsv'} icon="bi-file-earmark-arrow-up">{t("Import CSV (Project)")}</TransferLabel>
                                </button>
                            </div>
                            <p className="small text-muted mt-2 mb-0">
                                <i className="bi bi-info-circle me-1" aria-hidden="true"></i>
                                {t("CSV columns: title (required), description, status, priority, deadline (YYYY-MM-DD), estimatedEffort.")}
                            </p>
                        </div>
                    </div>
                    <p className="small text-muted mt-3 mb-0">
                        <i className="bi bi-database me-1" aria-hidden="true"></i>
                        {t("Full database backups (.db and encrypted) are in Settings, Backup and Synchronization.")}
                    </p>
                </div>
            </div>

            {/* Mapping Profiles */}
            <div className="card shadow-sm">
                <div className="card-header d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">{t("Import Mapping Profiles")}</h5>
                </div>
                <div className="card-body">
                    <p className="small text-muted">{t("Saved column mapping profiles for CSV imports.")}</p>
                    {mappingProfiles.length === 0 ? (
                        <p className="text-muted fst-italic">{t("No mapping profiles saved yet.")}</p>
                    ) : (
                        <table className="table table-sm table-hover">
                            <thead>
                                <tr>
                                    <th>{t("Name")}</th>
                                    <th>{t("Entity Type")}</th>
                                    <th>{t("Created")}</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {mappingProfiles.map(p => (
                                    <tr key={p.id}>
                                        <td>{p.name}</td>
                                        <td><span className="badge bg-secondary">{p.entityType}</span></td>
                                        <td className="small text-muted">{p.createdAt ? new Date(p.createdAt).toLocaleDateString() : '-'}</td>
                                        <td>
                                            <button className="btn btn-sm btn-outline-danger" onClick={async () => {
                                                const confirmed = await modal.confirm({ title: t("Delete this mapping profile?") });
                                                if (!confirmed) return;
                                                try {
                                                    await librepm.importMappingProfilesDelete(p.id);
                                                    toast.success(t("Profile deleted"));
                                                    loadMappingProfiles();
                                                } catch (e) {
                                                    toast.error(t("Error deleting profile"));
                                                }
                                            }}>
                                                <i className="bi bi-trash"></i>
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
}
