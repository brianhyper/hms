import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './bill-line-item.reducer';

export const BillLineItemDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const billLineItemEntity = useAppSelector(state => state.billLineItem.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="billLineItemDetailsHeading">Bill Line Item</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{billLineItemEntity.id}</dd>
          <dt>
            <span id="description">Description</span>
          </dt>
          <dd>{billLineItemEntity.description}</dd>
          <dt>
            <span id="amount">Amount</span>
          </dt>
          <dd>{billLineItemEntity.amount}</dd>
          <dt>
            <span id="sourceType">Source Type</span>
          </dt>
          <dd>{billLineItemEntity.sourceType}</dd>
          <dt>Bill</dt>
          <dd>{billLineItemEntity.bill ? billLineItemEntity.bill.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/bill-line-item" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/bill-line-item/${billLineItemEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default BillLineItemDetail;
