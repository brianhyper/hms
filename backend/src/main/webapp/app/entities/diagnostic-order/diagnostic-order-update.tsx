import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getLabTests } from 'app/entities/lab-test/lab-test.reducer';
import { getEntities as getRadiologyExams } from 'app/entities/radiology-exam/radiology-exam.reducer';
import { getEntities as getResults } from 'app/entities/result/result.reducer';
import { getEntities as getVisits } from 'app/entities/visit/visit.reducer';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';
import { OrderType } from 'app/shared/model/enumerations/order-type.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './diagnostic-order.reducer';

export const DiagnosticOrderUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const results = useAppSelector(state => state.result.entities);
  const visits = useAppSelector(state => state.visit.entities);
  const users = useAppSelector(state => state.userManagement.users);
  const labTests = useAppSelector(state => state.labTest.entities);
  const radiologyExams = useAppSelector(state => state.radiologyExam.entities);
  const diagnosticOrderEntity = useAppSelector(state => state.diagnosticOrder.entity);
  const loading = useAppSelector(state => state.diagnosticOrder.loading);
  const updating = useAppSelector(state => state.diagnosticOrder.updating);
  const updateSuccess = useAppSelector(state => state.diagnosticOrder.updateSuccess);
  const orderTypeValues = Object.keys(OrderType);
  const orderStatusValues = Object.keys(OrderStatus);

  const handleClose = () => {
    navigate(`/diagnostic-order${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getResults({}));
    dispatch(getVisits({}));
    dispatch(getUsers({}));
    dispatch(getLabTests({}));
    dispatch(getRadiologyExams({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    values.orderedAt = convertDateTimeToServer(values.orderedAt);

    const entity = {
      ...diagnosticOrderEntity,
      ...values,
      result: results.find(it => it.id.toString() === values.result?.toString()),
      visit: visits.find(it => it.id.toString() === values.visit?.toString()),
      orderedBy: users.find(it => it.id.toString() === values.orderedBy?.toString()),
      labTest: labTests.find(it => it.id.toString() === values.labTest?.toString()),
      radiologyExam: radiologyExams.find(it => it.id.toString() === values.radiologyExam?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          orderedAt: displayDefaultDateTime(),
        }
      : {
          type: 'LAB',
          status: 'PENDING',
          ...diagnosticOrderEntity,
          orderedAt: convertDateTimeFromServer(diagnosticOrderEntity.orderedAt),
          result: diagnosticOrderEntity?.result?.id,
          visit: diagnosticOrderEntity?.visit?.id,
          orderedBy: diagnosticOrderEntity?.orderedBy?.id,
          labTest: diagnosticOrderEntity?.labTest?.id,
          radiologyExam: diagnosticOrderEntity?.radiologyExam?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.diagnosticOrder.home.createOrEditLabel" data-cy="DiagnosticOrderCreateUpdateHeading">
            Create or edit a Diagnostic Order
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="diagnostic-order-id" label="ID" validate={{ required: true }} />}
              <ValidatedField label="Type" id="diagnostic-order-type" name="type" data-cy="type" type="select">
                {orderTypeValues.map(orderType => (
                  <option value={orderType} key={orderType}>
                    {orderType}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Test Name"
                id="diagnostic-order-testName"
                name="testName"
                data-cy="testName"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <ValidatedField label="Status" id="diagnostic-order-status" name="status" data-cy="status" type="select">
                {orderStatusValues.map(orderStatus => (
                  <option value={orderStatus} key={orderStatus}>
                    {orderStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Notes"
                id="diagnostic-order-notes"
                name="notes"
                data-cy="notes"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Ordered At"
                id="diagnostic-order-orderedAt"
                name="orderedAt"
                data-cy="orderedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField id="diagnostic-order-result" name="result" data-cy="result" label="Result" type="select">
                <option value="" key="0" />
                {results
                  ? results.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField id="diagnostic-order-visit" name="visit" data-cy="visit" label="Visit" type="select" required>
                <option value="" key="0" />
                {visits
                  ? visits.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <ValidatedField
                id="diagnostic-order-orderedBy"
                name="orderedBy"
                data-cy="orderedBy"
                label="Ordered By"
                type="select"
                required
              >
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <ValidatedField id="diagnostic-order-labTest" name="labTest" data-cy="labTest" label="Lab Test" type="select">
                <option value="" key="0" />
                {labTests
                  ? labTests.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="diagnostic-order-radiologyExam"
                name="radiologyExam"
                data-cy="radiologyExam"
                label="Radiology Exam"
                type="select"
              >
                <option value="" key="0" />
                {radiologyExams
                  ? radiologyExams.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/diagnostic-order" replace variant="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button variant="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default DiagnosticOrderUpdate;
