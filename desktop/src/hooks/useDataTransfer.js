import { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-toastify';
import { librepm } from '@api/librepm.js';
import { useModal } from './useModal.js';

/**
 * Shared export/import logic for Settings, Integrations and the profile menu.
 *
 * Every action reports progress (busy key), success and failure. Database restores ask for
 * confirmation, then — on desktop — wait for the backend restart and reload the app.
 */
export function useDataTransfer() {
    const { t } = useTranslation();
    const modal = useModal();
    const [busy, setBusy] = useState(null);
    const [caps, setCaps] = useState({ loading: true, fileTransferSupported: false, engine: null });
    const busyRef = useRef(false);

    useEffect(() => {
        let alive = true;
        librepm.transferCapabilities()
            .then((c) => { if (alive) setCaps({ loading: false, ...c }); })
            .catch(() => { if (alive) setCaps({ loading: false, fileTransferSupported: false, engine: 'unknown' }); });
        return () => { alive = false; };
    }, []);

    const describeError = useCallback((e) => {
        const code = e?.data?.code || e?.data?.error;
        const message = e?.message || String(e);
        if (code === 'FILE_TRANSFER_UNSUPPORTED') return t('Database file backups are not available with the shared database.');
        if (code === 'BUSY') return t('Another export or import is already running.');
        if (/Decryption failed|wrong passphrase/i.test(message)) return t('Wrong passphrase or damaged backup file.');
        if (/not a SQLite|not a LibrePM database|cannot be read as a LibrePM/i.test(message)) return t('The selected file is not a LibrePM database.');
        if (/newer LibrePM version/i.test(message)) return t('The file was created by a newer LibrePM version. Update LibrePM first.');
        if (/encrypted database backup/i.test(message)) return t('This is an encrypted backup: use Import encrypted backup in Settings.');
        return message;
    }, [t]);

    const run = useCallback(async (key, action, onSuccess, errorTitle) => {
        if (busyRef.current) {
            toast.info(t('Another export or import is already running.'));
            return null;
        }
        busyRef.current = true;
        setBusy(key);
        try {
            const result = await action();
            if (result?.canceled) return result;
            onSuccess?.(result);
            return result;
        } catch (e) {
            console.error(`[DataTransfer] ${key} failed`, e);
            toast.error(`${errorTitle}: ${describeError(e)}`);
            return null;
        } finally {
            busyRef.current = false;
            setBusy(null);
        }
    }, [t, describeError]);

    const savedTo = useCallback((result) => (result?.filePath ? ` ${t('Saved to {{path}}', { path: result.filePath })}` : ''), [t]);

    const afterRestore = useCallback((result) => {
        if (result?.restarted) {
            toast.success(t('Database restored. Reloading LibrePM...'));
            setTimeout(() => window.location.reload(), 1500);
        } else {
            toast.success(t('Import accepted: restart the LibrePM backend to apply it. A backup of the current database is kept.'));
        }
    }, [t]);

    const confirmRestore = useCallback(() => modal.confirm({
        title: t('Restore database'),
        message: t('Restoring replaces ALL current data with the content of the file. A backup copy of the current database is saved first and LibrePM restarts the backend. Continue?'),
    }), [modal, t]);

    const exportJson = useCallback(() => run('exportJson', librepm.transferExportJson, (r) => {
        const s = r?.summary;
        const counts = s ? ` ${t('{{projects}} projects, {{tasks}} tasks.', { projects: s.projects ?? 0, tasks: s.tasks ?? 0 })}` : '';
        toast.success(`${t('JSON export completed.')}${counts}${savedTo(r)}`);
    }, t('Export error')), [run, t, savedTo]);

    const importJson = useCallback(() => run('importJson', librepm.transferImportJson, (r) => {
        const s = r?.summary || {};
        toast.success(t('Import completed: {{projects}} projects and {{tasks}} tasks added.', { projects: s.projects ?? 0, tasks: s.tasks ?? 0 }));
        (s.warnings || []).forEach((w) => toast.warn(w));
        setTimeout(() => window.location.reload(), 1500);
    }, t('Import error')), [run, t]);

    const exportCsv = useCallback((projectId) => run('exportCsv', () => librepm.transferExportCsv(projectId), (r) => {
        toast.success(`${t('CSV export completed: {{tasks}} tasks.', { tasks: r?.summary?.tasks ?? 0 })}${savedTo(r)}`);
    }, t('Export error')), [run, t, savedTo]);

    const importCsv = useCallback((projectId) => run('importCsv', () => librepm.transferImportCsv(projectId), (r) => {
        toast.success(t('{{tasks}} tasks imported from CSV.', { tasks: r?.summary?.tasks ?? 0 }));
        if (r?.summary?.warnings) toast.warn(r.summary.warnings);
        setTimeout(() => window.location.reload(), 1500);
    }, t('Import error')), [run, t]);

    const exportDb = useCallback(() => run('exportDb', librepm.transferExportDb, (r) => {
        toast.success(`${t('Database exported.')}${savedTo(r)}`);
    }, t('Export error')), [run, t, savedTo]);

    const importDb = useCallback(async () => {
        if (!(await confirmRestore())) return null;
        return run('importDb', librepm.transferImportDb, afterRestore, t('Import error'));
    }, [run, t, confirmRestore, afterRestore]);

    const exportEncrypted = useCallback((passphrase) => {
        if (!passphrase || passphrase.length < 4) {
            toast.error(t('Passphrase must be at least 4 characters'));
            return null;
        }
        return run('exportEncrypted', () => librepm.transferExportEncrypted(passphrase), (r) => {
            toast.success(`${t('Encrypted export completed')}.${savedTo(r)}`);
        }, t('Export error'));
    }, [run, t, savedTo]);

    const importEncrypted = useCallback(async (passphrase) => {
        if (!passphrase) {
            toast.error(t('Enter the passphrase used for the encrypted export.'));
            return null;
        }
        if (!(await confirmRestore())) return null;
        return run('importEncrypted', () => librepm.transferImportEncrypted(passphrase), afterRestore, t('Import error'));
    }, [run, t, confirmRestore, afterRestore]);

    return {
        busy,
        caps,
        restoring: busy === 'importDb' || busy === 'importEncrypted',
        exportJson,
        importJson,
        exportCsv,
        importCsv,
        exportDb,
        importDb,
        exportEncrypted,
        importEncrypted,
    };
}
