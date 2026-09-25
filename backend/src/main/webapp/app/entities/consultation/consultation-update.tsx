import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getDiagnoses } from 'app/entities/diagnosis/diagnosis.reducer';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { ConsultationStatus } from 'app/shared/model/enumerations/consultation-status.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

import { createEntity, getEntity, reset, updateEntity } from './consultation.reducer';

export const ConsultationUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const diagnoses = useAppSelector(state => state.diagnosis.entities);
  const consultationEntity = useAppSelector(state => state.consultation.entity);
  const loading = useAppSelector(state => state.consultation.loading);
  const updating = useAppSelector(state => state.consultation.updating);
  const updateSuccess = useAppSelector(state => state.consultation.updateSuccess);
  const consultationStatusValues = Object.keys(ConsultationStatus);

  const handleClose = () => {
    navigate('/consultation');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUsers({}));
    dispatch(getDiagnoses({}));
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
    values.startedAt = convertDateTimeToServer(values.startedAt);
    values.completedAt = convertDateTimeToServer(values.completedAt);

    const entity = {
      ...consultationEntity,
      ...values,
      doctor: users.find(it => it.id.toString() === values.doctor?.toString()),
      diagnoseses: mapIdList(values.diagnoseses),
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
          startedAt: displayDefaultDateTime(),
          completedAt: displayDefaultDateTime(),
        }
      : {
          status: 'IN_PROGRESS',
          ...consultationEntity,
          startedAt: convertDateTimeFromServer(consultationEntity.startedAt),
          completedAt: convertDateTimeFromServer(consultationEntity.completedAt),
          doctor: consultationEntity?.doctor?.id,
          diagnoseses: consultationEntity?.diagnoseses?.map(e => e.id.toString()),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.consultation.home.createOrEditLabel" data-cy="ConsultationCreateUpdateHeading">
            Create or edit a Consultation
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="consultation-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Presenting Complaint"
                id="consultation-presentingComplaint"
                name="presentingComplaint"
                data-cy="presentingComplaint"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Examination Findings"
                id="consultation-examinationFindings"
                name="examinationFindings"
                data-cy="examinationFindings"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Diagnosis Other"
                id="consultation-diagnosisOther"
                name="diagnosisOther"
                data-cy="diagnosisOther"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Observations"
                id="consultation-observations"
                name="observations"
                data-cy="observations"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Follow Up Instructions"
                id="consultation-followUpInstructions"
                name="followUpInstructions"
                data-cy="followUpInstructions"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField label="Status" id="consultation-status" name="status" data-cy="status" type="select">
                {consultationStatusValues.map(consultationStatus => (
                  <option value={consultationStatus} key={consultationStatus}>
                    {consultationStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Started At"
                id="consultation-startedAt"
                name="startedAt"
                data-cy="startedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Completed At"
                id="consultation-completedAt"
                name="completedAt"
                data-cy="completedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField id="consultation-doctor" name="doctor" data-cy="doctor" label="Doctor" type="select" required>
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
              <ValidatedField label="Diagnoses" id="consultation-diagnoses" data-cy="diagnoses" type="select" multiple name="diagnoseses">
                <option value="" key="0" />
                {diagnoses
                  ? diagnoses.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/consultation" replace variant="info">
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

export default ConsultationUpdate;
