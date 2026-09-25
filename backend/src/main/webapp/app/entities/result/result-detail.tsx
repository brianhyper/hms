import React, { useEffect } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './result.reducer';

export const ResultDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const resultEntity = useAppSelector(state => state.result.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="resultDetailsHeading">Result</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{resultEntity.id}</dd>
          <dt>
            <span id="resultValue">Result Value</span>
          </dt>
          <dd>{resultEntity.resultValue}</dd>
          <dt>
            <span id="notes">Notes</span>
          </dt>
          <dd>{resultEntity.notes}</dd>
          <dt>
            <span id="enteredAt">Entered At</span>
          </dt>
          <dd>{resultEntity.enteredAt ? <TextFormat value={resultEntity.enteredAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="imageReference">Image Reference</span>
          </dt>
          <dd>{resultEntity.imageReference}</dd>
          <dt>Entered By</dt>
          <dd>{resultEntity.enteredBy ? resultEntity.enteredBy.login : ''}</dd>
        </dl>
        <Button as={Link as any} to="/result" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/result/${resultEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default ResultDetail;
