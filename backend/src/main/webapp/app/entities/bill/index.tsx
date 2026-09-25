import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Bill from './bill';
import BillDeleteDialog from './bill-delete-dialog';
import BillDetail from './bill-detail';
import BillUpdate from './bill-update';

const BillRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Bill />} />
    <Route path="new" element={<BillUpdate />} />
    <Route path=":id">
      <Route index element={<BillDetail />} />
      <Route path="edit" element={<BillUpdate />} />
      <Route path="delete" element={<BillDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default BillRoutes;
