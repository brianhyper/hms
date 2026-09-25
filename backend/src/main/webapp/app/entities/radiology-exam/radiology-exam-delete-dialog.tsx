import React, { useEffect, useState } from 'react';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { deleteEntity, getEntity } from './radiology-exam.reducer';

export const RadiologyExamDeleteDialog = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getEntity(id!));
    setLoadModal(true);
  }, []);

  const radiologyExamEntity = useAppSelector(state => state.radiologyExam.entity);
  const updateSuccess = useAppSelector(state => state.radiologyExam.updateSuccess);

  const handleClose = () => {
    navigate('/radiology-exam');
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(deleteEntity(radiologyExamEntity.id));
  };

  return (
    <Modal show onHide={handleClose}>
      <ModalHeader data-cy="radiologyExamDeleteDialogHeading" closeButton>
        Confirm delete operation
      </ModalHeader>
      <ModalBody id="hmsApp.radiologyExam.delete.question">
        Are you sure you want to delete Radiology Exam {radiologyExamEntity.id}?
      </ModalBody>
      <ModalFooter>
        <Button variant="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button id="jhi-confirm-delete-radiologyExam" data-cy="entityConfirmDeleteButton" variant="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Delete
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default RadiologyExamDeleteDialog;
