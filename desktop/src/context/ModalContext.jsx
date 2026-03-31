import React, { createContext, useState, useCallback, useRef } from 'react';

export const ModalContext = createContext(null);

export function ModalProvider({ children }) {
  const [modal, setModal] = useState(null);
  const previousFocusElement = useRef(null); // <-- Per salvare il focus

  const showModal = useCallback((Component, props = {}) => {
    previousFocusElement.current = document.activeElement; // Salva l'elemento attivo
    setModal({ Component, props });
  }, []);

  const hideModal = useCallback(() => {
    setModal(null);
    previousFocusElement.current?.focus(); // Ripristina il focus
  }, []);

  const value = { showModal, hideModal, isModalOpen: modal !== null };

  return (
    <ModalContext.Provider value={value}>
      {children}
      {modal && <modal.Component {...modal.props} />}
    </ModalContext.Provider>
  );
}
