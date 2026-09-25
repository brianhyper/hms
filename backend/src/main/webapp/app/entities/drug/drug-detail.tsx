import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './drug.reducer';

export const DrugDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const drugEntity = useAppSelector(state => state.drug.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="drugDetailsHeading">Drug</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{drugEntity.id}</dd>
          <dt>
            <span id="name">Name</span>
          </dt>
          <dd>{drugEntity.name}</dd>
          <dt>
            <span id="unit">Unit</span>
          </dt>
          <dd>{drugEntity.unit}</dd>
          <dt>
            <span id="currentStock">Current Stock</span>
          </dt>
          <dd>{drugEntity.currentStock}</dd>
          <dt>
            <span id="reservedStock">Reserved Stock</span>
          </dt>
          <dd>{drugEntity.reservedStock}</dd>
          <dt>
            <span id="lowStockThreshold">Low Stock Threshold</span>
          </dt>
          <dd>{drugEntity.lowStockThreshold}</dd>
          <dt>
            <span id="price">Price</span>
          </dt>
          <dd>{drugEntity.price}</dd>
          <dt>
            <span id="classification">Classification</span>
          </dt>
          <dd>{drugEntity.classification}</dd>
          <dt>
            <span id="active">Active</span>
          </dt>
          <dd>{drugEntity.active ? 'true' : 'false'}</dd>
        </dl>
        <Button as={Link as any} to="/drug" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/drug/${drugEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default DrugDetail;
