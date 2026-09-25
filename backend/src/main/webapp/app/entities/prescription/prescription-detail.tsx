import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './prescription.reducer';

export const PrescriptionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const prescriptionEntity = useAppSelector(state => state.prescription.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="prescriptionDetailsHeading">Prescription</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{prescriptionEntity.id}</dd>
          <dt>
            <span id="source">Source</span>
          </dt>
          <dd>{prescriptionEntity.source}</dd>
          <dt>
            <span id="prescribingSource">Prescribing Source</span>
          </dt>
          <dd>{prescriptionEntity.prescribingSource}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{prescriptionEntity.status}</dd>
          <dt>Visit</dt>
          <dd>{prescriptionEntity.visit ? prescriptionEntity.visit.id : ''}</dd>
          <dt>Doctor</dt>
          <dd>{prescriptionEntity.doctor ? prescriptionEntity.doctor.login : ''}</dd>
        </dl>
        <Button as={Link as any} to="/prescription" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/prescription/${prescriptionEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default PrescriptionDetail;
