import React, { useEffect } from 'react';
import { Button, Col, OverlayTrigger, Row, Tooltip } from 'react-bootstrap';
import { TextFormat } from 'react-jhipster';
import { Link, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './patient.reducer';

export const PatientDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id!));
  }, []);

  const patientEntity = useAppSelector(state => state.patient.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="patientDetailsHeading">Patient</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{patientEntity.id}</dd>
          <dt>
            <span id="hospitalId">Hospital Id</span>
            <OverlayTrigger
              overlay={
                <Tooltip>
                  Permanent business identifier. For unidentified records this temporarily\ncontains the generated UNK-YYYY-#### identifier.
                </Tooltip>
              }
            >
              <span id="hospitalId" className="d-inline-block">
                ?
              </span>
            </OverlayTrigger>
          </dt>
          <dd>{patientEntity.hospitalId}</dd>
          <dt>
            <span id="fullName">Full Name</span>
          </dt>
          <dd>{patientEntity.fullName}</dd>
          <dt>
            <span id="dateOfBirth">Date Of Birth</span>
          </dt>
          <dd>
            {patientEntity.dateOfBirth ? <TextFormat value={patientEntity.dateOfBirth} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="estimatedAge">Estimated Age</span>
          </dt>
          <dd>{patientEntity.estimatedAge}</dd>
          <dt>
            <span id="sex">Sex</span>
          </dt>
          <dd>{patientEntity.sex}</dd>
          <dt>
            <span id="sexEstimated">Sex Estimated</span>
          </dt>
          <dd>{patientEntity.sexEstimated ? 'true' : 'false'}</dd>
          <dt>
            <span id="phone">Phone</span>
          </dt>
          <dd>{patientEntity.phone}</dd>
          <dt>
            <span id="email">Email</span>
          </dt>
          <dd>{patientEntity.email}</dd>
          <dt>
            <span id="identityDocumentType">Identity Document Type</span>
          </dt>
          <dd>{patientEntity.identityDocumentType}</dd>
          <dt>
            <span id="identityDocumentNumber">Identity Document Number</span>
          </dt>
          <dd>{patientEntity.identityDocumentNumber}</dd>
          <dt>
            <span id="occupation">Occupation</span>
          </dt>
          <dd>{patientEntity.occupation}</dd>
          <dt>
            <span id="maritalStatus">Marital Status</span>
          </dt>
          <dd>{patientEntity.maritalStatus}</dd>
          <dt>
            <span id="nextOfKinName">Next Of Kin Name</span>
          </dt>
          <dd>{patientEntity.nextOfKinName}</dd>
          <dt>
            <span id="nextOfKinPhone">Next Of Kin Phone</span>
          </dt>
          <dd>{patientEntity.nextOfKinPhone}</dd>
          <dt>
            <span id="nextOfKinRelationship">Next Of Kin Relationship</span>
          </dt>
          <dd>{patientEntity.nextOfKinRelationship}</dd>
          <dt>
            <span id="knownAllergies">Known Allergies</span>
          </dt>
          <dd>{patientEntity.knownAllergies}</dd>
          <dt>
            <span id="knownConditions">Known Conditions</span>
          </dt>
          <dd>{patientEntity.knownConditions}</dd>
          <dt>
            <span id="villageEstate">Village Estate</span>
          </dt>
          <dd>{patientEntity.villageEstate}</dd>
          <dt>
            <span id="registrationStatus">Registration Status</span>
          </dt>
          <dd>{patientEntity.registrationStatus}</dd>
          <dt>Merged Into Patient</dt>
          <dd>{patientEntity.mergedIntoPatient ? patientEntity.mergedIntoPatient.id : ''}</dd>
        </dl>
        <Button as={Link as any} to="/patient" replace variant="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button as={Link as any} to={`/patient/${patientEntity.id}/edit`} replace variant="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default PatientDetail;
