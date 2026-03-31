import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import fs from 'fs';

// Leggi la versione dal package.json
const packageJson = JSON.parse(fs.readFileSync('./package.json', 'utf-8'));
const appVersion = packageJson.version;

/**
 * Vite Configuration for LibrePM
 * 
 * @author Lorenzo DM
 * @since 0.2.0
 */
export default defineConfig({
    plugins: [react()],
    
    // Definisci variabili globali
    define: {
        '__APP_VERSION__': JSON.stringify(appVersion),
    },

    // Base path for production build
    base: './',
    
    // Development server
    server: {
        port: 5173,
        strictPort: true,
        host: true,
    },
    
    // Build configuration
    build: {
        outDir: 'dist',
        emptyOutDir: true,
        sourcemap: true,
        rollupOptions: {
            output: {
                manualChunks: {
                    'react-vendor': ['react', 'react-dom'],
                    'ui-vendor': ['bootstrap', 'react-toastify'],
                    'calendar-vendor': ['react-big-calendar', 'date-fns'],
                    'editor-vendor': ['@uiw/react-md-editor'],
                },
            },
        },
    },
    
    // Path aliases
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
            '@components': path.resolve(__dirname, './src/components'),
            '@pages': path.resolve(__dirname, './src/pages'),
            '@api': path.resolve(__dirname, './src/api'),
            '@styles': path.resolve(__dirname, './src/styles'),
            '@layout': path.resolve(__dirname, './src/layout'),
        },
    },
    
    // CSS configuration
    css: {
        devSourcemap: true,
    },
    
    // Optimize dependencies
    optimizeDeps: {
        include: [
            'react',
            'react-dom',
            'bootstrap',
            'react-toastify',
            'react-big-calendar',
            'date-fns',
            '@uiw/react-md-editor',
        ],
    },
});
