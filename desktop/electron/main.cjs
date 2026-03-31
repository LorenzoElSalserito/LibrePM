/**
 * LibrePM Electron Main Process
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @updated 0.6.2 - Added Splash Screen & Custom Logo
 */

const { app, BrowserWindow, ipcMain, dialog, shell } = require("electron");
const path = require("path");
const fs = require("fs");
const http = require("http");
const os = require("os");
const { spawn } = require("child_process");

const isDev = process.env.NODE_ENV === "development" || !app.isPackaged;

// If GPU/ANGLE issues reappear on Linux, start with:
// LIBREPM_DISABLE_GPU=1 npm run dev
if (process.env.LIBREPM_DISABLE_GPU === "1") {
    app.disableHardwareAcceleration();
    app.commandLine.appendSwitch("disable-gpu");
}

const BACKEND_DEFAULT_PORT = 8080;

let mainWindow = null;
let splashWindow = null;
let backendPort = BACKEND_DEFAULT_PORT;
let backendProcess = null;

// =========================================================
// Focus Broker v2.1 (Conservative + webContents key focus)
// =========================================================
let _lastFocusReqAt = 0;
let _hardFocusCount = 0;

function hardFocus(win, reason = 'unknown', force = false) {
    if (!win || win.isDestroyed()) return false;

    const focusId = ++_hardFocusCount;

    try {
        if (!force && win.isFocused()) {
            console.log(`[Main] hardFocus #${focusId} SKIPPED (already focused) reason: ${reason}`);
            return true;
        }

        if (win.isMinimized()) {
            win.restore();
        }

        if (!win.isVisible()) {
            win.show();
        }

        if (process.platform === 'darwin') {
            try { app.focus({ steal: true }); } catch {}
        }

        win.setFocusable(true);
        try { win.moveTop(); } catch {}
        win.focus();

        console.log(`[Main] hardFocus #${focusId} EXECUTED reason: ${reason}`);

        setTimeout(() => {
            if (!win || win.isDestroyed()) return;
            if (win.isFocused()) return;

            console.log(`[Main] hardFocus #${focusId} FALLBACK (window still not focused)`);

            const wasAOT = win.isAlwaysOnTop();
            win.setAlwaysOnTop(true, 'screen-saver');
            win.show();
            try { win.moveTop(); } catch {}
            win.focus();

            setTimeout(() => {
                if (!win.isDestroyed()) win.setAlwaysOnTop(wasAOT);
            }, 120);
        }, 50);

        return true;
    } catch (e) {
        console.warn(`[Main] hardFocus #${focusId} FAILED:`, e);
        return false;
    }
}

function throttledHardFocus(win, reason, force = false) {
    const now = Date.now();
    if (now - _lastFocusReqAt < 300) {
        console.log(`[Main] hardFocus THROTTLED (too soon) reason: ${reason}`);
        return;
    }
    _lastFocusReqAt = now;
    hardFocus(win, reason, force);
}

// ----------------------------
// Utility Functions
// ----------------------------

function pad2(n) {
    return String(n).padStart(2, "0");
}

