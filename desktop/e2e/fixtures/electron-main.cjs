const { app, BrowserWindow, ipcMain } = require('electron');
const path = require('node:path');
app.setPath('userData', process.env.LIBREPM_E2E_PROFILE_DIR);
ipcMain.handle('backend:getPort', () => 8080);
ipcMain.handle('app:getPath', (_event, name) => app.getPath(name));
for (const event of ['lpm:force-focus-sync', 'lpm:ensure-webcontent-focus']) {
    ipcMain.on(event, message => { message.returnValue = true; });
}
app.whenReady().then(() => {
    const window = new BrowserWindow({ show: false, width: 1280, height: 900,
        webPreferences: { preload: path.resolve(__dirname, '../../electron/preload.cjs'), nodeIntegration: false, contextIsolation: true, sandbox: false } });
    window.loadFile(path.resolve(__dirname, '../../dist/index.html'));
});
app.on('window-all-closed', () => app.quit());
