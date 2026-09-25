import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './payment.reducer';

export const PaymentDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const paymentEntity = useAppSelector(state => state.payment.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="paymentDetailsHeading">Payment</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{paymentEntity.id}</dd>
          <dt>
            <span id="method">Method</span>
          </dt>
          <dd>{paymentEntity.method}</dd>
          <dt>
            <span id="mpesaReference">Mpesa Reference</span>
          </dt>
          <dd>{paymentEntity.mpesaReference}</dd>
          <dt>
            <span id="insurerName">Insurer Name</span>
          </dt>
          <dd>{paymentEntity.insurerName}</dd>
          <dt>
            <span id="confirmationStatus">Confirmation Status</span>
          </dt>
          <dd>{paymentEntity.confirmationStatus}</dd>
          <dt>
            <span id="receiptNumber">Receipt Number</span>
          </dt>
          <dd>{paymentEntity.receiptNumber}</dd>
          <dt>
            <span id="amount">Amount</span>
          </dt>
          <dd>{paymentEntity.amount}</dd>
          <dt>
            <span id="recordedAt">Recorded At</span>
          </dt>
          <dd>{paymentEntity.recordedAt ? <TextFormat value={paymentEntity.recordedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>Recorded By</dt>
          <dd>{paymentEntity.recordedBy ? paymentEntity.recordedBy.login : ''}</dd>
        </dl>
        <Button as={Link as any} to="/payment" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/payment/${paymentEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default PaymentDetail;