function nowStamp() {
    const d = new Date();
    return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}_${pad2(
        d.getHours()
    )}${pad2(d.getMinutes())}${pad2(d.getSeconds())}`;
}

// ----------------------------
// Backend Management (Spawn & Port Reading)
// ----------------------------

function getLibrePMHome() {
    return path.join(os.homedir(), ".librepm");
}

function getBackendPortFile() {
    return path.join(getLibrePMHome(), "config", "backend.port");
}

function tryParseBackendPort(content) {
    const s = (content ?? "").trim();
    if (!s) return null;

    if (s.startsWith("{")) {
        try {
            const obj = JSON.parse(s);
            const p = Number(obj?.port);
            if (Number.isInteger(p) && p > 0 && p < 65536) return p;
        } catch (_) {
            // ignore
        }
    }

    const p = Number(s);
    if (Number.isInteger(p) && p > 0 && p < 65536) return p;

    return null;
}

function waitForBackendPort(retries = 120, delay = 1000) {
    return new Promise((resolve) => {
        const portFile = getBackendPortFile();
        let attempts = 0;

        const check = () => {
            attempts++;
            try {
                if (fs.existsSync(portFile)) {
                    const content = fs.readFileSync(portFile, "utf-8");
                    const port = tryParseBackendPort(content);
                    if (port) {
                        console.log(`[Main] Backend port found: ${port} (attempt ${attempts})`);
                        resolve(port);
                        return;
                    }
                }
            } catch (e) {
                console.warn(`[Main] Error reading port file: ${e.message}`);
            }

            if (attempts >= retries) {
                console.error("[Main] Timeout waiting for backend port.");
                resolve(null);
            } else {
                setTimeout(check, delay);
            }
        };

        check();
    });
}

function getJavaExecutable() {
    // In dev mode, assume 'java' is in PATH
    if (isDev) return "java";

    // In production, look for bundled JRE
    const jrePath = path.join(process.resourcesPath, "jre");
    const javaBin = process.platform === "win32" ? "bin/java.exe" : "bin/java";
    const bundledJava = path.join(jrePath, javaBin);

    if (fs.existsSync(bundledJava)) {
        return bundledJava;
    }

    console.warn("[Main] Bundled JRE not found, falling back to system java");
    return "java";
}

function getBackendJar() {
    // In dev mode, we might not want to spawn the backend if we are running it separately
    // But if we wanted to, we'd look in ../build/libs
    if (isDev) return null; 

    // In production, look for bundled JAR
    // We configured electron-builder to put backend-libs in resources
    const libPath = path.join(process.resourcesPath, "backend-libs");
    
    if (!fs.existsSync(libPath)) {
        console.error("[Main] Backend libs directory not found:", libPath);
        return null;
    }

    const files = fs.readdirSync(libPath);
    const jarFile = files.find(f => f.endsWith(".jar") && !f.includes("plain")); // Avoid plain jars if any

    if (jarFile) {
        return path.join(libPath, jarFile);
    }
    
    console.error("[Main] No backend JAR found in:", libPath);
    return null;
}

function startBackend() {
    if (isDev) {
        console.log("[Main] Dev mode: Skipping backend spawn (assume running externally)");
        // Try to read port immediately, assuming it's already running
        return waitForBackendPort(60, 500).then(p => {
            backendPort = p || BACKEND_DEFAULT_PORT;
        });
    }

    const javaExec = getJavaExecutable();
    const jarPath = getBackendJar();

    if (!jarPath) {
        console.error("[Main] Cannot start backend: JAR not found.");
        return Promise.resolve();
    }

    console.log(`[Main] Spawning backend: ${javaExec} -jar ${jarPath}`);
    
    // Ensure data directory exists
    const dataPath = getLibrePMHome();
    if (!fs.existsSync(dataPath)) {
        fs.mkdirSync(dataPath, { recursive: true });
    }

    // Delete old port file to ensure we read the new one
    const portFile = getBackendPortFile();
    if (fs.existsSync(portFile)) {
        try { fs.unlinkSync(portFile); } catch(e) {}
    }

    backendProcess = spawn(javaExec, [
        `-Dlibrepm.data.path=${dataPath}`,
        "-jar", 
        jarPath
    ], {
        cwd: path.dirname(jarPath),
        detached: false,
        stdio: 'pipe' // Capture stdout/stderr
    });

    backendProcess.stdout.on('data', (data) => {
        console.log(`[Backend] ${data}`);
    });

    backendProcess.stderr.on('data', (data) => {
        console.error(`[Backend] ${data}`);
    });

    backendProcess.on('close', (code) => {
        console.log(`[Backend] Process exited with code ${code}`);
        backendProcess = null;
    });

    return waitForBackendPort().then(p => {
        if (p) backendPort = p;
        else console.warn("[Main] Failed to retrieve backend port after spawn.");
    });
}

function waitForBackendReady(port, retries = 480, delay = 20000) {
    return new Promise((resolve) => {
        let attempts = 0;

        const check = () => {
            attempts++;
            if (attempts > 99) {
                updateSplashStatus(`Preparazione Workspace in corso, potrebbe volerci del tempo... (${attempts}/${retries})`);
            } else{
                updateSplashStatus(`Caricamento dei dati preliminari in corso... (${attempts}/${retries})`);
            }

            const req = http.get(`http://localhost:${port}/api/health`, { timeout: 3000 }, (res) => {
                res.resume();
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    console.log(`[Main] Backend ready on port ${port} (attempt ${attempts})`);
                    resolve(true);
                } else {
                    retryOrFail();
                }
            });

            req.on("error", () => retryOrFail());
            req.on("timeout", () => { req.destroy(); retryOrFail(); });

            function retryOrFail() {
                if (attempts >= retries) {
                    console.warn(`[Main] Backend not ready after ${retries} attempts on port ${port}`);
                    resolve(false);
                } else {
                    setTimeout(check, delay);
                }
            }
        };

        check();
    });
}

