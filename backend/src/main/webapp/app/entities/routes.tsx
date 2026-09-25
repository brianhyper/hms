import React from 'react';
import { Route } from 'react-router'; // eslint-disable-line

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Appointment from './appointment';
import AuditLog from './audit-log';
import Bill from './bill';
import BillLineItem from './bill-line-item';
import Consultation from './consultation';
import Department from './department';
import Diagnosis from './diagnosis';
import DiagnosticOrder from './diagnostic-order';
import Dispense from './dispense';
import DispenseLine from './dispense-line';
import Drug from './drug';
import HospitalService from './hospital-service';
import LabTest from './lab-test';
import Patient from './patient';
import Payment from './payment';
import Prescription from './prescription';
import PrescriptionLine from './prescription-line';
import RadiologyExam from './radiology-exam';
import Referral from './referral';
import Result from './result';
import Visit from './visit';
import VitalSigns from './vital-signs';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="/patient/*" element={<Patient />} />
        <Route path="/department/*" element={<Department />} />
        <Route path="/appointment/*" element={<Appointment />} />
        <Route path="/visit/*" element={<Visit />} />
        <Route path="/vital-signs/*" element={<VitalSigns />} />
        <Route path="/consultation/*" element={<Consultation />} />
        <Route path="/diagnosis/*" element={<Diagnosis />} />
        <Route path="/hospital-service/*" element={<HospitalService />} />
        <Route path="/lab-test/*" element={<LabTest />} />
        <Route path="/radiology-exam/*" element={<RadiologyExam />} />
        <Route path="/diagnostic-order/*" element={<DiagnosticOrder />} />
        <Route path="/result/*" element={<Result />} />
        <Route path="/referral/*" element={<Referral />} />
        <Route path="/prescription/*" element={<Prescription />} />
        <Route path="/prescription-line/*" element={<PrescriptionLine />} />
        <Route path="/drug/*" element={<Drug />} />
        <Route path="/dispense/*" element={<Dispense />} />
        <Route path="/dispense-line/*" element={<DispenseLine />} />
        <Route path="/bill/*" element={<Bill />} />
        <Route path="/bill-line-item/*" element={<BillLineItem />} />
        <Route path="/payment/*" element={<Payment />} />
        <Route path="/audit-log/*" element={<AuditLog />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
