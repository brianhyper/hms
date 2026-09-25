import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getVisits } from 'app/entities/visit/visit.reducer';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { PrescriptionSource } from 'app/shared/model/enumerations/prescription-source.model';
import { PrescriptionStatus } from 'app/shared/model/enumerations/prescription-status.model';

import { createEntity, getEntity, reset, updateEntity } from './prescription.reducer';

export const PrescriptionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const visits = useAppSelector(state => state.visit.entities);
  const users = useAppSelector(state => state.userManagement.users);
  const prescriptionEntity = useAppSelector(state => state.prescription.entity);
  const loading = useAppSelector(state => state.prescription.loading);
  const updating = useAppSelector(state => state.prescription.updating);
  const updateSuccess = useAppSelector(state => state.prescription.updateSuccess);
  const prescriptionSourceValues = Object.keys(PrescriptionSource);
  const prescriptionStatusValues = Object.keys(PrescriptionStatus);

  const handleClose = () => {
    navigate(`/prescription${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getVisits({}));
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

    const entity = {
      ...prescriptionEntity,
      ...values,
      visit: visits.find(it => it.id.toString() === values.visit?.toString()),
      doctor: users.find(it => it.id.toString() === values.doctor?.toString()),
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
          source: 'INTERNAL',
          status: 'PENDING',
          ...prescriptionEntity,
          visit: prescriptionEntity?.visit?.id,
          doctor: prescriptionEntity?.doctor?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.prescription.home.createOrEditLabel" data-cy="PrescriptionCreateUpdateHeading">
            Create or edit a Prescription
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="prescription-id" label="ID" validate={{ required: true }} />}
              <ValidatedField label="Source" id="prescription-source" name="source" data-cy="source" type="select">
                {prescriptionSourceValues.map(prescriptionSource => (
                  <option value={prescriptionSource} key={prescriptionSource}>
                    {prescriptionSource}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Prescribing Source"
                id="prescription-prescribingSource"
                name="prescribingSource"
                data-cy="prescribingSource"
                type="text"
                validate={{
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField label="Status" id="prescription-status" name="status" data-cy="status" type="select">
                {prescriptionStatusValues.map(prescriptionStatus => (
                  <option value={prescriptionStatus} key={prescriptionStatus}>
                    {prescriptionStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField id="prescription-visit" name="visit" data-cy="visit" label="Visit" type="select">
                <option value="" key="0" />
                {visits
                  ? visits.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField id="prescription-doctor" name="doctor" data-cy="doctor" label="Doctor" type="select">
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/prescription" replace variant="info">
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

export default PrescriptionUpdate;
