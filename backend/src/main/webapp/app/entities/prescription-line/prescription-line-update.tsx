import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getDrugs } from 'app/entities/drug/drug.reducer';
import { getEntities as getPrescriptions } from 'app/entities/prescription/prescription.reducer';

import { createEntity, getEntity, reset, updateEntity } from './prescription-line.reducer';

export const PrescriptionLineUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const prescriptions = useAppSelector(state => state.prescription.entities);
  const drugs = useAppSelector(state => state.drug.entities);
  const prescriptionLineEntity = useAppSelector(state => state.prescriptionLine.entity);
  const loading = useAppSelector(state => state.prescriptionLine.loading);
  const updating = useAppSelector(state => state.prescriptionLine.updating);
  const updateSuccess = useAppSelector(state => state.prescriptionLine.updateSuccess);

  const handleClose = () => {
    navigate('/prescription-line');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPrescriptions({}));
    dispatch(getDrugs({}));
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
    if (values.quantity !== undefined && typeof values.quantity !== 'number') {
      values.quantity = Number(values.quantity);
    }

    const entity = {
      ...prescriptionLineEntity,
      ...values,
      prescription: prescriptions.find(it => it.id.toString() === values.prescription?.toString()),
      drug: drugs.find(it => it.id.toString() === values.drug?.toString()),
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
          ...prescriptionLineEntity,
          prescription: prescriptionLineEntity?.prescription?.id,
          drug: prescriptionLineEntity?.drug?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.prescriptionLine.home.createOrEditLabel" data-cy="PrescriptionLineCreateUpdateHeading">
            Create or edit a Prescription Line
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="prescription-line-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Dosage"
                id="prescription-line-dosage"
                name="dosage"
                data-cy="dosage"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <ValidatedField
                label="Duration"
                id="prescription-line-duration"
                name="duration"
                data-cy="duration"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 100, message: 'This field cannot be longer than 100 characters.' },
                }}
              />
              <ValidatedField
                label="Quantity"
                id="prescription-line-quantity"
                name="quantity"
                data-cy="quantity"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 1, message: 'This field should be at least 1.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                id="prescription-line-prescription"
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
              <ValidatedField id="prescription-line-drug" name="drug" data-cy="drug" label="Drug" type="select" required>
                <option value="" key="0" />
                {drugs
                  ? drugs.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/prescription-line" replace variant="info">
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

export default PrescriptionLineUpdate;
