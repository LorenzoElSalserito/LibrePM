const { randomUUID } = require("crypto");

function registerProjectsIpc(ipcMain, store, state) {
    ipcMain.handle("projects:list", async () => {
        console.log("[IPC] projects:list count=", state.data.projects.length);
        return state.data.projects;
    });

    ipcMain.handle("projects:create", async (_evt, name) => {
        console.log("[IPC] projects:create name=", name);

        const project = { id: randomUUID(), name, createdAt: Date.now() };
        state.data.projects.push(project);

        await store.save(state.data);

        console.log("[IPC] projects:create saved. totalProjects=", state.data.projects.length);
        return project;
    });

    ipcMain.handle("projects:delete", async (_evt, projectId) => {
        console.log("[IPC] projects:delete projectId=", projectId);

        state.data.projects = state.data.projects.filter((p) => p.id !== projectId);
        state.data.tasks = state.data.tasks.filter((t) => t.projectId !== projectId);

        await store.save(state.data);

        console.log("[IPC] projects:delete saved. totalProjects=", state.data.projects.length);
        return true;
    });
}

module.exports = { registerProjectsIpc };
