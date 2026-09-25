import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './hospital-service.reducer';

export const HospitalServiceDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const hospitalServiceEntity = useAppSelector(state => state.hospitalService.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="hospitalServiceDetailsHeading">Hospital Service</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{hospitalServiceEntity.id}</dd>
          <dt>
            <span id="name">Name</span>
          </dt>
          <dd>{hospitalServiceEntity.name}</dd>
          <dt>
            <span id="serviceType">Service Type</span>
          </dt>
          <dd>{hospitalServiceEntity.serviceType}</dd>
          <dt>
            <span id="price">Price</span>
          </dt>
          <dd>{hospitalServiceEntity.price}</dd>
          <dt>
            <span id="active">Active</span>
          </dt>
          <dd>{hospitalServiceEntity.active ? 'true' : 'false'}</dd>
        </dl>
        <Button as={Link as any} to="/hospital-service" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/hospital-service/${hospitalServiceEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default HospitalServiceDetail;
