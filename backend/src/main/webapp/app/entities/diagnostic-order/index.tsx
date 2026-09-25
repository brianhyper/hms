import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import DiagnosticOrder from './diagnostic-order';
import DiagnosticOrderDeleteDialog from './diagnostic-order-delete-dialog';
import DiagnosticOrderDetail from './diagnostic-order-detail';
import DiagnosticOrderUpdate from './diagnostic-order-update';

const DiagnosticOrderRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<DiagnosticOrder />} />
    <Route path="new" element={<DiagnosticOrderUpdate />} />
    <Route path=":id">
      <Route index element={<DiagnosticOrderDetail />} />
      <Route path="edit" element={<DiagnosticOrderUpdate />} />
      <Route path="delete" element={<DiagnosticOrderDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DiagnosticOrderRoutes;
