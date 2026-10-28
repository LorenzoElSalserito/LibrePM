import { defineConfig } from '@playwright/test';
export default defineConfig({
    testDir: './e2e',
    workers: 1,
    timeout: 45000,
    use: { baseURL: 'http://127.0.0.1:4173', headless: true,
        launchOptions: { ...(process.env.CHROMIUM_PATH ? { executablePath: process.env.CHROMIUM_PATH } : {}), args: ['--no-sandbox'] },
        screenshot: 'only-on-failure', trace: 'retain-on-failure' },
    reporter: [['list'], ['json', { outputFile: 'test-results/e2e-results.json' }]],
    webServer: { command: 'npm run preview -- --host 127.0.0.1 --port 4173', url: 'http://127.0.0.1:4173', reuseExistingServer: false },
});
