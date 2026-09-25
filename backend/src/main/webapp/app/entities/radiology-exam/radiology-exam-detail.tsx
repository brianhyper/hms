import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './radiology-exam.reducer';

export const RadiologyExamDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const radiologyExamEntity = useAppSelector(state => state.radiologyExam.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="radiologyExamDetailsHeading">Radiology Exam</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{radiologyExamEntity.id}</dd>
          <dt>
            <span id="name">Name</span>
          </dt>
          <dd>{radiologyExamEntity.name}</dd>
          <dt>
            <span id="price">Price</span>
          </dt>
          <dd>{radiologyExamEntity.price}</dd>
          <dt>
            <span id="active">Active</span>
          </dt>
          <dd>{radiologyExamEntity.active ? 'true' : 'false'}</dd>
        </dl>
        <Button as={Link as any} to="/radiology-exam" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/radiology-exam/${radiologyExamEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default RadiologyExamDetail;
