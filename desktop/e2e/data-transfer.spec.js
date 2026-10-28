import { test, expect, _electron as electron } from '@playwright/test';
import { mkdtemp, rm, readFile, readdir } from 'node:fs/promises';
import { existsSync, readdirSync } from 'node:fs';
import os from 'node:os';
import path from 'node:path';

/**
 * Export -> import round trips through the real Electron preload + main-process handlers
 * (electron/data-transfer.cjs) against a backend jar owned by the test app, so a database restore
 * really restarts the backend and swaps the SQLite file. Native dialogs are stubbed from outside.
 */

test.describe.configure({ mode: 'serial' });
test.setTimeout(300000);

function findJar() {
    if (process.env.LIBREPM_E2E_BACKEND_JAR) return process.env.LIBREPM_E2E_BACKEND_JAR;
    const libs = path.resolve('..', 'build', 'libs');
    if (!existsSync(libs)) return null;
    const jar = readdirSync(libs).filter(f => /^LibrePM-.*\.jar$/.test(f) && !f.includes('plain')).sort().pop();
    return jar ? path.join(libs, jar) : null;
}

const jar = findJar();

async function api(app, method, apiPath, body, userId) {
    const { port } = await app.evaluate(() => global.__lpmBackend);
    const response = await fetch(`http://127.0.0.1:${port}/api${apiPath}`, {
        method,
        headers: { 'Content-Type': 'application/json', ...(userId ? { 'X-User-Id': userId } : {}) },
        body: body ? JSON.stringify(body) : undefined,
    });
    const text = await response.text();
    if (!response.ok) throw new Error(`${method} ${apiPath} -> ${response.status} ${text}`);
    return text ? JSON.parse(text) : null;
}

async function launch(scratch) {
    const profile = path.join(scratch, 'profile');
    const dataDir = path.join(scratch, 'data');
    const app = await electron.launch({
        args: ['--no-sandbox', path.resolve('e2e/fixtures/electron-transfer-main.cjs')],
        env: { ...process.env, LIBREPM_E2E_PROFILE_DIR: profile, LIBREPM_E2E_DATA_DIR: dataDir, LIBREPM_E2E_BACKEND_JAR: jar },
        timeout: 180000,
    });
    const page = await app.firstWindow({ timeout: 180000 });
    page.on('console', m => { if (m.type() === 'error' || m.text().includes('DataTransfer')) console.log(`[renderer] ${m.text()}`); });
    await page.waitForURL('file:///**');
    const user = await api(app, 'POST', '/users', { username: `transfer-${Date.now()}`, displayName: 'Transfer QA', password: 'password123' });
    return { app, page, user, dataDir };
}

async function enter(page, userId, pageId) {
    await page.evaluate(id => {
        localStorage.setItem('librepm_autologin_enabled', 'true');
        localStorage.setItem('librepm_last_user_id', id);
        localStorage.setItem('librepm_lastPage', 'dashboard');
    }, userId);
    await page.reload();
    await page.locator(`[data-page="${pageId}"]`).click();
    await expect(page.locator(`[data-page="${pageId}"]`)).toHaveAttribute('aria-current', 'page');
}

/**
 * Clicks a transfer button and waits for its outcome toast (other toasts, like the welcome
 * message after a reload, are ignored). Fails with the error text on an unexpected error.
 */
async function clickTransfer(page, testId, { confirm = false, expectError = false } = {}) {
    const ok = page.locator('.Toastify__toast--success');
    const ko = page.locator('.Toastify__toast--error');
    const okBefore = await ok.count();
    const koBefore = await ko.count();
    await expect(page.getByTestId(testId)).toBeEnabled({ timeout: 30000 });
    await page.getByTestId(testId).click();
    if (confirm) await page.locator('.modal-container .btn-primary').click();
    if (expectError) {
        await expect.poll(() => ko.count(), { timeout: 180000 }).toBeGreaterThan(koBefore);
        return ko.nth(koBefore).innerText();
    }
    await expect.poll(async () => (await ok.count()) > okBefore || (await ko.count()) > koBefore, { timeout: 180000 }).toBe(true);
    if (await ko.count() > koBefore) throw new Error(`${testId}: ${await ko.nth(koBefore).innerText()}`);
    return ok.last().innerText();
}

