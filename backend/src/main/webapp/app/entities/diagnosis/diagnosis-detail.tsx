import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './diagnosis.reducer';

export const DiagnosisDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const diagnosisEntity = useAppSelector(state => state.diagnosis.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="diagnosisDetailsHeading">Diagnosis</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{diagnosisEntity.id}</dd>
          <dt>
            <span id="code">Code</span>
          </dt>
          <dd>{diagnosisEntity.code}</dd>
          <dt>
            <span id="name">Name</span>
          </dt>
          <dd>{diagnosisEntity.name}</dd>
          <dt>
            <span id="active">Active</span>
          </dt>
          <dd>{diagnosisEntity.active ? 'true' : 'false'}</dd>
        </dl>
        <Button as={Link as any} to="/diagnosis" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/diagnosis/${diagnosisEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default DiagnosisDetail;
