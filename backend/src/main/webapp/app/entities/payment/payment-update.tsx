import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { PaymentConfirmationStatus } from 'app/shared/model/enumerations/payment-confirmation-status.model';
import { PaymentMethod } from 'app/shared/model/enumerations/payment-method.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './payment.reducer';

export const PaymentUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const paymentEntity = useAppSelector(state => state.payment.entity);
  const loading = useAppSelector(state => state.payment.loading);
  const updating = useAppSelector(state => state.payment.updating);
  const updateSuccess = useAppSelector(state => state.payment.updateSuccess);
  const paymentMethodValues = Object.keys(PaymentMethod);
  const paymentConfirmationStatusValues = Object.keys(PaymentConfirmationStatus);

  const handleClose = () => {
    navigate(`/payment${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUsers({}));
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
    values.recordedAt = convertDateTimeToServer(values.recordedAt);

    const entity = {
      ...paymentEntity,
      ...values,
      recordedBy: users.find(it => it.id.toString() === values.recordedBy?.toString()),
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
          recordedAt: displayDefaultDateTime(),
        }
      : {
          method: 'CASH',
          confirmationStatus: 'PENDING',
          ...paymentEntity,
          recordedAt: convertDateTimeFromServer(paymentEntity.recordedAt),
          recordedBy: paymentEntity?.recordedBy?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.payment.home.createOrEditLabel" data-cy="PaymentCreateUpdateHeading">
            Create or edit a Payment
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="payment-id" label="ID" validate={{ required: true }} />}
              <ValidatedField label="Method" id="payment-method" name="method" data-cy="method" type="select">
                {paymentMethodValues.map(paymentMethod => (
                  <option value={paymentMethod} key={paymentMethod}>
                    {paymentMethod}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Mpesa Reference"
                id="payment-mpesaReference"
                name="mpesaReference"
                data-cy="mpesaReference"
                type="text"
                validate={{
                  maxLength: { value: 100, message: 'This field cannot be longer than 100 characters.' },
                }}
              />
              <ValidatedField
                label="Insurer Name"
                id="payment-insurerName"
                name="insurerName"
                data-cy="insurerName"
                type="text"
                validate={{
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <ValidatedField
                label="Confirmation Status"
                id="payment-confirmationStatus"
                name="confirmationStatus"
                data-cy="confirmationStatus"
                type="select"
              >
                {paymentConfirmationStatusValues.map(paymentConfirmationStatus => (
                  <option value={paymentConfirmationStatus} key={paymentConfirmationStatus}>
                    {paymentConfirmationStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Receipt Number"
                id="payment-receiptNumber"
                name="receiptNumber"
                data-cy="receiptNumber"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 64, message: 'This field cannot be longer than 64 characters.' },
                }}
              />
              <ValidatedField
                label="Amount"
                id="payment-amount"
                name="amount"
                data-cy="amount"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                label="Recorded At"
                id="payment-recordedAt"
                name="recordedAt"
                data-cy="recordedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField id="payment-recordedBy" name="recordedBy" data-cy="recordedBy" label="Recorded By" type="select" required>
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/payment" replace variant="info">
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

export default PaymentUpdate;
