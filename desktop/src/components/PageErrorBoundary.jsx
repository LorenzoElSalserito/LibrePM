import { Component } from 'react';

export default class PageErrorBoundary extends Component {
    state = { error: null };
    static getDerivedStateFromError(error) { return { error }; }
    componentDidCatch(error, info) { console.error('[LibrePM page]', error, info); }
    render() {
        if (!this.state.error) return this.props.children;
        return <section className="card m-4 p-4" role="alert">
            <h2>Pagina non disponibile</h2>
            <p>{this.state.error.message}</p>
            <button className="btn btn-primary align-self-start" onClick={() => this.setState({ error: null })}>Riprova</button>
        </section>;
    }
}
