import { test, expect } from '@playwright/test';
import { readFile } from 'node:fs/promises';

// Files shared between project members: one user uploads (as if from another PC),
// the other gets the notification, opens the project files and downloads them.
const api = 'http://127.0.0.1:8080/api';
let alice, bob, project, task;

async function createUser(request, name) {
    const response = await request.post(`${api}/users`, { data: { username: `${name}-${Date.now()}`, displayName: name, password: 'password123' } });
    expect(response.status()).toBe(201);
    return response.json();
}

test.beforeAll(async ({ request }) => {
    alice = await createUser(request, 'Alice Files');
    bob = await createUser(request, 'Bob Files');
    const created = await request.post(`${api}/users/${alice.id}/projects`, { data: { name: 'Shared Files Project' } });
    expect(created.status()).toBe(201);
    project = await created.json();
    const createdTask = await request.post(`${api}/users/${alice.id}/projects/${project.id}/tasks`, { data: { title: 'Task with files' } });
    expect(createdTask.status()).toBe(201);
    task = await createdTask.json();
    const member = await request.post(`${api}/users/${alice.id}/projects/${project.id}/members`, { data: { userId: bob.id, role: 'EDITOR' } });
    expect(member.status()).toBe(200);
});

async function enterAs(page, user) {
    await page.addInitScript(id => {
        localStorage.setItem('librepm_autologin_enabled', 'true');
        localStorage.setItem('librepm_last_user_id', id);
        localStorage.setItem('librepm_lastPage', 'dashboard');
    }, user.id);
    await page.goto('/');
    await expect(page.locator('[data-page="dashboard"]')).toBeVisible();
}

test('a file uploaded by a colleague is notified, listed and downloadable; uploads from the UI reach the colleague', async ({ page, request }) => {
    const content = `Budget from Alice ${Date.now()}`;
    const upload = await request.post(`${api}/projects/${project.id}/assets`, {
        headers: { 'X-User-Id': alice.id },
        multipart: { file: { name: 'budget-alice.txt', mimeType: 'text/plain', buffer: Buffer.from(content) } },
    });
    expect(upload.status()).toBe(201);

    const failures = [];
    page.on('response', response => { if (response.url().includes('/api/') && response.status() >= 400) failures.push(`${response.status()} ${response.url()}`); });
    await enterAs(page, bob);

    // The bell shows the new file and opens the project files.
    await page.getByTitle('Notifiche').click();
    await expect(page.getByText('Nuovo file').first()).toBeVisible();
    await page.getByRole('button', { name: 'Apri i file' }).first().click();
    const card = page.getByTestId('project-files-card');
    await expect(card.getByText('budget-alice.txt')).toBeVisible();
    await expect(card.getByText(/Caricato da Alice Files/)).toBeVisible();

    // Bob downloads Alice's file, byte-identical.
    const downloadPromise = page.waitForEvent('download');
    await card.getByRole('button', { name: 'Scarica budget-alice.txt' }).click();
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toBe('budget-alice.txt');
    expect(await readFile(await download.path(), 'utf8')).toBe(content);
    // Alice's file has no delete button for Bob.
    await expect(card.getByRole('button', { name: 'Elimina budget-alice.txt' })).toHaveCount(0);

    // Bob uploads from the UI; Alice sees it and is notified.
    await card.getByTestId('attachments-input').setInputFiles({ name: 'reply-bob.txt', mimeType: 'text/plain', buffer: Buffer.from('Approved by Bob') });
    await expect(card.getByText('reply-bob.txt')).toBeVisible();
    const list = await (await request.get(`${api}/projects/${project.id}/assets`, { headers: { 'X-User-Id': alice.id } })).json();
    expect(list.map(a => a.fileName)).toEqual(expect.arrayContaining(['budget-alice.txt', 'reply-bob.txt']));
    const notifications = await (await request.get(`${api}/notifications/unread`, { headers: { 'X-User-Id': alice.id } })).json();
    expect(notifications.some(n => n.type === 'FILE_ADDED' && n.referenceId === project.id)).toBeTruthy();

    // Bob deletes his own file, after confirming.
    await card.getByRole('button', { name: 'Elimina reply-bob.txt' }).click();
    await card.getByRole('button', { name: 'Elimina', exact: true }).click();
    await expect(card.getByText('reply-bob.txt')).toHaveCount(0);
    await page.screenshot({ path: 'test-results/attachments.png', fullPage: true });
    expect(failures).toEqual([]);
});

test('task attachments uploaded elsewhere appear in the task editor', async ({ page, request }) => {
    const upload = await request.post(`${api}/tasks/${task.id}/assets`, {
        headers: { 'X-User-Id': alice.id },
        multipart: { file: { name: 'task-spec.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4 fake') } },
    });
    expect(upload.status()).toBe(201);
    await enterAs(page, bob);
    await page.locator('[data-page="planner"]').click();
    await page.getByText('Task with files').first().click();
    const dialog = page.locator('.modal-content').last();
    await expect(dialog.getByText('task-spec.pdf')).toBeVisible();
    const downloadPromise = page.waitForEvent('download');
    await dialog.getByRole('button', { name: 'Scarica task-spec.pdf' }).click();
    expect(await readFile(await (await downloadPromise).path(), 'utf8')).toBe('%PDF-1.4 fake');
});
