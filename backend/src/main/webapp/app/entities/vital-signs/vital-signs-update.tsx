import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './vital-signs.reducer';

export const VitalSignsUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const vitalSignsEntity = useAppSelector(state => state.vitalSigns.entity);
  const loading = useAppSelector(state => state.vitalSigns.loading);
  const updating = useAppSelector(state => state.vitalSigns.updating);
  const updateSuccess = useAppSelector(state => state.vitalSigns.updateSuccess);

  const handleClose = () => {
    navigate('/vital-signs');
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
    if (values.temperature !== undefined && typeof values.temperature !== 'number') {
      values.temperature = Number(values.temperature);
    }
    if (values.pulseRate !== undefined && typeof values.pulseRate !== 'number') {
      values.pulseRate = Number(values.pulseRate);
    }
    if (values.systolicBp !== undefined && typeof values.systolicBp !== 'number') {
      values.systolicBp = Number(values.systolicBp);
    }
    if (values.diastolicBp !== undefined && typeof values.diastolicBp !== 'number') {
      values.diastolicBp = Number(values.diastolicBp);
    }
    if (values.oxygenSaturation !== undefined && typeof values.oxygenSaturation !== 'number') {
      values.oxygenSaturation = Number(values.oxygenSaturation);
    }
    if (values.weight !== undefined && typeof values.weight !== 'number') {
      values.weight = Number(values.weight);
    }
    if (values.height !== undefined && typeof values.height !== 'number') {
      values.height = Number(values.height);
    }
    if (values.bmi !== undefined && typeof values.bmi !== 'number') {
      values.bmi = Number(values.bmi);
    }

    const entity = {
      ...vitalSignsEntity,
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
          ...vitalSignsEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.vitalSigns.home.createOrEditLabel" data-cy="VitalSignsCreateUpdateHeading">
            Create or edit a Vital Signs
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="vital-signs-id" label="ID" validate={{ required: true }} />}
              <ValidatedField label="Temperature" id="vital-signs-temperature" name="temperature" data-cy="temperature" type="text" />
              <ValidatedField label="Pulse Rate" id="vital-signs-pulseRate" name="pulseRate" data-cy="pulseRate" type="text" />
              <ValidatedField label="Systolic Bp" id="vital-signs-systolicBp" name="systolicBp" data-cy="systolicBp" type="text" />
              <ValidatedField label="Diastolic Bp" id="vital-signs-diastolicBp" name="diastolicBp" data-cy="diastolicBp" type="text" />
              <ValidatedField
                label="Oxygen Saturation"
                id="vital-signs-oxygenSaturation"
                name="oxygenSaturation"
                data-cy="oxygenSaturation"
                type="text"
              />
              <ValidatedField label="Weight" id="vital-signs-weight" name="weight" data-cy="weight" type="text" />
              <ValidatedField label="Height" id="vital-signs-height" name="height" data-cy="height" type="text" />
              <ValidatedField label="Bmi" id="vital-signs-bmi" name="bmi" data-cy="bmi" type="text" />
              <ValidatedField
                label="Nutritional Status"
                id="vital-signs-nutritionalStatus"
                name="nutritionalStatus"
                data-cy="nutritionalStatus"
                type="text"
                validate={{
                  maxLength: { value: 100, message: 'This field cannot be longer than 100 characters.' },
                }}
              />
              <ValidatedField
                label="Pregnancy Screening"
                id="vital-signs-pregnancyScreening"
                name="pregnancyScreening"
                data-cy="pregnancyScreening"
                check
                type="checkbox"
              />
              <ValidatedField
                label="Triage Notes"
                id="vital-signs-triageNotes"
                name="triageNotes"
                data-cy="triageNotes"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Other Measurements"
                id="vital-signs-otherMeasurements"
                name="otherMeasurements"
                data-cy="otherMeasurements"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/vital-signs" replace variant="info">
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

export default VitalSignsUpdate;
