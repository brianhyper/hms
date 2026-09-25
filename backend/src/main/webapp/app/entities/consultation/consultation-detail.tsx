import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './consultation.reducer';

export const ConsultationDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const consultationEntity = useAppSelector(state => state.consultation.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="consultationDetailsHeading">Consultation</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{consultationEntity.id}</dd>
          <dt>
            <span id="presentingComplaint">Presenting Complaint</span>
          </dt>
          <dd>{consultationEntity.presentingComplaint}</dd>
          <dt>
            <span id="examinationFindings">Examination Findings</span>
          </dt>
          <dd>{consultationEntity.examinationFindings}</dd>
          <dt>
            <span id="diagnosisOther">Diagnosis Other</span>
          </dt>
          <dd>{consultationEntity.diagnosisOther}</dd>
          <dt>
            <span id="observations">Observations</span>
          </dt>
          <dd>{consultationEntity.observations}</dd>
          <dt>
            <span id="followUpInstructions">Follow Up Instructions</span>
          </dt>
          <dd>{consultationEntity.followUpInstructions}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{consultationEntity.status}</dd>
          <dt>
            <span id="startedAt">Started At</span>
          </dt>
          <dd>
            {consultationEntity.startedAt ? <TextFormat value={consultationEntity.startedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="completedAt">Completed At</span>
          </dt>
          <dd>
            {consultationEntity.completedAt ? (
              <TextFormat value={consultationEntity.completedAt} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>Doctor</dt>
          <dd>{consultationEntity.doctor ? consultationEntity.doctor.login : ''}</dd>
          <dt>Diagnoses</dt>
          <dd>
            {consultationEntity.diagnoseses
              ? consultationEntity.diagnoseses.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.id}</a>
                    {consultationEntity.diagnoseses && i === consultationEntity.diagnoseses.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
        </dl>
        <Button as={Link as any} to="/consultation" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/consultation/${consultationEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default ConsultationDetail;
