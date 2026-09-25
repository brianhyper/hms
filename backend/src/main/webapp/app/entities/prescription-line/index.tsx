import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import PrescriptionLine from './prescription-line';
import PrescriptionLineDeleteDialog from './prescription-line-delete-dialog';
import PrescriptionLineDetail from './prescription-line-detail';
import PrescriptionLineUpdate from './prescription-line-update';

const PrescriptionLineRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<PrescriptionLine />} />
    <Route path="new" element={<PrescriptionLineUpdate />} />
    <Route path=":id">
      <Route index element={<PrescriptionLineDetail />} />
      <Route path="edit" element={<PrescriptionLineUpdate />} />
      <Route path="delete" element={<PrescriptionLineDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PrescriptionLineRoutes;
