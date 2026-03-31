import React, { createContext, useContext, useState, useCallback, useEffect, useRef } from 'react';
import { librepm } from '@api/librepm.js';

const DataContext = createContext(null);

/**
 * DataProvider — Global data cache for the current project.
 * Ensures cross-view consistency: editing a task in Kanban updates Planner, Gantt, etc.
 * PRD-10-BR-002
 */
export function DataProvider({ projectId, userId, children }) {
    const [tasks, setTasks] = useState([]);
    const [notes, setNotes] = useState([]);
    const [deliverables, setDeliverables] = useState([]);
    const [loading, setLoading] = useState(false);
    const currentProjectRef = useRef(projectId);

    // Refresh all data for the current project
    const refreshAll = useCallback(async () => {
        if (!projectId || !userId) return;
        setLoading(true);
        try {
            const [t, n, d] = await Promise.all([
                librepm.tasksList(userId, projectId).catch(() => []),
                librepm.notesList(userId, projectId).catch(() => []),
                librepm.deliverablesList(userId, projectId).catch(() => []),
            ]);
            if (currentProjectRef.current === projectId) {
                setTasks(t || []);
                setNotes(n || []);
                setDeliverables(d || []);
            }
        } finally {
            setLoading(false);
        }
    }, [projectId, userId]);

    const refreshTasks = useCallback(async () => {
        if (!projectId || !userId) return;
        try {
            const t = await librepm.tasksList(userId, projectId);
            if (currentProjectRef.current === projectId) setTasks(t || []);
        } catch { /* ignore */ }
    }, [projectId, userId]);

    const refreshNotes = useCallback(async () => {
        if (!projectId || !userId) return;
        try {
            const n = await librepm.notesList(userId, projectId);
            if (currentProjectRef.current === projectId) setNotes(n || []);
        } catch { /* ignore */ }
    }, [projectId, userId]);

    // Update a single task in cache without full refresh
    const updateTaskInCache = useCallback((taskId, changes) => {
        setTasks(prev => prev.map(t => t.id === taskId ? { ...t, ...changes } : t));
    }, []);

    // Remove a task from cache
    const removeTaskFromCache = useCallback((taskId) => {
        setTasks(prev => prev.filter(t => t.id !== taskId));
    }, []);

    // Add a task to cache
    const addTaskToCache = useCallback((task) => {
        setTasks(prev => [...prev, task]);
    }, []);

    useEffect(() => {
        currentProjectRef.current = projectId;
        if (projectId) refreshAll();
    }, [projectId, refreshAll]);

    const value = {
        tasks, notes, deliverables, loading,
        refreshAll, refreshTasks, refreshNotes,
        updateTaskInCache, removeTaskFromCache, addTaskToCache,
    };

    return <DataContext.Provider value={value}>{children}</DataContext.Provider>;
}

export function useProjectData() {
    const ctx = useContext(DataContext);
    if (!ctx) {
        // Return safe defaults when used outside provider
        return {
            tasks: [], notes: [], deliverables: [], loading: false,
            refreshAll: () => {}, refreshTasks: () => {}, refreshNotes: () => {},
            updateTaskInCache: () => {}, removeTaskFromCache: () => {}, addTaskToCache: () => {},
        };
    }
    return ctx;
}
