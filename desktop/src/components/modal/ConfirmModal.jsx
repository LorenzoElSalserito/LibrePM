import React from 'react';
import { useTranslation } from 'react-i18next';
import Modal from './Modal';

function ConfirmModal({ title, message, onConfirm, onCancel }) {
  const { t } = useTranslation();
  return (
    <Modal title={title} onClose={onCancel}>
      <div className="modal-body">
        <p>{message}</p>
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-secondary" onClick={onCancel}>
          {t("Cancel")}
        </button>
        <button type="button" className="btn btn-primary" onClick={onConfirm} autoFocus>
          {t("Confirm")}
        </button>
      </div>
    </Modal>
  );
}

export default ConfirmModal;
