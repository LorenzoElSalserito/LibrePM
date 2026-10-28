import { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { librepm } from '../api/librepm.js';

export default function MessagesPage({ shell }) {
    const { t } = useTranslation();
    const projectId = shell.currentProject?.id;
    const [messages, setMessages] = useState([]);
    const [content, setContent] = useState('');
    const [error, setError] = useState('');
    const [sending, setSending] = useState(false);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const requestSequence = useRef(0);
    const activeProject = useRef(projectId);
    const activeFeed = useRef();
    activeFeed.current = `${projectId}:${page}`;
    activeProject.current = projectId;
    const load = useCallback(async () => {
        if (!projectId) return;
        const sequence = ++requestSequence.current;
        const feed = `${projectId}:${page}`;
        setLoading(true);
        try {
            const data = await librepm.messagesList(projectId, page);
            if (sequence !== requestSequence.current || activeFeed.current !== feed) return;
            setMessages([...data].reverse());
            setError('');
        } catch (e) { if (sequence === requestSequence.current && activeFeed.current === feed) setError(e.message); }
        finally { if (sequence === requestSequence.current && activeFeed.current === feed) setLoading(false); }
    }, [projectId, page]);
    useEffect(() => {
        setMessages([]);
        setContent('');
        setPage(0);
        setError('');
    }, [projectId]);
    useEffect(() => {
        load();
        const timer = setInterval(load, 10000);
        return () => clearInterval(timer);
    }, [load]);
    async function send(event) {
        event.preventDefault();
        if (!content.trim() || sending) return;
        setSending(true);
        try {
            await librepm.messagesSend(projectId, content.trim());
            if (activeProject.current !== projectId) return;
            setContent('');
            setPage(0);
            if (page === 0) await load();
        } catch (e) { if (activeProject.current === projectId) setError(e.message); }
        finally { setSending(false); }
    }
    const senderLabel = (message) => message.senderId === shell.currentUser?.id
        ? t('You')
        : message.senderName || t('Former member');
    return <section className="mx-auto" style={{ maxWidth: 960 }}>
        <div className="d-flex justify-content-between align-items-center gap-3 mb-4 flex-wrap">
            <div><h2>{t('Messages')}</h2><p className="text-muted mb-0">{t('Conversations shared within the project.')}</p></div>
            <select aria-label={t('Messages project')} className="form-select w-auto" value={projectId || ''}
                onChange={e => shell.setCurrentProject(shell.projects.find(p => p.id === e.target.value))}>
                <option value="" disabled>{t('Select project')}</option>
                {shell.projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
        </div>
        {!projectId ? <p role="status">{t('Select or create a project to get started.')}</p> : <>
            {error && <div className="alert alert-danger" role="alert">{error}<button className="btn btn-sm btn-outline-danger ms-3" onClick={load}>{t('Retry')}</button></div>}
            <div className="card p-4 mb-3" aria-busy={loading}>
                {!messages.length && !error && <p className="text-muted" role="status">{loading ? t('Loading…') : t('No messages yet. Start the conversation.')}</p>}
                <ol className="list-unstyled mb-0" aria-label={t('Project messages')}>
                    {messages.map(message => <li key={message.id} className="border-bottom py-3">
                        <div className="d-flex justify-content-between gap-3 small text-muted mb-2"><span className="fw-semibold">{senderLabel(message)}</span><time dateTime={message.createdAt}>{new Date(message.createdAt).toLocaleString()}</time></div>
                        <p className="mb-0" style={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }}>{message.content}</p>
                    </li>)}
                </ol>
            </div>
            <div className="d-flex gap-2 mb-3">
                <button className="btn btn-outline-secondary" disabled={messages.length < 50 || loading} onClick={() => setPage(page + 1)}>{t('Older')}</button>
                <button className="btn btn-outline-secondary" disabled={!page || loading} onClick={() => setPage(page - 1)}>{t('Newer')}</button>
                <button className="btn btn-outline-secondary ms-auto" onClick={load}>{t('Refresh')}</button>
            </div>
            <form onSubmit={send} className="card p-3">
                <label htmlFor="message-content" className="form-label">{t('New message')}</label>
                <textarea id="message-content" className="form-control mb-3" rows={3} maxLength={4000} required value={content} onChange={e => setContent(e.target.value)} />
                <div className="d-flex justify-content-between align-items-center"><small className="text-muted">{content.length}/4000</small><button className="btn btn-primary" disabled={sending || !content.trim()}>{sending ? t('Sending…') : t('Send message')}</button></div>
            </form>
        </>}
    </section>;
}
