import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Drug from './drug';
import DrugDeleteDialog from './drug-delete-dialog';
import DrugDetail from './drug-detail';
import DrugUpdate from './drug-update';

const DrugRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Drug />} />
    <Route path="new" element={<DrugUpdate />} />
    <Route path=":id">
      <Route index element={<DrugDetail />} />
      <Route path="edit" element={<DrugUpdate />} />
      <Route path="delete" element={<DrugDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default DrugRoutes;