async function stubSave(app, filePath) {
    await app.evaluate(({ dialog }, f) => { dialog.showSaveDialog = async () => ({ canceled: false, filePath: f }); }, filePath);
}

async function stubOpen(app, filePath) {
    await app.evaluate(({ dialog }, f) => { dialog.showOpenDialog = async () => ({ canceled: false, filePaths: [f] }); }, filePath);
}

async function projectNames(app, userId) {
    const doc = await api(app, 'GET', `/users/${userId}/workspace/export`, null, userId);
    return doc.projects.map(p => p.name).sort();
}

test.skip(!jar, 'Backend jar not built: run ./gradlew bootJar -x test');

test('preload exposes only narrow export/import functions', async () => {
    const scratch = await mkdtemp(path.join(os.tmpdir(), 'librepm-transfer-e2e-'));
    let app;
    try {
        ({ app } = await launch(scratch));
        const page = await app.firstWindow();
        const shape = await page.evaluate(() => ({
            ipc: typeof window.librepm.ipc,
            invoke: typeof window.librepm.invoke,
            fns: Object.keys(window.librepm.dataTransfer).sort(),
        }));
        expect(shape.ipc).toBe('undefined');
        expect(shape.invoke).toBe('undefined');
        expect(shape.fns).toEqual(['capabilities', 'exportCsv', 'exportDb', 'exportEncrypted', 'exportJson',
            'importCsv', 'importDb', 'importEncrypted', 'importJson']);
    } finally {
        if (app) await app.close();
        await rm(scratch, { recursive: true, force: true });
    }
});

test('Integrations: JSON and CSV export, then import back', async () => {
    const scratch = await mkdtemp(path.join(os.tmpdir(), 'librepm-transfer-e2e-'));
    let app;
    try {
        let page, user;
        ({ app, page, user } = await launch(scratch));
        const base = `/users/${user.id}/projects`;
        const project = await api(app, 'POST', base, { name: 'Transfer JSON' }, user.id);
        const parent = await api(app, 'POST', `${base}/${project.id}/tasks`, { title: 'Parent task', description: 'two\nlines' }, user.id);
        await api(app, 'POST', `${base}/${project.id}/tasks`, { title: 'Child task', parentTaskId: parent.id }, user.id);

        await enter(page, user.id, 'integrations');

        // JSON export
        const jsonFile = path.join(scratch, 'workspace.json');
        await stubSave(app, jsonFile);
        await clickTransfer(page, 'integrations-export-json');
        await expect.poll(() => existsSync(jsonFile), { timeout: 30000 }).toBe(true);
        const exported = JSON.parse(await readFile(jsonFile, 'utf-8'));
        expect(exported.format).toBe('librepm-workspace');
        expect(exported.projects.map(p => p.name)).toEqual(['Transfer JSON']);
        expect(exported.projects[0].tasks).toHaveLength(2);

        // CSV export of the current project
        const csvFile = path.join(scratch, 'tasks.csv');
        await stubSave(app, csvFile);
        await clickTransfer(page, 'integrations-export-csv');
        await expect.poll(() => existsSync(csvFile), { timeout: 30000 }).toBe(true);
        const csv = await readFile(csvFile, 'utf-8');
        expect(csv).toContain('Child task');
        expect(csv).toContain('"two\nlines"');

        // Lose the data, then JSON import restores it.
        await api(app, 'DELETE', `${base}/${project.id}`, null, user.id);
        expect(await projectNames(app, user.id)).toEqual([]);
        await stubOpen(app, jsonFile);
        await clickTransfer(page, 'integrations-import-json');
        await expect.poll(() => projectNames(app, user.id), { timeout: 30000 }).toEqual(['Transfer JSON']);
        const restored = (await api(app, 'GET', `/users/${user.id}/workspace/export`, null, user.id)).projects[0];
        expect(restored.tasks.map(t => t.title).sort()).toEqual(['Child task', 'Parent task']);
        const restoredParent = restored.tasks.find(t => t.title === 'Parent task');
        expect(restored.tasks.find(t => t.title === 'Child task').parentRef).toBe(restoredParent.ref);
        expect(restoredParent.description).toBe('two\nlines');

        // The page reloads after an import; CSV import then goes into the (restored) current project.
        await page.waitForLoadState('load');
        await page.locator('[data-page="integrations"]').click();
        await stubOpen(app, csvFile);
        await clickTransfer(page, 'integrations-import-csv');
        await expect.poll(async () => (await api(app, 'GET', `/users/${user.id}/workspace/export`, null, user.id)).summary.tasks,
            { timeout: 30000 }).toBe(4);
    } finally {
        if (app) await app.close();
        await rm(scratch, { recursive: true, force: true });
    }
});

