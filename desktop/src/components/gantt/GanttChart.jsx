import React, { useEffect, useRef, useLayoutEffect } from 'react';
import Gantt from 'frappe-gantt';
import { useTranslation } from 'react-i18next';
import 'frappe-gantt/dist/frappe-gantt.css';
import './GanttChart.css';

/**
 * GanttChart component using frappe-gantt (PRD-10-FR-004).
 *
 * Renders tasks with dependency arrows, critical path highlighting,
 * summary tasks, milestones, WBS codes, and configurable view modes.
 *
 * Layout: renders at its natural SVG height (no inner vertical scroll).
 * Only horizontal scroll is handled here; vertical scroll is delegated
 * to the parent page scroller (.jl-page).
 */
export default function GanttChart({ tasks, viewMode = 'Week', onTaskClick }) {
    const { t, i18n } = useTranslation();
    const ganttRef = useRef(null);
    const containerRef = useRef(null);
    const ganttInstance = useRef(null);

    // Scroll the container so that "today" is horizontally centered
    const scrollToToday = () => {
        const container = containerRef.current;
        if (!container) return;
        const todayEl = container.querySelector('.today-highlight');
        if (todayEl) {
            const todayX = parseFloat(todayEl.getAttribute('x')) || 0;
            const todayW = parseFloat(todayEl.getAttribute('width')) || 0;
            const center = todayX + todayW / 2;
            container.scrollLeft = center - container.clientWidth / 2;
        }
    };

    // Format date input preserving time when available.
    // Returns 'YYYY-MM-DD HH:mm' if time info exists, otherwise 'YYYY-MM-DD'.
    const formatDateStr = (dateInput) => {
        if (!dateInput) return null;

        // Date object → full datetime
        if (dateInput instanceof Date) {
            if (isNaN(dateInput.getTime())) return null;
            const y = dateInput.getFullYear();
            const m = String(dateInput.getMonth() + 1).padStart(2, '0');
            const d = String(dateInput.getDate()).padStart(2, '0');
            const hh = String(dateInput.getHours()).padStart(2, '0');
            const mm = String(dateInput.getMinutes()).padStart(2, '0');
            return `${y}-${m}-${d} ${hh}:${mm}`;
        }

        if (typeof dateInput === 'string') {
            // Already YYYY-MM-DD HH:mm
            if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}/.test(dateInput)) return dateInput.slice(0, 16);
            // ISO with T separator
            if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(dateInput)) return dateInput.slice(0, 16).replace('T', ' ');
            // Date-only string
            if (/^\d{4}-\d{2}-\d{2}$/.test(dateInput)) return dateInput;
            // Try parsing anything else
            const d = new Date(dateInput);
            if (!isNaN(d.getTime())) return formatDateStr(d);
        }
        return null;
    };

    useLayoutEffect(() => {
        if (!ganttRef.current) return;

        if (!tasks || !Array.isArray(tasks) || tasks.length === 0) {
            ganttRef.current.innerHTML = '';
            return;
        }

        const ganttTasks = [];
        const validTasksMap = new Map();

        // 1. Prepare and validate tasks
        tasks.forEach(task => {
            const startStr = formatDateStr(task.plannedStart || task.start);
            const endStr = formatDateStr(task.plannedFinish || task.end);

            if (startStr && endStr) {
                let finalStart = startStr;
                let finalEnd = endStr;
                if (finalEnd < finalStart) finalEnd = finalStart;

                let customClass = '';
                if (task.type === 'MILESTONE') customClass = 'bar-milestone';
                else if (task.type === 'SUMMARY_TASK') customClass = 'bar-summary';
                if (task.isCritical) customClass += ' bar-critical';

                const gTask = {
                    id: String(task.id),
                    name: task.wbsCode ? `${task.wbsCode} ${task.title}` : task.title,
                    start: finalStart,
                    end: finalEnd,
                    progress: Math.min(Math.max(task.progress || 0, 0), 100),
                    custom_class: customClass.trim(),
                    _originalDependencies: task.dependencies
                };

                ganttTasks.push(gTask);
                validTasksMap.set(String(task.id), task);
            }
        });

        if (ganttTasks.length === 0) {
            ganttRef.current.innerHTML = `<div class="text-center p-5 text-muted"><i class="bi bi-calendar-x fs-1 d-block mb-2"></i>${t('gantt.noTasks')}</div>`;
            return;
        }

        // 2. Resolve dependencies
        ganttTasks.forEach(gTask => {
            const originalDeps = gTask._originalDependencies;
            let depString = '';
            if (originalDeps && Array.isArray(originalDeps) && originalDeps.length > 0) {
                depString = originalDeps
                    .map(d => String(typeof d === 'object' ? d.predecessorId : d))
                    .filter(predId => validTasksMap.has(predId) && predId !== gTask.id)
                    .join(',');
            }
            gTask.dependencies = depString;
            delete gTask._originalDependencies;
        });

        // Clear previous content
        ganttRef.current.innerHTML = '';

        try {
            // 3. Initialize Gantt
            let lang = 'en';
            if (i18n.language) {
                const shortLang = i18n.language.substring(0, 2).toLowerCase();
                if (['es', 'ru', 'pt', 'fr', 'en', 'tr', 'zh', 'de', 'hu'].includes(shortLang)) {
                    lang = shortLang === 'pt' ? 'ptBr' : shortLang;
                }
                if (shortLang === 'it') lang = 'en';
            }

            ganttInstance.current = new Gantt(ganttRef.current, ganttTasks, {
                header_height: 50,
                column_width: viewMode === 'Day' ? 44 : viewMode === 'Month' ? 120 : 36,
                step: 24,
                view_modes: ['Quarter Day', 'Half Day', 'Day', 'Week', 'Month', 'Year'],
                bar_height: 30,
                bar_corner_radius: 4,
                arrow_curve: 5,
                padding: 18,
                view_mode: viewMode,
                date_format: 'YYYY-MM-DD',
                language: lang,
                popup_trigger: 'click',

                custom_popup_html: (task) => {
                   const originalTask = validTasksMap.get(task.id);
                   const statusLabel = originalTask?.statusName || '';
                   const isCritical = originalTask?.isCritical;
                   const typeLabel = originalTask?.type && originalTask.type !== 'TASK' ? t(originalTask.type) : '';

                   const fmtOpts = viewMode === 'Day'
                       ? { dateStyle: 'medium', timeStyle: 'short' }
                       : { dateStyle: 'medium' };
                   const startDate = task._start ? task._start.toLocaleString(undefined, fmtOpts) : task.start;
                   const endDate = task._end ? task._end.toLocaleString(undefined, fmtOpts) : task.end;

                   return `
                     <div class="gantt-popup-wrapper">
                       <div class="popup-header">
                         ${task.name}
                         ${isCritical ? `<span class="badge bg-danger ms-2" style="font-size: 0.7em;">CRITICAL</span>` : ''}
                       </div>
                       <div class="popup-content">
                         ${typeLabel ? `<div><strong>${t('Type')}:</strong> ${typeLabel}</div>` : ''}
                         <div><strong>${t('Start')}:</strong> ${startDate}</div>
                         <div><strong>${t('End')}:</strong> ${endDate}</div>
                         <div><strong>${t('Progress')}:</strong>
                           <div class="progress" style="height: 6px; width: 100px; display: inline-block; vertical-align: middle; margin-left: 5px;">
                             <div class="progress-bar" role="progressbar" style="width: ${task.progress}%" aria-valuenow="${task.progress}" aria-valuemin="0" aria-valuemax="100"></div>
                           </div>
                           <span class="ms-1">${task.progress}%</span>
                         </div>
                         ${statusLabel ? `<div><strong>${t('Status')}:</strong> ${statusLabel}</div>` : ''}
                       </div>
                     </div>
                   `;
                },

                on_click: (task) => {
                    const originalTask = validTasksMap.get(task.id);
                    if (originalTask && onTaskClick) onTaskClick(originalTask);
                },
            });

            // After render, scroll so today is centered
            requestAnimationFrame(() => scrollToToday());

        } catch (e) {
            console.error("Failed to initialize Gantt chart:", e);
            ganttRef.current.innerHTML = `<div class="text-danger p-3">Error initializing Gantt: ${e.message}</div>`;
        }

    }, [tasks, viewMode, onTaskClick, t, i18n.language]);

    return (
        <div className="gantt-outer" style={{ position: 'relative' }}>
            {/* Floating "Today" button */}
            <button
                onClick={scrollToToday}
                className="btn btn-sm btn-outline-primary shadow-sm"
                title={t('Today')}
                style={{
                    position: 'absolute',
                    top: 8,
                    right: 8,
                    zIndex: 5,
                    fontSize: '0.75rem',
                    padding: '2px 10px',
                    opacity: 0.85,
                }}
            >
                <i className="bi bi-calendar-event me-1"></i>{t('Today')}
            </button>
            <div
                ref={containerRef}
                className="gantt-container"
                style={{ overflowX: 'auto' }}
            >
                <svg
                    ref={ganttRef}
                    id="gantt"
                ></svg>
            </div>
        </div>
    );
}
