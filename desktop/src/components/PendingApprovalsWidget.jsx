import { useState, useEffect } from 'react';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

export default function PendingApprovalsWidget() {
    const { t } = useTranslation();
    const [approvals, setApprovals] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadApprovals();
    }, []);

    const loadApprovals = async () => {
        try {
            setLoading(true);
            const data = await librepm.approvalsPending();
            setApprovals(data);
        } catch (e) {
            console.error('Error loading approvals:', e);
        } finally {
            setLoading(false);
        }
    };

    const handleResolve = async (approvalId, status) => {
        try {
            await librepm.approvalsResolve(approvalId, status);
            toast.success(status === 'APPROVED' ? t("Approved") : t("Rejected"));
            loadApprovals();
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
        }
    };

    if (loading) {
        return (
            <div className="text-center py-3">
                <div className="spinner-border spinner-border-sm text-primary"></div>
            </div>
        );
    }

    if (approvals.length === 0) {
        return (
            <div className="text-center text-muted py-3">
                <i className="bi bi-check-circle fs-4 d-block mb-1"></i>
                <small>{t("No pending approvals")}</small>
            </div>
        );
    }

    return (
        <div className="list-group list-group-flush">
            {approvals.map(a => (
                <div key={a.id} className="list-group-item px-0">
                    <div className="d-flex justify-content-between align-items-start">
                        <div>
                            <div className="fw-bold small">
                                {t(`approvalType.${a.entityType}`, a.entityType.replace(/_/g, ' '))}
                            </div>
                            <div className="text-muted" style={{ fontSize: '0.75rem' }}>
                                {t("Requested by")} {a.requestedByName}
                            </div>
                            <div className="text-muted" style={{ fontSize: '0.7rem' }}>
                                {new Date(a.requestedAt).toLocaleDateString()}
                            </div>
                        </div>
                        <div className="btn-group btn-group-sm">
                            <button
                                className="btn btn-outline-success"
                                onClick={() => handleResolve(a.id, 'APPROVED')}
                                title={t("Approve")}
                            >
                                <i className="bi bi-check-lg"></i>
                            </button>
                            <button
                                className="btn btn-outline-danger"
                                onClick={() => handleResolve(a.id, 'REJECTED')}
                                title={t("Reject")}
                            >
                                <i className="bi bi-x-lg"></i>
                            </button>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
}
