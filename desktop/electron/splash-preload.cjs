/**
 * LibrePM Splash Screen Preload
 * Minimal preload to allow retry IPC from splash window.
 */
const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("splashApi", {
    retry: () => ipcRenderer.send("splash:retry"),
});
