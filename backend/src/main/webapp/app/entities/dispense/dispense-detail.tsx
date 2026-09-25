import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './dispense.reducer';

export const DispenseDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const dispenseEntity = useAppSelector(state => state.dispense.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="dispenseDetailsHeading">Dispense</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{dispenseEntity.id}</dd>
          <dt>
            <span id="dispensedAt">Dispensed At</span>
          </dt>
          <dd>
            {dispenseEntity.dispensedAt ? <TextFormat value={dispenseEntity.dispensedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="note">Note</span>
          </dt>
          <dd>{dispenseEntity.note}</dd>
          <dt>Prescription</dt>
          <dd>{dispenseEntity.prescription ? dispenseEntity.prescription.id : ''}</dd>
          <dt>Recorded By</dt>
          <dd>{dispenseEntity.recordedBy ? dispenseEntity.recordedBy.login : ''}</dd>
        </dl>
        <Button as={Link as any} to="/dispense" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/dispense/${dispenseEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default DispenseDetail;
