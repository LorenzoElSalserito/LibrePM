const { dialog, app } = require("electron");
const path = require("path");
const fs = require("fs/promises");

const { exportToJsonFile, exportToCsvFile } = require("./export.cjs");

function pad2(n) {
    return String(n).padStart(2, "0");
}

function nowStamp() {
    const d = new Date();
    return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}_${pad2(
        d.getHours()
    )}${pad2(d.getMinutes())}${pad2(d.getSeconds())}`;
}

function normalizeImportedData(raw) {
    const safe = raw && typeof raw === "object" ? raw : {};

    const projects = Array.isArray(safe.projects) ? safe.projects : [];
    const tasks = Array.isArray(safe.tasks) ? safe.tasks : [];
    const focusSessions = Array.isArray(safe.focusSessions) ? safe.focusSessions : [];
    const settings = safe.settings && typeof safe.settings === "object" ? safe.settings : {};

    return {
        projects,
        tasks,
        focusSessions,
        settings: {
            darkMode: typeof settings.darkMode === "boolean" ? settings.darkMode : true,
        },
    };
}

async function backupFileIfExists(filePath) {
    try {
        await fs.access(filePath);
    } catch (_) {
        return null; // non esiste
    }

    const dir = path.dirname(filePath);
    const backupPath = path.join(dir, `librepm.backup.${nowStamp()}.json`);
    await fs.copyFile(filePath, backupPath);
    return backupPath;
}

async function readJson(filePath) {
    const raw = await fs.readFile(filePath, "utf-8");
    return JSON.parse(raw);
}

function registerDataIpc(ipcMain, store, state) {
    // Export JSON con dialog (completo)
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
            console.log("[IPC] data:exportJsonDialog canceled");
            return { ok: false, canceled: true };
        }

        await exportToJsonFile(state, res.filePath);
        console.log("[IPC] data:exportJsonDialog saved to", res.filePath);
        return { ok: true, filePath: res.filePath };
    });

    // Export CSV del progetto selezionato con dialog
    ipcMain.handle("data:exportCsvDialog", async (_evt, projectId) => {
        if (!projectId) {
            return { ok: false, error: "Nessun progetto selezionato" };
        }

        const defaultPath = path.join(
            app.getPath("documents"),
            `LibrePM-project-${projectId}-${nowStamp()}.csv`
        );

        const res = await dialog.showSaveDialog({
            title: "LibrePM - Esporta task progetto (CSV)",
            defaultPath,
            buttonLabel: "Esporta",
            filters: [{ name: "CSV", extensions: ["csv"] }],
        });

        if (res.canceled || !res.filePath) {
            console.log("[IPC] data:exportCsvDialog canceled");
            return { ok: false, canceled: true };
        }

        await exportToCsvFile(state, res.filePath, projectId);
        console.log("[IPC] data:exportCsvDialog saved to", res.filePath);
        return { ok: true, filePath: res.filePath };
    });

    // Import JSON con dialog (sostituisce i dati locali, con backup automatico)
    ipcMain.handle("data:importJsonDialog", async () => {
        const res = await dialog.showOpenDialog({
            title: "LibrePM - Importa dati (JSON)",
            buttonLabel: "Importa",
            filters: [{ name: "JSON", extensions: ["json"] }],
            properties: ["openFile"],
        });

        if (res.canceled || !res.filePaths || res.filePaths.length === 0) {
            console.log("[IPC] data:importJsonDialog canceled");
            return { ok: false, canceled: true };
        }

        const importPath = res.filePaths[0];

        // backup del file locale attuale (se esiste)
        const backupPath = await backupFileIfExists(store.filePath);

        const imported = await readJson(importPath);
        const normalized = normalizeImportedData(imported);

        state.data = normalized;
        await store.save(state.data);

        console.log("[IPC] data:importJsonDialog imported from", importPath);
        console.log("[IPC] data:importJsonDialog backupPath =", backupPath);

        return {
            ok: true,
            importPath,
            backupPath,
            stats: {
                projects: state.data.projects.length,
                tasks: state.data.tasks.length,
                focusSessions: state.data.focusSessions.length,
            },
        };
    });

    // Debug: path del file dati reale (utile per troubleshooting)
    ipcMain.handle("data:getLocalDataPath", async () => {
        return { ok: true, filePath: store.filePath };
    });
}

module.exports = { registerDataIpc };
