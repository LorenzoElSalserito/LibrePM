import { useEffect } from "react";
import { createPortal } from "react-dom";

/**
 * Modal Component Globale
 * Utilizza React Portal per renderizzare la modale al livello del body,
 * evitando problemi di z-index e stacking context annidati.
 * 
 * @param {Object} props
 * @param {Function} props.onClose - Callback chiusura
 * @param {string} props.dialogClass - Classi aggiuntive per modal-dialog (es. modal-lg)
 * @param {Object} props.contentStyle - Stili inline per modal-content
 * @param {number} props.zIndex - Z-Index (default 1055)
 */
export default function Modal({ children, onClose, dialogClass = "", contentStyle = {}, zIndex = 1055 }) {
    
    // Gestione chiusura con ESC
    useEffect(() => {
        const handleEsc = (e) => {
            if (e.key === "Escape" && onClose) onClose();
        };
        window.addEventListener("keydown", handleEsc);
        return () => window.removeEventListener("keydown", handleEsc);
    }, [onClose]);

    // Renderizza direttamente nel body
    return createPortal(
        <div className="modal show d-block" style={{ backgroundColor: "rgba(0,0,0,0.5)", zIndex }} onClick={onClose}>
            <div className={`modal-dialog ${dialogClass}`} onClick={e => e.stopPropagation()}>
                <div className="modal-content" style={contentStyle}>
                    {children}
                </div>
            </div>
        </div>,
        document.body
    );
}