function stopBackend() {
    if (backendProcess) {
        console.log("[Main] Stopping backend process...");
        backendProcess.kill();
        backendProcess = null;
    }
}

// ----------------------------
// Window
// ----------------------------

// ----------------------------
// Splash Screen Helpers
// ----------------------------

function updateSplashStatus(text) {
    if (!splashWindow || splashWindow.isDestroyed()) return;
    const escaped = text.replace(/\\/g, "\\\\").replace(/'/g, "\\'");
    splashWindow.webContents.executeJavaScript(
        `(function(){ var s = document.getElementById('status'); if(s){ s.textContent='${escaped}'; s.className='status'; } })()`
    ).catch(() => {});
}

function showSplashWarning(message) {
    if (!splashWindow || splashWindow.isDestroyed()) return;
    const escaped = message.replace(/\\/g, "\\\\").replace(/'/g, "\\'");
    splashWindow.webContents.executeJavaScript(`(function(){
        var s = document.getElementById('status');
        if(s){ s.textContent='${escaped}'; s.className='status warning'; }
        var l = document.getElementById('loader');
        if(l){ l.style.display='none'; }
        var b = document.getElementById('retryBtn');
        if(b){ b.style.display='inline-block'; }
    })()`).catch(() => {});
}

function createSplashWindow() {
    splashWindow = new BrowserWindow({
        width: 400,
        height: 340,
        frame: false,
        transparent: true,
        alwaysOnTop: true,
        resizable: false,
        icon: path.join(__dirname, "..", "src/assets", "icon.svg"),
        webPreferences: {
            preload: path.join(__dirname, "splash-preload.cjs"),
            nodeIntegration: false,
            contextIsolation: true
        }
    });

    splashWindow.loadFile(path.join(__dirname, "splash.html"));
    splashWindow.center();

    splashWindow.on('closed', () => {
        splashWindow = null;
    });
}

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1600,
        height: 1000,
        minWidth: 800,
        minHeight: 600,
        title: "LibrePM",
        icon: path.join(__dirname, "..", "src/assets", "Logo.svg"), // Use SVG icon
        show: false, // Hidden initially, shown when ready
        webPreferences: {
            preload: path.join(__dirname, "preload.cjs"),
            nodeIntegration: false,
            contextIsolation: true,
            sandbox: false
        }
    });

    // Debug focus
    mainWindow.webContents.on('focus', () => console.log('[Main] webContents FOCUS'));
    mainWindow.webContents.on('blur', () => console.log('[Main] webContents BLUR'));

    // Notify renderer + hook for focus recovery
    mainWindow.on('focus', () => {
        console.log('[Main] window FOCUS');
        try { mainWindow.webContents.send('app:focus'); } catch {}
    });
    mainWindow.on('blur', () => {
        console.log('[Main] window BLUR');
        try { mainWindow.webContents.send('app:blur'); } catch {}
    });

    mainWindow.webContents.on("did-fail-load", (_event, errorCode, errorDescription, validatedURL) => {
        console.error("[Main] did-fail-load", { errorCode, errorDescription, validatedURL });
    });

    if (isDev) {
        mainWindow.loadURL("http://127.0.0.1:5173/");
        if (mainWindow.webContents.isDevToolsOpened()) {
            mainWindow.webContents.closeDevTools();
        }
        mainWindow.webContents.openDevTools({ mode: 'detach', activate: false });
    } else {
        mainWindow.loadFile(path.join(__dirname, "..", "dist", "index.html"));
    }

    mainWindow.once('ready-to-show', () => {
        // Close splash screen and show main window
        if (splashWindow) {
            splashWindow.close();
        }
        mainWindow.show();
        hardFocus(mainWindow, 'ready-to-show', true);
    });

    mainWindow.on('restore', () => hardFocus(mainWindow, 'restore'));

    mainWindow.on("closed", () => {
        mainWindow = null;
    });

    mainWindow.webContents.setWindowOpenHandler(({ url }) => {
        shell.openExternal(url);
        return { action: "deny" };
    });
    mainWindow.webContents.on("did-fail-load", (_e, code, desc, url) => {
        console.error("[Main] did-fail-load", { code, desc, url });
    });

    mainWindow.webContents.on("render-process-gone", (_e, details) => {
        console.error("[Main] render-process-gone", details);
    });

    mainWindow.webContents.on("console-message", (_e, level, message, line, sourceId) => {
        console.log(`[Renderer][L${level}] ${message} (${sourceId}:${line})`);
    });

    let didFinishLoadCalled = false;
    mainWindow.webContents.on("did-finish-load", () => {
        console.log("[Main] did-finish-load URL =", mainWindow.webContents.getURL());
        if (!didFinishLoadCalled) {
            didFinishLoadCalled = true;
            hardFocus(mainWindow, 'did-finish-load', true);
        }
    });
}

