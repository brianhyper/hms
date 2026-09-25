import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './vital-signs.reducer';

export const VitalSignsDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const vitalSignsEntity = useAppSelector(state => state.vitalSigns.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="vitalSignsDetailsHeading">Vital Signs</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{vitalSignsEntity.id}</dd>
          <dt>
            <span id="temperature">Temperature</span>
          </dt>
          <dd>{vitalSignsEntity.temperature}</dd>
          <dt>
            <span id="pulseRate">Pulse Rate</span>
          </dt>
          <dd>{vitalSignsEntity.pulseRate}</dd>
          <dt>
            <span id="systolicBp">Systolic Bp</span>
          </dt>
          <dd>{vitalSignsEntity.systolicBp}</dd>
          <dt>
            <span id="diastolicBp">Diastolic Bp</span>
          </dt>
          <dd>{vitalSignsEntity.diastolicBp}</dd>
          <dt>
            <span id="oxygenSaturation">Oxygen Saturation</span>
          </dt>
          <dd>{vitalSignsEntity.oxygenSaturation}</dd>
          <dt>
            <span id="weight">Weight</span>
          </dt>
          <dd>{vitalSignsEntity.weight}</dd>
          <dt>
            <span id="height">Height</span>
          </dt>
          <dd>{vitalSignsEntity.height}</dd>
          <dt>
            <span id="bmi">Bmi</span>
          </dt>
          <dd>{vitalSignsEntity.bmi}</dd>
          <dt>
            <span id="nutritionalStatus">Nutritional Status</span>
          </dt>
          <dd>{vitalSignsEntity.nutritionalStatus}</dd>
          <dt>
            <span id="pregnancyScreening">Pregnancy Screening</span>
          </dt>
          <dd>{vitalSignsEntity.pregnancyScreening ? 'true' : 'false'}</dd>
          <dt>
            <span id="triageNotes">Triage Notes</span>
          </dt>
          <dd>{vitalSignsEntity.triageNotes}</dd>
          <dt>
            <span id="otherMeasurements">Other Measurements</span>
          </dt>
          <dd>{vitalSignsEntity.otherMeasurements}</dd>
        </dl>
        <Button as={Link as any} to="/vital-signs" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/vital-signs/${vitalSignsEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default VitalSignsDetail;
