import React, { useEffect } from 'react';
import { Button, Col, FormText, Row } from 'react-bootstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { Link, useNavigate, useParams } from 'react-router';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getDepartments } from 'app/entities/department/department.reducer';
import { getEntities as getPatients } from 'app/entities/patient/patient.reducer';
import { getEntities as getVisits } from 'app/entities/visit/visit.reducer';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { AppointmentStatus } from 'app/shared/model/enumerations/appointment-status.model';

import { createEntity, getEntity, reset, updateEntity } from './appointment.reducer';

export const AppointmentUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const visits = useAppSelector(state => state.visit.entities);
  const patients = useAppSelector(state => state.patient.entities);
  const departments = useAppSelector(state => state.department.entities);
  const users = useAppSelector(state => state.userManagement.users);
  const appointmentEntity = useAppSelector(state => state.appointment.entity);
  const loading = useAppSelector(state => state.appointment.loading);
  const updating = useAppSelector(state => state.appointment.updating);
  const updateSuccess = useAppSelector(state => state.appointment.updateSuccess);
  const appointmentStatusValues = Object.keys(AppointmentStatus);

  const handleClose = () => {
    navigate(`/appointment${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getVisits({}));
    dispatch(getPatients({}));
    dispatch(getDepartments({}));
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

    const entity = {
      ...appointmentEntity,
      ...values,
      visit: visits.find(it => it.id.toString() === values.visit?.toString()),
      patient: patients.find(it => it.id.toString() === values.patient?.toString()),
      department: departments.find(it => it.id.toString() === values.department?.toString()),
      doctor: users.find(it => it.id.toString() === values.doctor?.toString()),
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
          status: 'SCHEDULED',
          ...appointmentEntity,
          visit: appointmentEntity?.visit?.id,
          patient: appointmentEntity?.patient?.id,
          department: appointmentEntity?.department?.id,
          doctor: appointmentEntity?.doctor?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="hmsApp.appointment.home.createOrEditLabel" data-cy="AppointmentCreateUpdateHeading">
            Create or edit a Appointment
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" required readOnly id="appointment-id" label="ID" validate={{ required: true }} />}
              <ValidatedField
                label="Scheduled Date"
                id="appointment-scheduledDate"
                name="scheduledDate"
                data-cy="scheduledDate"
                type="date"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Scheduled Time"
                id="appointment-scheduledTime"
                name="scheduledTime"
                data-cy="scheduledTime"
                type="time"
                placeholder="HH:mm"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Reason"
                id="appointment-reason"
                name="reason"
                data-cy="reason"
                type="text"
                validate={{
                  maxLength: { value: 10000, message: 'This field cannot be longer than 10000 characters.' },
                }}
              />
              <ValidatedField label="Status" id="appointment-status" name="status" data-cy="status" type="select">
                {appointmentStatusValues.map(appointmentStatus => (
                  <option value={appointmentStatus} key={appointmentStatus}>
                    {appointmentStatus}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField id="appointment-visit" name="visit" data-cy="visit" label="Visit" type="select">
                <option value="" key="0" />
                {visits
                  ? visits.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField id="appointment-patient" name="patient" data-cy="patient" label="Patient" type="select" required>
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
              <ValidatedField id="appointment-department" name="department" data-cy="department" label="Department" type="select" required>
                <option value="" key="0" />
                {departments
                  ? departments.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <ValidatedField id="appointment-doctor" name="doctor" data-cy="doctor" label="Doctor" type="select">
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button as={Link as any} id="cancel-save" data-cy="entityCreateCancelButton" to="/appointment" replace variant="info">
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

export default AppointmentUpdate;
