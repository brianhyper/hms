import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getPayments } from 'app/entities/payment/payment.reducer';
import { BillStatus } from 'app/shared/model/enumerations/bill-status.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './bill.reducer';

export const BillUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const payments = useAppSelector(state => state.payment.entities);
  const billEntity = useAppSelector(state => state.bill.entity);
  const loading = useAppSelector(state => state.bill.loading);
  const updating = useAppSelector(state => state.bill.updating);
  const updateSuccess = useAppSelector(state => state.bill.updateSuccess);
  const billStatusValues = Object.keys(BillStatus);

  const handleClose = () => {
    navigate(`/bill${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPayments({}));
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
    if (values.totalAmount !== undefined && typeof values.totalAmount !== 'number') {
      values.totalAmount = Number(values.totalAmount);
    }
    values.paidAt = convertDateTimeToServer(values.paidAt);

    const entity = {
      ...billEntity,
      ...values,
      payment: payments.find(it => it.id.toString() === values.payment?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          paidAt: displayDefaultDateTime(),
        }
      : {
          status: 'UNPAID',
          ...billEntity,
          paidAt: convertDateTimeFromServer(billEntity.paidAt),
          payment: billEntity?.payment?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.bill.home.createOrEditLabel" data-cy="BillCreateUpdateHeading">
            Create or edit a Bill
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="bill-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Total Amount"
                id="bill-totalAmount"
                name="totalAmount"
                data-cy="totalAmount"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField label="Status" id="bill-status" name="status" data-cy="status" type="select">
                {billStatusValues.map(billStatus => (
                  <option value={billStatus} key={billStatus}>
                    {billStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Paid At"
                id="bill-paidAt"
                name="paidAt"
                data-cy="paidAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField id="bill-payment" name="payment" data-cy="payment" label="Payment" type="select">
                <option value="" key="0" />
                {payments
                  ? payments.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/bill" replace variant="info">
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

export default BillUpdate;
