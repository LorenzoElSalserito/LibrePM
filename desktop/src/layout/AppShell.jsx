import { useEffect, useMemo, useRef, useState, useCallback, lazy, Suspense } from "react";
import { librepm } from "@api/librepm.js";
import { toast } from "react-toastify";
import { useTranslation } from 'react-i18next';
import { useModal } from "../hooks/useModal.js";

// Layout components
import LeftNav from "./LeftNav.jsx";
import TopHeader from "./TopHeader.jsx";
import ProfileMenu from "./ProfileMenu.jsx";
import CommandPalette from "../components/CommandPalette.jsx";
import NotificationBell from "../components/NotificationBell.jsx";
import PortalModal from "../components/PortalModal.jsx";

// Pages
import PageErrorBoundary from "../components/PageErrorBoundary.jsx";
const MessagesPage = lazy(() => import("../pages/MessagesPage.jsx"));
const MenuPage = lazy(() => import("../pages/MenuPage.jsx"));
const CalendarPage = lazy(() => import("../pages/CalendarPage.jsx"));
const PlannerPage = lazy(() => import("../pages/PlannerPage.jsx"));
const KanbanPage = lazy(() => import("../pages/KanbanPage.jsx"));
const NotesPage = lazy(() => import("../pages/NotesPage.jsx"));
const SettingsPage = lazy(() => import("../pages/SettingsPage.jsx"));
const Dashboard = lazy(() => import("../pages/Dashboard.jsx"));
const AnalyticsPage = lazy(() => import("../pages/AnalyticsPage.jsx"));
const TeamPage = lazy(() => import("../pages/TeamPage.jsx"));
const ResourcePage = lazy(() => import("../pages/ResourcePage.jsx"));
const ConnectionsPage = lazy(() => import("../pages/ConnectionsPage.jsx"));
const GanttPage = lazy(() => import("../pages/GanttPage.jsx"));
const ChartPage = lazy(() => import("../pages/ChartPage.jsx"));
const TemplatesPage = lazy(() => import("../pages/TemplatesPage.jsx"));
const IntegrationsPage = lazy(() => import("../pages/IntegrationsPage.jsx"));
const GuidesPage = lazy(() => import("../pages/GuidesPage.jsx"));
const InboxPage = lazy(() => import("../pages/InboxPage.jsx"));
const TimeTrackingPage = lazy(() => import("../pages/TimeTrackingPage.jsx"));
const ConflictReviewPage = lazy(() => import("../pages/ConflictReviewPage.jsx"));
const FinancePage = lazy(() => import("../pages/FinancePage.jsx"));
const GrantsPage = lazy(() => import("../pages/GrantsPage.jsx"));
const StakeholderPage = lazy(() => import("../pages/StakeholderPage.jsx"));
const ChangeControlPage = lazy(() => import("../pages/ChangeControlPage.jsx"));
const PortfolioPage = lazy(() => import("../pages/PortfolioPage.jsx"));

const DONATION_URL = "https://www.paypal.com/paypalme/lorenzodemarco92";

function AboutLibrePMModal({ onClose, t }) {
    return (
        <PortalModal className="modal-dialog-centered modal-md" onClick={onClose}>
            <div className="modal-header border-0 pb-0">
                <div>
                    <h5 className="modal-title d-flex align-items-center gap-2 mb-1">
                        <span className="d-inline-flex align-items-center justify-content-center rounded-2 bg-primary-subtle text-primary" style={{ width: 36, height: 36 }}>
                            <i className="bi bi-info-circle"></i>
                        </span>
                        LibrePM
                    </h5>
                    <div className="text-muted small">{t("Planner & Task Manager")}</div>
                </div>
                <button type="button" className="btn-close" aria-label={t("Close")} onClick={onClose}></button>
            </div>
            <div className="modal-body pt-3">
                <div className="border rounded-2 p-3 mb-3">
                    <div className="d-flex justify-content-between gap-3 mb-2">
                        <span className="text-muted">{t("Version")}</span>
                        <span className="fw-semibold">LibrePM v{__APP_VERSION__}</span>
                    </div>
                    <div className="d-flex justify-content-between gap-3 mb-2">
                        <span className="text-muted">{t("Website")}</span>
                        <a href="https://www.lorenzodm.it" target="_blank" rel="noopener noreferrer">
                            https://www.lorenzodm.it
                        </a>
                    </div>
                    <div className="d-flex justify-content-between gap-3 mb-2">
                        <span className="text-muted">{t("Copyright")}</span>
                        <span className="text-end">© Lorenzo DM 2026 - All Rights Reserved</span>
                    </div>
                    <div className="d-flex justify-content-between gap-3">
                        <span className="text-muted">{t("License")}</span>
                        <span className="text-end">Distributed under the LICENSE AGPLv3</span>
                    </div>
                </div>
                <a
                    className="btn btn-primary w-100"
                    href={DONATION_URL}
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    <i className="bi bi-heart me-2"></i>
                    {t("Donations")}
                </a>
            </div>
        </PortalModal>
    );
}

