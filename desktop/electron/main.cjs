/**
 * LibrePM Electron Main Process
 *
 * @author Lorenzo DM
 * @since 0.2.0
 * @updated 0.6.2 - Added Splash Screen & Custom Logo
 */

const { app, BrowserWindow, ipcMain, dialog, shell, Menu } = require("electron");
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
const BUG_REPORT_RECIPIENT = "commercial.lorenzodm@gmail.com";
const LOG_BUFFER_LIMIT = 200;
const LOG_TEXT_LIMIT = 12000;

let mainWindow = null;
let splashWindow = null;
let backendPort = BACKEND_DEFAULT_PORT;
let backendProcess = null;
let latestStartupFailure = null;
let startupFailureUserMessage = null;
const rendererLogBuffer = [];
const backendLogBuffer = [];
const mainLogBuffer = [];

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

function trimLogBuffer(buffer) {
    while (buffer.length > LOG_BUFFER_LIMIT) {
        buffer.shift();
    }
}

function appendLog(buffer, level, message) {
    buffer.push(`[${new Date().toISOString()}] [${level}] ${message}`);
    trimLogBuffer(buffer);
}

function serializeLogPart(part) {
    if (typeof part === "string") return part;
    try {
        return JSON.stringify(part);
    } catch (_) {
        return String(part);
    }
}

function truncateText(text, maxLength = LOG_TEXT_LIMIT) {
    if (text.length <= maxLength) return text;
    return `${text.slice(text.length - maxLength)}\n[truncated]`;
}

function getLogSection(title, buffer) {
    const content = buffer.length ? buffer.join("\n") : "No logs captured.";
    return `=== ${title} ===\n${truncateText(content)}`;
}

