import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getDispenses } from 'app/entities/dispense/dispense.reducer';
import { getEntities as getDrugs } from 'app/entities/drug/drug.reducer';
import { getEntities as getPrescriptionLines } from 'app/entities/prescription-line/prescription-line.reducer';

import { createEntity, getEntity, reset, updateEntity } from './dispense-line.reducer';

export const DispenseLineUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const dispenses = useAppSelector(state => state.dispense.entities);
  const prescriptionLines = useAppSelector(state => state.prescriptionLine.entities);
  const drugs = useAppSelector(state => state.drug.entities);
  const dispenseLineEntity = useAppSelector(state => state.dispenseLine.entity);
  const loading = useAppSelector(state => state.dispenseLine.loading);
  const updating = useAppSelector(state => state.dispenseLine.updating);
  const updateSuccess = useAppSelector(state => state.dispenseLine.updateSuccess);

  const handleClose = () => {
    navigate('/dispense-line');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getDispenses({}));
    dispatch(getPrescriptionLines({}));
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
      ...dispenseLineEntity,
      ...values,
      dispense: dispenses.find(it => it.id.toString() === values.dispense?.toString()),
      prescriptionLine: prescriptionLines.find(it => it.id.toString() === values.prescriptionLine?.toString()),
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
          ...dispenseLineEntity,
          dispense: dispenseLineEntity?.dispense?.id,
          prescriptionLine: dispenseLineEntity?.prescriptionLine?.id,
          drug: dispenseLineEntity?.drug?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.dispenseLine.home.createOrEditLabel" data-cy="DispenseLineCreateUpdateHeading">
            Create or edit a Dispense Line
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="dispense-line-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Quantity"
                id="dispense-line-quantity"
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
                label="Substitution Reason"
                id="dispense-line-substitutionReason"
                name="substitutionReason"
                data-cy="substitutionReason"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField id="dispense-line-dispense" name="dispense" data-cy="dispense" label="Dispense" type="select" required>
                <option value="" key="0" />
                {dispenses
                  ? dispenses.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <ValidatedField
                id="dispense-line-prescriptionLine"
                name="prescriptionLine"
                data-cy="prescriptionLine"
                label="Prescription Line"
                type="select"
                required
              >
                <option value="" key="0" />
                {prescriptionLines
                  ? prescriptionLines.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <ValidatedField id="dispense-line-drug" name="drug" data-cy="drug" label="Drug" type="select" required>
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
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/dispense-line" replace variant="info">
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

export default DispenseLineUpdate;
