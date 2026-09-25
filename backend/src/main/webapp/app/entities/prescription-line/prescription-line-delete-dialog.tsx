import React, { useEffect, useState } from 'react';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { deleteEntity, getEntity } from './prescription-line.reducer';

export const PrescriptionLineDeleteDialog = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getEntity(id!));
    setLoadModal(true);
  }, []);

  const prescriptionLineEntity = useAppSelector(state => state.prescriptionLine.entity);
  const updateSuccess = useAppSelector(state => state.prescriptionLine.updateSuccess);

  const handleClose = () => {
    navigate('/prescription-line');
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(deleteEntity(prescriptionLineEntity.id));
  };

  return (
    <Modal show onHide={handleClose}>
      <ModalHeader data-cy="prescriptionLineDeleteDialogHeading" closeButton>
        Confirm delete operation
      </ModalHeader>
      <ModalBody id="hmsApp.prescriptionLine.delete.question">
        Are you sure you want to delete Prescription Line {prescriptionLineEntity.id}?
      </ModalBody>
      <ModalFooter>
        <Button variant="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button id="jhi-confirm-delete-prescriptionLine" data-cy="entityConfirmDeleteButton" variant="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Delete
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default PrescriptionLineDeleteDialog;
