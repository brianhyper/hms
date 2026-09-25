import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './diagnostic-order.reducer';

export const DiagnosticOrderDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const diagnosticOrderEntity = useAppSelector(state => state.diagnosticOrder.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="diagnosticOrderDetailsHeading">Diagnostic Order</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{diagnosticOrderEntity.id}</dd>
          <dt>
            <span id="type">Type</span>
          </dt>
          <dd>{diagnosticOrderEntity.type}</dd>
          <dt>
            <span id="testName">Test Name</span>
          </dt>
          <dd>{diagnosticOrderEntity.testName}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{diagnosticOrderEntity.status}</dd>
          <dt>
            <span id="notes">Notes</span>
          </dt>
          <dd>{diagnosticOrderEntity.notes}</dd>
          <dt>
            <span id="orderedAt">Ordered At</span>
          </dt>
          <dd>
            {diagnosticOrderEntity.orderedAt ? (
              <TextFormat value={diagnosticOrderEntity.orderedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>Result</dt>
          <dd>{diagnosticOrderEntity.result ? diagnosticOrderEntity.result.id : ''}</dd>
          <dt>Visit</dt>
          <dd>{diagnosticOrderEntity.visit ? diagnosticOrderEntity.visit.id : ''}</dd>
          <dt>Ordered By</dt>
          <dd>{diagnosticOrderEntity.orderedBy ? diagnosticOrderEntity.orderedBy.login : ''}</dd>
          <dt>Lab Test</dt>
          <dd>{diagnosticOrderEntity.labTest ? diagnosticOrderEntity.labTest.id : ''}</dd>
          <dt>Radiology Exam</dt>
          <dd>{diagnosticOrderEntity.radiologyExam ? diagnosticOrderEntity.radiologyExam.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/diagnostic-order" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/diagnostic-order/${diagnosticOrderEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default DiagnosticOrderDetail;
