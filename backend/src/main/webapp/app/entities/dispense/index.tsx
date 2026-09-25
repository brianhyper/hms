import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Dispense from './dispense';
import DispenseDeleteDialog from './dispense-delete-dialog';
import DispenseDetail from './dispense-detail';
import DispenseUpdate from './dispense-update';

const DispenseRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Dispense />} />
    <Route path="new" element={<DispenseUpdate />} />
    <Route path=":id">
      <Route index element={<DispenseDetail />} />
      <Route path="edit" element={<DispenseUpdate />} />
      <Route path="delete" element={<DispenseDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DispenseRoutes;
