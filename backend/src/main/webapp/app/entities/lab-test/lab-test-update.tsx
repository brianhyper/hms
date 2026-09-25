import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './lab-test.reducer';

export const LabTestUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const labTestEntity = useAppSelector(state => state.labTest.entity);
  const loading = useAppSelector(state => state.labTest.loading);
  const updating = useAppSelector(state => state.labTest.updating);
  const updateSuccess = useAppSelector(state => state.labTest.updateSuccess);

  const handleClose = () => {
    navigate('/lab-test');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
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
    if (values.price !== undefined && typeof values.price !== 'number') {
      values.price = Number(values.price);
    }
    if (values.turnaroundTimeMinutes !== undefined && typeof values.turnaroundTimeMinutes !== 'number') {
      values.turnaroundTimeMinutes = Number(values.turnaroundTimeMinutes);
    }

    const entity = {
      ...labTestEntity,
      ...values,
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
          ...labTestEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.labTest.home.createOrEditLabel" data-cy="LabTestCreateUpdateHeading">
            Create or edit a Lab Test
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="lab-test-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Name"
                id="lab-test-name"
                name="name"
                data-cy="name"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 160, message: 'This field cannot be longer than 160 characters.' },
                }}
              />
              <ValidatedField
                label="Price"
                id="lab-test-price"
                name="price"
                data-cy="price"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                label="Specimen Type"
                id="lab-test-specimenType"
                name="specimenType"
                data-cy="specimenType"
                type="text"
                validate={{
                  maxLength: { value: 100, message: 'This field cannot be longer than 100 characters.' },
                }}
              />
              <ValidatedField
                label="Turnaround Time Minutes"
                id="lab-test-turnaroundTimeMinutes"
                name="turnaroundTimeMinutes"
                data-cy="turnaroundTimeMinutes"
                type="text"
                validate={{
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField label="Active" id="lab-test-active" name="active" data-cy="active" check type="checkbox" />
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/lab-test" replace variant="info">
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

export default LabTestUpdate;
