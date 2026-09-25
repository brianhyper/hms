import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './prescription-line.reducer';

export const PrescriptionLineDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const prescriptionLineEntity = useAppSelector(state => state.prescriptionLine.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="prescriptionLineDetailsHeading">Prescription Line</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{prescriptionLineEntity.id}</dd>
          <dt>
            <span id="dosage">Dosage</span>
          </dt>
          <dd>{prescriptionLineEntity.dosage}</dd>
          <dt>
            <span id="duration">Duration</span>
          </dt>
          <dd>{prescriptionLineEntity.duration}</dd>
          <dt>
            <span id="quantity">Quantity</span>
          </dt>
          <dd>{prescriptionLineEntity.quantity}</dd>
          <dt>Prescription</dt>
          <dd>{prescriptionLineEntity.prescription ? prescriptionLineEntity.prescription.id : ''}</dd>
          <dt>Drug</dt>
          <dd>{prescriptionLineEntity.drug ? prescriptionLineEntity.drug.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/prescription-line" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/prescription-line/${prescriptionLineEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default PrescriptionLineDetail;
