import React, { useEffect, useRef } from 'react';
import './modal.css';

function Modal({ children, title, onClose }) {
  const modalRef = useRef(null);

  // Gestione chiusura con tasto 'Escape'
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  // Gestione focus trap (semplificata)
  useEffect(() => {
    const firstFocusableElement = modalRef.current?.querySelector(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    firstFocusableElement?.focus();
  }, []);

  return (
    <div className="modal-backdrop" onMouseDown={onClose}>
      <div
        ref={modalRef}
        className="modal-container"
        onMouseDown={(e) => e.stopPropagation()} // Evita chiusura se si clicca dentro il modale
        role="dialog"
        aria-modal="true"
      >
        {title && <h3 className="modal-title">{title}</h3>}
        {children}
      </div>
    </div>
  );
}

export default Modal;