function openBugReportEmail(source = "general") {
    const subject = `[LibrePM] Bug Report (${source})`;
    const body = [
        "Please describe the issue here.",
        "",
        `Source: ${source}`,
        `App version: ${app.getVersion()}`,
        `Platform: ${process.platform}`,
        `Arch: ${process.arch}`,
        `Electron: ${process.versions.electron}`,
        `Chrome: ${process.versions.chrome}`,
        `Node: ${process.versions.node}`,
        latestStartupFailure ? `Startup failure: ${latestStartupFailure}` : "Startup failure: none recorded",
        "",
        getLogSection("MAIN LOGS", mainLogBuffer),
        "",
        getLogSection("FRONTEND LOGS", rendererLogBuffer),
        "",
        getLogSection("BACKEND LOGS", backendLogBuffer)
    ].join("\n");

    const url = `mailto:${BUG_REPORT_RECIPIENT}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    return shell.openExternal(url);
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

function getBundledJavaExecutable() {
    const jrePath = path.join(process.resourcesPath, "jre");
    const javaBin = process.platform === "win32" ? "bin/java.exe" : "bin/java";
    const bundledJava = path.join(jrePath, javaBin);

    if (fs.existsSync(bundledJava)) {
        return bundledJava;
    }

    return null;
}

function getSystemJavaExecutable() {
    return process.platform === "win32" ? "java.exe" : "java";
}

function looksLikeNativeJavaRuntimeFailure(text) {
    if (!text) return false;
    return /GLIBC_[0-9.]+\s+not found/i.test(text)
        || /error while loading shared libraries/i.test(text)
        || /No such file or directory/i.test(text)
        || /Exec format error/i.test(text)
        || /cannot execute/i.test(text)
        || /failed to start/i.test(text);
}

function summarizeRuntimeFailure(result) {
    const details = (result?.output || result?.error?.message || "").trim();
    if (/GLIBC_[0-9.]+\s+not found/i.test(details)) {
        return "Il runtime Java incluso non e' compatibile con questa versione di Linux.";
    }
    if (result?.error?.code === "ENOENT") {
        return result?.source === "system"
            ? "Java di sistema non trovato nel PATH."
            : "Runtime Java incluso non trovato nel pacchetto.";
    }
    if (/error while loading shared libraries/i.test(details)) {
        return "Il runtime Java non riesce a caricare le librerie di sistema richieste.";
    }
    return "Il backend Java non e' riuscito ad avviarsi.";
}

function buildStartupFailureUserMessage(details = {}) {
    const bundledFailed = details.bundledFailed;
    const systemTried = details.systemTried;
    const systemFailed = details.systemFailed;
    const bundledSummary = details.bundledResult ? summarizeRuntimeFailure(details.bundledResult) : null;
    const systemSummary = details.systemResult ? summarizeRuntimeFailure(details.systemResult) : null;

    if (bundledFailed && systemTried && systemFailed) {
        return `${bundledSummary || "Il runtime integrato non e' partito."} Ho provato anche con Java di sistema, ma non e' disponibile o non e' compatibile. Installa Java 21 oppure usa un pacchetto LibrePM compatibile con la tua distribuzione.`;
    }

    if (bundledFailed && systemTried && !systemFailed) {
        return `${bundledSummary || "Il runtime integrato non e' partito."} LibrePM sta usando Java di sistema come workaround.`;
    }

    if (bundledFailed) {
        return bundledSummary || "Il runtime Java incluso non e' riuscito a partire.";
    }

    return systemSummary || "Il backend non e' riuscito a partire.";
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

    const canonicalJarPath = path.join(libPath, "backend.jar");
    if (fs.existsSync(canonicalJarPath)) {
        return canonicalJarPath;
    }

    console.error("[Main] Canonical backend JAR not found:", canonicalJarPath);
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

    const jarPath = getBackendJar();
    if (!jarPath) {
        console.error("[Main] Cannot start backend: JAR not found.");
        startupFailureUserMessage = "LibrePM non trova il backend incluso nel pacchetto.";
        return Promise.reject(new Error("Bundled backend JAR not found."));
    }

    const bundledJava = getBundledJavaExecutable();
    const systemJava = getSystemJavaExecutable();
    const startupDetails = {
        bundledFailed: false,
        systemTried: false,
        systemFailed: false,
        bundledResult: null,
        systemResult: null
    };

    return (async () => {
        if (bundledJava) {
            startupDetails.bundledResult = await tryStartBackendWithJava(bundledJava, "bundled", jarPath);
            if (startupDetails.bundledResult.ok) {
                if (startupDetails.bundledResult.port == null) {
                    console.warn("[Main] Bundled Java started but backend port was not found yet.");
                }
                return;
            }

            startupDetails.bundledFailed = true;
            appendLog(mainLogBuffer, "startup-warning", `Bundled Java failed: ${startupDetails.bundledResult.output || startupDetails.bundledResult.error?.message || startupDetails.bundledResult.code || "unknown error"}`);
            console.warn("[Main] Bundled Java failed to start backend.");

            if (!looksLikeNativeJavaRuntimeFailure(startupDetails.bundledResult.output || startupDetails.bundledResult.error?.message || "")) {
                startupFailureUserMessage = buildStartupFailureUserMessage(startupDetails);
                throw new Error(startupDetails.bundledResult.output || startupDetails.bundledResult.error?.message || "Bundled Java failed.");
            }

            updateSplashStatus("Runtime integrato non compatibile, provo Java di sistema...");
        } else {
            startupDetails.bundledFailed = true;
            startupDetails.bundledResult = {
                ok: false,
                source: "bundled",
                output: "",
                error: new Error("Bundled Java not found")
            };
            appendLog(mainLogBuffer, "startup-warning", "Bundled Java not found. Falling back to system Java.");
            updateSplashStatus("Runtime integrato non trovato, provo Java di sistema...");
        }

        startupDetails.systemTried = true;
        startupDetails.systemResult = await tryStartBackendWithJava(systemJava, "system", jarPath);
        if (startupDetails.systemResult.ok) {
            appendLog(mainLogBuffer, "startup-warning", "Using system Java as backend runtime fallback.");
            if (startupDetails.systemResult.port == null) {
                console.warn("[Main] System Java started but backend port was not found yet.");
            }
            return;
        }

        startupDetails.systemFailed = true;
        startupFailureUserMessage = buildStartupFailureUserMessage(startupDetails);
        throw new Error(
            startupDetails.systemResult.output
            || startupDetails.systemResult.error?.message
            || "System Java fallback failed."
        );
    })();
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

function spawnBackendProcess(javaExec, source, jarPath) {
    console.log(`[Main] Spawning backend (${source}): ${javaExec} -jar ${jarPath}`);

    const dataPath = getLibrePMHome();
    if (!fs.existsSync(dataPath)) {
        fs.mkdirSync(dataPath, { recursive: true });
    }

    const portFile = getBackendPortFile();
    if (fs.existsSync(portFile)) {
        try {
            fs.unlinkSync(portFile);
        } catch (_) {
            // ignore
        }
    }

    const child = spawn(javaExec, [
        `-Dlibrepm.data.path=${dataPath}`,
        "-jar",
        jarPath
    ], {
        cwd: path.dirname(jarPath),
        detached: false,
        stdio: "pipe"
    });

    backendProcess = child;

    return child;
}

function attachBackendLogging(child, source, state) {
    child.stdout.on("data", (data) => {
        const text = String(data).trim();
        if (!text) return;
        state.output += `${text}\n`;
        appendLog(backendLogBuffer, `stdout:${source}`, text);
        console.log(`[Backend:${source}] ${text}`);
    });

    child.stderr.on("data", (data) => {
        const text = String(data).trim();
        if (!text) return;
        state.output += `${text}\n`;
        appendLog(backendLogBuffer, `stderr:${source}`, text);
        console.error(`[Backend:${source}] ${text}`);
    });

    child.on("error", (error) => {
        state.error = error;
        if (backendProcess === child) {
            backendProcess = null;
        }
    });

    child.on("close", (code) => {
        state.closed = true;
        state.code = code;
        appendLog(backendLogBuffer, `close:${source}`, `Process exited with code ${code}`);
        console.log(`[Backend:${source}] Process exited with code ${code}`);
        if (backendProcess === child) {
            backendProcess = null;
        }
    });
}

async function tryStartBackendWithJava(javaExec, source, jarPath) {
    const state = {
        source,
        output: "",
        error: null,
        closed: false,
        code: null
    };

    let child;
    try {
        child = spawnBackendProcess(javaExec, source, jarPath);
    } catch (error) {
        state.error = error;
        return {
            ok: false,
            source,
            error,
            output: state.output
        };
    }

    attachBackendLogging(child, source, state);

    await new Promise((resolve) => setTimeout(resolve, 2500));

    if (state.error || state.closed) {
        return {
            ok: false,
            source,
            error: state.error,
            code: state.code,
            output: state.output
        };
    }

    const port = await waitForBackendPort();
    if (port) {
        backendPort = port;
        return {
            ok: true,
            source,
            port,
            output: state.output
        };
    }

    return {
        ok: true,
        source,
        port: null,
        output: state.output
    };
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
        var bug = document.getElementById('bugBtn');
        if(bug){ bug.style.display='inline-block'; }
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
        appendLog(mainLogBuffer, "did-fail-load", JSON.stringify({ errorCode, errorDescription, validatedURL }));
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
        appendLog(mainLogBuffer, "render-process-gone", serializeLogPart(details));
        console.error("[Main] render-process-gone", details);
    });

    mainWindow.webContents.on("console-message", (_e, level, message, line, sourceId) => {
        appendLog(rendererLogBuffer, `console:${level}`, `${message} (${sourceId}:${line})`);
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
        startupFailureUserMessage = null;

        // 1. Spawn backend. On retry, respawn it if the previous attempt failed.
        if (!isRetry || !backendProcess) {
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
            latestStartupFailure = null;
            startupFailureUserMessage = null;
            updateSplashStatus("Caricamento interfaccia...");
            createWindow();
        } else {
            latestStartupFailure = `Backend not ready on port ${backendPort} after ${retries} attempts`;
            startupFailureUserMessage = "LibrePM non riesce ancora a contattare il backend locale. Riprova tra poco. Se il problema persiste, verifica il runtime Java o segnala il bug.";
            appendLog(mainLogBuffer, "startup-warning", latestStartupFailure);
            console.warn("[Main] Backend not ready after health-check loop");
            showSplashWarning(startupFailureUserMessage);
        }
    } catch (e) {
        latestStartupFailure = e?.stack || e?.message || String(e);
        if (!startupFailureUserMessage) {
            startupFailureUserMessage = "LibrePM non e' riuscito ad avviare il backend locale. Verifica Java di sistema o usa un pacchetto compatibile con la tua distribuzione.";
        }
        appendLog(mainLogBuffer, "startup-error", latestStartupFailure);
        console.error("[Main] Startup error:", e);
        showSplashWarning(startupFailureUserMessage);
    }
}

app.whenReady().then(async () => {
    console.log("[Main] LibrePM starting...");
    console.log("[Main] isDev:", isDev);
    console.log("[Main] cwd:", process.cwd());
    console.log("[Main] userData:", app.getPath("userData"));
    console.log("[Main] Data path:", getLibrePMHome());

    Menu.setApplicationMenu(null);

    // 1. Show Splash Screen immediately
    createSplashWindow();

    // 2. Start backend + health-check (small delay so splash renders first)
    setTimeout(() => attemptStartup(false), 500);

    // 3. Handle manual retry from splash
    ipcMain.on("splash:retry", () => {
        console.log("[Main] Retry requested from splash");
        latestStartupFailure = null;
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
ipcMain.handle("bug-report:open", async (_, source = "general") => openBugReportEmail(source));
ipcMain.handle("bug-report:open-startup", async () => openBugReportEmail("startup"));

ipcMain.on("window:minimize", () => mainWindow?.minimize());
ipcMain.on("window:maximize", () => {
    if (mainWindow?.isMaximized()) mainWindow.unmaximize();
    else mainWindow?.maximize();
});
ipcMain.on("window:close", () => mainWindow?.close());

ipcMain.on("renderer:log", (_, level, ...args) => {
    const prefix = "[Renderer]";
    appendLog(rendererLogBuffer, level || "log", args.map(serializeLogPart).join(" "));
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
