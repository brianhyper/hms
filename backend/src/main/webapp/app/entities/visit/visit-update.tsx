import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getBills } from 'app/entities/bill/bill.reducer';
import { getEntities as getConsultations } from 'app/entities/consultation/consultation.reducer';
import { getEntities as getPatients } from 'app/entities/patient/patient.reducer';
import { getEntities as getVitalSignses } from 'app/entities/vital-signs/vital-signs.reducer';
import { VisitPriority } from 'app/shared/model/enumerations/visit-priority.model';
import { VisitStatus } from 'app/shared/model/enumerations/visit-status.model';
import { VisitType } from 'app/shared/model/enumerations/visit-type.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';

import { createEntity, getEntity, reset, updateEntity } from './visit.reducer';

export const VisitUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const vitalSignses = useAppSelector(state => state.vitalSigns.entities);
  const consultations = useAppSelector(state => state.consultation.entities);
  const bills = useAppSelector(state => state.bill.entities);
  const patients = useAppSelector(state => state.patient.entities);
  const visitEntity = useAppSelector(state => state.visit.entity);
  const loading = useAppSelector(state => state.visit.loading);
  const updating = useAppSelector(state => state.visit.updating);
  const updateSuccess = useAppSelector(state => state.visit.updateSuccess);
  const visitTypeValues = Object.keys(VisitType);
  const visitPriorityValues = Object.keys(VisitPriority);
  const visitStatusValues = Object.keys(VisitStatus);

  const handleClose = () => {
    navigate(`/visit${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getVitalSignses({}));
    dispatch(getConsultations({}));
    dispatch(getBills({}));
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
    values.createdAt = convertDateTimeToServer(values.createdAt);
    values.startedVitalsAt = convertDateTimeToServer(values.startedVitalsAt);
    values.startedConsultationAt = convertDateTimeToServer(values.startedConsultationAt);
    values.closedAt = convertDateTimeToServer(values.closedAt);

    const entity = {
      ...visitEntity,
      ...values,
      vitals: vitalSignses.find(it => it.id.toString() === values.vitals?.toString()),
      consultation: consultations.find(it => it.id.toString() === values.consultation?.toString()),
      bill: bills.find(it => it.id.toString() === values.bill?.toString()),
      patient: patients.find(it => it.id.toString() === values.patient?.toString()),
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
          startedVitalsAt: displayDefaultDateTime(),
          startedConsultationAt: displayDefaultDateTime(),
          closedAt: displayDefaultDateTime(),
        }
      : {
          type: 'OUTPATIENT',
          priority: 'NORMAL',
          status: 'REGISTERED',
          ...visitEntity,
          createdAt: convertDateTimeFromServer(visitEntity.createdAt),
          startedVitalsAt: convertDateTimeFromServer(visitEntity.startedVitalsAt),
          startedConsultationAt: convertDateTimeFromServer(visitEntity.startedConsultationAt),
          closedAt: convertDateTimeFromServer(visitEntity.closedAt),
          vitals: visitEntity?.vitals?.id,
          consultation: visitEntity?.consultation?.id,
          bill: visitEntity?.bill?.id,
          patient: visitEntity?.patient?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.visit.home.createOrEditLabel" data-cy="VisitCreateUpdateHeading">
            Create or edit a Visit
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="visit-id" label="ID" validate={{ required: true }} />}
              <ValidatedField label="Type" id="visit-type" name="type" data-cy="type" type="select">
                {visitTypeValues.map(visitType => (
                  <option value={visitType} key={visitType}>
                    {visitType}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField label="Priority" id="visit-priority" name="priority" data-cy="priority" type="select">
                {visitPriorityValues.map(visitPriority => (
                  <option value={visitPriority} key={visitPriority}>
                    {visitPriority}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Reason For Visit"
                id="visit-reasonForVisit"
                name="reasonForVisit"
                data-cy="reasonForVisit"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField label="Status" id="visit-status" name="status" data-cy="status" type="select">
                {visitStatusValues.map(visitStatus => (
                  <option value={visitStatus} key={visitStatus}>
                    {visitStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label="Queue Skip Reason"
                id="visit-queueSkipReason"
                name="queueSkipReason"
                data-cy="queueSkipReason"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField
                label="Created At"
                id="visit-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Started Vitals At"
                id="visit-startedVitalsAt"
                name="startedVitalsAt"
                data-cy="startedVitalsAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Started Consultation At"
                id="visit-startedConsultationAt"
                name="startedConsultationAt"
                data-cy="startedConsultationAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="Closed At"
                id="visit-closedAt"
                name="closedAt"
                data-cy="closedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField id="visit-vitals" name="vitals" data-cy="vitals" label="Vitals" type="select">
                <option value="" key="0" />
                {vitalSignses
                  ? vitalSignses.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField id="visit-consultation" name="consultation" data-cy="consultation" label="Consultation" type="select">
                <option value="" key="0" />
                {consultations
                  ? consultations.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField id="visit-bill" name="bill" data-cy="bill" label="Bill" type="select">
                <option value="" key="0" />
                {bills
                  ? bills.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField id="visit-patient" name="patient" data-cy="patient" label="Patient" type="select" required>
                <option value="" key="0" />
                {patients
                  ? patients.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/visit" replace variant="info">
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

export default VisitUpdate;
