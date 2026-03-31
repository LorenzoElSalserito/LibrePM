/**
 * TopHeader - Barra superiore dell'applicazione
 *
 * Features:
 * - Titolo pagina dinamico
 * - Slot per azioni custom
 * - Responsive
 *
 * @author Lorenzo DM
 * @since 0.2.0
 */
export default function TopHeader({ title, actions }) {
    return (
        <header className="jl-header">
            <div className="jl-header-title">
                <h1>{title}</h1>
            </div>

            <div className="jl-header-actions">
                {actions}
            </div>

            <style>{`
                .jl-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 0.75rem 1.5rem;
                    background: var(--jl-bg-secondary);
                    border-bottom: 1px solid var(--jl-border-color);
                    min-height: 60px;
                    color: var(--jl-text-primary);
                    transition: background-color 0.3s ease, border-color 0.3s ease;
                }
                
                .jl-header-title h1 {
                    margin: 0;
                    font-size: 1.25rem;
                    font-weight: 600;
                    color: var(--jl-text-primary);
                }
                
                .jl-header-actions {
                    display: flex;
                    align-items: center;
                    gap: 0.75rem;
                }
                
                @media (max-width: 768px) {
                    .jl-header {
                        padding: 0.5rem 1rem;
                    }
                    
                    .jl-header-title h1 {
                        font-size: 1rem;
                    }
                }
            `}</style>
        </header>
    );
}
