import React, { useState, useEffect } from 'react';
import { librepm } from '@api/librepm.js';
import { Tooltip } from 'react-tooltip';
import { useTranslation } from 'react-i18next';

const FocusHeatmapWidget = ({ projectId, range = 365 }) => {
    const { t } = useTranslation();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadData();
    }, [projectId, range]);

    const loadData = async () => {
        try {
            setLoading(true);
            const response = await librepm.analyticsFocusHeatmap(projectId, range);
            setData(response.data);
        } catch (err) {
            console.error('Error loading heatmap:', err);
        } finally {
            setLoading(false);
        }
    };

    // Helper to generate the grid (GitHub style)
    // Simplification: we only show the last 52 weeks (or fewer)
    // Each cell is a day.
    
    const getColor = (minutes) => {
        if (minutes === 0) return '#ebedf0'; // Light grey
        if (minutes < 30) return '#9be9a8'; // Light green
        if (minutes < 60) return '#40c463';
        if (minutes < 120) return '#30a14e';
        return '#216e39'; // Dark green
    };

    if (loading) return <div className="text-center p-3">{t("Loading heatmap...")}</div>;

    // Transform the data into a map for fast access
    const dataMap = new Map(data.map(d => [d.date, d.totalMinutes]));

    // Generate dates for the last year
    const today = new Date();
    const dates = [];
    for (let i = 364; i >= 0; i--) {
        const d = new Date(today);
        d.setDate(d.getDate() - i);
        dates.push(d);
    }

    // Group by weeks (columns)
    const weeks = [];
    let currentWeek = [];
    
    // Align to the day of the week (Sunday start)
    const firstDay = dates[0].getDay();
    for(let i=0; i<firstDay; i++) currentWeek.push(null); // Initial padding

    dates.forEach(date => {
        currentWeek.push(date);
        if (currentWeek.length === 7) {
            weeks.push(currentWeek);
            currentWeek = [];
        }
    });
    if (currentWeek.length > 0) weeks.push(currentWeek);

    return (
        <div className="d-flex flex-column align-items-center w-100 overflow-auto">
            <div className="d-flex gap-1">
                {weeks.map((week, wIndex) => (
                    <div key={wIndex} className="d-flex flex-column gap-1">
                        {week.map((date, dIndex) => {
                            if (!date) return <div key={dIndex} style={{ width: 12, height: 12 }} />;
                            
                            const dateStr = date.toISOString().split('T')[0];
                            const minutes = dataMap.get(dateStr) || 0;
                            
                            return (
                                <div
                                    key={dateStr}
                                    data-tooltip-id="heatmap-tooltip"
                                    data-tooltip-content={`${dateStr}: ${minutes} min`}
                                    style={{
                                        width: 12,
                                        height: 12,
                                        backgroundColor: getColor(minutes),
                                        borderRadius: 2,
                                        cursor: 'pointer'
                                    }}
                                />
                            );
                        })}
                    </div>
                ))}
            </div>
            <Tooltip id="heatmap-tooltip" />
            
            <div className="d-flex align-items-center gap-2 mt-2 text-muted small">
                <span>{t("Less")}</span>
                <div style={{width: 10, height: 10, background: '#ebedf0'}}></div>
                <div style={{width: 10, height: 10, background: '#9be9a8'}}></div>
                <div style={{width: 10, height: 10, background: '#40c463'}}></div>
                <div style={{width: 10, height: 10, background: '#30a14e'}}></div>
                <div style={{width: 10, height: 10, background: '#216e39'}}></div>
                <span>{t("More")}</span>
            </div>
        </div>
    );
};

export default FocusHeatmapWidget;
