import { useState, useEffect } from 'react';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

const EVENT_TYPES = [
    'NEW_NOTE',
    'TASK_ASSIGNED',
    'TASK_STATUS_CHANGED',
    'MENTION',
    'CONNECTION_REQUEST',
    'CONNECTION_ACCEPTED',
    'DEADLINE_APPROACHING',
    'BUDGET_THRESHOLD',
    'REVIEW_REQUESTED',
    'APPROVAL_REQUESTED',
    'APPROVAL_RESOLVED'
];

const CHANNELS = ['IN_APP', 'DESKTOP'];

const PRESETS = {
    all: () => true,
    essential: (eventType) => ['TASK_ASSIGNED', 'MENTION', 'DEADLINE_APPROACHING', 'APPROVAL_REQUESTED'].includes(eventType),
    critical: (eventType) => ['DEADLINE_APPROACHING', 'BUDGET_THRESHOLD'].includes(eventType),
};

export default function NotificationPreferencesPanel() {
    const { t } = useTranslation();
    const [prefs, setPrefs] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadPrefs();
    }, []);

    const loadPrefs = async () => {
        try {
            setLoading(true);
            const data = await librepm.notificationPreferencesList();
            const map = {};
            data.forEach(p => {
                const key = `${p.eventType}__${p.channel}`;
                map[key] = p.enabled;
            });
            setPrefs(map);
        } catch (e) {
            console.error('Error loading notification preferences:', e);
        } finally {
            setLoading(false);
        }
    };

    const isEnabled = (eventType, channel) => {
        const key = `${eventType}__${channel}`;
        return prefs[key] !== undefined ? prefs[key] : true; // Default enabled
    };

    const toggle = async (eventType, channel) => {
        const key = `${eventType}__${channel}`;
        const newVal = !isEnabled(eventType, channel);
        setPrefs(prev => ({ ...prev, [key]: newVal }));
        try {
            await librepm.notificationPreferencesUpsert({
                eventType,
                channel,
                enabled: newVal,
            });
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
            setPrefs(prev => ({ ...prev, [key]: !newVal })); // Revert
        }
    };

    const applyPreset = async (presetName) => {
        const fn = PRESETS[presetName];
        if (!fn) return;

        const newPrefs = {};
        const updates = [];
        for (const eventType of EVENT_TYPES) {
            for (const channel of CHANNELS) {
                const key = `${eventType}__${channel}`;
                const enabled = fn(eventType);
                newPrefs[key] = enabled;
                updates.push(librepm.notificationPreferencesUpsert({ eventType, channel, enabled }));
            }
        }
        setPrefs(newPrefs);
        try {
            await Promise.all(updates);
            toast.success(t("Preferences updated"));
        } catch (e) {
            toast.error(t("Error") + ": " + e.message);
            loadPrefs(); // Reload on error
        }
    };

    if (loading) {
        return <div className="text-center py-3"><div className="spinner-border spinner-border-sm text-primary"></div></div>;
    }

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h6 className="mb-0"><i className="bi bi-bell me-2"></i>{t("Notification Preferences")}</h6>
                <div className="btn-group btn-group-sm">
                    <button className="btn btn-outline-secondary" onClick={() => applyPreset('all')}>{t("All")}</button>
                    <button className="btn btn-outline-secondary" onClick={() => applyPreset('essential')}>{t("Essential")}</button>
                    <button className="btn btn-outline-secondary" onClick={() => applyPreset('critical')}>{t("Critical Only")}</button>
                </div>
            </div>

            <div className="table-responsive">
                <table className="table table-sm table-bordered mb-0">
                    <thead className="bg-light">
                    <tr>
                        <th className="ps-3">{t("Event Type")}</th>
                        {CHANNELS.map(ch => (
                            <th key={ch} className="text-center" style={{ width: 100 }}>
                                {ch === 'IN_APP' ? t("In-App") : t("Desktop")}
                            </th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {EVENT_TYPES.map(et => (
                        <tr key={et}>
                            <td className="ps-3 small">{t(`notifEvent.${et}`, et.replace(/_/g, ' '))}</td>
                            {CHANNELS.map(ch => (
                                <td key={ch} className="text-center">
                                    <div className="form-check form-switch d-inline-block mb-0">
                                        <input
                                            className="form-check-input"
                                            type="checkbox"
                                            checked={isEnabled(et, ch)}
                                            onChange={() => toggle(et, ch)}
                                        />
                                    </div>
                                </td>
                            ))}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
