/**
 * LibrePM Electron Preload Script v0.2.2
 *
 * Exposes secure APIs to the renderer process for:
 * - Logging to the main process
 * - Backend configuration access
 * - Native Electron functions (dialogs, file system)
 * - Synchronized focus management (forceFocusSync, ensureWebContentFocusSync)
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @updated 0.2.2 - Added ensureWebContentFocusSync for key focus recovery on Linux
 */

const { contextBridge, ipcRenderer } = require("electron");
const fs = require("fs");
const path = require("path");

// ========================================
// Helpers
// ========================================

function safeSerialize(arg) {
    try {
        if (arg instanceof Error) {
            return { name: arg.name, message: arg.message, stack: arg.stack };
        }
        return arg;
    } catch (_) {
        return String(arg);
    }
}

function sendLog(level, ...args) {
    ipcRenderer.send(
        "renderer:log",
        level,
        ...args.map((a) => safeSerialize(a))
    );
}

/**
 * Reads the Java backend port from the configuration file
 */
async function getBackendPort() {
    try {
        // Ask the main process
        const port = await ipcRenderer.invoke("backend:getPort");
        if (port) return port;

        // Fallback: read the file directly (if available)
        const userData = await ipcRenderer.invoke("app:getPath", "userData");
        const configPaths = [
            path.join(userData, "config", "backend.port"),
            path.join(process.cwd(), "data", "config", "backend.port"),
            "./data/config/backend.port",
        ];

        for (const configPath of configPaths) {
            try {
                if (fs.existsSync(configPath)) {
                    const content = fs.readFileSync(configPath, "utf-8").trim();
                    const port = parseInt(content, 10);
                    if (!isNaN(port) && port > 0 && port < 65536) {
                        console.log("[Preload] Backend port from file:", port);
                        return port;
                    }
                }
            } catch (e) {
                // Ignore
            }
        }

        return null;
    } catch (e) {
        console.error("[Preload] Error reading backend port:", e);
        return null;
    }
}

// ========================================
// Context Bridge - Focus Management API
// ========================================

/**
 * LPMWindow - API for Electron window focus management
 *
 * forceFocus: Async request (fallback)
 * forceFocusSync: SYNCHRONOUS request (essential for text-boxes on Linux/Wayland)
 * ensureWebContentFocusSync: Forces webContents.focus() to recover key focus
 */
contextBridge.exposeInMainWorld('LPMWindow', {
    /**
     * Asynchronous focus request (fallback, less effective)
     */
    forceFocus: (reason) => {
        try {
            ipcRenderer.send('lpm:force-focus', { reason: reason || 'async' });
        } catch (e) {
            console.warn('[Preload] forceFocus error:', e);
        }
    },

    /**
     * SYNCHRONOUS focus request - CALL IT during pointerdown/keydown!
     * Essential on Linux/Wayland where window managers
     * only accept focus requests during real user gestures.
     */
    forceFocusSync: (reason) => {
        try {
            return ipcRenderer.sendSync('lpm:force-focus-sync', { reason: reason || 'sync' });
        } catch (e) {
            console.warn('[Preload] forceFocusSync fallback to async:', e);
            try {
                ipcRenderer.send('lpm:force-focus', { reason: (reason || 'sync') + '-fallback' });
            } catch (e2) {
                console.warn('[Preload] forceFocus fallback also failed:', e2);
            }
            return false;
        }
    },

    /**
     * NEW in v0.2.2: Forces webContents.focus() to recover key focus
     *
     * On Linux, it can happen that:
     * - win.isFocused() = true (the window has focus at window manager level)
     * - webContents does NOT have internal "key focus"
     * - Result: the keyboard does not work!
     *
     * This handler ALWAYS calls webContents.focus() to ensure
     * that the renderer can receive keyboard input.
     *
     * USE WITH CAUTION: calling it during normal typing
     * would steal focus from the text-box!
     */
    ensureWebContentFocusSync: (reason) => {
        try {
            return ipcRenderer.sendSync('lpm:ensure-webcontent-focus', { reason: reason || 'ensure' });
        } catch (e) {
            console.warn('[Preload] ensureWebContentFocusSync error:', e);
            // Fallback: try forceFocusSync
            try {
                return ipcRenderer.sendSync('lpm:force-focus-sync', { reason: (reason || 'ensure') + '-fallback' });
            } catch (e2) {
                console.warn('[Preload] ensureWebContentFocusSync fallback also failed:', e2);
            }
            return false;
        }
    },
});

// ========================================
// Context Bridge - Main API (librepm)
// ========================================

