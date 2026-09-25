import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getDepartments } from 'app/entities/department/department.reducer';
import { getEntities as getVisits } from 'app/entities/visit/visit.reducer';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { ReferralStatus } from 'app/shared/model/enumerations/referral-status.model';
import { ReferralType } from 'app/shared/model/enumerations/referral-type.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './referral.reducer';

export const ReferralUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const visits = useAppSelector(state => state.visit.entities);
  const users = useAppSelector(state => state.userManagement.users);
  const departments = useAppSelector(state => state.department.entities);
  const referralEntity = useAppSelector(state => state.referral.entity);
  const loading = useAppSelector(state => state.referral.loading);
  const updating = useAppSelector(state => state.referral.updating);
  const updateSuccess = useAppSelector(state => state.referral.updateSuccess);
  const referralTypeValues = Object.keys(ReferralType);
  const referralStatusValues = Object.keys(ReferralStatus);

  const handleClose = () => {
    navigate('/referral');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getVisits({}));
    dispatch(getUsers({}));
    dispatch(getDepartments({}));
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
    values.createdAt = convertDateTimeToServer(values.createdAt);

    const entity = {
      ...referralEntity,
      ...values,
      visit: visits.find(it => it.id.toString() === values.visit?.toString()),
      referredBy: users.find(it => it.id.toString() === values.referredBy?.toString()),
      department: departments.find(it => it.id.toString() === values.department?.toString()),
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
          createdAt: displayDefaultDateTime(),
        }
      : {
          type: 'INTERNAL',
          status: 'PENDING',
          ...referralEntity,
          createdAt: convertDateTimeFromServer(referralEntity.createdAt),
          visit: referralEntity?.visit?.id,
          referredBy: referralEntity?.referredBy?.id,
          department: referralEntity?.department?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.referral.home.createOrEditLabel" data-cy="ReferralCreateUpdateHeading">
            Create or edit a Referral
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="referral-id" label="ID" validate={{ required: true }} />}
              <ValidatedField label="Type" id="referral-type" name="type" data-cy="type" type="select">
                {referralTypeValues.map(referralType => (
                  <option value={referralType} key={referralType}>
                    {referralType}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Destination"
                id="referral-destination"
                name="destination"
                data-cy="destination"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 255, message: 'This field cannot be longer than 255 characters.' },
                }}
              />
              <ValidatedField
                label="Destination Email"
                id="referral-destinationEmail"
                name="destinationEmail"
                data-cy="destinationEmail"
                type="text"
                validate={{
                  maxLength: { value: 254, message: 'This field cannot be longer than 254 characters.' },
                }}
              />
              <ValidatedField
                label="Reason"
                id="referral-reason"
                name="reason"
                data-cy="reason"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Notes"
                id="referral-notes"
                name="notes"
                data-cy="notes"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField label="Status" id="referral-status" name="status" data-cy="status" type="select">
                {referralStatusValues.map(referralStatus => (
                  <option value={referralStatus} key={referralStatus}>
                    {referralStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Created At"
                id="referral-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField id="referral-visit" name="visit" data-cy="visit" label="Visit" type="select" required>
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
              <ValidatedField id="referral-referredBy" name="referredBy" data-cy="referredBy" label="Referred By" type="select" required>
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
              <ValidatedField id="referral-department" name="department" data-cy="department" label="Department" type="select">
                <option value="" key="0" />
                {departments
                  ? departments.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/referral" replace variant="info">
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

export default ReferralUpdate;
