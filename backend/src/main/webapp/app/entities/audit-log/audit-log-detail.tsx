import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './audit-log.reducer';

export const AuditLogDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const auditLogEntity = useAppSelector(state => state.auditLog.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="auditLogDetailsHeading">Audit Log</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{auditLogEntity.id}</dd>
          <dt>
            <span id="action">Action</span>
          </dt>
          <dd>{auditLogEntity.action}</dd>
          <dt>
            <span id="entityName">Entity Name</span>
          </dt>
          <dd>{auditLogEntity.entityName}</dd>
          <dt>
            <span id="entityId">Entity Id</span>
          </dt>
          <dd>{auditLogEntity.entityId}</dd>
          <dt>
            <span id="reason">Reason</span>
          </dt>
          <dd>{auditLogEntity.reason}</dd>
          <dt>
            <span id="oldValue">Old Value</span>
          </dt>
          <dd>{auditLogEntity.oldValue}</dd>
          <dt>
            <span id="newValue">New Value</span>
          </dt>
          <dd>{auditLogEntity.newValue}</dd>
          <dt>
            <span id="details">Details</span>
          </dt>
          <dd>{auditLogEntity.details}</dd>
          <dt>
            <span id="performedAt">Performed At</span>
          </dt>
          <dd>
            {auditLogEntity.performedAt ? <TextFormat value={auditLogEntity.performedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>Actor</dt>
          <dd>{auditLogEntity.actor ? auditLogEntity.actor.login : ''}</dd>
        </dl>
        <Button as={Link as any} to="/audit-log" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/audit-log/${auditLogEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default AuditLogDetail;
