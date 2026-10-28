import { useTranslation } from 'react-i18next';

/** Inline progress banner shown while an export/import runs. */
export function TransferStatus({ busy }) {
    const { t } = useTranslation();
    if (!busy) return null;
    const restoring = busy === 'importDb' || busy === 'importEncrypted';
    return (
        <div className="alert alert-info d-flex align-items-center gap-2 py-2 mb-3 lpm-transfer-status" role="status" aria-live="polite">
            <span className="spinner-border spinner-border-sm flex-shrink-0" aria-hidden="true"></span>
            <span className="small">
                {restoring
                    ? t('Restoring the database and restarting the backend. This can take a few seconds...')
                    : busy.startsWith('import') ? t('Import in progress...') : t('Export in progress...')}
            </span>
        </div>
    );
}

/** Button content with a spinner while its own action runs. */
export function TransferLabel({ active, icon, children }) {
    return (
        <>
            {active
                ? <span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                : <i className={`bi ${icon} me-2`} aria-hidden="true"></i>}
            {children}
        </>
    );
}
