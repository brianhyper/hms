import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { DrugClassification } from 'app/shared/model/enumerations/drug-classification.model';

import { createEntity, getEntity, reset, updateEntity } from './drug.reducer';

export const DrugUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const drugEntity = useAppSelector(state => state.drug.entity);
  const loading = useAppSelector(state => state.drug.loading);
  const updating = useAppSelector(state => state.drug.updating);
  const updateSuccess = useAppSelector(state => state.drug.updateSuccess);
  const drugClassificationValues = Object.keys(DrugClassification);

  const handleClose = () => {
    navigate(`/drug${location.search}`);
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
    if (values.currentStock !== undefined && typeof values.currentStock !== 'number') {
      values.currentStock = Number(values.currentStock);
    }
    if (values.reservedStock !== undefined && typeof values.reservedStock !== 'number') {
      values.reservedStock = Number(values.reservedStock);
    }
    if (values.lowStockThreshold !== undefined && typeof values.lowStockThreshold !== 'number') {
      values.lowStockThreshold = Number(values.lowStockThreshold);
    }
    if (values.price !== undefined && typeof values.price !== 'number') {
      values.price = Number(values.price);
    }

    const entity = {
      ...drugEntity,
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
          classification: 'OTC',
          ...drugEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.drug.home.createOrEditLabel" data-cy="DrugCreateUpdateHeading">
            Create or edit a Drug
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="drug-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Name"
                id="drug-name"
                name="name"
                data-cy="name"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <ValidatedField
                label="Unit"
                id="drug-unit"
                name="unit"
                data-cy="unit"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 80, message: 'This field cannot be longer than 80 characters.' },
                }}
              />
              <ValidatedField
                label="Current Stock"
                id="drug-currentStock"
                name="currentStock"
                data-cy="currentStock"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                label="Reserved Stock"
                id="drug-reservedStock"
                name="reservedStock"
                data-cy="reservedStock"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                label="Low Stock Threshold"
                id="drug-lowStockThreshold"
                name="lowStockThreshold"
                data-cy="lowStockThreshold"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField
                label="Price"
                id="drug-price"
                name="price"
                data-cy="price"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  min: { value: 0, message: 'This field should be at least 0.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField label="Classification" id="drug-classification" name="classification" data-cy="classification" type="select">
                {drugClassificationValues.map(drugClassification => (
                  <option value={drugClassification} key={drugClassification}>
                    {drugClassification}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField label="Active" id="drug-active" name="active" data-cy="active" check type="checkbox" />
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/drug" replace variant="info">
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

export default DrugUpdate;