test('Settings: .db and encrypted export, then restore with backend restart', async () => {
    const scratch = await mkdtemp(path.join(os.tmpdir(), 'librepm-transfer-e2e-'));
    let app;
    try {
        let page, user, dataDir;
        ({ app, page, user, dataDir } = await launch(scratch));
        const base = `/users/${user.id}/projects`;
        await api(app, 'POST', base, { name: 'Before export' }, user.id);
        await enter(page, user.id, 'settings');
        await expect(page.getByTestId('settings-export-db')).toBeEnabled();
        await expect(page.getByTestId('db-transfer-unavailable')).toHaveCount(0);

        // .db export
        const dbFile = path.join(scratch, 'backup.db');
        await stubSave(app, dbFile);
        await clickTransfer(page, 'settings-export-db');
        await expect.poll(() => existsSync(dbFile), { timeout: 30000 }).toBe(true);
        expect((await readFile(dbFile)).subarray(0, 15).toString('latin1')).toBe('SQLite format 3');

        // A change made after the export must disappear after the restore.
        await api(app, 'POST', base, { name: 'After export' }, user.id);
        expect(await projectNames(app, user.id)).toEqual(['After export', 'Before export']);

        await stubOpen(app, dbFile);
        await page.getByTestId('settings-import-db').click();
        await page.locator('.modal-container .btn-primary').click();
        await expect.poll(async () => (await app.evaluate(() => global.__lpmBackend)).starts, { timeout: 180000 }).toBe(2);
        expect(await projectNames(app, user.id)).toEqual(['Before export']);
        const backups = await readdir(path.join(dataDir, 'config', 'backup'));
        expect(backups.some(f => f.startsWith('librepm-before-import-') && f.endsWith('.db'))).toBe(true);

        // The renderer reloads itself and reconnects to the restarted backend (new port).
        await page.waitForLoadState('load');
        await page.locator('[data-page="settings"]').click();
        await expect(page.getByTestId('settings-export-encrypted')).toBeEnabled({ timeout: 60000 });

        // Encrypted export
        const encFile = path.join(scratch, 'backup.enc.json');
        await page.locator('#encryptPassphrase').fill('correct horse');
        await stubSave(app, encFile);
        await clickTransfer(page, 'settings-export-encrypted');
        await expect.poll(() => existsSync(encFile), { timeout: 30000 }).toBe(true);
        const enc = JSON.parse(await readFile(encFile, 'utf-8'));
        expect(enc.format).toBe('librepm-encrypted-db');
        expect(enc.ciphertext.length).toBeGreaterThan(100);

        await api(app, 'POST', base, { name: 'After encrypted export' }, user.id);

        // Wrong passphrase: rejected, nothing restarted.
        await stubOpen(app, encFile);
        await page.locator('#encryptPassphrase').fill('wrong passphrase');
        const wrong = await clickTransfer(page, 'settings-import-encrypted', { confirm: true, expectError: true });
        console.log(`wrong passphrase toast: ${wrong}`);
        expect((await app.evaluate(() => global.__lpmBackend)).starts).toBe(2);
        expect(await projectNames(app, user.id)).toEqual(['After encrypted export', 'Before export']);

        // Right passphrase: restored.
        await page.locator('#encryptPassphrase').fill('correct horse');
        await page.getByTestId('settings-import-encrypted').click();
        await page.locator('.modal-container .btn-primary').click();
        await expect.poll(async () => (await app.evaluate(() => global.__lpmBackend)).starts, { timeout: 180000 }).toBe(3);
        expect(await projectNames(app, user.id)).toEqual(['Before export']);
        await page.waitForLoadState('load');
        await expect(page.locator('[data-page="settings"]')).toBeVisible({ timeout: 60000 });
    } finally {
        if (app) await app.close();
        await rm(scratch, { recursive: true, force: true });
    }
});
