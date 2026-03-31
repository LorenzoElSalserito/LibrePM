import React, { useState, useEffect } from 'react';
import { librepm } from '@api/librepm.js';
import { Bar } from 'react-chartjs-2';
import { useTranslation } from 'react-i18next';

const EstimatesWidget = ({ projectId }) => {
    const { t } = useTranslation();
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadData();
    }, [projectId]);

    const loadData = async () => {
        try {
            setLoading(true);
            const response = await librepm.analyticsEstimates(projectId);
            setData(response);
        } catch (err) {
            console.error('Error loading estimates:', err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="text-center p-3">{t("Loading estimates...")}</div>;
    if (!data || !data.projectAnalytics || data.projectAnalytics.length === 0) return <div className="text-center p-3 text-muted">{t("No estimate data available.")}</div>;

    // Prepare data for the chart
    // If projectId is selected, show task detail (top outliers)
    // If global, show aggregate by project
    
    let chartData;
    let options = {
        responsive: true,
        plugins: {
            legend: { position: 'top' },
            title: { display: false }
        }
    };

    if (projectId) {
        // Show Top 5 Underestimated (took longer than expected)
        const items = data.topUnderestimated ? data.topUnderestimated.slice(0, 5) : [];
        chartData = {
            labels: items.map(t => t.taskTitle.substring(0, 20) + '...'),
            datasets: [
                {
                    label: t("Estimated (min)"),
                    data: items.map(t => t.estimatedMinutes),
                    backgroundColor: 'rgba(53, 162, 235, 0.5)',
                },
                {
                    label: t("Actual (min)"),
                    data: items.map(t => t.actualMinutes),
                    backgroundColor: 'rgba(255, 99, 132, 0.5)',
                }
            ]
        };
    } else {
        // Show aggregate by project
        const items = data.projectAnalytics.slice(0, 5);
        chartData = {
            labels: items.map(p => p.projectName),
            datasets: [
                {
                    label: t("Average Deviation (%)"),
                    data: items.map(p => p.deviationPercentage),
                    backgroundColor: items.map(p => p.deviationPercentage > 0 ? 'rgba(255, 99, 132, 0.5)' : 'rgba(75, 192, 192, 0.5)'),
                }
            ]
        };
    }

    const deviation = data.globalDeviation || 0;

    return (
        <div>
            <div className="mb-3 text-center">
                <h3 className={deviation > 0 ? "text-danger" : "text-success"}>
                    {deviation > 0 ? "+" : ""}{deviation.toFixed(1)}%
                </h3>
                <p className="text-muted small">{t("Global average deviation")}</p>
            </div>
            <Bar options={options} data={chartData} />
        </div>
    );
};

export default EstimatesWidget;
