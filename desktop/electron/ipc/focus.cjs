const { randomUUID } = require("crypto");

function registerFocusIpc(ipcMain, store, state) {
    ipcMain.handle("focus:running", async () => {
        return state.data.focusRunning ?? null;
    });

    ipcMain.handle("focus:start", async (_evt, taskId) => {
        if (!taskId) return { ok: false, error: "taskId mancante" };

        if (state.data.focusRunning) {
            return { ok: false, error: "C'Ã¨ giÃ  un timer attivo" };
        }

        state.data.focusRunning = { taskId, startedAt: Date.now() };
        await store.save(state.data);

        console.log("[IPC] focus:start", state.data.focusRunning);
        return { ok: true };
    });

    ipcMain.handle("focus:stop", async () => {
        const running = state.data.focusRunning;
        if (!running) return { ok: false, error: "Nessun timer attivo" };

        const endedAt = Date.now();
        const durationMs = Math.max(0, endedAt - running.startedAt);

        const session = {
            id: randomUUID(),
            taskId: running.taskId,
            startedAt: running.startedAt,
            endedAt,
            durationMs,
        };

        state.data.focusSessions.push(session);
        state.data.focusRunning = null;

        await store.save(state.data);

        console.log("[IPC] focus:stop session=", session);
        return { ok: true, session };
    });

    ipcMain.handle("focus:history", async (_evt, taskId) => {
        if (!taskId) return [];
        const list = state.data.focusSessions
            .filter((s) => s.taskId === taskId)
            .sort((a, b) => (b.startedAt ?? 0) - (a.startedAt ?? 0));
        return list;
    });

    // Nuovo: tutte le sessioni (per totale giornaliero globale)
    ipcMain.handle("focus:historyAll", async () => {
        return [...state.data.focusSessions].sort(
            (a, b) => (b.startedAt ?? 0) - (a.startedAt ?? 0)
        );
    });
}

module.exports = { registerFocusIpc };
