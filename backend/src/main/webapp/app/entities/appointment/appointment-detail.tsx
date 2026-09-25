import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './appointment.reducer';

export const AppointmentDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const appointmentEntity = useAppSelector(state => state.appointment.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="appointmentDetailsHeading">Appointment</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{appointmentEntity.id}</dd>
          <dt>
            <span id="scheduledDate">Scheduled Date</span>
          </dt>
          <dd>
            {appointmentEntity.scheduledDate ? (
              <TextFormat value={appointmentEntity.scheduledDate} type="date" format={APP_LOCAL_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="scheduledTime">Scheduled Time</span>
          </dt>
          <dd>{appointmentEntity.scheduledTime}</dd>
          <dt>
            <span id="reason">Reason</span>
          </dt>
          <dd>{appointmentEntity.reason}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{appointmentEntity.status}</dd>
          <dt>Visit</dt>
          <dd>{appointmentEntity.visit ? appointmentEntity.visit.id : ''}</dd>
          <dt>Patient</dt>
          <dd>{appointmentEntity.patient ? appointmentEntity.patient.id : ''}</dd>
          <dt>Department</dt>
          <dd>{appointmentEntity.department ? appointmentEntity.department.id : ''}</dd>
          <dt>Doctor</dt>
          <dd>{appointmentEntity.doctor ? appointmentEntity.doctor.login : ''}</dd>
        </dl>
        <Button as={Link as any} to="/appointment" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/appointment/${appointmentEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default AppointmentDetail;
