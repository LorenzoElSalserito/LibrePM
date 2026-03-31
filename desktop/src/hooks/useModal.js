import { useContext } from 'react';
import { ModalContext } from '../context/ModalContext';
import ConfirmModal from '../components/modal/ConfirmModal';

export const useModal = () => {
  const context = useContext(ModalContext);

  if (context === null) {
    throw new Error('useModal must be used within a ModalProvider.');
  }
  
  const { showModal, hideModal, isModalOpen } = context;

  const confirm = (props) => {
    return new Promise((resolve) => {
      showModal(ConfirmModal, {
        ...props,
        onConfirm: () => {
          hideModal();
          resolve(true);
        },
        onCancel: () => {
          hideModal();
          resolve(false);
        },
      });
    });
  };

  return {
    showModal,
    hideModal,
    isModalOpen,
    confirm,
    // You can add other modal types here, e.g.: alert, prompt, etc.
  };
};
