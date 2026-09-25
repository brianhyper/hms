import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import VitalSigns from './vital-signs';
import VitalSignsDeleteDialog from './vital-signs-delete-dialog';
import VitalSignsDetail from './vital-signs-detail';
import VitalSignsUpdate from './vital-signs-update';

const VitalSignsRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<VitalSigns />} />
    <Route path="new" element={<VitalSignsUpdate />} />
    <Route path=":id">
      <Route index element={<VitalSignsDetail />} />
      <Route path="edit" element={<VitalSignsUpdate />} />
      <Route path="delete" element={<VitalSignsDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default VitalSignsRoutes;
