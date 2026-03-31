const { randomUUID } = require("crypto");

function registerTasksIpc(ipcMain, store, state) {
    ipcMain.handle("tasks:list", async (_evt, projectId) => {
        console.log("[IPC] tasks:list projectId=", projectId);
        const list = state.data.tasks.filter((t) => t.projectId === projectId);
        console.log("[IPC] tasks:list resultCount=", list.length);
        return list;
    });

    ipcMain.handle("tasks:create", async (_evt, payload) => {
        console.log("[IPC] tasks:create payload=", payload);

        const task = {
            id: randomUUID(),
            projectId: payload.projectId,
            title: payload.title ?? "Nuovo task",
            description: payload.description ?? "",
            status: payload.status ?? "TODO",
            priority: payload.priority ?? "MED",
            createdAt: Date.now(),
            updatedAt: Date.now(),
        };

        state.data.tasks.push(task);
        await store.save(state.data);

        console.log("[IPC] tasks:create saved. totalTasks=", state.data.tasks.length);
        return task;
    });

    ipcMain.handle("tasks:update", async (_evt, taskId, patch) => {
        console.log("[IPC] tasks:update taskId=", taskId, "patch=", patch);

        const t = state.data.tasks.find((x) => x.id === taskId);
        if (!t) {
            console.warn("[IPC] tasks:update NOT FOUND taskId=", taskId);
            return null;
        }

        Object.assign(t, patch, { updatedAt: Date.now() });
        await store.save(state.data);
        return t;
    });

    ipcMain.handle("tasks:delete", async (_evt, taskId) => {
        console.log("[IPC] tasks:delete taskId=", taskId);

        state.data.tasks = state.data.tasks.filter((t) => t.id !== taskId);
        await store.save(state.data);

        console.log("[IPC] tasks:delete saved. totalTasks=", state.data.tasks.length);
        return true;
    });
}

module.exports = { registerTasksIpc };
