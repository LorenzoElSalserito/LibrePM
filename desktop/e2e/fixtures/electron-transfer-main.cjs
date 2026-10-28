/**
 * E2E main process for export/import tests.
 *
 * Like the packaged app it owns its backend: it spawns the backend jar on a random port with an
 * isolated data dir, loads the built renderer over file:// with the real preload, and registers the
 * real export/import handlers from electron/data-transfer.cjs (native dialogs included; tests stub
 * `dialog` from the outside with electronApp.evaluate). Restoring a database restarts this backend
 * exactly like main.cjs does, so the pending-import swap really happens.
 *
 * Env: LIBREPM_E2E_PROFILE_DIR, LIBREPM_E2E_DATA_DIR, LIBREPM_E2E_BACKEND_JAR, LIBREPM_E2E_JAVA (optional)
 */
const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const path = require('node:path');
const fs = require('node:fs');
const http = require('node:http');
const { spawn } = require('node:child_process');
const { createDataTransfer } = require('../../electron/data-transfer.cjs');

app.setPath('userData', process.env.LIBREPM_E2E_PROFILE_DIR);
const dataDir = process.env.LIBREPM_E2E_DATA_DIR;
const jar = process.env.LIBREPM_E2E_BACKEND_JAR;
const portFile = path.join(dataDir, 'config', 'backend.port');

let backend = null;
let port = null;
let mainWindow = null;
global.__lpmBackend = { port: null, starts: 0 };

function health(p) {
    return new Promise(resolve => {
        const req = http.get({ host: '127.0.0.1', port: p, path: '/api/health', timeout: 2000 }, res => { res.resume(); resolve(res.statusCode === 200); });
        req.on('error', () => resolve(false));
        req.on('timeout', () => { req.destroy(); resolve(false); });
    });
}

async function startBackend() {
    fs.mkdirSync(path.join(dataDir, 'config'), { recursive: true });
    fs.rmSync(portFile, { force: true });
    backend = spawn(process.env.LIBREPM_E2E_JAVA || 'java', [`-Dlibrepm.data.path=${dataDir}`, '-jar', jar], { stdio: ['ignore', 'pipe', 'pipe'] });
    const log = fs.createWriteStream(path.join(dataDir, 'backend-e2e.log'), { flags: 'a' });
    backend.stdout.pipe(log);
    backend.stderr.pipe(log);
    const deadline = Date.now() + 120000;
    while (Date.now() < deadline) {
        if (backend.exitCode !== null) throw new Error(`backend exited with ${backend.exitCode}`);
        const raw = fs.existsSync(portFile) ? fs.readFileSync(portFile, 'utf-8').trim() : '';
        const p = Number(raw.startsWith('{') ? JSON.parse(raw).port : raw);
        if (p && await health(p)) {
            port = p;
            global.__lpmBackend = { port, starts: global.__lpmBackend.starts + 1 };
            return;
        }
        await new Promise(r => setTimeout(r, 300));
    }
    throw new Error('backend did not start');
}

function stopBackend() {
    const child = backend;
    backend = null;
    if (!child || child.exitCode !== null) return Promise.resolve();
    return new Promise(resolve => {
        const timer = setTimeout(() => { child.kill('SIGKILL'); }, 20000);
        child.once('close', () => { clearTimeout(timer); resolve(); });
        child.kill();
    });
}

ipcMain.handle('backend:getPort', () => port);
ipcMain.handle('app:getPath', (_event, name) => app.getPath(name));
for (const event of ['lpm:force-focus-sync', 'lpm:ensure-webcontent-focus']) {
    ipcMain.on(event, message => { message.returnValue = true; });
}

createDataTransfer({
    dialog,
    app,
    getBackendPort: () => port,
    getWindow: () => mainWindow,
    getDataDir: () => dataDir,
    restartBackend: async () => { await stopBackend(); await startBackend(); return { restarted: true }; },
}).register(ipcMain);

app.whenReady().then(async () => {
    try {
        await startBackend();
    } catch (e) {
        console.error('[e2e] backend start failed', e);
        app.exit(1);
        return;
    }
    mainWindow = new BrowserWindow({ show: false, width: 1280, height: 900,
        webPreferences: { preload: path.resolve(__dirname, '../../electron/preload.cjs'), nodeIntegration: false, contextIsolation: true, sandbox: false } });
    mainWindow.loadFile(path.resolve(__dirname, '../../dist/index.html'));
});
app.on('window-all-closed', () => app.quit());
app.on('will-quit', () => { if (backend) backend.kill('SIGKILL'); });
