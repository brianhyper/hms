import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getPrescriptions } from 'app/entities/prescription/prescription.reducer';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './dispense.reducer';

export const DispenseUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const prescriptions = useAppSelector(state => state.prescription.entities);
  const users = useAppSelector(state => state.userManagement.users);
  const dispenseEntity = useAppSelector(state => state.dispense.entity);
  const loading = useAppSelector(state => state.dispense.loading);
  const updating = useAppSelector(state => state.dispense.updating);
  const updateSuccess = useAppSelector(state => state.dispense.updateSuccess);

  const handleClose = () => {
    navigate('/dispense');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPrescriptions({}));
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
    values.dispensedAt = convertDateTimeToServer(values.dispensedAt);

    const entity = {
      ...dispenseEntity,
      ...values,
      prescription: prescriptions.find(it => it.id.toString() === values.prescription?.toString()),
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
          dispensedAt: displayDefaultDateTime(),
        }
      : {
          ...dispenseEntity,
          dispensedAt: convertDateTimeFromServer(dispenseEntity.dispensedAt),
          prescription: dispenseEntity?.prescription?.id,
          recordedBy: dispenseEntity?.recordedBy?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.dispense.home.createOrEditLabel" data-cy="DispenseCreateUpdateHeading">
            Create or edit a Dispense
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="dispense-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Dispensed At"
                id="dispense-dispensedAt"
                name="dispensedAt"
                data-cy="dispensedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Note"
                id="dispense-note"
                name="note"
                data-cy="note"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                id="dispense-prescription"
                name="prescription"
                data-cy="prescription"
                label="Prescription"
                type="select"
                required
              >
                <option value="" key="0" />
                {prescriptions
                  ? prescriptions.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <ValidatedField id="dispense-recordedBy" name="recordedBy" data-cy="recordedBy" label="Recorded By" type="select" required>
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
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/dispense" replace variant="info">
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

export default DispenseUpdate;
