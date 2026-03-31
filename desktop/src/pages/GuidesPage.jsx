import React, { useState, useEffect } from 'react';
import MDEditor from '@uiw/react-md-editor';
import { useTranslation } from 'react-i18next';

const GuidesPage = ({ shell }) => {
    const { t, i18n } = useTranslation();
    const [selectedLang, setSelectedLang] = useState(i18n.language || 'en');
    const [selectedGuide, setSelectedGuide] = useState('01-intro');
    const [markdownContent, setMarkdownContent] = useState('');

    // Definizione dei capitoli della guida
    const guides = [
        { id: '01-intro', label: t('01. Welcome & Basics') },
        { id: '02-progetti', label: t('02. Projects Management') },
        { id: '03-task', label: t('03. Tasks & Lists') },
        { id: '04-gantt', label: t('04. Advanced Planning (Gantt)') },
        { id: '05-conoscenza', label: t('05. Knowledge & Backup') },
        { id: '06-team', label: t('06. Team & Collaboration') },
        { id: '07-calendario', label: t('07. Calendar') },
        { id: '08-charter', label: t('08. Executive Dashboard & Charter') },
        { id: '09-risorse', label: t('09. Resources & Workload') },
        { id: '10-template', label: t('10. Templates & Gallery') },
        { id: '11-analytics', label: t('11. Analytics') },
        { id: '12-integrazioni', label: t('12. Integrations') },
        { id: '13-impostazioni', label: t('13. Settings & Personalization') },
        { id: '14-finanza', label: t('14. Project Finance') },
        { id: '15-bandi', label: t('15. Grants & Stakeholders') },
        { id: '16-controllo-modifiche', label: t('16. Change Control & Branches') },
        { id: '17-portfolio', label: t('17. Portfolio Management') },
        { id: '18-conformita', label: t('18. Privacy & Compliance') },
    ];

    useEffect(() => {
        shell?.setTitle?.(t('Guides & Documentation'));
        shell?.setHeaderActions?.(null);

        return () => {
            shell?.setHeaderActions?.(null);
        };
    }, [shell, t]);

    useEffect(() => {
        // If the app language changes, also update the local selector (if not manually changed)
        // This is optional, but improves UX.
        // setSelectedLang(i18n.language);
        // Note: we let the user choose the guide language independently from the UI.
    }, [i18n.language]);

    useEffect(() => {
        const fetchGuide = async () => {
            try {
                let content = '';
                try {
                    // Dynamic loading based on language and chapter ID
                    if (selectedLang.startsWith('it')) {
                        switch (selectedGuide) {
                            case '01-intro': content = (await import('../docs/it/01-intro.md?raw')).default; break;
                            case '02-progetti': content = (await import('../docs/it/02-progetti.md?raw')).default; break;
                            case '03-task': content = (await import('../docs/it/03-task.md?raw')).default; break;
                            case '04-gantt': content = (await import('../docs/it/04-gantt.md?raw')).default; break;
                            case '05-conoscenza': content = (await import('../docs/it/05-conoscenza.md?raw')).default; break;
                            case '06-team': content = (await import('../docs/it/06-team.md?raw')).default; break;
                            case '07-calendario': content = (await import('../docs/it/07-calendario.md?raw')).default; break;
                            case '08-charter': content = (await import('../docs/it/08-charter.md?raw')).default; break;
                            case '09-risorse': content = (await import('../docs/it/09-risorse.md?raw')).default; break;
                            case '10-template': content = (await import('../docs/it/10-template.md?raw')).default; break;
                            case '11-analytics': content = (await import('../docs/it/11-analytics.md?raw')).default; break;
                            case '12-integrazioni': content = (await import('../docs/it/12-integrazioni.md?raw')).default; break;
                            case '13-impostazioni': content = (await import('../docs/it/13-impostazioni.md?raw')).default; break;
                            case '14-finanza': content = (await import('../docs/it/14-finanza.md?raw')).default; break;
                            case '15-bandi': content = (await import('../docs/it/15-bandi.md?raw')).default; break;
                            case '16-controllo-modifiche': content = (await import('../docs/it/16-controllo-modifiche.md?raw')).default; break;
                            case '17-portfolio': content = (await import('../docs/it/17-portfolio.md?raw')).default; break;
                            case '18-conformita': content = (await import('../docs/it/18-conformita.md?raw')).default; break;
                            default: content = (await import('../docs/it/01-intro.md?raw')).default;
                        }
                    } else {
                        // English version
                        switch (selectedGuide) {
                            case '01-intro': content = (await import('../docs/en/01-intro.md?raw')).default; break;
                            case '02-progetti': content = (await import('../docs/en/02-projects.md?raw')).default; break;
                            case '03-task': content = (await import('../docs/en/03-task.md?raw')).default; break;
                            case '04-gantt': content = (await import('../docs/en/04-gantt.md?raw')).default; break;
                            case '05-conoscenza': content = (await import('../docs/en/05-knowledge.md?raw')).default; break;
                            case '06-team': content = (await import('../docs/en/06-team.md?raw')).default; break;
                            case '07-calendario': content = (await import('../docs/en/07-calendar.md?raw')).default; break;
                            case '08-charter': content = (await import('../docs/en/08-charter.md?raw')).default; break;
                            case '09-risorse': content = (await import('../docs/en/09-resources.md?raw')).default; break;
                            case '10-template': content = (await import('../docs/en/10-templates.md?raw')).default; break;
                            case '11-analytics': content = (await import('../docs/en/11-analytics.md?raw')).default; break;
                            case '12-integrazioni': content = (await import('../docs/en/12-integrations.md?raw')).default; break;
                            case '13-impostazioni': content = (await import('../docs/en/13-settings.md?raw')).default; break;
                            case '14-finanza': content = (await import('../docs/en/14-finance.md?raw')).default; break;
                            case '15-bandi': content = (await import('../docs/en/15-grants.md?raw')).default; break;
                            case '16-controllo-modifiche': content = (await import('../docs/en/16-change-control.md?raw')).default; break;
                            case '17-portfolio': content = (await import('../docs/en/17-portfolio.md?raw')).default; break;
                            case '18-conformita': content = (await import('../docs/en/18-compliance.md?raw')).default; break;
                            default: content = (await import('../docs/en/01-intro.md?raw')).default;
                        }
                    }
                } catch (err) {
                    console.error("Error loading guide:", err);
                    content = "# Guide not found\nSorry, the requested guide could not be loaded.";
                }

                setMarkdownContent(content);
            } catch (error) {
                console.error('Failed to load guide:', error);
                setMarkdownContent('# Error\nFailed to load guide content.');
            }
        };

        fetchGuide();
    }, [selectedLang, selectedGuide]);

    return (
        <div className="container-fluid p-4 h-100 d-flex flex-column">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="mb-0">{t('Documentation')}</h2>
                <div className="d-flex gap-3">
                    <select
                        className="form-select"
                        value={selectedLang}
                        onChange={(e) => setSelectedLang(e.target.value)}
                        style={{ width: '150px' }}
                    >
                        <option value="en">English</option>
                        <option value="it">Italiano</option>
                    </select>

                    <select
                        className="form-select"
                        value={selectedGuide}
                        onChange={(e) => setSelectedGuide(e.target.value)}
                        style={{ width: '350px' }}
                    >
                        {guides.map(g => (
                            <option key={g.id} value={g.id}>{g.label}</option>
                        ))}
                    </select>
                </div>
            </div>

            <div className="card flex-grow-1 shadow-sm" style={{ overflow: 'hidden' }}>
                <div className="card-body h-100 p-0" style={{ overflowY: 'auto' }}>
                    <div className="p-4" data-color-mode="light">
                        <MDEditor.Markdown source={markdownContent} style={{ whiteSpace: 'pre-wrap' }} />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default GuidesPage;
