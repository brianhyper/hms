import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './hospital-service.reducer';

export const HospitalServiceUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const hospitalServiceEntity = useAppSelector(state => state.hospitalService.entity);
  const loading = useAppSelector(state => state.hospitalService.loading);
  const updating = useAppSelector(state => state.hospitalService.updating);
  const updateSuccess = useAppSelector(state => state.hospitalService.updateSuccess);

  const handleClose = () => {
    navigate('/hospital-service');
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

    const entity = {
      ...hospitalServiceEntity,
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
          ...hospitalServiceEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.hospitalService.home.createOrEditLabel" data-cy="HospitalServiceCreateUpdateHeading">
            Create or edit a Hospital Service
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="hospital-service-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Name"
                id="hospital-service-name"
                name="name"
                data-cy="name"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 160, message: 'This field cannot be longer than 160 characters.' },
                }}
              />
              <ValidatedField
                label="Service Type"
                id="hospital-service-serviceType"
                name="serviceType"
                data-cy="serviceType"
                type="text"
                validate={{
                  maxLength: { value: 80, message: 'This field cannot be longer than 80 characters.' },
                }}
              />
              <ValidatedField
                label="Price"
                id="hospital-service-price"
                name="price"
                data-cy="price"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField label="Active" id="hospital-service-active" name="active" data-cy="active" check type="checkbox" />
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/hospital-service" replace variant="info">
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

export default HospitalServiceUpdate;
