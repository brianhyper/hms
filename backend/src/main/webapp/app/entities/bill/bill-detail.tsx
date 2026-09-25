import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './bill.reducer';

export const BillDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const billEntity = useAppSelector(state => state.bill.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="billDetailsHeading">Bill</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{billEntity.id}</dd>
          <dt>
            <span id="totalAmount">Total Amount</span>
          </dt>
          <dd>{billEntity.totalAmount}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{billEntity.status}</dd>
          <dt>
            <span id="paidAt">Paid At</span>
          </dt>
          <dd>{billEntity.paidAt ? <TextFormat value={billEntity.paidAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>Payment</dt>
          <dd>{billEntity.payment ? billEntity.payment.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/bill" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/bill/${billEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default BillDetail;
