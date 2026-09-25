import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Appointment from './appointment';
import AppointmentDeleteDialog from './appointment-delete-dialog';
import AppointmentDetail from './appointment-detail';
import AppointmentUpdate from './appointment-update';

const AppointmentRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Appointment />} />
    <Route path="new" element={<AppointmentUpdate />} />
    <Route path=":id">
      <Route index element={<AppointmentDetail />} />
      <Route path="edit" element={<AppointmentUpdate />} />
      <Route path="delete" element={<AppointmentDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AppointmentRoutes;
