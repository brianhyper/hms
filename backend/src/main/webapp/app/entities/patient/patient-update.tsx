import React, { useEffect } from 'react';
import { Button, Col, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { ValidatedField, ValidatedForm, isNumber } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getPatients } from 'app/entities/patient/patient.reducer';
import { IdentityDocumentType } from 'app/shared/model/enumerations/identity-document-type.model';
import { NextOfKinRelationship } from 'app/shared/model/enumerations/next-of-kin-relationship.model';
import { RegistrationStatus } from 'app/shared/model/enumerations/registration-status.model';
import { Sex } from 'app/shared/model/enumerations/sex.model';

import { createEntity, getEntity, reset, updateEntity } from './patient.reducer';

export const PatientUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const patients = useAppSelector(state => state.patient.entities);
  const patientEntity = useAppSelector(state => state.patient.entity);
  const loading = useAppSelector(state => state.patient.loading);
  const updating = useAppSelector(state => state.patient.updating);
  const updateSuccess = useAppSelector(state => state.patient.updateSuccess);
  const sexValues = Object.keys(Sex);
  const identityDocumentTypeValues = Object.keys(IdentityDocumentType);
  const nextOfKinRelationshipValues = Object.keys(NextOfKinRelationship);
  const registrationStatusValues = Object.keys(RegistrationStatus);

  const handleClose = () => {
    navigate(`/patient${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPatients({}));
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
    if (values.estimatedAge !== undefined && typeof values.estimatedAge !== 'number') {
      values.estimatedAge = Number(values.estimatedAge);
    }

    const entity = {
      ...patientEntity,
      ...values,
      mergedIntoPatient: patients.find(it => it.id.toString() === values.mergedIntoPatient?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          sex: 'MALE',
          identityDocumentType: 'NATIONAL_ID',
          nextOfKinRelationship: 'PARENT',
          registrationStatus: 'COMPLETE',
          ...patientEntity,
          mergedIntoPatient: patientEntity?.mergedIntoPatient?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.patient.home.createOrEditLabel" data-cy="PatientCreateUpdateHeading">
            Create or edit a Patient
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="patient-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Hospital Id"
                id="patient-hospitalId"
                name="hospitalId"
                data-cy="hospitalId"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 32, message: 'This field cannot be longer than 32 characters.' },
                }}
              />
              <OverlayTrigger
                overlay={
                  <Tooltip>
                    Permanent business identifier. For unidentified records this temporarily\ncontains the generated UNK-YYYY-####
                    identifier.
                  </Tooltip>
                }
              >
                <span id="hospitalIdLabel" className="d-inline-block">
                  ?
                </span>
              </OverlayTrigger>
              <ValidatedField
                label="Full Name"
                id="patient-fullName"
                name="fullName"
                data-cy="fullName"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <ValidatedField label="Date Of Birth" id="patient-dateOfBirth" name="dateOfBirth" data-cy="dateOfBirth" type="date" />
              <ValidatedField
                label="Estimated Age"
                id="patient-estimatedAge"
                name="estimatedAge"
                data-cy="estimatedAge"
                type="text"
                validate={{
                  min: { value: 0, message: 'This field should be at least 0.' },
                  max: { value: 150, message: 'This field cannot be more than 150.' },
                  validate: v => isNumber(v) || 'This field should be a number.',
                }}
              />
              <ValidatedField label="Sex" id="patient-sex" name="sex" data-cy="sex" type="select">
                {sexValues.map(sex => (
                  <option value={sex} key={sex}>
                    {sex}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Sex Estimated"
                id="patient-sexEstimated"
                name="sexEstimated"
                data-cy="sexEstimated"
                check
                type="checkbox"
              />
              <ValidatedField
                label="Phone"
                id="patient-phone"
                name="phone"
                data-cy="phone"
                type="text"
                validate={{
                  maxLength: { value: 32, message: 'This field cannot be longer than 32 characters.' },
                }}
              />
              <ValidatedField
                label="Email"
                id="patient-email"
                name="email"
                data-cy="email"
                type="text"
                validate={{
                  maxLength: { value: 254, message: 'This field cannot be longer than 254 characters.' },
                }}
              />
              <ValidatedField
                label="Identity Document Type"
                id="patient-identityDocumentType"
                name="identityDocumentType"
                data-cy="identityDocumentType"
                type="select"
              >
                {identityDocumentTypeValues.map(identityDocumentType => (
                  <option value={identityDocumentType} key={identityDocumentType}>
                    {identityDocumentType}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Identity Document Number"
                id="patient-identityDocumentNumber"
                name="identityDocumentNumber"
                data-cy="identityDocumentNumber"
                type="text"
                validate={{
                  maxLength: { value: 64, message: 'This field cannot be longer than 64 characters.' },
                }}
              />
              <ValidatedField
                label="Occupation"
                id="patient-occupation"
                name="occupation"
                data-cy="occupation"
                type="text"
                validate={{
                  maxLength: { value: 120, message: 'This field cannot be longer than 120 characters.' },
                }}
              />
              <ValidatedField
                label="Marital Status"
                id="patient-maritalStatus"
                name="maritalStatus"
                data-cy="maritalStatus"
                type="text"
                validate={{
                  maxLength: { value: 50, message: 'This field cannot be longer than 50 characters.' },
                }}
              />
              <ValidatedField
                label="Next Of Kin Name"
                id="patient-nextOfKinName"
                name="nextOfKinName"
                data-cy="nextOfKinName"
                type="text"
                validate={{
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <ValidatedField
                label="Next Of Kin Phone"
                id="patient-nextOfKinPhone"
                name="nextOfKinPhone"
                data-cy="nextOfKinPhone"
                type="text"
                validate={{
                  maxLength: { value: 32, message: 'This field cannot be longer than 32 characters.' },
                }}
              />
              <ValidatedField
                label="Next Of Kin Relationship"
                id="patient-nextOfKinRelationship"
                name="nextOfKinRelationship"
                data-cy="nextOfKinRelationship"
                type="select"
              >
                {nextOfKinRelationshipValues.map(nextOfKinRelationship => (
                  <option value={nextOfKinRelationship} key={nextOfKinRelationship}>
                    {nextOfKinRelationship}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Known Allergies"
                id="patient-knownAllergies"
                name="knownAllergies"
                data-cy="knownAllergies"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Known Conditions"
                id="patient-knownConditions"
                name="knownConditions"
                data-cy="knownConditions"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Village Estate"
                id="patient-villageEstate"
                name="villageEstate"
                data-cy="villageEstate"
                type="text"
                validate={{
                  maxLength: { value: 200, message: 'This field cannot be longer than 200 characters.' },
                }}
              />
              <ValidatedField
                label="Registration Status"
                id="patient-registrationStatus"
                name="registrationStatus"
                data-cy="registrationStatus"
                type="select"
              >
                {registrationStatusValues.map(registrationStatus => (
                  <option value={registrationStatus} key={registrationStatus}>
                    {registrationStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="patient-mergedIntoPatient"
                name="mergedIntoPatient"
                data-cy="mergedIntoPatient"
                label="Merged Into Patient"
                type="select"
              >
                <option value="" key="0" />
                {patients
                  ? patients.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/patient" replace variant="info">
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

export default PatientUpdate;
