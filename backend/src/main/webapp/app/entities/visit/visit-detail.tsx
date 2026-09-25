import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './visit.reducer';

export const VisitDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const visitEntity = useAppSelector(state => state.visit.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="visitDetailsHeading">Visit</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{visitEntity.id}</dd>
          <dt>
            <span id="type">Type</span>
          </dt>
          <dd>{visitEntity.type}</dd>
          <dt>
            <span id="priority">Priority</span>
          </dt>
          <dd>{visitEntity.priority}</dd>
          <dt>
            <span id="reasonForVisit">Reason For Visit</span>
          </dt>
          <dd>{visitEntity.reasonForVisit}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{visitEntity.status}</dd>
          <dt>
            <span id="queueSkipReason">Queue Skip Reason</span>
          </dt>
          <dd>{visitEntity.queueSkipReason}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>{visitEntity.createdAt ? <TextFormat value={visitEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="startedVitalsAt">Started Vitals At</span>
          </dt>
          <dd>
            {visitEntity.startedVitalsAt ? <TextFormat value={visitEntity.startedVitalsAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="startedConsultationAt">Started Consultation At</span>
          </dt>
          <dd>
            {visitEntity.startedConsultationAt ? (
              <TextFormat value={visitEntity.startedConsultationAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="closedAt">Closed At</span>
          </dt>
          <dd>{visitEntity.closedAt ? <TextFormat value={visitEntity.closedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>Vitals</dt>
          <dd>{visitEntity.vitals ? visitEntity.vitals.id : ''}</dd>
          <dt>Consultation</dt>
          <dd>{visitEntity.consultation ? visitEntity.consultation.id : ''}</dd>
          <dt>Bill</dt>
          <dd>{visitEntity.bill ? visitEntity.bill.id : ''}</dd>
          <dt>Patient</dt>
          <dd>{visitEntity.patient ? visitEntity.patient.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/visit" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/visit/${visitEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default VisitDetail;
