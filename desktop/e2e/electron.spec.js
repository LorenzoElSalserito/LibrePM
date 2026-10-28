import { test, expect, _electron as electron } from '@playwright/test';
import { mkdtemp, rm } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';

test('installed renderer loads local assets and Gantt over file protocol with real preload', async ({ request }) => {
    const userResponse = await request.post('http://127.0.0.1:8080/api/users', { data: { username: `electron-${Date.now()}`, displayName: 'Electron QA', password: 'password123' } });
    expect(userResponse.status()).toBe(201);
    const user = await userResponse.json();
    const projects = `http://127.0.0.1:8080/api/users/${user.id}/projects`;
    const created = await request.post(projects, { data: { name: 'Installed project' } });
    expect(created.status()).toBe(201);
    const project = await created.json();
    expect((await request.post(`${projects}/${project.id}/tasks`, { data: { title: 'Installed timeline', plannedStart: '2026-10-16T09:00:00', plannedFinish: '2026-10-20T18:00:00' } })).status()).toBe(201);
    const profile = await mkdtemp(path.join(os.tmpdir(), 'librepm-electron-e2e-'));
    let application;
    try {
        application = await electron.launch({ args: ['--no-sandbox', path.resolve('e2e/fixtures/electron-main.cjs')], env: { ...process.env, LIBREPM_E2E_PROFILE_DIR: profile } });
        const page = await application.firstWindow();
        await page.waitForURL('file:///**');
        await page.evaluate(userId => {
            localStorage.setItem('librepm_autologin_enabled', 'true');
            localStorage.setItem('librepm_last_user_id', userId);
        }, user.id);
        await page.reload();
        await page.locator('[data-page="gantt"]').click();
        await expect(page.locator('.gantt .bar-wrapper')).toHaveCount(1);
        await expect(page.locator('.gantt .bar-label')).toHaveText('Installed timeline');
        await page.screenshot({ path: 'test-results/electron-gantt.png', fullPage: true });
    } finally {
        if (application) await application.close();
        await rm(profile, { recursive: true, force: true });
    }
});
