import { test, expect } from '@playwright/test';
const api = 'http://127.0.0.1:8080/api';
let user, project;
test.beforeAll(async ({ request }) => {
    const response = await request.post(`${api}/users`, { data: { username: `e2e-${Date.now()}`, displayName: 'Quality E2E', password: 'password123' } });
    expect(response.status()).toBe(201);
    user = await response.json();
    const created = await request.post(`${api}/users/${user.id}/projects`, { data: { name: 'Quality Project' } });
    expect(created.status()).toBe(201);
    project = await created.json();
    const task = await request.post(`${api}/users/${user.id}/projects/${project.id}/tasks`, { data: { title: 'E2E timeline task', plannedStart: '2026-10-16T09:00:00', plannedFinish: '2026-10-20T18:00:00' } });
    expect(task.status()).toBe(201);
});
async function enter(page) {
    await page.addInitScript(id => {
        localStorage.setItem('librepm_autologin_enabled', 'true');
        localStorage.setItem('librepm_last_user_id', id);
        localStorage.setItem('librepm_lastPage', 'dashboard');
    }, user.id);
    await page.goto('/');
    await expect(page.locator('[data-page="gantt"]')).toBeVisible();
}
test('all pages remain reachable on personal profile without renderer crashes', async ({ page }) => {
    const crashes = [];
    const failures = [];
    page.on("response", response => { if (response.url().includes("/api/") && response.status() >= 400) failures.push(`${response.status()} ${response.url()}`); });
    page.on('pageerror', e => crashes.push(e.message));
    await enter(page);
    const ids = await page.locator('[data-page]').evaluateAll(nodes => nodes.map(n => n.dataset.page));
    expect(ids).toContain('resources');
    expect(ids).toContain('settings');
    for (const id of ids) {
        await page.locator(`[data-page="${id}"]`).click();
        await expect(page.locator(`[data-page="${id}"]`)).toHaveAttribute('aria-current', 'page');
        await page.waitForLoadState('networkidle');
        await expect(page.locator('#main-content')).not.toBeEmpty();
        await expect(page.getByText('Pagina non disponibile', { exact: true })).toHaveCount(0);
    }
    expect(crashes).toEqual([]);
    expect(failures).toEqual([]);
});
test('built Gantt renders task and survives view changes and revisit', async ({ page }) => {
    await enter(page);
    await page.locator('[data-page="gantt"]').click();
    await expect(page.locator('.gantt .bar-wrapper')).toHaveCount(1);
    await expect(page.locator('.gantt')).toHaveCount(1);
    for (const mode of ['Day', 'Month', 'Week']) {
        await page.locator(`[data-view-mode="${mode}"]`).click();
        await expect(page.locator('.gantt')).toHaveCount(1);
        await expect(page.locator('.gantt .bar-wrapper')).toHaveCount(1);
    }
    await page.locator('[data-page="planner"]').click();
    await page.locator('[data-page="gantt"]').click();
    await expect(page.locator('.gantt .bar-wrapper')).toHaveCount(1);
    await expect(page.locator('.gantt')).toHaveCount(1);
    await page.screenshot({ path: 'test-results/gantt.png', fullPage: true });
});
test('messages travel from UI to backend and survive reload without interpreting HTML', async ({ page, request }) => {
    await enter(page);
    await page.locator('[data-page="messages"]').click();
    const text = `E2E message ${Date.now()} <img src=x onerror=alert(1)>`;
    await page.getByLabel('Nuovo messaggio').fill(text);
    await page.getByRole('button', { name: 'Invia messaggio' }).click();
    await expect(page.getByText(text, { exact: true })).toBeVisible();
    const response = await request.get(`${api}/users/${user.id}/projects/${project.id}/messages`);
    expect((await response.json()).some(m => m.content === text)).toBeTruthy();
    await page.reload();
    await page.locator('[data-page="messages"]').click();
    await expect(page.getByText(text, { exact: true })).toBeVisible();
    await expect(page.locator('#main-content img[src="x"]')).toHaveCount(0);
    await page.screenshot({ path: 'test-results/messages.png', fullPage: true });
});

