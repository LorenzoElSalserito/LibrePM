const fs = require("fs/promises");
const path = require("path");

function defaultData() {
    return {
        projects: [],
        tasks: [],
        focusSessions: [],
        focusRunning: null,
        settings: { darkMode: true },
    };
}

function normalizeData(raw) {
    const safe = raw && typeof raw === "object" ? raw : {};

    const projects = Array.isArray(safe.projects) ? safe.projects : [];
    const tasks = Array.isArray(safe.tasks) ? safe.tasks : [];
    const focusSessions = Array.isArray(safe.focusSessions) ? safe.focusSessions : [];
    const focusRunning =
        safe.focusRunning && typeof safe.focusRunning === "object" ? safe.focusRunning : null;

    const settings = safe.settings && typeof safe.settings === "object" ? safe.settings : {};
    const darkMode = typeof settings.darkMode === "boolean" ? settings.darkMode : true;

    return {
        projects,
        tasks,
        focusSessions,
        focusRunning,
        settings: { darkMode },
    };
}

async function ensureDir(filePath) {
    await fs.mkdir(path.dirname(filePath), { recursive: true });
}

function createStore(filePath) {
    async function load() {
        try {
            const raw = await fs.readFile(filePath, "utf-8");
            return normalizeData(JSON.parse(raw));
        } catch (e) {
            if (e && e.code === "ENOENT") return defaultData();
            console.error("[STORE] load error:", e);
            return defaultData();
        }
    }

    async function save(data) {
        await ensureDir(filePath);
        await fs.writeFile(filePath, JSON.stringify(normalizeData(data), null, 2), "utf-8");
    }

    return { filePath, load, save };
}

module.exports = { createStore };