// ----------------------------
// App lifecycle
// ----------------------------

// ----------------------------
// Startup orchestration
// ----------------------------

async function attemptStartup(isRetry = false) {
    try {
        // 1. Spawn backend (only on first run; retries skip re-spawn)
        if (!isRetry) {
            updateSplashStatus("Avvio App in corso...");
            await startBackend();
        } else {
            // On retry, re-read port file in case it appeared late
            updateSplashStatus("Ricerca backend...");
            const p = await waitForBackendPort(30, 1000);
            if (p) backendPort = p;
        }

        console.log("[Main] Backend port:", backendPort);

        // 2. Wait until backend HTTP is actually responsive
        //    Initial: 480 retries * 20s = 20 min
        //    Retry:    240 retries * 10s = 10 min
        const retries = isRetry ? 240 : 480;
        const delay   = isRetry ? 10000 : 20000;
        const ready = await waitForBackendReady(backendPort, retries, delay);

        if (ready) {
            updateSplashStatus("Caricamento interfaccia...");
            createWindow();
        } else {
            console.warn("[Main] Backend not ready after health-check loop");
            showSplashWarning(
                "WARNING - La App ci sta mettendo pi\u00F9 del dovuto ad avviarsi, riprovare manualmente tra un po'."
            );
        }
    } catch (e) {
        console.error("[Main] Startup error:", e);
        showSplashWarning(
            "WARNING - La App ci sta mettendo pi\u00F9 del dovuto ad avviarsi, riprovare manualmente tra un po'."
        );
    }
}

