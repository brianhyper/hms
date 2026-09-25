import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './result.reducer';

export const ResultUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const resultEntity = useAppSelector(state => state.result.entity);
  const loading = useAppSelector(state => state.result.loading);
  const updating = useAppSelector(state => state.result.updating);
  const updateSuccess = useAppSelector(state => state.result.updateSuccess);

  const handleClose = () => {
    navigate('/result');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUsers({}));
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
    values.enteredAt = convertDateTimeToServer(values.enteredAt);

    const entity = {
      ...resultEntity,
      ...values,
      enteredBy: users.find(it => it.id.toString() === values.enteredBy?.toString()),
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
          enteredAt: displayDefaultDateTime(),
        }
      : {
          ...resultEntity,
          enteredAt: convertDateTimeFromServer(resultEntity.enteredAt),
          enteredBy: resultEntity?.enteredBy?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.result.home.createOrEditLabel" data-cy="ResultCreateUpdateHeading">
            Create or edit a Result
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="result-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Result Value"
                id="result-resultValue"
                name="resultValue"
                data-cy="resultValue"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Notes"
                id="result-notes"
                name="notes"
                data-cy="notes"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Entered At"
                id="result-enteredAt"
                name="enteredAt"
                data-cy="enteredAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Image Reference"
                id="result-imageReference"
                name="imageReference"
                data-cy="imageReference"
                type="text"
                validate={{
                  maxLength: { value: 2000, message: 'This field cannot be longer than 2000 characters.' },
                }}
              />
              <ValidatedField id="result-enteredBy" name="enteredBy" data-cy="enteredBy" label="Entered By" type="select" required>
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
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/result" replace variant="info">
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

export default ResultUpdate;