contextBridge.exposeInMainWorld("librepm", {
    // ========================================
    // Logging (Renderer -> Main terminal)
    // ========================================
    log: (...args) => sendLog("log", ...args),
    warn: (...args) => sendLog("warn", ...args),
    error: (...args) => sendLog("error", ...args),

    // ========================================
    // Backend Configuration
    // ========================================

    /**
     * Gets the Java backend port
     */
    getBackendPort: () => getBackendPort(),

    /**
     * Checks if the backend is available
     */
    checkBackendHealth: async () => {
        const port = await getBackendPort();
        if (!port) return { ok: false, error: "Port not found" };

        try {
            const response = await fetch(`http://localhost:${port}/api/health`, {
                signal: AbortSignal.timeout(3000),
            });
            return { ok: response.ok, port };
        } catch (e) {
            return { ok: false, error: e.message, port };
        }
    },

    // ========================================
    // Projects (IPC -> Backend via Main)
    // ========================================
    projectsList: () => ipcRenderer.invoke("projects:list"),
    projectsCreate: (name) => ipcRenderer.invoke("projects:create", name),
    projectsDelete: (projectId) => ipcRenderer.invoke("projects:delete", projectId),

    // ========================================
    // Tasks (IPC -> Backend via Main)
    // ========================================
    tasksList: (projectId) => ipcRenderer.invoke("tasks:list", projectId),
    tasksCreate: (payload) => ipcRenderer.invoke("tasks:create", payload),
    tasksUpdate: (taskId, patch) => ipcRenderer.invoke("tasks:update", taskId, patch),
    tasksDelete: (taskId) => ipcRenderer.invoke("tasks:delete", taskId),

    // ========================================
    // Focus Sessions
    // ========================================
    focusStart: (taskId) => ipcRenderer.invoke("focus:start", taskId),
    focusStop: () => ipcRenderer.invoke("focus:stop"),
    focusRunning: () => ipcRenderer.invoke("focus:running"),
    focusHistory: (taskId) => ipcRenderer.invoke("focus:history", taskId),
    focusHistoryAll: () => ipcRenderer.invoke("focus:historyAll"),

    // ========================================
    // Export/Import with Native Dialogs
    // ========================================

    /**
     * Export JSON with native dialog to choose destination
     */
    exportJsonDialog: () => ipcRenderer.invoke("data:exportJsonDialog"),

    /**
     * Export CSV with native dialog
     */
    exportCsvDialog: (projectId) => ipcRenderer.invoke("data:exportCsvDialog", projectId),

    /**
     * Import JSON with native dialog to choose file
     */
    importJsonDialog: () => ipcRenderer.invoke("data:importJsonDialog"),

    /**
     * Import database (.db) from the Java backend
     */
    importDbDialog: () => ipcRenderer.invoke("data:importDbDialog"),

    /**
     * Export database (.db) from the Java backend
     */
    exportDbDialog: () => ipcRenderer.invoke("data:exportDbDialog"),

    // ========================================
    // App Paths
    // ========================================

    /**
     * Gets the local data path
     */
    getLocalDataPath: () => ipcRenderer.invoke("data:getLocalDataPath"),

    /**
     * Gets the Electron app path
     */
    getAppPath: (name) => ipcRenderer.invoke("app:getPath", name),

    // ========================================
    // File Operations
    // ========================================

    /**
     * Opens file/folder in the system file manager
     */
    openPath: (filePath) => ipcRenderer.invoke("shell:openPath", filePath),

    /**
     * Shows file in the file manager (with selection)
     */
    showItemInFolder: (filePath) => ipcRenderer.invoke("shell:showItemInFolder", filePath),

    /**
     * Opens external URL in the browser
     */
    openExternal: (url) => ipcRenderer.invoke("shell:openExternal", url),

    /**
     * Opens the default mail client with a prefilled bug report
     */
    reportBug: (source = "general") => ipcRenderer.invoke("bug-report:open", source),

    // ========================================
    // System Info
    // ========================================

    /**
     * Platform information
     */
    platform: process.platform,

    /**
     * Electron version
     */
    versions: {
        electron: process.versions.electron,
        chrome: process.versions.chrome,
        node: process.versions.node,
    },

    // ========================================
    // Window Controls
    // ========================================

    /**
     * Minimize window
     */
    minimize: () => ipcRenderer.send("window:minimize"),

    /**
     * Maximize/Restore window
     */
    maximize: () => ipcRenderer.send("window:maximize"),

    /**
     * Close window
     */
    close: () => ipcRenderer.send("window:close"),

    /**
     * Force focus on the window (useful for modals)
     */
    focusWindow: () => ipcRenderer.invoke("librepm:focus-window"),

    // ========================================
    // Notifications
    // ========================================

    /**
     * Show system notification
     */
    showNotification: (title, body, options = {}) => {
        ipcRenderer.invoke("notification:show", { title, body, ...options });
    },

    /**
     * Request notification permission (if needed)
     */
    requestNotificationPermission: () => {
        return ipcRenderer.invoke("notification:requestPermission");
    },

    // ========================================
    // App Events (from Main to Renderer)
    // ========================================

    /**
     * Register callback for events
     */
    on: (channel, callback) => {
        const validChannels = [
            "app:focus",
            "app:blur",
            "backend:status",
            "reminder:due",
            "sync:status",
        ];

        if (validChannels.includes(channel)) {
            const subscription = (_event, ...args) => callback(...args);
            ipcRenderer.on(channel, subscription);

            // Return function to remove listener
            return () => ipcRenderer.removeListener(channel, subscription);
        }
    },

    /**
     * Remove all listeners for a channel
     */
    removeAllListeners: (channel) => {
        ipcRenderer.removeAllListeners(channel);
    },
});

// ========================================
// Initialization log
// ========================================

console.log("[Preload] LibrePM preload script loaded v0.2.2");
console.log("[Preload] Platform:", process.platform);
console.log("[Preload] Electron:", process.versions.electron);
console.log("[Preload] forceFocusSync available:", typeof ipcRenderer.sendSync === 'function');
console.log("[Preload] ensureWebContentFocusSync available:", typeof ipcRenderer.sendSync === 'function');