app.whenReady().then(async () => {
    console.log("[Main] LibrePM starting...");
    console.log("[Main] isDev:", isDev);
    console.log("[Main] cwd:", process.cwd());
    console.log("[Main] userData:", app.getPath("userData"));
    console.log("[Main] Data path:", getLibrePMHome());

    // 1. Show Splash Screen immediately
    createSplashWindow();

    // 2. Start backend + health-check (small delay so splash renders first)
    setTimeout(() => attemptStartup(false), 500);

    // 3. Handle manual retry from splash
    ipcMain.on("splash:retry", () => {
        console.log("[Main] Retry requested from splash");
        attemptStartup(true);
    });

    app.on("activate", () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
        else hardFocus(mainWindow, 'app-activate');
    });
});

app.on("window-all-closed", () => {
    if (process.platform !== "darwin") {
        stopBackend();
        app.quit();
    }
});

app.on("will-quit", () => {
    stopBackend();
});

// ----------------------------
// IPC - Core
// ----------------------------

ipcMain.on('lpm:force-focus', (event, payload) => {
    const win = BrowserWindow.fromWebContents(event.sender);
    const reason = payload?.reason || 'async';
    console.log('[Main] lpm:force-focus RECEIVED reason:', reason);
    if (!win) return;
    throttledHardFocus(win, `ipc:${reason}`, false);
});

ipcMain.on('lpm:force-focus-sync', (event, payload) => {
    const win = BrowserWindow.fromWebContents(event.sender);
    const reason = payload?.reason || 'sync';
    if (!win) { event.returnValue = true; return; }

    if (!win.isFocused()) {
        throttledHardFocus(win, `ipc-sync:${reason}`, false);
    } else {
        // Window is focused but webContents might not have key focus
        try { win.webContents.focus(); } catch (e) { /* ignore */ }
    }
    event.returnValue = true;
});

ipcMain.on('lpm:ensure-webcontent-focus', (event, payload) => {
    const win = BrowserWindow.fromWebContents(event.sender);
    const reason = payload?.reason || 'ensure';

    if (!win || win.isDestroyed()) {
        event.returnValue = false;
        return;
    }

    // Only log for window-refocus (proactive), not for every call
    if (reason !== 'window-refocus') {
        console.log(`[Main] lpm:ensure-webcontent-focus reason: ${reason}`);
    }

    if (win.isFocused()) {
        // Window is focused → webContents.focus() to recover key focus
        try {
            win.webContents.focus();
        } catch (e) { /* ignore */ }
    } else {
        // Window not focused → focus window first, then webContents
        if (!win.isVisible()) win.show();
        try {
            win.focus();
            win.webContents.focus();
        } catch (e) { /* ignore */ }
    }

    event.returnValue = true;
});

ipcMain.handle("librepm:focus-window", () => {
    const win = BrowserWindow.getFocusedWindow() || mainWindow;
    if (!win) return false;
    if (win.isFocused()) {
        console.log('[Main] librepm:focus-window SKIPPED (already focused)');
        return true;
    }
    throttledHardFocus(win, "ipc:librepm:focus-window", false);
    return true;
});

ipcMain.handle("backend:getPort", () => backendPort);

ipcMain.handle("backend:refreshPort", () => {
    // In standalone mode, we rely on the port we found at startup
    // But we can re-check the file if needed
    return backendPort;
});

ipcMain.handle("app:getPath", (_, name) => {
    try {
        return app.getPath(name);
    } catch (e) {
        return null;
    }
});

ipcMain.handle("data:getLocalDataPath", () => getLibrePMHome());

ipcMain.handle("shell:openPath", async (_, filePath) => shell.openPath(filePath));
ipcMain.handle("shell:showItemInFolder", (_, filePath) => shell.showItemInFolder(filePath));
ipcMain.handle("shell:openExternal", async (_, url) => shell.openExternal(url));

ipcMain.on("window:minimize", () => mainWindow?.minimize());
ipcMain.on("window:maximize", () => {
    if (mainWindow?.isMaximized()) mainWindow.unmaximize();
    else mainWindow?.maximize();
});
ipcMain.on("window:close", () => mainWindow?.close());

