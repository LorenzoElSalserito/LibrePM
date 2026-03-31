const fs = require("fs/promises");

function toCsv(tasks) {
    const header = "id,projectId,title,status,priority,createdAt,updatedAt";
    const rows = tasks.map((t) =>
        [
            t.id,
            t.projectId,
            JSON.stringify(t.title),
            t.status,
            t.priority,
            t.createdAt,
            t.updatedAt,
        ].join(",")
    );
    return [header, ...rows].join("\n");
}

async function exportToJsonFile(state, filePath) {
    await fs.writeFile(filePath, JSON.stringify(state.data, null, 2), "utf-8");
}

async function exportToCsvFile(state, filePath, projectId) {
    const tasks = state.data.tasks.filter((t) => t.projectId === projectId);
    await fs.writeFile(filePath, toCsv(tasks), "utf-8");
}

// Manteniamo anche gli IPC â€œvecchiâ€ (non eliminiamo funzionalitÃ )
function registerExportIpc(ipcMain, _store, state) {
    ipcMain.handle("export:json", async (_evt, filePath) => {
        await exportToJsonFile(state, filePath);
        return true;
    });

    ipcMain.handle("export:csv", async (_evt, filePath, projectId) => {
        await exportToCsvFile(state, filePath, projectId);
        return true;
    });
}

module.exports = { registerExportIpc, exportToJsonFile, exportToCsvFile };