test('task create through planner UI, backend update and delete stay coherent', async ({ page, request }) => {
    await enter(page);
    await page.locator('[data-page="planner"]').click();
    await page.locator('.jl-header-actions .btn-primary').click();
    const dialog = page.locator('.modal-content').last();
    await expect(dialog).toBeVisible();
    const title = `UI task ${Date.now()}`;
    await dialog.locator('input[type="text"]').first().fill(title);
    const responsePromise = page.waitForResponse(response => response.url().endsWith(`/projects/${project.id}/tasks`) && response.request().method() === 'POST');
    await dialog.getByRole('button', { name: /Crea Task|Create Task|Crea attività/i }).click();
    const response = await responsePromise;
    expect(response.status()).toBe(201);
    const task = await response.json();
    await expect(page.getByText(title, { exact: true }).first()).toBeVisible();
    const path = `${api}/users/${user.id}/projects/${project.id}/tasks/${task.id}`;
    const update = await request.put(path, { data: { title: `${title} updated` } });
    expect(update.status()).toBe(200);
    await page.locator('[data-page="kanban"]').click();
    await expect(page.getByText(`${title} updated`, { exact: true }).first()).toBeVisible();
    expect((await request.delete(path)).status()).toBe(204);
    expect((await request.get(path)).status()).toBe(404);
});

test('compact viewport and both themes retain visible navigation and message controls', async ({ page }) => {
    await page.setViewportSize({ width: 1024, height: 768 });
    await enter(page);
    await page.locator('[data-page="messages"]').click();
    for (const theme of ['light', 'dark']) {
        await page.evaluate(theme => {
            document.documentElement.setAttribute('data-theme', theme);
            document.body.setAttribute('data-theme', theme);
        }, theme);
        await expect(page.getByLabel('Nuovo messaggio')).toBeVisible();
        await expect(page.getByRole('button', { name: 'Invia messaggio' })).toBeVisible();
        await page.screenshot({ path: `test-results/messages-${theme}.png`, fullPage: true });
    }
});

test('Gantt treats task labels as text instead of SVG markup', async ({ page, request }) => {
    const path = `${api}/users/${user.id}/projects/${project.id}/tasks`;
    const title = '<tspan data-injected="yes">Unsafe label</tspan>';
    const response = await request.post(path, { data: { title, plannedStart: '2026-10-16T09:00:00', plannedFinish: '2026-10-20T18:00:00' } });
    expect(response.status()).toBe(201);
    const task = await response.json();
    try {
        await enter(page);
        await page.locator('[data-page="gantt"]').click();
        await expect(page.locator('.gantt .bar-label').filter({ hasText: title })).toBeVisible();
        await expect(page.locator('.gantt [data-injected]')).toHaveCount(0);
    } finally { expect((await request.delete(`${path}/${task.id}`)).status()).toBe(204); }
});

test('diagnostics downloads a ZIP through settings and report endpoint returns PDF', async ({ page, request }) => {
    await enter(page);
    await page.locator('[data-page="settings"]').click();
    const downloadPromise = page.waitForEvent('download');
    await page.getByRole('button', { name: /Genera.*diagnostic|Generate Diagnostics Bundle/i }).click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.zip$/);
    expect(await download.failure()).toBeNull();
    const report = await request.get(`${api}/projects/${project.id}/report`);
    expect(report.status()).toBe(200);
    expect(report.headers()['content-type']).toContain('application/pdf');
    expect((await report.body()).subarray(0, 4).toString()).toBe('%PDF');
});

test('OKR key result accepts editing and persists value on blur', async ({ page, request }) => {
    const base = `${api}/users/${user.id}/projects/${project.id}/okrs`;
    const created = await request.post(base, { data: { objective: 'Editable quality target' } });
    expect(created.status()).toBe(201);
    const okr = await created.json();
    const keyResult = await request.post(`${base}/${okr.id}/key-results`, { data: { name: 'Verified flows', targetValue: 100, currentValue: 0, unit: '%' } });
    expect(keyResult.status()).toBe(200);
    const metric = await keyResult.json();
    await enter(page);
    await page.locator('[data-page="charter"]').click();
    await page.getByText('Editable quality target', { exact: true }).click();
    const row = page.locator('.border-top').filter({ has: page.getByText('Verified flows', { exact: true }) });
    const input = row.locator('input[type="number"]');
    await input.fill('42');
    await expect(input).toHaveValue('42');
    const updated = page.waitForResponse(response => response.url().endsWith(`/key-results/${metric.id}`) && response.request().method() === 'PUT');
    await input.blur();
    expect((await updated).status()).toBe(200);
    const stored = await request.get(`${base}/${okr.id}`);
    expect((await stored.json()).keyResults.find(item => item.id === metric.id).currentValue).toBe(42);
});