ipcMain.on("renderer:log", (_, level, ...args) => {
    const prefix = "[Renderer]";
    if (level === "warn") console.warn(prefix, ...args);
    else if (level === "error") console.error(prefix, ...args);
    else console.log(prefix, ...args);
});

// ----------------------------
// Proxy IPC -> Backend HTTP
// ----------------------------

async function callBackend(method, apiPath, body = null, extraHeaders = {}) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: "localhost",
            port: backendPort,
            path: `/api${apiPath}`,
            method,
            headers: {
                "Content-Type": "application/json",
                Accept: "application/json",
                ...extraHeaders
            }
        };

        const req = http.request(options, (res) => {
            let data = "";
            res.on("data", (chunk) => (data += chunk));
            res.on("end", () => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    try {
                        resolve(data ? JSON.parse(data) : null);
                    } catch (e) {
                        reject(e);
                    }
                } else {
                    reject(new Error(`HTTP ${res.statusCode}: ${data}`));
                }
            });
        });

        req.on("error", reject);
        req.setTimeout(10000, () => {
            req.destroy();
            reject(new Error("Request timeout"));
        });

        if (body) req.write(JSON.stringify(body));
        req.end();
    });
}

// Projects
ipcMain.handle("projects:list", async () => callBackend("GET", "/projects"));
ipcMain.handle("projects:create", async (_, name) => callBackend("POST", "/projects", { name }));
ipcMain.handle("projects:delete", async (_, id) => callBackend("DELETE", `/projects/${id}`));

// Tasks
ipcMain.handle("tasks:list", async (_, projectId) => callBackend("GET", `/projects/${projectId}/tasks`));
ipcMain.handle("tasks:create", async (_, payload) =>
    callBackend("POST", `/projects/${payload.projectId}/tasks`, payload)
);
ipcMain.handle("tasks:update", async (_, taskId, patch) =>
    callBackend("PUT", `/projects/${patch.projectId}/tasks/${taskId}`, patch)
);
ipcMain.handle("tasks:delete", async (_, taskId) => callBackend("DELETE", `/tasks/${taskId}`));

// Focus Sessions
ipcMain.handle("focus:start", async (_, taskId) => callBackend("POST", "/focus/start", { taskId }));
ipcMain.handle("focus:stop", async () => callBackend("POST", "/focus/stop"));
ipcMain.handle("focus:running", async () => callBackend("GET", "/focus/running"));
ipcMain.handle("focus:history", async (_, taskId) => callBackend("GET", `/focus/task/${taskId}`));
ipcMain.handle("focus:historyAll", async () => callBackend("GET", "/focus"));

// ----------------------------
// IPC - Export/Import Data
// ----------------------------

ipcMain.handle("data:exportJsonDialog", async () => {
    const defaultPath = path.join(
        app.getPath("documents"),
        `LibrePM-export-${nowStamp()}.json`
    );

    const res = await dialog.showSaveDialog({
        title: "LibrePM - Esporta dati (JSON)",
        defaultPath,
        buttonLabel: "Esporta",
        filters: [{ name: "JSON", extensions: ["json"] }],
    });

    if (res.canceled || !res.filePath) {
        return { ok: false, canceled: true };
    }

    try {
        const projects = await callBackend("GET", "/projects");
        const exportData = {
            version: "0.5.2",
            exportedAt: new Date().toISOString(),
            projects: projects || [],
        };

        fs.writeFileSync(res.filePath, JSON.stringify(exportData, null, 2), "utf-8");
        return { ok: true, filePath: res.filePath };
    } catch (e) {
        return { ok: false, error: e.message };
    }
});

