import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import LabTest from './lab-test';
import LabTestDeleteDialog from './lab-test-delete-dialog';
import LabTestDetail from './lab-test-detail';
import LabTestUpdate from './lab-test-update';

const LabTestRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<LabTest />} />
    <Route path="new" element={<LabTestUpdate />} />
    <Route path=":id">
      <Route index element={<LabTestDetail />} />
      <Route path="edit" element={<LabTestUpdate />} />
      <Route path="delete" element={<LabTestDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default LabTestRoutes;
