import { test, expect } from '@playwright/test';
const api = 'http://127.0.0.1:8080/api';

async function openWizard(page) {
    await page.addInitScript(() => {
        localStorage.setItem('librepm_autologin_enabled', 'false');
        localStorage.removeItem('librepm_last_user_id');
    });
    await page.goto('/');
    await expect(page.locator('.onboarding-page.jl-root')).toBeVisible();
}

async function finishWorkspaceStep(page) {
    const workspace = page.locator('.onboarding-workspace .profile-card').first();
    const app = page.locator('[data-page="dashboard"]');
    await expect(workspace.or(app)).toBeVisible();
    if (await workspace.isVisible()) await workspace.click();
    await expect(app).toBeVisible();
}

test('wizard creates a profile and enters the app without renderer errors', async ({ page }) => {
    const crashes = [];
    page.on('pageerror', e => crashes.push(e.message));
    await openWizard(page);
    const createButton = page.getByRole('button', { name: /Create new profile|Crea nuovo profilo/ });
    if (await createButton.isVisible()) await createButton.click();
    const stamp = Date.now();
    await page.locator('#username').fill(`wizard${stamp}`);
    await page.locator('#displayName').fill('Wizard E2E');
    await page.locator('#password').fill('Wizard123!');
    await page.locator('.onboarding-create button[type="submit"]').click();
    await finishWorkspaceStep(page);
    expect(crashes).toEqual([]);
});

test('wizard logs into an existing profile and rejects a wrong password', async ({ page, request }) => {
    const username = `wizlogin${Date.now()}`;
    const response = await request.post(`${api}/users`, { data: { username, displayName: `Login ${username}`, password: 'password123' } });
    expect(response.status()).toBe(201);
    await openWizard(page);
    const card = page.locator('.profile-card', { hasText: `Login ${username}` });
    await card.click();
    const password = card.locator('.profile-login-form input').first();
    await password.fill('wrong-password');
    await card.getByRole('button', { name: /^(Login|Accedi)$/ }).click();
    await expect(page.locator('.onboarding-alert')).toBeVisible();
    await password.fill('password123');
    await card.getByRole('button', { name: /^(Login|Accedi)$/ }).click();
    await expect(page.locator('[data-page="dashboard"]')).toBeVisible();
});

test('wizard stays still and fully visible with reduced motion', async ({ page }) => {
    await page.emulateMedia({ reducedMotion: 'reduce' });
    await openWizard(page);
    const container = page.locator('.onboarding-container');
    await expect(container).toBeVisible();
    const state = await container.evaluate(node => {
        const style = getComputedStyle(node);
        return { animation: style.animationName, opacity: style.opacity };
    });
    expect(state).toEqual({ animation: 'none', opacity: '1' });
    const width = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(width).toBeLessThanOrEqual(0);
});
