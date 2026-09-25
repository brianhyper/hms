import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import DispenseLine from './dispense-line';
import DispenseLineDeleteDialog from './dispense-line-delete-dialog';
import DispenseLineDetail from './dispense-line-detail';
import DispenseLineUpdate from './dispense-line-update';

const DispenseLineRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<DispenseLine />} />
    <Route path="new" element={<DispenseLineUpdate />} />
    <Route path=":id">
      <Route index element={<DispenseLineDetail />} />
      <Route path="edit" element={<DispenseLineUpdate />} />
      <Route path="delete" element={<DispenseLineDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DispenseLineRoutes;
