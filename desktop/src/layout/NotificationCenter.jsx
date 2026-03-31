import React, { useEffect, useMemo, useState } from "react";
import { Badge, Button, Dropdown, Spinner } from "react-bootstrap";
import { toast } from "react-toastify";
import { librepm } from "@api/librepm.js";
import { useTranslation } from 'react-i18next';

const DEFAULT_POLL_MS = 15000;

function safeDateLabel(value) {
    try {
        if (!value) return "";
        const d = new Date(value);
        if (Number.isNaN(d.getTime())) return "";
        return d.toLocaleString();
    } catch {
        return "";
    }
}

export default function NotificationCenter({ pollMs = DEFAULT_POLL_MS }) {
    const { t } = useTranslation();
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const unreadCount = useMemo(
        () => notifications.filter((n) => !n.read).length,
        [notifications]
    );

    const loadNotifications = async (isBackground = false) => {
        try {
            if (!isBackground) {
                setLoading(true);
                setError(null);
            }
            const res = await librepm.notificationsList();
            setNotifications(res?.notifications ?? []);
            // If the fetch succeeds, clear any previous errors
            setError(null);
        } catch (e) {
            console.error(e);
            // Only show the error if it's not a background update
            if (!isBackground) {
                setError(e);
                setNotifications([]);
            }
        } finally {
            if (!isBackground) setLoading(false);
        }
    };

    useEffect(() => {
        loadNotifications(false); // Initial load (shows spinner)
        const id = setInterval(() => loadNotifications(true), pollMs); // Silent polling
        return () => clearInterval(id);
    }, [pollMs]);

    const markRead = async (id) => {
        try {
            await librepm.notificationsMarkRead(id);
            setNotifications((prev) =>
                prev.map((n) => (n.id === id ? { ...n, read: true } : n))
            );
        } catch (e) {
            console.error(e);
            toast.error(t("Error updating notification"));
        }
    };

    const markAllRead = async () => {
        try {
            await librepm.notificationsMarkAllRead();
            setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
        } catch (e) {
            console.error(e);
            toast.error(t("Error marking all as read"));
        }
    };

    return (
        <Dropdown align="end">
            <Dropdown.Toggle
                variant="outline-secondary"
                size="sm"
                className="position-relative"
            >
                <i className="bi bi-bell"></i>
                {unreadCount > 0 && (
                    <Badge
                        bg="danger"
                        pill
                        className="position-absolute top-0 start-100 translate-middle"
                    >
                        {unreadCount > 99 ? "99+" : unreadCount}
                    </Badge>
                )}
            </Dropdown.Toggle>

            <Dropdown.Menu style={{ minWidth: 340 }}>
                <div className="d-flex justify-content-between align-items-center px-3 py-2">
                    <strong>{t("Notifications")}</strong>
                    <Button
                        variant="link"
                        size="sm"
                        className="text-decoration-none"
                        onClick={markAllRead}
                        disabled={notifications.length === 0}
                    >
                        {t("Mark all as read")}
                    </Button>
                </div>

                <Dropdown.Divider />

                {loading && (
                    <div className="px-3 py-2 d-flex align-items-center gap-2">
                        <Spinner size="sm" />
                        <span>{t("Loading...")}</span>
                    </div>
                )}

                {!loading && error && (
                    <div className="px-3 py-2 text-danger small">
                        {t("Notifications unavailable (API missing or backend error)")}
                    </div>
                )}

                {!loading && !error && notifications.length === 0 && (
                    <div className="px-3 py-2 text-muted small">
                        {t("No notifications")}
                    </div>
                )}

                {!loading &&
                    !error &&
                    notifications.map((n) => (
                        <Dropdown.Item
                            key={n.id}
                            className={`py-2 ${n.read ? "" : "fw-bold"}`}
                            onClick={() => markRead(n.id)}
                        >
                            <div className="d-flex justify-content-between">
                                <span>{n.title || t("Notification")}</span>
                                <small className="text-muted">
                                    {safeDateLabel(n.createdAt || n.created_at)}
                                </small>
                            </div>
                            {n.message && (
                                <div className="small text-muted">{n.message}</div>
                            )}
                        </Dropdown.Item>
                    ))}
            </Dropdown.Menu>
        </Dropdown>
    );
}
