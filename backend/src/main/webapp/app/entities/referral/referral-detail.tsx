import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './referral.reducer';

export const ReferralDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const referralEntity = useAppSelector(state => state.referral.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="referralDetailsHeading">Referral</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{referralEntity.id}</dd>
          <dt>
            <span id="type">Type</span>
          </dt>
          <dd>{referralEntity.type}</dd>
          <dt>
            <span id="destination">Destination</span>
          </dt>
          <dd>{referralEntity.destination}</dd>
          <dt>
            <span id="destinationEmail">Destination Email</span>
          </dt>
          <dd>{referralEntity.destinationEmail}</dd>
          <dt>
            <span id="reason">Reason</span>
          </dt>
          <dd>{referralEntity.reason}</dd>
          <dt>
            <span id="notes">Notes</span>
          </dt>
          <dd>{referralEntity.notes}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{referralEntity.status}</dd>
          <dt>
            <span id="createdAt">Created At</span>
          </dt>
          <dd>{referralEntity.createdAt ? <TextFormat value={referralEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>Visit</dt>
          <dd>{referralEntity.visit ? referralEntity.visit.id : ''}</dd>
          <dt>Referred By</dt>
          <dd>{referralEntity.referredBy ? referralEntity.referredBy.login : ''}</dd>
          <dt>Department</dt>
          <dd>{referralEntity.department ? referralEntity.department.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/referral" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/referral/${referralEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default ReferralDetail;
