import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './dispense-line.reducer';

export const DispenseLineDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const dispenseLineEntity = useAppSelector(state => state.dispenseLine.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="dispenseLineDetailsHeading">Dispense Line</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{dispenseLineEntity.id}</dd>
          <dt>
            <span id="quantity">Quantity</span>
          </dt>
          <dd>{dispenseLineEntity.quantity}</dd>
          <dt>
            <span id="substitutionReason">Substitution Reason</span>
          </dt>
          <dd>{dispenseLineEntity.substitutionReason}</dd>
          <dt>Dispense</dt>
          <dd>{dispenseLineEntity.dispense ? dispenseLineEntity.dispense.id : ''}</dd>
          <dt>Prescription Line</dt>
          <dd>{dispenseLineEntity.prescriptionLine ? dispenseLineEntity.prescriptionLine.id : ''}</dd>
          <dt>Drug</dt>
          <dd>{dispenseLineEntity.drug ? dispenseLineEntity.drug.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/dispense-line" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/dispense-line/${dispenseLineEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default DispenseLineDetail;
