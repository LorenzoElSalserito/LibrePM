import React, { useState, useEffect, useRef } from 'react';
import { librepm } from '@api/librepm.js';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

export default function NotificationBell() {
    const { t } = useTranslation();
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifications, setNotifications] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const dropdownRef = useRef(null);

    // Polling for the count
    useEffect(() => {
        const fetchCount = async () => {
            try {
                if (librepm.hasCurrentUser()) {
                    const count = await librepm.notificationsCount();
                    setUnreadCount(count);
                }
            } catch (e) {
                console.error("Error fetching notifications:", e);
            }
        };

        fetchCount();
        const interval = setInterval(fetchCount, 30000); // 30 seconds

        return () => clearInterval(interval);
    }, []);

    // Close dropdown if clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const toggleDropdown = async () => {
        if (!isOpen) {
            setIsOpen(true);
            loadNotifications();
        } else {
            setIsOpen(false);
        }
    };

    const loadNotifications = async () => {
        try {
            setLoading(true);
            const list = await librepm.notificationsUnread();
            setNotifications(list);
        } catch (e) {
            console.error("Error loading notification list:", e);
        } finally {
            setLoading(false);
        }
    };

    const markAsRead = async (id) => {
        try {
            await librepm.notificationsMarkRead(id);
            setNotifications(prev => prev.filter(n => n.id !== id));
            setUnreadCount(prev => Math.max(0, prev - 1));
        } catch (e) {
            toast.error(t("Error"));
        }
    };

    const markAllRead = async () => {
        try {
            await librepm.notificationsMarkAllRead();
            setNotifications([]);
            setUnreadCount(0);
            setIsOpen(false);
            toast.success(t("All notifications marked as read"));
        } catch (e) {
            toast.error(t("Error"));
        }
    };

    const getNotificationIcon = (type) => {
        switch (type) {
            case 'NEW_NOTE':
                return <i className="bi bi-chat-square-text text-primary fs-5"></i>;
            case 'CONNECTION_REQUEST':
                return <i className="bi bi-person-plus text-warning fs-5"></i>;
            case 'CONNECTION_ACCEPTED':
                return <i className="bi bi-check-circle text-success fs-5"></i>;
            default:
                return <i className="bi bi-info-circle text-secondary fs-5"></i>;
        }
    };

    const getNotificationTitle = (type) => {
        switch (type) {
            case 'NEW_NOTE':
                return t("New comment");
            case 'CONNECTION_REQUEST':
                return t("Incoming Requests");
            case 'CONNECTION_ACCEPTED':
                return t("Success");
            default:
                return t("Notification");
        }
    };

    return (
        <div className="position-relative" ref={dropdownRef}>
            <button 
                className="btn btn-link text-dark position-relative p-2" 
                onClick={toggleDropdown}
                title={t("Notifications")}
            >
                <i className="bi bi-bell fs-5"></i>
                {unreadCount > 0 && (
                    <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style={{ fontSize: '0.6rem' }}>
                        {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                )}
            </button>

            {isOpen && (
                <div className="card shadow position-absolute end-0 mt-2" style={{ width: '320px', zIndex: 1050, maxHeight: '400px', overflow: 'hidden' }}>
                    <div className="card-header bg-white d-flex justify-content-between align-items-center py-2">
                        <h6 className="mb-0 fw-bold small">{t("Notifications")}</h6>
                        {notifications.length > 0 && (
                            <button className="btn btn-link btn-sm p-0 text-decoration-none small" onClick={markAllRead}>
                                {t("Mark all as read")}
                            </button>
                        )}
                    </div>
                    <div className="list-group list-group-flush overflow-auto" style={{ maxHeight: '350px' }}>
                        {loading ? (
                            <div className="p-3 text-center text-muted small">{t("Loading...")}</div>
                        ) : notifications.length === 0 ? (
                            <div className="p-3 text-center text-muted small">{t("No new notifications")}</div>
                        ) : (
                            notifications.map(n => (
                                <div key={n.id} className="list-group-item list-group-item-action p-3">
                                    <div className="d-flex w-100 justify-content-between mb-1 align-items-center">
                                        <div className="d-flex align-items-center gap-2">
                                            {getNotificationIcon(n.type)}
                                            <small className="fw-bold">{getNotificationTitle(n.type)}</small>
                                        </div>
                                        <small className="text-muted" style={{fontSize: '0.7rem'}}>
                                            {new Date(n.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                                        </small>
                                    </div>
                                    <p className="mb-1 small text-break ps-4">{n.message}</p>
                                    <div className="mt-2 text-end">
                                        <button className="btn btn-sm btn-outline-secondary py-0 px-2" style={{fontSize: '0.7rem'}} onClick={(e) => { e.stopPropagation(); markAsRead(n.id); }}>
                                            {t("Mark as read")}
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
