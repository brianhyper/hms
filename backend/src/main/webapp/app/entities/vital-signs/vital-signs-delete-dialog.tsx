import React, { useEffect, useState } from 'react';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { deleteEntity, getEntity } from './vital-signs.reducer';

export const VitalSignsDeleteDialog = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getEntity(id!));
    setLoadModal(true);
  }, []);

  const vitalSignsEntity = useAppSelector(state => state.vitalSigns.entity);
  const updateSuccess = useAppSelector(state => state.vitalSigns.updateSuccess);

  const handleClose = () => {
    navigate('/vital-signs');
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(deleteEntity(vitalSignsEntity.id));
  };

  return (
    <Modal show onHide={handleClose}>
      <ModalHeader data-cy="vitalSignsDeleteDialogHeading" closeButton>
        Confirm delete operation
      </ModalHeader>
      <ModalBody id="hmsApp.vitalSigns.delete.question">Are you sure you want to delete Vital Signs {vitalSignsEntity.id}?</ModalBody>
      <ModalFooter>
        <Button variant="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button id="jhi-confirm-delete-vitalSigns" data-cy="entityConfirmDeleteButton" variant="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Delete
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default VitalSignsDeleteDialog;
