import { createContext, useContext, useState, useEffect, useCallback } from "react";
import { librepm } from "@api/librepm.js";

const WorkflowContext = createContext(null);

export function WorkflowProvider({ children }) {
    const [statuses, setStatuses] = useState([]);
    const [priorities, setPriorities] = useState([]);
    const [loaded, setLoaded] = useState(false);

    useEffect(() => {
        async function load() {
            try {
                const [s, p] = await Promise.all([
                    librepm.taskStatusesList(),
                    librepm.taskPrioritiesList(),
                ]);
                setStatuses(s || []);
                setPriorities(p || []);
            } catch (e) {
                console.error("[WorkflowContext] Failed to load statuses/priorities:", e);
            } finally {
                setLoaded(true);
            }
        }
        load();
    }, []);

    const getStatusById = useCallback((id) => statuses.find(s => s.id === id), [statuses]);
    const getStatusByName = useCallback((name) => statuses.find(s => s.name === name), [statuses]);
    const getPriorityById = useCallback((id) => priorities.find(p => p.id === id), [priorities]);
    const getPriorityByName = useCallback((name) => priorities.find(p => p.name === name), [priorities]);

    // Show a lightweight spinner while workflow data loads (avoids full UI flash)
    if (!loaded) return (
        <div className="d-flex justify-content-center align-items-center" style={{height: '100%'}}>
            <div className="spinner-border spinner-border-sm text-primary" />
        </div>
    );

    return (
        <WorkflowContext.Provider value={{
            statuses,
            priorities,
            loaded,
            getStatusById,
            getStatusByName,
            getPriorityById,
            getPriorityByName,
        }}>
            {children}
        </WorkflowContext.Provider>
    );
}

export function useWorkflow() {
    const ctx = useContext(WorkflowContext);
    if (!ctx) throw new Error("useWorkflow must be used within WorkflowProvider");
    return ctx;
}