// Context
import { WorkflowProvider } from "../context/WorkflowContext.jsx";

// Assets

/**
 * AppShell - Main Layout Component for LibrePM.
 *
 * Provides the structural skeleton for the authenticated application:
 * - Sidebar Navigation (LeftNav)
 * - Top Header with dynamic title and actions
 * - Collapsible Right Panel for context details
 * - Main Content Area for pages
 * - Global features like Command Palette and Notifications
 *
 * It also manages global state like the current project, theme, and user session.
 *
 * @component
 * @param {Object} props
 * @param {Object} props.initialUser - The currently logged-in user.
 * @param {Function} props.onLogout - Callback function to handle logout.
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @updated 0.10.0 - Added Integrations Page
 */
export default function AppShell({ initialUser, onLogout }) {
    const { t } = useTranslation();
    const modal = useModal();

    // ========================================
    // State - Backend Data
    // ========================================

    const [currentUser] = useState(initialUser);
    const [currentProject, setCurrentProject] = useState(null);
    const [projects, setProjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [workspaceModules, setWorkspaceModules] = useState({});

    // ========================================
    // State - Pages and Navigation
    // ========================================

    const PAGES = useMemo(() => [
        { id: "dashboard", label: t("Dashboard"), icon: "bi-speedometer2", component: Dashboard },
        { id: "messages", label: t("Messages"), icon: "bi-chat-dots", component: MessagesPage },
        { id: "inbox", label: t("Inbox"), icon: "bi-inbox", component: InboxPage },
        { id: "charter", label: t("Charter"), icon: "bi-file-earmark-text", component: ChartPage },
        { id: "menu", label: t("Projects"), icon: "bi-folder2-open", component: MenuPage },
        { id: "templates", label: t("Gallery"), icon: "bi-grid-3x3-gap", component: TemplatesPage },
        { id: "team", label: t("Team"), icon: "bi-people", component: TeamPage },
        { id: "resources", label: t("Resources"), icon: "bi-bar-chart-steps", component: ResourcePage, module: "planning" },
        { id: "connections", label: t("Contacts"), icon: "bi-person-lines-fill", component: ConnectionsPage },
        { id: "calendar", label: t("Calendar"), icon: "bi-calendar3", component: CalendarPage },
        { id: "planner", label: t("Planner"), icon: "bi-card-checklist", component: PlannerPage },
        { id: "gantt", label: t("Gantt"), icon: "bi-bar-chart-steps", component: GanttPage, module: "planning" },
        { id: "kanban", label: t("Kanban"), icon: "bi-kanban", component: KanbanPage },
        { id: "notes", label: t("Notes"), icon: "bi-journal-text", component: NotesPage },
        { id: "time-tracking", label: t("Time Tracking"), icon: "bi-clock", component: TimeTrackingPage },
        { id: "finance", label: t("Finance"), icon: "bi-calculator", component: FinancePage },
        { id: "grants", label: t("Grants"), icon: "bi-megaphone", component: GrantsPage },
        { id: "stakeholders", label: t("Stakeholders"), icon: "bi-diagram-3", component: StakeholderPage },
        { id: "change-control", label: t("Change Control"), icon: "bi-signpost-split", component: ChangeControlPage },
        { id: "portfolio", label: t("Portfolio"), icon: "bi-collection", component: PortfolioPage },
        { id: "guides", label: t("Guides"), icon: "bi-book", component: GuidesPage },
        { id: "analytics", label: t("Analytics"), icon: "bi-graph-up", component: AnalyticsPage },
        { id: "integrations", label: t("Integrations"), icon: "bi-plug", component: IntegrationsPage },
        { id: "sync-conflicts", label: t("Sync Conflicts"), icon: "bi-arrow-left-right", component: ConflictReviewPage },
        { id: "settings", label: t("Settings"), icon: "bi-gear", component: SettingsPage },
    ], [t]);

    const [activeId, setActiveId] = useState(() => {
        try { return localStorage.getItem('librepm_lastPage') || 'dashboard'; } catch { return 'dashboard'; }
    });
    const [navContext, setNavContext] = useState(null);
    const [navAction, setNavAction] = useState(null);

    // Filter pages based on workspace profile modules
    const filteredPages = useMemo(() => {
        if (loading) return []; // Return empty array while loading
        return PAGES;
    }, [PAGES, loading]);

    const activePage = useMemo(() => filteredPages.find((p) => p.id === activeId) ?? filteredPages[0], [filteredPages, activeId]);

    // ========================================
    // State - UI Components
    // ========================================

    const [title, setTitle] = useState(activePage?.label || "Loading...");
    const [headerActions, setHeaderActions] = useState(null);
    const [rightPanel, setRightPanel] = useState(null);
    const [rightPanelOpen, setRightPanelOpen] = useState(true);

    // ========================================
    // State - Theme
    // ========================================

    const [currentTheme, setCurrentTheme] = useState("dark");

    // ========================================
    // State - Profile Menu
    // ========================================

    const [profileItems, setProfileItems] = useState([]);

    // Update profile items when language changes
    useEffect(() => {
        setProfileItems([
            {
                id: "settings",
                label: t("Settings"),
                icon: "bi-gear",
                onClick: () => setActiveId("settings"),
            },
            {
                id: "about",
                label: t("Info LibrePM"),
                icon: "bi-info-circle",
                onClick: () => {
                    modal.showModal(AboutLibrePMModal, {
                        t,
                        onClose: modal.hideModal,
                    });
                },
            },
            { type: "sep" },
            {
                id: "openData",
                label: t("Open Data Folder"),
                icon: "bi-folder2-open",
                onClick: async () => {
                    const path = await librepm.getLocalDataPath();
                    if (path) {
                        toast.info(`${t("Data folder")}: ${path}`);
                    } else {
                        toast.warning(t("Data path not available"));
                    }
                },
            },
            {
                id: "export",
                label: t("Export data (JSON)"),
                icon: "bi-box-arrow-up",
                onClick: async () => {
                    // Same workspace JSON export as Integrations; works on SQLite and MariaDB.
                    const pending = toast.loading(t("Export in progress..."));
                    try {
                        const result = await librepm.transferExportJson();
                        toast.dismiss(pending);
                        if (result?.canceled) return;
                        const s = result?.summary;
                        toast.success(`${t("JSON export completed.")}${s ? ` ${t("{{projects}} projects, {{tasks}} tasks.", { projects: s.projects ?? 0, tasks: s.tasks ?? 0 })}` : ""}${result?.filePath ? ` ${t("Saved to {{path}}", { path: result.filePath })}` : ""}`);
                    } catch (e) {
                        toast.dismiss(pending);
                        console.error('Export error:', e);
                        toast.error(t("Export error") + ': ' + e.message);
                    }
                },
            },
            { type: "sep" },
            {
                id: "switchProfile",
                label: t("Change Profile"),
                icon: "bi-person-badge",
                onClick: async () => {
                    if (onLogout) {
                        const confirmed = await modal.confirm({
                            title: t("Change Profile"),
                            message: t("Change profile? You will return to the selection screen.")
                        });
                        if (confirmed) {
                            onLogout();
                        }
                    }
                },
            },
            {
                id: "logout",
                label: t("Exit"),
                icon: "bi-box-arrow-right",
                danger: true,
                onClick: async () => {
                    if (onLogout) {
                        const confirmed = await modal.confirm({
                            title: t("Exit"),
                            message: t("Exit? Autologin will be disabled.")
                        });
                        if (confirmed) {
                            onLogout();
                        }
                    }
                },
            },
        ]);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [t, onLogout]);

    // ========================================
    // Effect - Lock session shortcut (Ctrl+L)
    // ========================================

    useEffect(() => {
        const handleKeyDown = (e) => {
            // Ctrl+L — Lock session
            if ((e.ctrlKey || e.metaKey) && e.key === "l") {
                e.preventDefault();
                if (onLogout) onLogout();
            }
            // Ctrl+N — New task (navigate to planner)
            if ((e.ctrlKey || e.metaKey) && !e.shiftKey && e.key === "n") {
                e.preventDefault();
                setActiveId("planner");
                setNavAction("newTask");
            }
            // Ctrl+Shift+N — New note
            if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === "N") {
                e.preventDefault();
                setActiveId("notes");
                setNavAction("newNote");
            }
            // Esc — Close modals/panels
            if (e.key === "Escape") {
                if (rightPanelOpen) {
                    setRightPanelOpen(false);
                }
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [onLogout, rightPanelOpen]);

    // ========================================
    // Effect - Load theme from settings
    // ========================================

    useEffect(() => {
        async function loadTheme() {
            try {
                const settings = await librepm.settingsGet();
                if (settings?.theme) {
                    setCurrentTheme(settings.theme);
                }
            } catch (e) {
                console.warn("[AppShell] Impossibile caricare tema, uso default:", e.message);
                setCurrentTheme("dark");
            }
        }

        if (currentUser) {
            loadTheme();
        }
    }, [currentUser]);

    // ========================================
    // Effect - Apply theme to DOM
    // ========================================

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', currentTheme);
        document.body.setAttribute('data-theme', currentTheme);

        if (currentTheme === 'dark') {
            document.documentElement.setAttribute('data-bs-theme', 'dark');
        } else {
            document.documentElement.removeAttribute('data-bs-theme');
        }
    }, [currentTheme]);

    // ========================================
    // Initialization - Load projects
    // ========================================

    useEffect(() => {
        async function loadData() {
            try {
                setLoading(true);
                setError(null);

                const projectsList = await librepm.projectsList();
                setProjects(projectsList);

                if (projectsList.length > 0) {
                    setCurrentProject(projectsList[0]);
                    librepm.setCurrentProject(projectsList[0].id);
                }

                // Load workspace profile modules
                try {
                    const profileId = currentUser.workspaceProfileId || 'wp-personal';
                    const profile = await librepm.workspaceProfileGet(profileId);
                    if (profile?.modulesJson) {
                        const modules = typeof profile.modulesJson === 'string'
                            ? JSON.parse(profile.modulesJson)
                            : profile.modulesJson;
                        setWorkspaceModules(modules);
                    }
                } catch (wpErr) {
                    console.warn('[AppShell] Could not load workspace profile, all modules enabled:', wpErr.message);
                    setWorkspaceModules({});
                }

                setLoading(false);
            } catch (e) {
                console.error('[AppShell] Errore caricamento dati:', e);
                setError(e.message);
                setLoading(false);
            }
        }

        if (currentUser) {
            loadData();
        }
    }, [currentUser]);

    // ========================================
    // Shell API exposed to pages (stable refs)
    // ========================================

    // Keep stable refs for functions to avoid shell object recreation
    const setTitleRef = useRef(setTitle);
    const setHeaderActionsRef = useRef(setHeaderActions);
    const setRightPanelRef = useRef(setRightPanel);
    const setRightPanelOpenRef = useRef(setRightPanelOpen);
    const setProfileItemsRef = useRef(setProfileItems);

    // Update refs on each render (cheap, no re-render triggered)
    setTitleRef.current = setTitle;
    setHeaderActionsRef.current = setHeaderActions;
    setRightPanelRef.current = setRightPanel;
    setRightPanelOpenRef.current = setRightPanelOpen;
    setProfileItemsRef.current = setProfileItems;

    const navigateFn = useCallback((pageId, context = null, action = null) => {
        setActiveId(pageId);
        setNavContext(context);
        setNavAction(action);
    }, []);

    const clearNavActionFn = useCallback(() => setNavAction(null), []);

    const setCurrentProjectFn = useCallback((project) => {
        setCurrentProject(project);
        librepm.setCurrentProject(project?.id);
    }, []);

    const refreshProjectsFn = useCallback(async () => {
        const projectsList = await librepm.projectsList();
        setProjects(projectsList);
        return projectsList;
    }, []);

    const createProjectFn = useCallback(async (name, description) => {
        const newProject = await librepm.projectsCreate(name, description);
        const projectsList = await librepm.projectsList();
        setProjects(projectsList);
        setCurrentProject(newProject);
        librepm.setCurrentProject(newProject.id);
        return newProject;
    }, []);

    const deleteProjectFn = useCallback(async (projectId) => {
        await librepm.projectsDelete(projectId);
        const remaining = await librepm.projectsList();
        setProjects(remaining);
        setCurrentProject(prev => {
            if (prev?.id === projectId) {
                const newCurrent = remaining.length > 0 ? remaining[0] : null;
                librepm.setCurrentProject(newCurrent?.id);
                return newCurrent;
            }
            return prev;
        });
    }, []);

    const shell = useMemo(() => ({
        // UI Controls (stable via ref wrappers)
        setTitle: (...args) => setTitleRef.current(...args),
        setHeaderActions: (...args) => setHeaderActionsRef.current(...args),
        setRightPanel: (...args) => setRightPanelRef.current(...args),
        setRightPanelOpen: (...args) => setRightPanelOpenRef.current(...args),
        setProfileMenuItems: (...args) => setProfileItemsRef.current(...args),
        navigate: navigateFn,
        clearNavAction: clearNavActionFn,

        // Reactive data (these change, triggering shell recreation only when needed)
        navContext,
        navAction,
        currentTheme,
        setTheme: setCurrentTheme,
        currentUser,
        currentProject,
        projects,

        // Workspace profile
        workspaceModules,
        setWorkspaceModules,

        // Stable action callbacks
        setCurrentProject: setCurrentProjectFn,
        refreshProjects: refreshProjectsFn,
        createProject: createProjectFn,
        deleteProject: deleteProjectFn,
        logout: onLogout,
    }), [currentUser, currentProject, projects, currentTheme, onLogout, navContext, navAction, workspaceModules,
         navigateFn, clearNavActionFn, setCurrentProjectFn, refreshProjectsFn, createProjectFn, deleteProjectFn]);

    // ========================================
    // Effect - Reset slots on page change ONLY
    // ========================================

    useEffect(() => {
        if (activePage) {
            setTitle(activePage.label);
        }
        setRightPanel(null);
        try { localStorage.setItem('librepm_lastPage', activeId); } catch { /* ignore */ }
    }, [activeId, activePage]);

    // ========================================
    // Render - Loading State
    // ========================================

    if (loading || !activePage) {
        return (
            <div className={`jl-root ${currentTheme === 'dark' ? 'dark-theme' : ''}`}>
                <div className="jl-loading d-flex flex-column align-items-center justify-content-center vh-100">
                    <div className="spinner-border text-primary mb-3" role="status">
                        <span className="visually-hidden">{t("Loading...")}</span>
                    </div>
                    <h5 className="text-muted">{t("Loading projects...")}</h5>
                    <p className="small text-muted">
                        {currentUser?.displayName || currentUser?.username || t("User")}
                    </p>
                </div>
            </div>
        );
    }

    // ========================================
    // Render - Error State
    // ========================================

    if (error) {
        return (
            <div className={`jl-root ${currentTheme === 'dark' ? 'dark-theme' : ''}`}>
                <div className="jl-error d-flex flex-column align-items-center justify-content-center vh-100">
                    <div className="alert alert-danger text-center" style={{ maxWidth: 500 }}>
                        <h5 className="alert-heading">
                            <i className="bi bi-exclamation-triangle me-2"></i>
                            {t("Loading Error")}
                        </h5>
                        <p className="mb-2">{error}</p>
                        <hr />
                        <p className="small mb-3">
                            {t("An error occurred while loading data.")}
                        </p>
                        <div className="d-flex gap-2 justify-content-center">
                            <button
                                className="btn btn-primary"
                                onClick={() => window.location.reload()}
                            >
                                <i className="bi bi-arrow-clockwise me-2"></i>
                                {t("Retry")}
                            </button>
                            {onLogout && (
                                <button
                                    className="btn btn-outline-secondary"
                                    onClick={onLogout}
                                >
                                    <i className="bi bi-person-badge me-2"></i>
                                    {t("Change Profile")}
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    // ========================================
    // Render - Main App
    // ========================================

    const PageComponent = activePage.component;

    const panelToggleBtn = (
        <button
            className="btn btn-sm btn-light"
            type="button"
            title={rightPanelOpen ? t("Hide right panel") : t("Show right panel")}
            onClick={() => setRightPanelOpen((v) => !v)}
        >
            {rightPanelOpen ? `${t("Panel")} ▸` : `${t("Panel")} ◂`}
        </button>
    );

    return (
        <WorkflowProvider>
        <div className={`jl-root ${currentTheme === 'dark' ? 'dark-theme' : ''}`}>
            <div className="jl-appframe">
                <div className="jl-shell">
                    {/* Command Palette */}
                    <CommandPalette shell={shell} />

                    {/* Sidebar Navigation */}
                    <LeftNav
                        items={filteredPages}
                        activeId={activeId}
                        onSelect={(id) => {
                            setActiveId(id);
                            setNavContext(null); // Reset context on manual nav
                            setNavAction(null); // Reset action on manual nav
                        }}
                    />

                    {/* Main Content Area */}
                    <main className="jl-main">
                        {/* Top Header */}
                        <TopHeader
                            title={title}
                            actions={
                                <>
                                    {headerActions}
                                    <NotificationBell shell={shell} />
                                    {panelToggleBtn}
                                    <ProfileMenu
                                        initials={currentUser?.displayName?.substring(0, 2)?.toUpperCase() || "JL"}
                                        title={currentUser?.displayName || t("User")}
                                        subtitle={`@${currentUser?.username || "librepm"}`}
                                        items={profileItems}
                                    />
                                </>
                            }
                        />

                        {/* Content Section */}
                        <section className="jl-content">
                            {/* Page Content */}
                            <div className="jl-page" id="main-content">
                                <PageErrorBoundary key={activePage.id}>
                                    <Suspense fallback={<div className="p-5 text-center" role="status"><span className="spinner-border text-primary" /><span className="visually-hidden">Caricamento pagina</span></div>}>
                                        <PageComponent shell={shell} />
                                    </Suspense>
                                </PageErrorBoundary>
                            </div>

                            {/* Right Panel */}
                            <aside className={"jl-rightpanel " + (rightPanelOpen ? "" : "closed")}>
                                {rightPanel ?? (
                                    <div className="d-flex flex-column gap-2">
                                        <div className="d-flex align-items-start justify-content-between gap-2">
                                            <div>
                                                <div className="fw-bold">LibrePM v{__APP_VERSION__}</div>
                                                <div className="jl-muted small">
                                                    {t("Info panel. Pages can customize this space.")}
                                                </div>
                                            </div>
                                            <span className="badge bg-primary-subtle text-primary border border-primary-subtle">
                                                v{__APP_VERSION__}
                                            </span>
                                        </div>
                                        {currentProject && (
                                            <div className="p-2 border rounded-3">
                                                <div className="small fw-bold">{t("Active Project")}</div>
                                                <div className="text-primary">{currentProject.name}</div>
                                            </div>
                                        )}

                                        {currentUser && (
                                            <div className="p-2 border rounded-3">
                                                <div className="small fw-bold">{t("User")}</div>
                                                <div className="jl-muted small">
                                                    {currentUser.displayName || currentUser.username}
                                                </div>
                                                <div className="text-muted small" style={{ fontSize: '0.75rem' }}>
                                                    @{currentUser.username}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                )}
                            </aside>
                        </section>
                    </main>
                </div>
            </div>
        </div>
        </WorkflowProvider>
    );
}
