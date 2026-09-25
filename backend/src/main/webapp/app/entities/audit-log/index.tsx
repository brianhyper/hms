import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import AuditLog from './audit-log';
import AuditLogDetail from './audit-log-detail';

const AuditLogRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<AuditLog />} />
    <Route path=":id">
      <Route index element={<AuditLogDetail />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AuditLogRoutes;
