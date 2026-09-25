import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getBills } from 'app/entities/bill/bill.reducer';
import { BillLineSourceType } from 'app/shared/model/enumerations/bill-line-source-type.model';

import { createEntity, getEntity, reset, updateEntity } from './bill-line-item.reducer';

export const BillLineItemUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const bills = useAppSelector(state => state.bill.entities);
  const billLineItemEntity = useAppSelector(state => state.billLineItem.entity);
  const loading = useAppSelector(state => state.billLineItem.loading);
  const updating = useAppSelector(state => state.billLineItem.updating);
  const updateSuccess = useAppSelector(state => state.billLineItem.updateSuccess);
  const billLineSourceTypeValues = Object.keys(BillLineSourceType);

  const handleClose = () => {
    navigate('/bill-line-item');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBills({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    if (values.amount !== undefined && typeof values.amount !== 'number') {
      values.amount = Number(values.amount);
    }

    const entity = {
      ...billLineItemEntity,
      ...values,
      bill: bills.find(it => it.id.toString() === values.bill?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          sourceType: 'CONSULTATION',
          ...billLineItemEntity,
          bill: billLineItemEntity?.bill?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.billLineItem.home.createOrEditLabel" data-cy="BillLineItemCreateUpdateHeading">
            Create or edit a Bill Line Item
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="bill-line-item-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Description"
                id="bill-line-item-description"
                name="description"
                data-cy="description"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="Amount"
                id="bill-line-item-amount"
                name="amount"
                data-cy="amount"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField label="Source Type" id="bill-line-item-sourceType" name="sourceType" data-cy="sourceType" type="select">
                {billLineSourceTypeValues.map(billLineSourceType => (
                  <option value={billLineSourceType} key={billLineSourceType}>
                    {billLineSourceType}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField id="bill-line-item-bill" name="bill" data-cy="bill" label="Bill" type="select" required>
                <option value="" key="0" />
                {bills
                  ? bills.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/bill-line-item" replace variant="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default BillLineItemUpdate;
