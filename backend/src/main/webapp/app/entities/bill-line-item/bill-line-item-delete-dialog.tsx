import React, { useEffect, useState } from 'react';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { deleteEntity, getEntity } from './bill-line-item.reducer';

export const BillLineItemDeleteDialog = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getEntity(id!));
    setLoadModal(true);
  }, []);

  const billLineItemEntity = useAppSelector(state => state.billLineItem.entity);
  const updateSuccess = useAppSelector(state => state.billLineItem.updateSuccess);

  const handleClose = () => {
    navigate('/bill-line-item');
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(deleteEntity(billLineItemEntity.id));
  };

  return (
    <Modal show onHide={handleClose}>
      <ModalHeader data-cy="billLineItemDeleteDialogHeading" closeButton>
        Confirm delete operation
      </ModalHeader>
      <ModalBody id="hmsApp.billLineItem.delete.question">
        Are you sure you want to delete Bill Line Item {billLineItemEntity.id}?
      </ModalBody>
      <ModalFooter>
        <Button variant="secondary" onClick={handleClose}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancel
        </Button>
        <Button id="jhi-confirm-delete-billLineItem" data-cy="entityConfirmDeleteButton" variant="danger" onClick={confirmDelete}>
          <FontAwesomeIcon icon="trash" />
          &nbsp; Delete
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default BillLineItemDeleteDialog;
