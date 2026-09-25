import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './lab-test.reducer';

export const LabTestDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const labTestEntity = useAppSelector(state => state.labTest.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="labTestDetailsHeading">Lab Test</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{labTestEntity.id}</dd>
          <dt>
            <span id="name">Name</span>
          </dt>
          <dd>{labTestEntity.name}</dd>
          <dt>
            <span id="price">Price</span>
          </dt>
          <dd>{labTestEntity.price}</dd>
          <dt>
            <span id="specimenType">Specimen Type</span>
          </dt>
          <dd>{labTestEntity.specimenType}</dd>
          <dt>
            <span id="turnaroundTimeMinutes">Turnaround Time Minutes</span>
          </dt>
          <dd>{labTestEntity.turnaroundTimeMinutes}</dd>
          <dt>
            <span id="active">Active</span>
          </dt>
          <dd>{labTestEntity.active ? 'true' : 'false'}</dd>
        </dl>
        <Button as={Link as any} to="/lab-test" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/lab-test/${labTestEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default LabTestDetail;
