import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import HospitalService from './hospital-service';
import HospitalServiceDeleteDialog from './hospital-service-delete-dialog';
import HospitalServiceDetail from './hospital-service-detail';
import HospitalServiceUpdate from './hospital-service-update';

const HospitalServiceRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<HospitalService />} />
    <Route path="new" element={<HospitalServiceUpdate />} />
    <Route path=":id">
      <Route index element={<HospitalServiceDetail />} />
      <Route path="edit" element={<HospitalServiceUpdate />} />
      <Route path="delete" element={<HospitalServiceDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default HospitalServiceRoutes;