ipcMain.handle("data:importJsonDialog", async () => {
    const res = await dialog.showOpenDialog({
        title: "LibrePM - Importa dati (JSON)",
        buttonLabel: "Importa",
        filters: [{ name: "JSON", extensions: ["json"] }],
        properties: ["openFile"],
    });

    if (res.canceled || !res.filePaths || res.filePaths.length === 0) {
        return { ok: false, canceled: true };
    }

    const importPath = res.filePaths[0];

    try {
        const raw = fs.readFileSync(importPath, "utf-8");
        const data = JSON.parse(raw);
        return { ok: true, importPath, data };
    } catch (e) {
        return { ok: false, error: e.message };
    }
});

ipcMain.handle("data:exportCsvDialog", async (_evt, projectId) => {
    if (!projectId) {
        return { ok: false, error: "Nessun progetto selezionato" };
    }

    const defaultPath = path.join(
        app.getPath("documents"),
        `LibrePM-project-${nowStamp()}.csv`
    );

    const res = await dialog.showSaveDialog({
        title: "LibrePM - Esporta task progetto (CSV)",
        defaultPath,
        buttonLabel: "Esporta",
        filters: [{ name: "CSV", extensions: ["csv"] }],
    });

    if (res.canceled || !res.filePath) {
        return { ok: false, canceled: true };
    }

    try {
        const tasks = await callBackend("GET", `/projects/${projectId}/tasks`);
        const header = "id,title,status,priority,deadline,owner,createdAt,updatedAt";
        const rows = (tasks || []).map((t) =>
            [
                t.id,
                `"${(t.title || "").replace(/"/g, '""')}"`,
                t.status,
                t.priority,
                t.deadline || "",
                t.owner || "",
                t.createdAt,
                t.updatedAt,
            ].join(",")
        );
        const csv = [header, ...rows].join("\n");

        fs.writeFileSync(res.filePath, csv, "utf-8");
        return { ok: true, filePath: res.filePath };
    } catch (e) {
        return { ok: false, error: e.message };
    }
});

ipcMain.handle("data:exportDbDialog", async () => {
    const defaultPath = path.join(
        app.getPath("documents"),
        `LibrePM-db-${nowStamp()}.db`
    );

    const res = await dialog.showSaveDialog({
        title: "LibrePM - Esporta Database",
        defaultPath,
        buttonLabel: "Esporta",
        filters: [{ name: "SQLite Database", extensions: ["db"] }],
    });

    if (res.canceled || !res.filePath) {
        return { ok: false, canceled: true };
    }

    try {
        // Find the database file in the new location
        const dbPath = path.join(getLibrePMHome(), "librepm.db");

        if (!fs.existsSync(dbPath)) {
            return { ok: false, error: "Database non trovato in " + dbPath };
        }

        fs.copyFileSync(dbPath, res.filePath);
        return { ok: true, filePath: res.filePath };
    } catch (e) {
        return { ok: false, error: e.message };
    }
});

ipcMain.handle("data:importDbDialog", async () => {
    const res = await dialog.showOpenDialog({
        title: "LibrePM - Importa Database",
        buttonLabel: "Importa",
        filters: [{ name: "SQLite Database", extensions: ["db"] }],
        properties: ["openFile"],
    });

    if (res.canceled || !res.filePaths || res.filePaths.length === 0) {
        return { ok: false, canceled: true };
    }

    const importPath = res.filePaths[0];

    try {
        const destDb = path.join(getLibrePMHome(), "librepm.db");

        // Backup the existing database
        if (fs.existsSync(destDb)) {
            const backupPath = destDb.replace(".db", `.backup-${nowStamp()}.db`);
            fs.copyFileSync(destDb, backupPath);
        }

        // Copy the new database
        fs.copyFileSync(importPath, destDb);

        return {
            ok: true,
            importPath,
            destPath: destDb,
            message: "Database importato. Riavvia l'applicazione per applicare le modifiche."
        };
    } catch (e) {
        return { ok: false, error: e.message };
    }
});

console.log("[Main] IPC handlers registered");
