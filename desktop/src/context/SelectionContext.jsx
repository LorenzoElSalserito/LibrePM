import React, { createContext, useState, useContext, useMemo } from 'react';

/**
 * SelectionContext (PRD-10)
 * 
 * Provides a shared context for managing UI selection state across different views
 * (e.g., List, Board, Gantt). This ensures a consistent user experience when
 * switching between views.
 * 
 * It manages:
 * - The currently selected project.
 * - The currently selected task.
 * - The current view mode (e.g., 'list', 'board').
 * - Shared filters.
 */
const SelectionContext = createContext(null);

export function SelectionProvider({ children }) {
    const [currentProjectId, setCurrentProjectId] = useState(null);
    const [selectedTaskId, setSelectedTaskId] = useState(null);
    const [currentView, setCurrentView] = useState('list'); // 'list', 'board', 'gantt', 'timeline'
    const [filters, setFilters] = useState({
        searchTerm: '',
        status: null,
        priority: null,
        tags: [],
    });

    const value = useMemo(() => ({
        // Project
        currentProjectId,
        setCurrentProjectId,
        
        // Task
        selectedTaskId,
        setSelectedTaskId,

        // View
        currentView,
        setCurrentView,

        // Filters
        filters,
        setFilters,
        
        // Helper to update a single filter
        setFilter: (key, value) => {
            setFilters(prev => ({ ...prev, [key]: value }));
        },

        // Helper to reset all filters
        resetFilters: () => {
            setFilters({
                searchTerm: '',
                status: null,
                priority: null,
                tags: [],
            });
        }
    }), [currentProjectId, selectedTaskId, currentView, filters]);

    return (
        <SelectionContext.Provider value={value}>
            {children}
        </SelectionContext.Provider>
    );
}

/**
 * Custom hook to easily access the SelectionContext.
 */
export function useSelection() {
    const context = useContext(SelectionContext);
    if (!context) {
        throw new Error('useSelection must be used within a SelectionProvider');
    }
    return context;
}
